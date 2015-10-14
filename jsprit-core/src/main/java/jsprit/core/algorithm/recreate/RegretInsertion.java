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

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

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
public class RegretInsertion extends AbstractInsertionStrategy {

    static class ScoredJob {

        private Job job;

        private double score;

        private InsertionData insertionData;

        private VehicleRoute route;

        private boolean newRoute;


        ScoredJob(Job job, double score, InsertionData insertionData, VehicleRoute route, boolean isNewRoute) {
            this.job = job;
            this.score = score;
            this.insertionData = insertionData;
            this.route = route;
            this.newRoute = isNewRoute;
        }

        public boolean isNewRoute() {
            return newRoute;
        }

        public Job getJob() {
            return job;
        }

        public double getScore() {
            return score;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }
    }

    static class BadJob extends ScoredJob {

        BadJob(Job job) {
            super(job, 0., null, null, false);
        }
    }

    /**
     * Scorer to include other impacts on score such as time-window length or distance to depot.
     *
     * @author schroeder
     */
    static interface ScoringFunction {

        public double score(InsertionData best, Job job);

    }

    /**
     * Scorer that includes the length of the time-window when scoring a job. The wider the time-window, the lower the score.
     * <p/>
     * <p>This is the default scorer, i.e.: score = (secondBest - firstBest) + this.TimeWindowScorer.score(job)
     *
     * @author schroeder
     */
    public static class DefaultScorer implements ScoringFunction {

        private VehicleRoutingProblem vrp;

        private double tw_param = -0.5;

        private double depotDistance_param = +0.1;

        private double minTimeWindowScore = -100000;

        public DefaultScorer(VehicleRoutingProblem vrp) {
            this.vrp = vrp;
        }

        public void setTimeWindowParam(double tw_param) {
            this.tw_param = tw_param;
        }

        public void setDepotDistanceParam(double depotDistance_param) {
            this.depotDistance_param = depotDistance_param;
        }

        @Override
        public double score(InsertionData best, Job job) {
            double score;
            if (job instanceof Service) {
                score = scoreService(best, job);
            } else if (job instanceof Shipment) {
                score = scoreShipment(best, job);
            } else throw new IllegalStateException("not supported");
            return score;
        }

