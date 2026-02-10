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

package com.graphhopper.jsprit.core.analysis;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Calculates a set of statistics for a solution.
 */
public class SolutionAnalyser {


    private static class RouteStates {
        private final VehicleRoute route;

        private final VehicleRoutingProblem vrp;
        private final TransportDistance distanceCalculator;

        // States at each activity
        private final Map<TourActivity, Capacity> loadStates = new HashMap<>();
        private final Map<TourActivity, Double> distanceStates = new HashMap<>();
        private final Map<TourActivity, Double> waitingTimeStates = new HashMap<>();
        private final Map<TourActivity, Double> transportTimeStates = new HashMap<>();
        private final Map<TourActivity, Boolean> skillViolationStates = new HashMap<>();

        private final Map<TourActivity, Double> timeViolationStates = new HashMap<>();
        private final Map<TourActivity, Double> lastTransportTimeStates = new HashMap<>();

        private final Map<TourActivity, Double> lastTransportDistanceStates = new HashMap<>();

        private final Map<TourActivity, Double> lastTransportCostsStates = new HashMap<>();

        private final Map<TourActivity, Boolean> shipmentStates = new HashMap<>();

        private final Map<TourActivity, Boolean> backhaulStates = new HashMap<>();

        private final Map<TourActivity, Double> variableCostStates = new HashMap<>();
        public Capacity loadAtBeginning;
        public double totalVariableCost;

        // Route-level states
        private Capacity maxLoad;
        private double totalDistance;
        private double totalTransportTime;
        private double totalWaitingTime;
        private double totalServiceTime;

        private double totalOperationTime;
        private double totalTimeWindowViolation;
        private int pickupCount;
        private int deliveryCount;

        private int pickupCountAtBeginning;
        private int deliveryCountAtEnd;
        private Capacity totalPickupLoad;
        private Capacity totalDeliveryLoad;

        private boolean shipmentConstraintOnRouteViolated = false;

        private boolean backhaulConstraintOnRouteViolated = false;
        private boolean skillViolation;
        private Capacity loadAtEnd;


        public RouteStates(VehicleRoute route, VehicleRoutingProblem vrp, TransportDistance distanceCalculator) {
            this.route = route;
            this.vrp = vrp;
            this.distanceCalculator = distanceCalculator;
            calculate();
        }

        public void calculate() {
            calculateLoadAndActivityStates();
            calculateTimeAndCostStates();
            calculateDistanceStates();
            calculateBackhaulAndShipmentStates();
            calculateSkillStates();
        }

        private void calculateSkillStates() {
            boolean skillConstraintViolatedOnRoute = false;
            for (TourActivity activity : route.getActivities()) {
                boolean violatedAtActivity = false;
                if (activity instanceof TourActivity.JobActivity) {
                    Set<String> requiredForActivity = ((TourActivity.JobActivity) activity).getJob().getRequiredSkills().values();
                    for (String skill : requiredForActivity) {
                        if (!route.getVehicle().getSkills().containsSkill(skill)) {
                            violatedAtActivity = true;
                            skillConstraintViolatedOnRoute = true;
                        }
                    }
                }
                this.skillViolationStates.put(activity, violatedAtActivity);
            }
            this.skillViolation = skillConstraintViolatedOnRoute;
        }

        private void calculateDistanceStates() {
            double sumDistance = 0.;
            TourActivity prevAct = route.getStart();
            for (TourActivity activity : route.getActivities()) {
                double distance = distanceCalculator.getDistance(prevAct.getLocation(), activity.getLocation(), prevAct.getEndTime(), route.getVehicle());
                sumDistance += distance;
                this.distanceStates.put(activity, sumDistance);
                this.lastTransportDistanceStates.put(activity, distance);
                prevAct = activity;
            }
            double distance = distanceCalculator.getDistance(prevAct.getLocation(), route.getEnd().getLocation(), prevAct.getEndTime(), route.getVehicle());
            this.lastTransportDistanceStates.put(route.getEnd(), distance);
            sumDistance += distance;
            this.totalDistance = sumDistance;
        }

