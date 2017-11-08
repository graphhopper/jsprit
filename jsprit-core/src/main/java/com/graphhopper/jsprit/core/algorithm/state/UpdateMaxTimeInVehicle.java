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

    private Map<Integer, Map<Job, Double>> openPickupEndTimesPerVehicle = new HashMap<>();

    private Map<Integer, Map<TourActivity, Double>> slackTimesPerVehicle = new HashMap<>();

    private Map<Integer, Map<TourActivity, Double>> actStartTimesPerVehicle = new HashMap<>();

    private VehicleRoute route;

    private final StateManager stateManager;

    private final StateId minSlackId;

    private final StateId openJobsId;

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


    public UpdateMaxTimeInVehicle(StateManager stateManager, StateId slackTimeId, TransportTime transportTime, VehicleRoutingActivityCosts activityCosts, StateId openJobsId) {
        this.stateManager = stateManager;
        this.minSlackId = slackTimeId;
        this.openJobsId = openJobsId;
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
        openPickupEndTimesPerVehicle.clear();
        slackTimesPerVehicle.clear();
        actStartTimesPerVehicle.clear();
        vehicles = vehiclesToUpdate.get(route);
        this.route = route;
        for(Vehicle v : vehicles){
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();
            openPickupEndTimesPerVehicle.put(vehicleIndex, new HashMap<Job, Double>());
            slackTimesPerVehicle.put(vehicleIndex, new HashMap<TourActivity, Double>());
            actStartTimesPerVehicle.put(vehicleIndex, new HashMap<TourActivity, Double>());
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
            double activityEnd = activityStart + activityCosts.getActivityDuration(activity, activityArrival, route.getDriver(), v);
            Map<Job, Double> openPickups = openPickupEndTimesPerVehicle.get(vehicleIndex);
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
                slackTimesPerVehicle.get(vehicleIndex).put(activity, slackTime);
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

    private void memorizeActStart(TourActivity activity, Vehicle v, double activityStart) {
        actStartTimesPerVehicle.get(v.getVehicleTypeIdentifier().getIndex()).put(activity, activityStart);
    }

    @Override
    public void finish() {
        for(Vehicle v : vehicles) {
            int vehicleIndex = v.getVehicleTypeIdentifier().getIndex();

            //!!! open routes !!!
            double routeEnd;
            if(!v.isReturnToDepot()) routeEnd = prevActEndTimes[vehicleIndex];
            else routeEnd = prevActEndTimes[vehicleIndex] + transportTime.getTransportTime(prevActLocations[vehicleIndex],v.getEndLocation(),prevActEndTimes[vehicleIndex],route.getDriver(),v);

            Map<Job, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimesPerVehicle.get(vehicleIndex).keySet()) {
                double actEndTime = openPickupEndTimesPerVehicle.get(vehicleIndex).get(job);
                double slackTime = job.getMaxTimeInVehicle() - (routeEnd - actEndTime);
                openDeliveries.put(job, slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, minSlackId, minSlackTimeAtEnd);
            stateManager.putRouteState(route, v, openJobsId, new HashMap<>(openDeliveries));
            List<TourActivity> acts = new ArrayList<>(this.route.getActivities());
            Collections.reverse(acts);
            for (TourActivity act : acts) {
                Job job = ((TourActivity.JobActivity) act).getJob();
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    openDeliveries.remove(job);
                    double minSlackTime = minSlackTime(openDeliveries);
//                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, openJobsId, new HashMap<>(openDeliveries));
                    stateManager.putActivityState(act, v, minSlackId, minSlackTime);
                } else {
                    if (slackTimesPerVehicle.get(vehicleIndex).containsKey(act)) {
                        double slackTime = slackTimesPerVehicle.get(vehicleIndex).get(act);
                        openDeliveries.put(job, slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
//                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, openJobsId, new HashMap<>(openDeliveries));
                    stateManager.putActivityState(act, v, minSlackId, minSlackTime);
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

            Map<Job, Double> openDeliveries = new HashMap<>();
            for (Job job : openPickupEndTimesPerVehicle.get(vehicleIndex).keySet()) {
                if (job == ignore) continue;
                double actEndTime = openPickupEndTimesPerVehicle.get(vehicleIndex).get(job);
                double slackTime = job.getMaxTimeInVehicle() - (routeEnd - actEndTime);
                openDeliveries.put(job, slackTime);
            }

            double minSlackTimeAtEnd = minSlackTime(openDeliveries);
            stateManager.putRouteState(route, v, minSlackId, routeEnd + minSlackTimeAtEnd);
            List<TourActivity> acts = new ArrayList<>(activities);
            Collections.reverse(acts);
            for (TourActivity act : acts) {
                Job job = ((TourActivity.JobActivity) act).getJob();
                if (act instanceof ServiceActivity || act instanceof PickupActivity) {
                    String jobId = job.getId();
                    openDeliveries.remove(jobId);
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, minSlackId, latestStart);
                } else {
                    if (slackTimesPerVehicle.get(vehicleIndex).containsKey(act)) {
                        double slackTime = slackTimesPerVehicle.get(vehicleIndex).get(act);
                        openDeliveries.put(job, slackTime);
                    }
                    double minSlackTime = minSlackTime(openDeliveries);
                    double latestStart = actStart(act, v) + minSlackTime;
                    stateManager.putActivityState(act, v, minSlackId, latestStart);
                }
            }
        }
    }

    private double actStart(TourActivity act, Vehicle v) {
        return actStartTimesPerVehicle.get(v.getVehicleTypeIdentifier().getIndex()).get(act);
    }

    private double minSlackTime(Map<Job, Double> openDeliveries) {
        double min = Double.MAX_VALUE;
        for(Double value : openDeliveries.values()){
           if(value < min) min = value;
        }
        return min;
    }
}
