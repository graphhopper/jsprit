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

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;

import java.util.*;

/**
 * Created by schroeder on 15/10/15.
 */
class InsertionDataUpdater {

    static boolean update(boolean addAllAvailable, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, TreeSet<VersionedInsertionData> insertionDataSet, int updateRound, Job unassignedJob, Collection<VehicleRoute> routes) {
        for(VehicleRoute route : routes) {
            Collection<Vehicle> relevantVehicles = new ArrayList<>();
            if (!(route.getVehicle() instanceof VehicleImpl.NoVehicle)) {
                relevantVehicles.add(route.getVehicle());
                if(addAllAvailable && !initialVehicleIds.contains(route.getVehicle().getId())){
                    relevantVehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                }
            } else relevantVehicles.addAll(fleetManager.getAvailableVehicles());
            double bestCost = Double.MAX_VALUE;
            InsertionData bestIData = new InsertionData.NoInsertionFound();
            for (Vehicle v : relevantVehicles) {
                double depTime = v.getEarliestDeparture();
                InsertionData iData = insertionCostsCalculator.getInsertionData(route, unassignedJob, v, depTime, route.getDriver(), Double.MAX_VALUE);
                if (iData instanceof InsertionData.NoInsertionFound) {
                    continue;
                }
                if (iData.getInsertionCost() < bestCost) {
                    bestIData = iData;
                    bestCost = iData.getInsertionCost();
                }
            }
            Iterator<VersionedInsertionData> iterator = insertionDataSet.iterator();
            while (iterator.hasNext()) {
                VersionedInsertionData versionedInsertionData = iterator.next();
                if (versionedInsertionData.getRoute() == route &&
                    versionedInsertionData.getVersion() != updateRound)
                    iterator.remove();
            }
            insertionDataSet.add(new VersionedInsertionData(bestIData, updateRound, route));
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
        return (o1, o2) -> {
            if (o1.getiData().getInsertionCost() < o2.getiData().getInsertionCost()) return -1;
            return 1;
        };
    }

    static ScoredJob getBest(boolean switchAllowed, Set<String> initialVehicleIds, VehicleFleetManager fleetManager, JobInsertionCostsCalculator insertionCostsCalculator, RegretScoringFunction scoringFunction, TreeSet<VersionedInsertionData>[] priorityQueues, Map<VehicleRoute, Integer> updates, List<Job> unassignedJobList, List<ScoredJob> badJobs) {
        ScoredJob bestScoredJob = null;
        for (Job j : unassignedJobList) {
            VehicleRoute bestRoute = null;
            InsertionData best = null;
            InsertionData secondBest = null;
            TreeSet<VersionedInsertionData> priorityQueue = priorityQueues[j.getIndex()];
            Iterator<VersionedInsertionData> iterator = priorityQueue.iterator();
            List<String> failedConstraintNames = new ArrayList<>();
            while (iterator.hasNext()) {
                VersionedInsertionData versionedIData = iterator.next();
                if(bestRoute != null){
                    if(versionedIData.getRoute() == bestRoute){
                        continue;
                    }
                }
                if (versionedIData.getiData() instanceof InsertionData.NoInsertionFound) {
                    failedConstraintNames.addAll(versionedIData.getiData().getFailedConstraintNames());
                    continue;
                }
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
                if(best == null) {
                    best = versionedIData.getiData();
                    bestRoute = versionedIData.getRoute();
                }
                else {
                    secondBest = versionedIData.getiData();
                    break;
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
            } else failedConstraintNames.addAll(iData.getFailedConstraintNames());
            if (best == null) {
                badJobs.add(new ScoredJob.BadJob(j, failedConstraintNames));
                continue;
            }
            double score = scoringFunction.score(best, secondBest, j);
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
            else if (scoredJob.getScore() == bestScoredJob.getScore()) {
                if (scoredJob.getJob().getId().compareTo(bestScoredJob.getJob().getId()) <= 0) {
                    bestScoredJob = scoredJob;
                }
            }
        }
        return bestScoredJob;
    }


}
