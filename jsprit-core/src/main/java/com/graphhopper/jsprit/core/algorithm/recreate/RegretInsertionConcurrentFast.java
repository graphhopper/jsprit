/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.DependencyType;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

/**
 * Insertion based on regret approach.
 * <p>
 * <p>Basically calculates the insertion cost of the firstBest and the secondBest alternative. The score is then calculated as difference
 * between secondBest and firstBest, plus additional scoring variables that can defined in this.ScoringFunction.
 * The idea is that if the cost of the secondBest alternative is way higher than the first best, it seems to be important to insert this
 * customer immediatedly. If difference is not that high, it might not impact solution if this customer is inserted later.
 *
 * @author stefan schroeder
 */
public class RegretInsertionConcurrentFast extends AbstractInsertionStrategy {


    private static final Logger logger = LoggerFactory.getLogger(RegretInsertionConcurrentFast.class);

    private RegretScoringFunction regretScoringFunction;

    private final JobInsertionCostsCalculator insertionCostsCalculator;

    private final ExecutorService executor;

    private final VehicleFleetManager fleetManager;

    private final Set<String> initialVehicleIds;

    private boolean switchAllowed = true;

    private DependencyType[] dependencyTypes = null;


    /**
     * Sets the scoring function.
     * <p>
     * <p>By default, the this.TimeWindowScorer is used.
     *
     * @param scoringFunction to score
     */
    public void setScoringFunction(ScoringFunction scoringFunction) {
        this.regretScoringFunction = new DefaultRegretScoringFunction(scoringFunction);
    }

    public void setRegretScoringFunction(RegretScoringFunction regretScoringFunction) {
        this.regretScoringFunction = regretScoringFunction;
    }

    public RegretInsertionConcurrentFast(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem, ExecutorService executorService, VehicleFleetManager fleetManager) {
        super(vehicleRoutingProblem);
        this.regretScoringFunction = new DefaultRegretScoringFunction(new DefaultScorer(vehicleRoutingProblem));
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
        this.executor = executorService;
        this.fleetManager = fleetManager;
        this.initialVehicleIds = getInitialVehicleIds(vehicleRoutingProblem);
        logger.debug("initialise " + this);
    }

    @Override
    public String toString() {
        return "[name=regretInsertion][additionalScorer=" + regretScoringFunction + "]";
    }

    public void setSwitchAllowed(boolean switchAllowed) {
        this.switchAllowed = switchAllowed;
    }

    private Set<String> getInitialVehicleIds(VehicleRoutingProblem vehicleRoutingProblem) {
        Set<String> ids = new HashSet<>();
        for(VehicleRoute r : vehicleRoutingProblem.getInitialVehicleRoutes()){
            ids.add(r.getVehicle().getId());
        }
        return ids;
    }

    public void setDependencyTypes(DependencyType[] dependencyTypes){
        this.dependencyTypes = dependencyTypes;
    }


    /**
     * Runs insertion.
     * <p>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     *
     * @throws java.lang.RuntimeException if smth went wrong with thread execution
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<>(unassignedJobs.size());

        Iterator<Job> jobIterator = unassignedJobs.iterator();
        while (jobIterator.hasNext()){
            Job job = jobIterator.next();
            if (job.getJobType().isBreak()) {
                VehicleRoute route = InsertionDataUpdater.findRoute(routes, job);
                if (route == null) {
                    badJobs.add(job);
                } else {
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

        List<Job> jobs = new ArrayList<>(unassignedJobs);
        TreeSet<VersionedInsertionData>[] priorityQueues = new TreeSet[vrp.getJobs().values().size() + 2];
        VehicleRoute lastModified = null;
        boolean firstRun = true;
        int updateRound = 0;
        Map<VehicleRoute, Integer> updates = new HashMap<>();
        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<>(jobs);
            List<ScoredJob> badJobList = new ArrayList<>();
            if(!firstRun && lastModified == null) throw new IllegalStateException("ho. this must not be.");
            updateInsertionData(priorityQueues, routes, unassignedJobList, updateRound,firstRun,lastModified,updates);
            if(firstRun) firstRun = false;
            updateRound++;
            ScoredJob bestScoredJob = InsertionDataUpdater.getBest(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, regretScoringFunction, priorityQueues, updates, unassignedJobList, badJobList);
            if (bestScoredJob != null) {
                if (bestScoredJob.isNewRoute()) {
                    routes.add(bestScoredJob.getRoute());
                }
                insertJob(bestScoredJob.getJob(), bestScoredJob.getInsertionData(), bestScoredJob.getRoute());
                jobs.remove(bestScoredJob.getJob());
                lastModified = bestScoredJob.getRoute();
            }
            else lastModified = null;
            for (ScoredJob bad : badJobList) {
                Job unassigned = bad.getJob();
                jobs.remove(unassigned);
                badJobs.add(unassigned);
                markUnassigned(unassigned, bad.getInsertionData().getFailedConstraintNames());
            }
        }
        return badJobs;
    }

    private void updateInsertionData(final TreeSet<VersionedInsertionData>[] priorityQueues, final Collection<VehicleRoute> routes, List<Job> unassignedJobList, final int updateRound, final boolean firstRun, final VehicleRoute lastModified, Map<VehicleRoute, Integer> updates) {
        List<Callable<Boolean>> tasks = new ArrayList<>();
        boolean updatedAllRoutes = false;
        for (final Job unassignedJob : unassignedJobList) {
            if(priorityQueues[unassignedJob.getIndex()] == null){
                priorityQueues[unassignedJob.getIndex()] = new TreeSet<>(InsertionDataUpdater.getComparator());
            }
            if(firstRun) {
                updatedAllRoutes = true;
                makeCallables(tasks, true, priorityQueues[unassignedJob.getIndex()], updateRound, unassignedJob, routes, lastModified);
            }
            else{
                if(dependencyTypes == null || dependencyTypes[unassignedJob.getIndex()] == null){
                    makeCallables(tasks, updatedAllRoutes, priorityQueues[unassignedJob.getIndex()], updateRound, unassignedJob, routes, lastModified);
                }
                else {
                    DependencyType dependencyType = dependencyTypes[unassignedJob.getIndex()];
                    if (dependencyType.equals(DependencyType.INTER_ROUTE) || dependencyType.equals(DependencyType.INTRA_ROUTE)) {
                        updatedAllRoutes = true;
                        makeCallables(tasks, true, priorityQueues[unassignedJob.getIndex()], updateRound, unassignedJob, routes, lastModified);
                    } else {
                        makeCallables(tasks, updatedAllRoutes, priorityQueues[unassignedJob.getIndex()], updateRound, unassignedJob, routes, lastModified);
                    }
                }
            }
        }
        if(updatedAllRoutes){
            for(VehicleRoute r : routes) updates.put(r,updateRound);
        }
        else{
            updates.put(lastModified,updateRound);
        }
        try {
            executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private void makeCallables(List<Callable<Boolean>> tasks, boolean updateAll, final TreeSet<VersionedInsertionData> priorityQueue, final int updateRound, final Job unassignedJob, final Collection<VehicleRoute> routes, final VehicleRoute lastModified) {
        if(updateAll) {
            tasks.add(() -> InsertionDataUpdater.update(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, priorityQueue, updateRound, unassignedJob, routes));
        }
        else {
            tasks.add(() -> InsertionDataUpdater.update(switchAllowed, initialVehicleIds, fleetManager, insertionCostsCalculator, priorityQueue, updateRound, unassignedJob, Arrays.asList(lastModified)));
        }
    }


}
