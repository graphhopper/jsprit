package jsprit.core.algorithm.recreate;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleFleetManager;
import jsprit.core.problem.vehicle.VehicleImpl;

import java.util.*;

/**
 * Created by schroeder on 15/10/15.
 */
class InsertionDataUpdater {

    static boolean update(boolean addAllAvailable, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, TreeSet<VersionedInsertionData> insertionDataSet, int updateRound, Job unassignedJob, Collection<VehicleRoute> routes) {
        for(VehicleRoute route : routes) {
            Collection<Vehicle> relevantVehicles = new ArrayList<Vehicle>();
            if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                relevantVehicles.add(route.getVehicle());
                if(addAllAvailable && !initialVehicleIds.contains(route.getVehicle().getId())){
                    relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                }
            } else relevantVehicles.addAll(fleetManager.getAvailableVehicles());
            for (Vehicle v : relevantVehicles) {
                double depTime = v.getEarliestDeparture();
                InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, v, depTime, route.getDriver(), Double.MAX_VALUE);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    continue;
                }
                insertionDataSet.add(new VersionedInsertionData(iData, updateRound, route));
            }
        }
        return true;
    }



    static VehicleRoute findRoute(Collection<VehicleRoute> routes, Job job) {
        for(VehicleRoute r : routes){
            if(r.getVehicle().getBreak() == job) return r;
        }
        return null;
    }

    static Comparator<VersionedInsertionData> getComparator(){
        return new Comparator<VersionedInsertionData>() {
            @Override
            public int compare(VersionedInsertionData o1, VersionedInsertionData o2) {
                if(o1.getiData().getInsertionCost() < o2.getiData().getInsertionCost()) return -1;
                return 1;
            }
        };
    }

    static ScoredJob getBest(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, ScoringFunction scoringFunction, TreeSet<VersionedInsertionData>[] priorityQueues, Map<VehicleRoute, Integer> updates, List<Job> unassignedJobList, List<Job> badJobs) {
        ScoredJob bestScoredJob = null;
        for(Job j : unassignedJobList){
            VehicleRoute bestRoute = null;
            InsertionData best = null;
            InsertionData secondBest = null;
            TreeSet<VersionedInsertionData> priorityQueue = priorityQueues[j.getIndex()];
            Iterator<VersionedInsertionData> iterator = priorityQueue.iterator();
            while(iterator.hasNext()){
                VersionedInsertionData versionedIData = iterator.next();
                if(bestRoute != null){
                    if(versionedIData.getRoute() == bestRoute){
                        continue;
                    }
                }
                if(versionedIData.getiData() instanceof InsertionData.NoInsertionFound) continue;
                if(!(versionedIData.getRoute().getVehicle() instanceof VehicleImpl.NoVehicle)) {
                    if (versionedIData.getiData().getSelectedVehicle() != versionedIData.getRoute().getVehicle()) {
                        if (!switchAllowed) continue;
                        if (initialVehicleIds.contains(versionedIData.getRoute().getVehicle().getId())) continue;
                    }
                }
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
            InsertionData iData = insertionCostsCalculator.getInsertionData(emptyRoute, j, null, -1, null, Double.MAX_VALUE);
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