        private double scoreShipment(InsertionData best, Job job) {
            Shipment shipment = (Shipment) job;
            double maxDepotDistance_1 = Math.max(
                getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getPickupLocation()),
                getDistance(best.getSelectedVehicle().getStartLocation(), shipment.getDeliveryLocation())
            );
            double maxDepotDistance_2 = Math.max(
                getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getPickupLocation()),
                getDistance(best.getSelectedVehicle().getEndLocation(), shipment.getDeliveryLocation())
            );
            double maxDepotDistance = Math.max(maxDepotDistance_1, maxDepotDistance_2);
            double minTimeToOperate = Math.min(shipment.getPickupTimeWindow().getEnd() - shipment.getPickupTimeWindow().getStart(),
                shipment.getDeliveryTimeWindow().getEnd() - shipment.getDeliveryTimeWindow().getStart());
            return Math.max(tw_param * minTimeToOperate, minTimeWindowScore) + depotDistance_param * maxDepotDistance;
        }

        private double scoreService(InsertionData best, Job job) {
            Location location = ((Service) job).getLocation();
            double maxDepotDistance = 0;
            if (location != null) {
                maxDepotDistance = Math.max(
                    getDistance(best.getSelectedVehicle().getStartLocation(), location),
                    getDistance(best.getSelectedVehicle().getEndLocation(), location)
                );
            }
            return Math.max(tw_param * (((Service) job).getTimeWindow().getEnd() - ((Service) job).getTimeWindow().getStart()), minTimeWindowScore) +
                depotDistance_param * maxDepotDistance;
        }


        private double getDistance(Location loc1, Location loc2) {
            return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
        }

        @Override
        public String toString() {
            return "[name=defaultScorer][twParam=" + tw_param + "][depotDistanceParam=" + depotDistance_param + "]";
        }

    }

    static class VersionedInsertionData {

        private InsertionData iData;

        private VehicleRoute route;

        private int version;

        public VersionedInsertionData(InsertionData iData, int version, VehicleRoute route) {
            this.iData = iData;
            this.version = version;
            this.route = route;
        }

        public InsertionData getiData() {
            return iData;
        }

        public int getVersion() {
            return version;
        }

        public VehicleRoute getRoute() {
            return route;
        }
    }

    private static Logger logger = LogManager.getLogger(RegretInsertion.class);

    private ScoringFunction scoringFunction;

    private JobInsertionCostsCalculator insertionCostsCalculator;

    private VehicleFleetManager fleetManager;

    public void setFleetManager(VehicleFleetManager fleetManager) {
        this.fleetManager = fleetManager;
    }

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

    public RegretInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
        super(vehicleRoutingProblem);
        this.scoringFunction = new DefaultScorer(vehicleRoutingProblem);
        this.insertionCostsCalculator = jobInsertionCalculator;
        this.vrp = vehicleRoutingProblem;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=regretInsertion][additionalScorer=" + scoringFunction + "]";
    }


    /**
     * Runs insertion.
     * <p/>
     * <p>Before inserting a job, all unassigned jobs are scored according to its best- and secondBest-insertion plus additional scoring variables.
     */
    @Override
    public Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());

        Iterator<Job> jobIterator = unassignedJobs.iterator();
        while (jobIterator.hasNext()){
            Job job = jobIterator.next();
            if(job instanceof Break){
                VehicleRoute route = findRoute(routes,job);
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
        PriorityQueue<VersionedInsertionData>[] priorityQueues = new PriorityQueue[vrp.getJobs().values().size() + 2];
        VehicleRoute lastModified = null;
        boolean firstRun = true;
        int updateRound = 0;
        Map<VehicleRoute,Integer> updates = new HashMap<VehicleRoute, Integer>();
        while (!jobs.isEmpty()) {
            List<Job> unassignedJobList = new ArrayList<Job>(jobs);
            List<Job> badJobList = new ArrayList<Job>();
            if(!firstRun && lastModified == null) throw new IllegalStateException("fooo");
            if(firstRun){
                firstRun = false;
                updateInsertionData(priorityQueues, routes, unassignedJobList, badJobList, updateRound, updates);
            }
            else{
                updateInsertionData(priorityQueues, Arrays.asList(lastModified), unassignedJobList, badJobList, updateRound, updates);
            }
            updateRound++;
            ScoredJob bestScoredJob = getBest(priorityQueues,updates,unassignedJobList,badJobList);
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

    private ScoredJob getBest(PriorityQueue<VersionedInsertionData>[] priorityQueues, Map<VehicleRoute, Integer> updates, List<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for(Job j : unassignedJobList){
            VehicleRoute bestRoute = null;
            InsertionData best = null;
            InsertionData secondBest = null;
            PriorityQueue<VersionedInsertionData> priorityQueue = priorityQueues[j.getIndex()];
            Iterator<VersionedInsertionData> iterator = priorityQueue.iterator();
            while(iterator.hasNext()){
                VersionedInsertionData versionedIData = iterator.next();
                if(bestRoute != null){
                    if(versionedIData.getRoute() == bestRoute){
                        continue;
                    }
                }
                if(versionedIData.getiData() instanceof InsertionData.NoInsertionFound) continue;
                if(versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                    if (fleetManager.isLocked(versionedIData.getiData().getSelectedVehicle())) {
                        Vehicle available = fleetManager.getAvailableVehicle(versionedIData.getiData().getSelectedVehicle().getVehicleTypeIdentifier());
                        if (available != null) {
                            InsertionData oldData = versionedIData.getiData();
                            InsertionData newData = new InsertionData(oldData.getInsertionCost(), oldData.getPickupInsertionIndex(),
                                oldData.getDeliveryInsertionIndex(), available, oldData.getSelectedDriver());
                            newData.setVehicleDepartureTime(oldData.getVehicleDepartureTime());
                            for(Event e : oldData.getEvents()){
                                if(e instanceof SwitchVehicle){
                                    newData.getEvents().add(new SwitchVehicle(versionedIData.getRoute(),available,oldData.getVehicleDepartureTime()));
                                }
                                else newData.getEvents().add(e);
                            }
                            versionedIData = new VersionedInsertionData(newData, versionedIData.getVersion(), versionedIData.getRoute());
                        } else continue;
                    }
                }
                int currentDataVersion = updates.get(versionedIData.getRoute());
                if(versionedIData.getVersion() == currentDataVersion){
                    if(best == null) {
                        best = versionedIData.getiData();
                        bestRoute = versionedIData.getRoute();
                    }
                    else {
                        secondBest = versionedIData.getiData();
                        break;
                    }
                }
            }
            VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
            InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, j, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
            if(!(iData instanceof InsertionData.NoInsertionFound)){
                if (best == null) {
                    best = iData;
                    bestRoute = emptyRoute;
                } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                    secondBest = best;
                    best = iData;
                    bestRoute = emptyRoute;
                } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                    secondBest = iData;
                }
            }
            if (best == null) {
                badJobs.add(j);
                continue;
            }
            double score = score(j, best, secondBest, scoringFunction);
            ScoredJob scoredJob;
            if (bestRoute == emptyRoute) {
                scoredJob = new ScoredJob(j, score, best, bestRoute, true);
            } else scoredJob = new ScoredJob(j, score, best, bestRoute, false);

            if(bestScoredJob == null){
                bestScoredJob = scoredJob;
            }
            else if(scoredJob.getScore() > bestScoredJob.getScore()){
                bestScoredJob = scoredJob;
            }
        }
        return bestScoredJob;
    }

    private Comparator<VersionedInsertionData> getComparator(){
        return new Comparator<VersionedInsertionData>() {
            @Override
            public int compare(VersionedInsertionData o1, VersionedInsertionData o2) {
                if(o1.getiData().getInsertionCost() < o2.getiData().getInsertionCost()) return -1;
                return 1;
            }
        };
    }

    private void updateInsertionData(PriorityQueue<VersionedInsertionData>[] priorityQueues, Collection<VehicleRoute> routes, List<Job> unassignedJobList, List<Job> badJobList, int updateRound, Map<VehicleRoute, Integer> updates) {
        for (Job unassignedJob : unassignedJobList) {
            if(priorityQueues[unassignedJob.getIndex()] == null){
                priorityQueues[unassignedJob.getIndex()] = new PriorityQueue<VersionedInsertionData>(unassignedJobList.size(), getComparator());
            }
            for(VehicleRoute route : routes) {
                Collection<Vehicle> relevantVehicles = new ArrayList<Vehicle>();
                if(!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    relevantVehicles.add(route.getVehicle());
                    relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                }
                else relevantVehicles.addAll(fleetManager.getAvailableVehicles());
                for (Vehicle v : relevantVehicles) {
                    double depTime = v.getEarliestDeparture();
                    InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, v, depTime, route.getDriver(), Double.MAX_VALUE);
                    if (iData instanceof InsertionData.NoInsertionFound) {
                        continue;
                    }
                    priorityQueues[unassignedJob.getIndex()].add(new VersionedInsertionData(iData,updateRound,route));
                }
                updates.put(route,updateRound);
            }

//
//
//
//            ScoredJob scoredJob = getScoredJob(routes, unassignedJob, insertionCostsCalculator, scoringFunction);
//            if (scoredJob instanceof BadJob) {
//                badJobs.add(unassignedJob);
//                continue;
//            }
//            if (bestScoredJob == null) bestScoredJob = scoredJob;
//            else {
//                if (scoredJob.getScore() > bestScoredJob.getScore()) {
//                    bestScoredJob = scoredJob;
//                } else if (scoredJob.getScore() == bestScoredJob.getScore()) {
//                    if (scoredJob.getJob().getId().compareTo(bestScoredJob.getJob().getId()) <= 0) {
//                        bestScoredJob = scoredJob;
//                    }
//                }
//            }
        }
    }

    private VehicleRoute findRoute(Collection<VehicleRoute> routes, Job job) {
        for(VehicleRoute r : routes){
            if(r.getVehicle().getBreak() == job) return r;
        }
        return null;
    }

    private ScoredJob nextJob(Collection<VehicleRoute> routes, Collection<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job unassignedJob : unassignedJobList) {
            ScoredJob scoredJob = getScoredJob(routes, unassignedJob, insertionCostsCalculator, scoringFunction);
            if (scoredJob instanceof BadJob) {
                badJobs.add(unassignedJob);
                continue;
            }
            if (bestScoredJob == null) bestScoredJob = scoredJob;
            else {
                if (scoredJob.getScore() > bestScoredJob.getScore()) {
                    bestScoredJob = scoredJob;
                } else if (scoredJob.getScore() == bestScoredJob.getScore()) {
                    if (scoredJob.getJob().getId().compareTo(bestScoredJob.getJob().getId()) <= 0) {
                        bestScoredJob = scoredJob;
                    }
                }
            }
        }
        return bestScoredJob;
    }

    static ScoredJob getScoredJob(Collection<VehicleRoute> routes, Job unassignedJob, JobInsertionCostsCalculator insertionCostsCalculator, ScoringFunction scoringFunction) {
        InsertionData best = null;
        InsertionData secondBest = null;
        VehicleRoute bestRoute = null;

        double benchmark = Double.MAX_VALUE;
        for (VehicleRoute route : routes) {
            if (secondBest != null) {
                benchmark = secondBest.getInsertionCost();
            }
            InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
            if (iData instanceof InsertionData.NoInsertionFound) continue;
            if (best == null) {
                best = iData;
                bestRoute = route;
            } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                secondBest = best;
                best = iData;
                bestRoute = route;
            } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                secondBest = iData;
            }
        }

        VehicleRoute emptyRoute = VehicleRoute.emptyRoute();
        InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, benchmark);
        if (!(iData instanceof InsertionData.NoInsertionFound)) {
            if (best == null) {
                best = iData;
                bestRoute = emptyRoute;
            } else if (iData.getInsertionCost() < best.getInsertionCost()) {
                secondBest = best;
                best = iData;
                bestRoute = emptyRoute;
            } else if (secondBest == null || (iData.getInsertionCost() < secondBest.getInsertionCost())) {
                secondBest = iData;
            }
        }
        if (best == null) {
            return new RegretInsertion.BadJob(unassignedJob);
        }
        double score = score(unassignedJob, best, secondBest, scoringFunction);
        ScoredJob scoredJob;
        if (bestRoute == emptyRoute) {
            scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, true);
        } else scoredJob = new ScoredJob(unassignedJob, score, best, bestRoute, false);
        return scoredJob;
    }


    static double score(Job unassignedJob, InsertionData best, InsertionData secondBest, ScoringFunction scoringFunction) {
        if (best == null) {
            throw new IllegalStateException("cannot insert job " + unassignedJob.getId());
        }
        double score;
        if (secondBest == null) { //either there is only one vehicle or there are more vehicles, but they cannot load unassignedJob
            //if only one vehicle, I want the job to be inserted with min iCosts
            //if there are more vehicles, I want this job to be prioritized since there are no alternatives
            score = Integer.MAX_VALUE - best.getInsertionCost() + scoringFunction.score(best, unassignedJob);
        } else {
            score = (secondBest.getInsertionCost() - best.getInsertionCost()) + scoringFunction.score(best, unassignedJob);
        }
        return score;
    }


}
