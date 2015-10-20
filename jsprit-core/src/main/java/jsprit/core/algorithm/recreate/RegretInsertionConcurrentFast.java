/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.algorithm.recreate;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Insertion based on regret approach.
 * <p/>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 *
 * @author stefan schroeder
 */
public class RegretInsertionConcurrentFast extends AbstractInsertionStrategy {


    private static Logger logger = LogManager.getLogger(RegretInsertionConcurrentFast.class);

    private ScoringFunction scoringFunction;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private final ExecutorService executor;

    private VehicleFleetManager fleetManager;

    private Set<String> initialVehicleIds;

    private boolean switchAllowed = true;


    /**
     * Sets the scoring function.
     * <p/>
     * <p>By default, the this.TimeWindowScorer is used.
     *
     * @param scoringFunction to score
     */
    public void setScoringFunction(ScoringFunction scoringFunction) {
        this.scoringFunction = scoringFunction;
    }

    public RegretInsertionConcurrentFast(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, ExecutorService executorService, VehicleFleetManager fleetManager) {
        super(vehicleRoutingProblem);
        this.scoringFunction = new DefaultScorer(vehicleRoutingProblem);
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
        this.executor = executorService;
        this.fleetManager = fleetManager;
        this.initialVehicleIds = getInitialVehicleIds(vehicleRoutingProblem);
        logger.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=regretInsertion][additionalScorer=" + scoringFunction + "]";
    }

    public void setSwitchAllowed(boolean switchAllowed) {
        this.switchAllowed = switchAllowed;
    }

    private Set<String> getInitialVehicleIds(VehicleRoutingProblem vehicleRoutingProblem) {
        Set<String> ids = new HashSet<String>();
        for(VehicleRoute r : vehicleRoutingProblem.getInitialVehicleRoutes()){
            ids.add(r.getVehicle().getId());
        }
        return ids;
    }


    /**
     * Runs insertion.
     * <p/>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     *
     * @throws java.lang.RuntimeException if smth went wrong with thread execution
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());

        Iterator<Job> jobIterator = unassignedJobs.iterator();
        while (jobIterator.hasNext()){
            Job job = jobIterator.next();
            if(job instanceof Break){
                VehicleRoute route = InsertionDataUpdater.findRoute(routes, job);
                if(route == null){
                    badJobs.add(job);
                }
                else {
                    InsertionData iData = insertionCostsCalculator.getInsertionData(route, job, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        badJobs.add(job);
                    } else {
                        insertJob(job, iData, route);
                    }
                }
                jobIterator.remove();
            }
        }

        List<Job> jobs = new ArrayList<Job>(unassignedJobs);
        TreeSet<VersionedInsertionData>[] priorityQueues = new TreeSet[vrp.getJobs().values().size() + 2];
        VehicleRoute lastModified = null;
        boolean firstRun = true;
        int updateRound = 0;
        Map<VehicleRoute,Integer> updates = new HashMap<VehicleRoute, Integer>();
        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            if(!firstRun && lastModified == null) throw new IllegalStateException("ho. this must not be.");
            if(firstRun){
                firstRun = false;
                updateInsertionData(priorityQueues, routes, unassignedJobList, updateRound);
                for(VehicleRoute r : routes) updates.put(r,updateRound);
            }
            else{
                updateInsertionData(priorityQueues, Arrays.asList(lastModified), unassignedJobList, updateRound);
                updates.put(lastModified,updateRound);
            }
            updateRound++;
            ScoredJob bestScoredJob = InsertionDataUpdater.getBest(switchAllowed,initialVehicleIds,fleetManager, insertionCostsCalculator, scoringFunction, priorityQueues, updates, unassignedJobList, badJobList);
            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
                lastModified = bestScoredJob.getRoute();
            }
            else lastModified = null;
            for (Job bad : badJobList) {
                jobs.remove(bad);
                badJobs.add(bad);
            }
        }
        return badJobs;
    }

    private void updateInsertionData(final TreeSet<VersionedInsertionData>[] priorityQueues, final Collection<VehicleRoute> routes, List<Job> unassignedJobList, final int updateRound) {
        List<Callable<Boolean>> tasks = new ArrayList<Callable<Boolean>>();
        for (final Job unassignedJob : unassignedJobList) {
            if(priorityQueues[unassignedJob.getIndex()] == null){
                priorityQueues[unassignedJob.getIndex()] = new TreeSet<VersionedInsertionData>(InsertionDataUpdater.getComparator());
            }
            final TreeSet<VersionedInsertionData> priorityQueue = priorityQueues[unassignedJob.getIndex()];
            tasks.add(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    return InsertionDataUpdater.update(switchAllowed,initialVehicleIds,fleetManager, insertionCostsCalculator, priorityQueue, updateRound, unassignedJob, routes);
                }
            });
        }
        try {
            List<Future<Boolean>> futures = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }





}