        private void calculateTimeAndCostStates() {
            double currentTime = route.getDepartureTime();
            TourActivity prevAct = route.getStart();
            totalTransportTime = 0;
            totalWaitingTime = 0;
            totalServiceTime = 0;
            totalTimeWindowViolation = 0;
            waitingTimeStates.put(route.getStart(), 0d);
            totalVariableCost = 0;
            variableCostStates.put(route.getStart(), totalVariableCost);
            timeViolationStates.put(route.getStart(), 0d);

            for (TourActivity activity : route.getActivities()) {
                double transportCost = vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), activity.getLocation(), currentTime, route.getDriver(), route.getVehicle());
                totalVariableCost += transportCost;

                double transportTime = getTransportTime(prevAct, activity, currentTime);
                lastTransportTimeStates.put(activity, transportTime);
                totalTransportTime += transportTime;
                currentTime += transportTime;
                transportTimeStates.put(activity, totalTransportTime);

                // Waiting time
                double waitingTime = Math.max(0, activity.getTheoreticalEarliestOperationStartTime() - currentTime);
                totalWaitingTime += waitingTime;
                currentTime += waitingTime;
                waitingTimeStates.put(activity, waitingTime);

                // Time window violation
                double twViolation = Math.max(0, currentTime - activity.getTheoreticalLatestOperationStartTime());
                timeViolationStates.put(activity, twViolation);
                totalTimeWindowViolation += twViolation;

                double activityCost = vrp.getActivityCosts().getActivityCost(activity, currentTime, route.getDriver(), route.getVehicle());
                totalVariableCost += activityCost;
                variableCostStates.put(activity, totalVariableCost);
                lastTransportCostsStates.put(activity, transportCost + activityCost);

                // Service time
                double serviceTime = getActivityDuration(prevAct, activity, currentTime);
                totalServiceTime += serviceTime;
                currentTime += serviceTime;

                prevAct = activity;
            }

