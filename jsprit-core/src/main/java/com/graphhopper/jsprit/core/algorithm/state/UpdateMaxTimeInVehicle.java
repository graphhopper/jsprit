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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.TransportTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.*;

/**
 * Created by schroeder on 15/09/16.
 */
public class UpdateMaxTimeInVehicle implements StateUpdater, ActivityVisitor{

    private Map<Integer,Map<Job,Double>> openPickupEndTimes = new HashMap<>();

    private Map<Integer,Map<TourActivity,Double>> slackTimes = new HashMap<>();

    private Map<Integer,Map<TourActivity,Double>> actStartTimes = new HashMap<>();

    private VehicleRoute route;

    private final StateManager stateManager;

    private final StateId latestStartId;

    private double[] prevActEndTimes;

    private Location[] prevActLocations;

    private Collection<Vehicle> vehicles;

    private final TransportTime transportTime;

    private final VehicleRoutingActivityCosts activityCosts;

    private UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate vehiclesToUpdate = new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

        @Override
        public Collection<Vehicle> get(VehicleRoute route) {
            return Arrays.asList(route.getVehicle());
        }

    };


    public UpdateMaxTimeInVehicle(StateManager stateManager, StateId slackTimeId, TransportTime transportTime, VehicleRoutingActivityCosts activityCosts) {
        this.stateManager = stateManager;
        this.latestStartId = slackTimeId;
        this.transportTime = transportTime;
        prevActEndTimes = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        prevActLocations = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        this.activityCosts = activityCosts;
    }


    public void setVehiclesToUpdate(UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    @Override
    public void begin(VehicleRoute route) {
        openPickupEndTimes.clear();
        slackTimes.clear();
        actStartTimes.clear();
        vehicles = vehiclesToUpdate.get(route);
        this.route = route;
        for(Vehicle v : vehicles){
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();
            openPickupEndTimes.put(vehicleIndex,new HashMap<Job, Double>());
            slackTimes.put(vehicleIndex,new HashMap<TourActivity, Double>());
            actStartTimes.put(vehicleIndex,new HashMap<TourActivity, Double>());
            prevActEndTimes[vehicleIndex] = v.getEarliestDeparture();
            prevActLocations[vehicleIndex] = v.getStartLocation();
        }
    }

    @Override
    public void visit(TourActivity activity) {
        double maxTime = getMaxTimeInVehicle(activity);

        for(Vehicle v : vehicles) {
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();
            Location prevActLocation = prevActLocations[vehicleIndex];
            double prevActEndTime = prevActEndTimes[v.getVehicleTypeIdentifier().getIndex()];
            double activityArrival = prevActEndTimes[v.getVehicleTypeIdentifier().getIndex()] + transportTime.getTransportTime(prevActLocation,activity.getLocation(),prevActEndTime,route.getDriver(),v);
            double activityStart = Math.max(activityArrival,activity.getTheoreticalEarliestOperationStartTime());
            memorizeActStart(activity,v,activityStart);
            double activityEnd = activityStart + activityCosts.getActivityDuration(null, activity, activityArrival, route.getDriver(), v);
            Map<Job, Double> openPickups = openPickupEndTimes.get(vehicleIndex);
            if (activity instanceof ServiceActivity || activity instanceof PickupActivity) {
                openPickups.put(((TourActivity.JobActivity) activity).getJob(), activityEnd);
            } else if (activity instanceof DeliveryActivity) {
                Job job = ((TourActivity.JobActivity) activity).getJob();
                double pickupEnd;
                if (openPickups.containsKey(job)) {
                    pickupEnd = openPickups.get(job);
                    openPickups.remove(job);
                } else pickupEnd = v.getEarliestDeparture();
                double slackTime = maxTime - (activityStart - pickupEnd);
                slackTimes.get(vehicleIndex).put(activity, slackTime);
            }
            prevActLocations[vehicleIndex] = activity.getLocation();
            prevActEndTimes[vehicleIndex] = activityEnd;
        }

    }

    private double getMaxTimeInVehicle(TourActivity activity) {
        double maxTime = Double.MAX_VALUE;
        if(activity instanceof TourActivity.JobActivity){
            maxTime = ((TourActivity.JobActivity) activity).getJob().getMaxTimeInVehicle();
        }
        return maxTime;
    }

//    private double getMaxTimeInVehicle(String jobId) {
//        double maxTime = Double.MAX_VALUE;
//        if(maxTimes.containsKey(jobId)){
//            maxTime = maxTimes.get(jobId);
//        }
//        return maxTime;
//    }

    private void memorizeActStart(TourActivity activity, Vehicle v, double activityStart) {
        actStartTimes.get(v.getVehicleTypeIdentifier().getIndex()).put(activity,activityStart);
    }

    @Override
    public void finish() {
        for(Vehicle v : vehicles) {
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();

            //!!! open routes !!!
            double routeEnd;
            if(!v.isReturnToDepot()) routeEnd = prevActEndTimes[vehicleIndex];
            else routeEnd = prevActEndTimes[vehicleIndex] + transportTime.getTransportTime(prevActLocations[vehicleIndex],v.getEndLocation(),prevActEndTimes[vehicleIndex],route.getDriver(),v);

            Map<String, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimes.get(vehicleIndex).keySet()) {
                double actEndTime = openPickupEndTimes.get(vehicleIndex).get(job);
                double slackTime = job.getMaxTimeInVehicle() - (routeEnd - actEndTime);
                openDeliveries.put(job.getId(), slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, latestStartId, routeEnd + minSlackTimeAtEnd);
            List<TourActivity> acts = new ArrayList<>(this.route.getActivities());
            Collections.reverse(acts);
            for (TourActivity act : acts) {
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    String jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    openDeliveries.remove(jobId);
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, latestStartId, latestStart);
                } else {
                    String jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    if(slackTimes.get(vehicleIndex).containsKey(act)){
                        double slackTime = slackTimes.get(vehicleIndex).get(act);
                        openDeliveries.put(jobId,slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, latestStartId, latestStart);
                }
            }
        }
    }

    public void finish(List<TourActivity> activities, Job ignore) {
        for (Vehicle v : vehicles) {
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();

            //!!! open routes !!!
            double routeEnd;
            if (!v.isReturnToDepot()) routeEnd = prevActEndTimes[vehicleIndex];
            else
                routeEnd = prevActEndTimes[vehicleIndex] + transportTime.getTransportTime(prevActLocations[vehicleIndex], v.getEndLocation(), prevActEndTimes[vehicleIndex], route.getDriver(), v);

            Map<String, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimes.get(vehicleIndex).keySet()) {
                if (job == ignore) continue;
                double actEndTime = openPickupEndTimes.get(vehicleIndex).get(job);
                double slackTime = job.getMaxTimeInVehicle() - (routeEnd - actEndTime);
                openDeliveries.put(job.getId(), slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, latestStartId, routeEnd + minSlackTimeAtEnd);
            List<TourActivity> acts = new ArrayList<>(activities);
            Collections.reverse(acts);
            for (TourActivity act : acts) {
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    String jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    openDeliveries.remove(jobId);
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, latestStartId, latestStart);
                } else {
                    String jobId = ((TourActivity.JobActivity) act).getJob().getId();
                    if (slackTimes.get(vehicleIndex).containsKey(act)) {
                        double slackTime = slackTimes.get(vehicleIndex).get(act);
                        openDeliveries.put(jobId, slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, latestStartId, latestStart);
                }
            }
        }
    }

    private double actStart(TourActivity act, Vehicle v) {
        return actStartTimes.get(v.getVehicleTypeIdentifier().getIndex()).get(act);
    }

    private double minSlackTime(Map<String, Double> openDeliveries) {
        double min = Double.MAX_VALUE;
        for(Double value : openDeliveries.values()){
           if(value < min) min = value;
        }
        return min;
    }
}