            // Handle final leg to end location
            double transportCost = vrp.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(), currentTime, route.getDriver(), route.getVehicle());
            totalVariableCost += transportCost;

            double transportTime = getTransportTime(prevAct, route.getEnd(), currentTime);
            totalTransportTime += transportTime;
            currentTime += transportTime;
            lastTransportTimeStates.put(route.getEnd(), transportTime);

            double activityCost = vrp.getActivityCosts().getActivityCost(route.getEnd(), currentTime, route.getDriver(), route.getVehicle());
            totalVariableCost += activityCost;
            variableCostStates.put(route.getEnd(), totalVariableCost);

            waitingTimeStates.put(route.getEnd(), 0d);

            double endTimeWindowViolation = Math.max(0,
                currentTime - route.getEnd().getTheoreticalLatestOperationStartTime()
            );
            timeViolationStates.put(route.getEnd(), endTimeWindowViolation);
            totalTimeWindowViolation += endTimeWindowViolation;
            totalOperationTime = currentTime - route.getDepartureTime();

        }

        private double getTransportTime(TourActivity from, TourActivity to, double departureTime) {
            return vrp.getTransportCosts().getTransportTime(
                from.getLocation(),
                to.getLocation(),
                departureTime,
                route.getDriver(),
                route.getVehicle()
            );
        }

        private double getActivityDuration(TourActivity prevAct, TourActivity activity, double arrivalTime) {
            return vrp.getActivityCosts().getActivityDuration(
                    prevAct,
                activity,
                arrivalTime,
                route.getDriver(),
                route.getVehicle()
            );
        }

        private void calculateBackhaulAndShipmentStates() {
            Map<String, PickupShipment> openShipments = new HashMap<>();
            boolean pickupOccured = false;
            boolean shipmentConstraintOnRouteViolated = false;
            boolean backhaulConstraintOnRouteViolated = false;
            for (TourActivity activity : route.getActivities()) {
                //shipment
                if (activity instanceof PickupShipment) {
                    openShipments.put(((PickupShipment) activity).getJob().getId(), (PickupShipment) activity);
                } else if (activity instanceof DeliverShipment) {
                    String jobId = ((DeliverShipment) activity).getJob().getId();
                    if (!openShipments.containsKey(jobId)) {
                        //deliverShipment without pickupShipment
                        shipmentStates.put(activity, true);
                        shipmentConstraintOnRouteViolated = true;
                    } else {
                        PickupShipment removed = openShipments.remove(jobId);
                        shipmentStates.put(removed, false);
                        shipmentStates.put(activity, false);
                    }
                } else {
                    shipmentStates.put(activity, false);
                }

                //backhaul
                if (activity instanceof DeliverService && pickupOccured) {
                    backhaulStates.put(activity, true);
                    backhaulConstraintOnRouteViolated = true;
                } else {
                    if (activity instanceof PickupService || activity instanceof ServiceActivity || activity instanceof PickupShipment) {
                        pickupOccured = true;
                        backhaulStates.put(activity, false);
                    } else {
                        backhaulStates.put(activity, false);
                    }
                }
            }
            for (TourActivity act : openShipments.values()) {
                shipmentStates.put(act, true);
                shipmentConstraintOnRouteViolated = true;
            }
            this.shipmentConstraintOnRouteViolated = shipmentConstraintOnRouteViolated;
            this.backhaulConstraintOnRouteViolated = backhaulConstraintOnRouteViolated;

        }

        private void calculateLoadAndActivityStates() {
            Capacity loadAtDepot = Capacity.Builder.newInstance().build();
            Capacity loadAtEnd = Capacity.Builder.newInstance().build();
            for (Job j : route.getTourActivities().getJobs()) {
                if (j.isPickedUpAtVehicleStart()) {
                    loadAtDepot = Capacity.addup(loadAtDepot, j.getSize());
                }
                if (j.isDeliveredToVehicleEnd()) {
                    loadAtEnd = Capacity.addup(loadAtEnd, j.getSize());
                }
            }
            this.loadAtBeginning = loadAtDepot;
            this.loadAtEnd = loadAtEnd;

            Capacity maxLoad = Capacity.copyOf(loadAtDepot);
            Capacity currentLoad = Capacity.copyOf(loadAtDepot);

            ActivityCounters counters = new ActivityCounters();
            LoadTracking loads = new LoadTracking();

            for (TourActivity activity : route.getActivities()) {
                currentLoad = Capacity.addup(currentLoad, activity.getSize());
                maxLoad = Capacity.max(maxLoad, currentLoad);
                this.loadStates.put(activity, currentLoad);
                processActivity(activity, counters, loads);
            }
            this.maxLoad = maxLoad;
            updateStateFromCounters(counters, loads);
        }

        private static class ActivityCounters {
            int pickups = 0;
            int pickupsAtBeginning = 0;
            int deliveries = 0;
            int deliveriesAtEnd = 0;
        }

        private static class LoadTracking {
            Capacity pickedUp = Capacity.Builder.newInstance().build();
            Capacity delivered = Capacity.Builder.newInstance().build();
        }

        private void processActivity(TourActivity activity, ActivityCounters counters, LoadTracking loads) {
            if (activity instanceof PickupActivity) {
                processPickupActivity((PickupActivity) activity, counters, loads);
            } else if (activity instanceof DeliveryActivity) {
                processDeliveryActivity((DeliveryActivity) activity, counters, loads);
            }
        }

        private void processPickupActivity(PickupActivity activity, ActivityCounters counters, LoadTracking loads) {
            counters.pickups++;
            loads.pickedUp = Capacity.addup(loads.pickedUp, activity.getJob().getSize());

            if (activity instanceof PickupService) {
                counters.deliveriesAtEnd++;
            }
        }

        private void processDeliveryActivity(DeliveryActivity activity, ActivityCounters counters, LoadTracking loads) {
            counters.deliveries++;
            loads.delivered = Capacity.addup(loads.delivered, activity.getJob().getSize());

            if (activity instanceof DeliverService) {
                counters.pickupsAtBeginning++;
            }
        }

        private void updateStateFromCounters(ActivityCounters counters, LoadTracking loads) {
            this.pickupCount = counters.pickups;
            this.deliveryCount = counters.deliveries;
            this.totalPickupLoad = loads.pickedUp;
            this.totalDeliveryLoad = loads.delivered;
            this.pickupCountAtBeginning = counters.pickupsAtBeginning;
            this.deliveryCountAtEnd = counters.deliveriesAtEnd;
        }


    }

    private final VehicleRoutingProblem vrp;

    private final TransportDistance distanceCalculator;

    private final VehicleRoutingProblemSolution solution;

    private final Map<VehicleRoute, RouteStates> routeStatesMap = new HashMap<>();

    /**
     *
     */
    public SolutionAnalyser(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, TransportDistance distanceCalculator) {
        this.vrp = vrp;
        this.solution = solution;
        this.distanceCalculator = distanceCalculator;
        calculate();
    }

    public SolutionAnalyser(VehicleRoutingProblem vrp, VehicleRoutingProblemSolution solution, SolutionCostCalculator solutionCostCalculator, TransportDistance distanceCalculator) {
        this.vrp = vrp;
        this.solution = solution;
        this.distanceCalculator = distanceCalculator;
        calculate();
    }

    private void calculate() {
        for (VehicleRoute route : solution.getRoutes()) {
            RouteStates routeStates = new RouteStates(route, vrp, distanceCalculator);
            routeStatesMap.put(route, routeStates);
        }
    }


    /**
     * @param route to get the load at beginning from
     * @return load at start location of specified route
     */
    public Capacity getLoadAtBeginning(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).loadAtBeginning;
    }

    /**
     * @param route to get the load at the end from
     * @return load at end location of specified route
     */
    public Capacity getLoadAtEnd(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).loadAtEnd;
    }

    /**
     * @param route to get max load from
     * @return max load of specified route, i.e. for each capacity dimension the max value.
     */
    public Capacity getMaxLoad(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).maxLoad;
    }

    /**
     * @param activity to get the load from (after activity)
     * @return load right after the specified activity. If act is Start, it returns the load atBeginning of the specified
     * route. If act is End, it returns the load atEnd of specified route.
     * Returns null if no load can be found.
     */
    public Capacity getLoadRightAfterActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return getLoadAtBeginning(route);
        if (activity instanceof End) return getLoadAtEnd(route);
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).loadStates.get(activity);
//        return stateManager.getActivityState(activity, InternalStates.LOAD, Capacity.class);
    }

    private void verifyThatRouteContainsAct(TourActivity activity, VehicleRoute route) {
        if (!route.getActivities().contains(activity)) {
            throw new IllegalArgumentException("specified route does not contain specified activity " + activity);
        }
    }

    /**
     * @param activity to get the load from (before activity)
     * @return load just before the specified activity. If act is Start, it returns the load atBeginning of the specified
     * route. If act is End, it returns the load atEnd of specified route.
     */
    public Capacity getLoadJustBeforeActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return getLoadAtBeginning(route);
        if (activity instanceof End) return getLoadAtEnd(route);
        verifyThatRouteContainsAct(activity, route);
        Capacity afterAct = this.routeStatesMap.get(route).loadStates.get(activity);
//        Capacity afterAct = stateManager.getActivityState(activity, InternalStates.LOAD, Capacity.class);
        if (afterAct != null && activity.getSize() != null) {
            return Capacity.subtract(afterAct, activity.getSize());
        } else return afterAct;
    }

    /**
     * @param route to get number of pickups from
     * @return number of pickups picked up on specified route (without load at beginning)
     */
    public Integer getNumberOfPickups(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).pickupCount;
    }

    /**
     * @param route to get number of deliveries from
     * @return number of deliveries delivered on specified route (without load at end)
     */
    public Integer getNumberOfDeliveries(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).deliveryCount;
    }

    /**
     * @param route to get the picked load from
     * @return picked load (without load at beginning)
     */
    public Capacity getLoadPickedUp(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalPickupLoad;
    }

    /**
     * @param route to get delivered load from
     * @return delivered laod (without load at end)
     */
    public Capacity getLoadDelivered(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalDeliveryLoad;
    }

    /**
     * @param route to get the capacity violation from
     * @return the capacity violation on this route, i.e. maxLoad - vehicleCapacity
     */
    public Capacity getCapacityViolation(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        Capacity maxLoad = getMaxLoad(route);
        return Capacity.max(Capacity.Builder.newInstance().build(), Capacity.subtract(maxLoad, route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the capacity violation from (at beginning of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAtBeginning(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        Capacity atBeginning = getLoadAtBeginning(route);
        return Capacity.max(Capacity.Builder.newInstance().build(), Capacity.subtract(atBeginning, route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the capacity violation from (at end of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAtEnd(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        Capacity atEnd = getLoadAtEnd(route);
        return Capacity.max(Capacity.Builder.newInstance().build(), Capacity.subtract(atEnd, route.getVehicle().getType().getCapacityDimensions()));
    }


    /**
     * @param route to get the capacity violation from (at activity of the route)
     * @return violation, i.e. all dimensions and their corresponding violation. For example, if vehicle has two capacity
     * dimension with dimIndex=0 and dimIndex=1 and dimIndex=1 is violated by 4 units then this method returns
     * [[dimIndex=0][dimValue=0][dimIndex=1][dimValue=4]]
     */
    public Capacity getCapacityViolationAfterActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        Capacity afterAct = getLoadRightAfterActivity(activity, route);
        return Capacity.max(Capacity.Builder.newInstance().build(), Capacity.subtract(afterAct, route.getVehicle().getType().getCapacityDimensions()));
    }

    /**
     * @param route to get the time window violation from
     * @return time violation of route, i.e. sum of individual activity time window violations.
     */
    public Double getTimeWindowViolation(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalTimeWindowViolation;
    }

    /**
     * @param activity to get the time window violation from
     * @param route    where activity needs to be part of
     * @return time violation of activity
     */
    public Double getTimeWindowViolationAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        return routeStatesMap.get(route).timeViolationStates.get(activity);
    }

    /**
     * @param route to check skill constraint
     * @return true if skill constraint is violated, i.e. if vehicle does not have the required skills to conduct all
     * activities on the specified route. Returns null if route is null or skill state cannot be found.
     */
    public Boolean hasSkillConstraintViolation(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).skillViolation;
    }

    /**
     * @param activity to check skill constraint
     * @param route    that must contain specified activity
     * @return true if vehicle does not have the skills to conduct specified activity, false otherwise. Returns null
     * if specified route or activity is null or if route does not contain specified activity or if skill state connot be
     * found. If specified activity is Start or End, it returns false.
     */
    public Boolean hasSkillConstraintViolationAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return false;
        if (activity instanceof End) return false;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).skillViolationStates.get(activity);
    }

    /**
     * Returns true if backhaul constraint is violated (false otherwise). Backhaul constraint is violated if either a
     * pickupService, serviceActivity (which is basically modeled as pickupService) or a pickupShipment occur before
     * a deliverService activity or - to put it in other words - if a depot bounded delivery occurs after a pickup, thus
     * the backhaul ensures depot bounded delivery activities first.
     *
     * @param route to check backhaul constraint
     * @return true if backhaul constraint for specified route is violated. returns null if route is null or no backhaul
     * state can be found. In latter case try routeChanged(route).
     */
    public Boolean hasBackhaulConstraintViolation(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).backhaulConstraintOnRouteViolated;
    }

    /**
     * @param activity to check backhaul violation
     * @param route    that must contain the specified activity
     * @return true if backhaul constraint is violated, false otherwise. Null if either specified route or activity is null.
     * Null if specified route does not contain specified activity.
     */
    public Boolean hasBackhaulConstraintViolationAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return false;
        if (activity instanceof End) return false;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).backhaulStates.get(activity);
    }

    /**
     * Returns true, if shipment constraint is violated. Two activities are associated to a shipment: pickupShipment
     * and deliverShipment. If both shipments are not in the same route OR deliverShipment occurs before pickupShipment
     * then the shipment constraint is violated.
     *
     * @param route to check the shipment constraint.
     * @return true if violated, false otherwise. Null if no state can be found or specified route is null.
     */
    public Boolean hasShipmentConstraintViolation(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).shipmentConstraintOnRouteViolated;
    }

    /**
     * Returns true if shipment constraint is violated, i.e. if activity is deliverShipment but no pickupShipment can be
     * found before OR activity is pickupShipment and no deliverShipment can be found afterwards.
     *
     * @param activity to check the shipment constraint
     * @param route    that must contain specified activity
     * @return true if shipment constraint is violated, false otherwise. If activity is either Start or End, it returns
     * false. Returns null if either specified activity or route is null or route does not containt activity.
     */
    public Boolean hasShipmentConstraintViolationAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return false;
        if (activity instanceof End) return false;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).shipmentStates.get(activity);
    }


    /**
     * @param route to get the total operation time from
     * @return operation time of this route, i.e. endTime - startTime of specified route
     */
    public Double getOperationTime(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalOperationTime;
    }

    /**
     * @param route to get the total waiting time from
     * @return total waiting time of this route, i.e. sum of waiting times at activities.
     * Returns null if no waiting time value exists for the specified route
     */
    public Double getWaitingTime(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalWaitingTime;
    }

    /**
     * @param route to get the total transport time from
     * @return total transport time of specified route. Returns null if no time value exists for the specified route.
     */
    public Double getTransportTime(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalTransportTime;
    }

    /**
     * @param route to get the total service time from
     * @return total service time of specified route. Returns null if no time value exists for specified route.
     */
    public Double getServiceTime(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalServiceTime;
    }

    /**
     * @param route to get the transport costs from
     * @return total variable transport costs of route, i.e. sum of transport costs specified by
     * vrp.getTransportCosts().getTransportCost(fromId,toId,...)
     */
    public Double getVariableTransportCosts(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalVariableCost;
    }

    /**
     * @param route to get the fixed costs from
     * @return fixed costs of route, i.e. fixed costs of employed vehicle on this route.
     */
    public Double getFixedCosts(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return route.getVehicle().getType().getVehicleCostParams().fix;
    }


    /**
     * @param activity to get the variable transport costs from
     * @param route    where the activity should be part of
     * @return variable transport costs at activity, i.e. sum of transport costs from start of route to the specified activity
     * If activity is start, it returns 0.. If it is end, it returns .getVariableTransportCosts(route).
     */
    public Double getVariableTransportCostsAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        if (activity instanceof End) return getVariableTransportCosts(route);
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).variableCostStates.get(activity);
    }

    /**
     * @param activity to get the transport time from
     * @param route    where the activity should be part of
     * @return transport time at the activity, i.e. the total time spent driving since the start of the route to the specified activity.
     */
    public Double getTransportTimeAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        if (activity instanceof End) return getTransportTime(route);
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).transportTimeStates.get(activity);
    }

    /**
     * @param activity to get the last transport time from
     * @param route    where the activity should be part of
     * @return The transport time from the previous activity to this one.
     */
    public Double getLastTransportTimeAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).lastTransportTimeStates.get(activity);
    }

    /**
     * @param activity to get the last transport distance from
     * @param route    where the activity should be part of
     * @return The transport distance from the previous activity to this one.
     */
    public Double getLastTransportDistanceAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).lastTransportDistanceStates.get(activity);
    }

    /**
     * @param activity to get the last transport cost from
     * @param route    where the activity should be part of
     * @return The transport cost from the previous activity to this one.
     */
    public Double getLastTransportCostAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).lastTransportCostsStates.get(activity);
    }


    /**
     * @param activity to get the waiting from
     * @param route    where activity should be part of
     * @return waiting time at activity
     */
    public Double getWaitingTimeAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        return routeStatesMap.get(route).waitingTimeStates.get(activity);
    }

    /**
     * @param route to get the distance from
     * @return total distance of route
     */
    public Double getDistance(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).totalDistance;
    }

    /**
     * @param activity at which is distance of the current route is measured
     * @return distance at activity
     */
    public Double getDistanceAtActivity(TourActivity activity, VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        if (activity == null) throw new IllegalArgumentException("activity is missing.");
        if (activity instanceof Start) return 0.;
        if (activity instanceof End) return getDistance(route);
        verifyThatRouteContainsAct(activity, route);
        return routeStatesMap.get(route).distanceStates.get(activity);
    }

    /**
     * @return number of pickups in specified solution (without load at beginning of each route)
     */
    public Integer getNumberOfPickups() {
        return routeStatesMap.values().stream()
            .mapToInt(rs -> rs.pickupCount)
            .sum();
    }

    /**
     * @param route to get the number of pickups at beginning from
     * @return number of pickups at beginning
     */
    public Integer getNumberOfPickupsAtBeginning(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).pickupCountAtBeginning;
    }

    /**
     * @return number of pickups in specified solution at beginning of each route
     */
    public Integer getNumberOfPickupsAtBeginning() {
        return routeStatesMap.values().stream()
            .mapToInt(rs -> rs.pickupCountAtBeginning)
            .sum();
    }

    /**
     * @return number of deliveries in specified solution (without load at end of each route)
     */
    public Integer getNumberOfDeliveries() {
        return routeStatesMap.values().stream()
            .mapToInt(rs -> rs.deliveryCount)
            .sum();
    }

    /**
     * @return number of deliveries in specified solution at end of each route
     */
    public Integer getNumberOfDeliveriesAtEnd() {
        return routeStatesMap.values().stream()
            .mapToInt(rs -> rs.deliveryCountAtEnd)
            .sum();
    }

    /**
     * @param route to get the number of deliveries at end from
     * @return number of deliveries at end of specified route
     */
    public Integer getNumberOfDeliveriesAtEnd(VehicleRoute route) {
        if (route == null) throw new IllegalArgumentException("route is missing.");
        return routeStatesMap.get(route).deliveryCountAtEnd;
    }

    /**
     * @return load picked up in solution (without load at beginning of each route)
     */
    public Capacity getLoadPickedUp() {
        return routeStatesMap.values().stream()
            .map(rs -> rs.totalPickupLoad)
            .reduce(Capacity.Builder.newInstance().build(), Capacity::addup);
    }

    /**
     * @return load picked up in solution at beginning of each route
     */
    public Capacity getLoadAtBeginning() {
        return routeStatesMap.values().stream()
            .map(rs -> rs.loadAtBeginning)
            .reduce(Capacity.Builder.newInstance().build(), Capacity::addup);
    }

    /**
     * @return load delivered in solution (without load at end of each route)
     */
    public Capacity getLoadDelivered() {
        return routeStatesMap.values().stream()
            .map(rs -> rs.totalDeliveryLoad)
            .reduce(Capacity.Builder.newInstance().build(), Capacity::addup);
    }

    /**
     * @return load delivered in solution at end of each route
     */
    public Capacity getLoadAtEnd() {
        return routeStatesMap.values().stream()
            .map(rs -> rs.loadAtEnd)
            .reduce(Capacity.Builder.newInstance().build(), Capacity::addup);
    }


    /**
     * @return total distance for specified solution
     */
    public Double getDistance() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalDistance)
            .sum();
    }

    /**
     * @return total operation time for specified solution
     */
    public Double getOperationTime() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalOperationTime)
            .sum();
    }

    public Double getMaxOperationTime() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalOperationTime)
            .max()
            .orElse(0.0);
    }

    /**
     * @return total waiting time for specified solution
     */
    public Double getWaitingTime() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalWaitingTime)
            .sum();
    }

    /**
     * @return total transportation time
     */
    public Double getTransportTime() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalTransportTime)
            .sum();
    }

    /**
     * @return total time window violation for specified solution
     */
    public Double getTimeWindowViolation() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalTimeWindowViolation)
            .sum();
    }

    /**
     * @return total capacity violation for specified solution
     */
    public Capacity getCapacityViolation() {
        return routeStatesMap.values().stream()
            .map(rs -> getCapacityViolation(rs.route))
            .reduce(Capacity.Builder.newInstance().build(), Capacity::addup);
    }

    /**
     * @return total service time for specified solution
     */
    public Double getServiceTime() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalServiceTime)
            .sum();
    }

    /**
     * @return total fixed costs for specified solution
     */
    public Double getFixedCosts() {
        return solution.getRoutes().stream()
            .mapToDouble(this::getFixedCosts)
            .sum();
    }

    /**
     * @return total variable transport costs for specified solution
     */
    public Double getVariableTransportCosts() {
        return routeStatesMap.values().stream()
            .mapToDouble(rs -> rs.totalVariableCost)
            .sum();
    }

    /**
     * @return total costs defined by solutionCostCalculator
     */
    public Double getTotalCosts() {
        return getFixedCosts() + getVariableTransportCosts();
    }

    /**
     * @return true if at least one route in specified solution has shipment constraint violation
     */
    public Boolean hasShipmentConstraintViolation() {
        return routeStatesMap.values().stream()
            .anyMatch(rs -> rs.shipmentConstraintOnRouteViolated);
    }

    /**
     * @return true if at least one route in specified solution has backhaul constraint violation
     */
    public Boolean hasBackhaulConstraintViolation() {
        return routeStatesMap.values().stream()
            .anyMatch(rs -> rs.backhaulConstraintOnRouteViolated);
    }

    /**
     * @return true if at least one route in specified solution has skill constraint violation
     */
    public Boolean hasSkillConstraintViolation() {
        return routeStatesMap.values().stream()
            .anyMatch(rs -> rs.skillViolation);
    }


}

