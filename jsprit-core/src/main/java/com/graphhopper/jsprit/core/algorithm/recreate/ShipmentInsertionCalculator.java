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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


final class ShipmentInsertionCalculator extends AbstractInsertionCalculator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShipmentInsertionCalculator.class);

    private final ConstraintManager constraintManager;

    private final ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final JobActivityFactory activityFactory;

    private final AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private InsertionPositionFilter positionFilter;

    public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager, JobActivityFactory jobActivityFactory) {
        super();
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        this.constraintManager = constraintManager;
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = jobActivityFactory;
        LOGGER.debug("initialise {}", this);
    }

    /**
     * Sets the position filter for reducing position evaluations.
     * <p>
     * Position filtering selects a subset of candidate positions to evaluate
     * for shipment pickup and delivery, reducing the O(p²) complexity.
     *
     * @param filter the position filter, or null to disable filtering
     */
    public void setPositionFilter(InsertionPositionFilter filter) {
        this.positionFilter = filter;
    }

    /**
     * Gets the position filter.
     *
     * @return the position filter, or null if not set
     */
    public InsertionPositionFilter getPositionFilter() {
        return positionFilter;
    }

    @Override
    public String toString() {
        return "[name=calculatesShipmentInsertion]";
    }

    /**
     * Calculates the marginal cost of inserting job i locally. This is based on the
     * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
     */
    @Override
    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
        LOGGER.trace("shipment-id: " + jobToInsert.getId() + " Starting insertion evaluation into vehicle {} with departure time {}", newVehicle.getId(), newVehicleDepartureTime);

        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Shipment shipment = (Shipment) jobToInsert;
        // Call createActivities once and cache both activities
        List<AbstractActivity> shipmentActivities = activityFactory.createActivities(shipment);
        TourActivity pickupShipment = shipmentActivities.get(0);
        TourActivity deliverShipment = shipmentActivities.get(1);
        insertionContext.getAssociatedActivities().add(pickupShipment);
        insertionContext.getAssociatedActivities().add(deliverShipment);

        /*
        check hard route constraints
         */
        InsertionData noInsertion = checkRouteConstraints(insertionContext, constraintManager);
        if (noInsertion != null) return noInsertion;
        /*
        check soft route constraints
         */
        InsertionCostBreakdown routeBreakdown = constraintManager.getRouteCostsBreakdown(insertionContext);
        double additionalICostsAtRouteLevel = routeBreakdown.getTotal();

        double accessEgressCosts = additionalAccessEgressCalculator.getCosts(insertionContext);
        if (accessEgressCosts != 0) {
            routeBreakdown.add("AccessEgress", accessEgressCosts);
        }
        additionalICostsAtRouteLevel += accessEgressCosts;

        double bestCost = bestKnownCosts;
        InsertionCostBreakdown bestBreakdown = null;

        int pickupInsertionIndex = InsertionData.NO_INDEX;
        int deliveryInsertionIndex = InsertionData.NO_INDEX;

        TimeWindow bestPickupTimeWindow = null;
        TimeWindow bestDeliveryTimeWindow = null;

        Start start = createStartActivity(newVehicle, newVehicleDepartureTime);

        End end = createEndActivity(newVehicle);

        ActivityContext pickupContext = new ActivityContext();

        List<TourActivity> activities = currentRoute.getTourActivities().getActivities();
        int activitiesSize = activities.size();

        List<HardConstraint> failedActivityConstraints = new ArrayList<>();
        // Reuse ActivityContext instances - safe because they're method-local
        ActivityContext pickupActivityContext = new ActivityContext();
        ActivityContext deliveryActivityContext = new ActivityContext();

        // Get filtered positions
        Set<Integer> filteredPickupPositions = getFilteredPickupPositions(shipment, currentRoute, activities);
        boolean useCachedTiming = canUseCachedTiming(currentRoute, newVehicle, newVehicleDepartureTime);
        boolean timingRequired = isTimingRequired();

        // Pickup loop - iterate through positions, evaluating only filtered ones
        TourActivity prevAct = start;
        double prevActEndTime = newVehicleDepartureTime;
        int i = 0;
        boolean tourEnd = false;

        while (!tourEnd) {
            TourActivity nextAct;
            if (i < activitiesSize) {
                nextAct = activities.get(i);
            } else {
                nextAct = end;
                tourEnd = true;
            }

            // Check if we should evaluate this pickup position
            boolean shouldEvaluatePickup = (filteredPickupPositions == null || filteredPickupPositions.contains(i));

            // If using cached timing and filtering, we can jump directly
            if (useCachedTiming && filteredPickupPositions != null && !shouldEvaluatePickup) {
                // Skip this position entirely - timing is cached on activities
                i++;
                continue;
            }

            // If timing is required and we can't use cached timing, we must track it even for skipped positions
            if (!shouldEvaluatePickup) {
                // Update timing but don't evaluate
                double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
                prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                prevAct = nextAct;
                i++;
                continue;
            }

            // If using cached timing, get timing from previous activity
            if (useCachedTiming && i > 0 && !activities.isEmpty()) {
                prevAct = activities.get(i - 1);
                prevActEndTime = activities.get(i - 1).getEndTime();
            }

            LOGGER.trace("Evaluating pickup at position {}", i);

            boolean pickupInsertionNotFulfilledBreak = true;
            for (TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                pickupActivityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(pickupActivityContext);
                ConstraintsStatus pickupShipmentConstraintStatus = fulfilled(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime, failedActivityConstraints, constraintManager);
                if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                    continue;
                } else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    continue;
                } else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                }
                InsertionCostBreakdown pickupActBreakdown = constraintManager.getActivityCostsBreakdown(
                        insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                double additionalPickupICosts = pickupActBreakdown.getTotal();
                double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);

                TourActivity prevAct_deliveryLoop = pickupShipment;
                double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);

                pickupContext.setArrivalTime(shipmentPickupArrTime);
                pickupContext.setEndTime(shipmentPickupEndTime);
                pickupContext.setInsertionIndex(i);
                insertionContext.setRelatedActivityContext(pickupContext);

                double prevActEndTime_deliveryLoop = shipmentPickupEndTime;

                if (bestCost <= pickupAIC + additionalPickupICosts + additionalICostsAtRouteLevel) {
                    continue;
                }

                // Get filtered delivery positions for this pickup position
                Set<Integer> filteredDeliveryPositions = getFilteredDeliveryPositions(shipment, currentRoute, activities, i);

                // Delivery loop - must iterate sequentially for timing, but only evaluate filtered positions
                int j = i;
                boolean tourEnd_deliveryLoop = false;
                while (!tourEnd_deliveryLoop) {
                    TourActivity nextAct_deliveryLoop;
                    if (j < activitiesSize) {
                        nextAct_deliveryLoop = activities.get(j);
                    } else {
                        nextAct_deliveryLoop = end;
                        tourEnd_deliveryLoop = true;
                    }

                    // Check if we should evaluate this delivery position
                    boolean shouldEvaluateDelivery = (filteredDeliveryPositions == null || filteredDeliveryPositions.contains(j));

                    if (shouldEvaluateDelivery) {
                        LOGGER.trace("Evaluating delivery at position {}", j);

                        boolean deliveryInsertionNotFulfilledBreak = true;
                        for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                            deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                            deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                            deliveryActivityContext.setInsertionIndex(j);
                            insertionContext.setActivityContext(deliveryActivityContext);
                            ConstraintsStatus deliverShipmentConstraintStatus = fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop, failedActivityConstraints, constraintManager);
                            if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                                InsertionCostBreakdown deliveryActBreakdown = constraintManager.getActivityCostsBreakdown(
                                        insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                                double additionalDeliveryICosts = deliveryActBreakdown.getTotal();
                                double deliveryAIC = calculate(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                                double totalActivityInsertionCosts = pickupAIC + deliveryAIC
                                        + additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
                                LOGGER.trace("Position cost: {}, feasible: {}", totalActivityInsertionCosts, true);

                                if (totalActivityInsertionCosts < bestCost) {
                                    bestCost = totalActivityInsertionCosts;
                                    pickupInsertionIndex = i;
                                    deliveryInsertionIndex = j;
                                    bestPickupTimeWindow = pickupTimeWindow;
                                    bestDeliveryTimeWindow = deliveryTimeWindow;
                                    // Build complete breakdown for this position
                                    bestBreakdown = new InsertionCostBreakdown();
                                    bestBreakdown.merge(routeBreakdown);
                                    bestBreakdown.merge(pickupActBreakdown);
                                    bestBreakdown.merge(deliveryActBreakdown);
                                    bestBreakdown.add("PickupInsertion", pickupAIC);
                                    bestBreakdown.add("DeliveryInsertion", deliveryAIC);
                                }
                                deliveryInsertionNotFulfilledBreak = false;
                            } else if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                                LOGGER.trace("Position cost: {}, feasible: {}", -1, false);
                                deliveryInsertionNotFulfilledBreak = false;
                            }
                        }
                        if (deliveryInsertionNotFulfilledBreak) {
                            LOGGER.trace("Position cost: {}, feasible: {}", -1, false);
                            break;
                        }
                    }

                    // Always update timing for delivery loop (needed for subsequent positions)
                    if (timingRequired) {
                        double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct_deliveryLoop.getLocation(), nextAct_deliveryLoop.getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                        prevActEndTime_deliveryLoop = Math.max(nextActArrTime, nextAct_deliveryLoop.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct_deliveryLoop, nextAct_deliveryLoop, nextActArrTime, newDriver, newVehicle);
                        prevAct_deliveryLoop = nextAct_deliveryLoop;
                    }
                    j++;
                }
            }
            if (pickupInsertionNotFulfilledBreak) {
                break;
            }
            // Update timing for pickup loop
            double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
            prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
            prevAct = nextAct;
            i++;
        }
        if (pickupInsertionIndex == InsertionData.NO_INDEX) {
            LOGGER.trace("Position cost: {}, feasible: {}", -1, false);
            return createNoInsertionFoundResult(failedActivityConstraints);
        }
        InsertionData insertionData = new InsertionData(bestCost, pickupInsertionIndex, deliveryInsertionIndex, newVehicle, newDriver);
        insertionData.setCostBreakdown(bestBreakdown);
        pickupShipment.setTheoreticalEarliestOperationStartTime(bestPickupTimeWindow.getStart());
        pickupShipment.setTheoreticalLatestOperationStartTime(bestPickupTimeWindow.getEnd());
        deliverShipment.setTheoreticalEarliestOperationStartTime(bestDeliveryTimeWindow.getStart());
        deliverShipment.setTheoreticalLatestOperationStartTime(bestDeliveryTimeWindow.getEnd());
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        addActivitiesAndVehicleSwitch(insertionData, currentRoute, newVehicle, pickupShipment, pickupInsertionIndex, deliverShipment, deliveryInsertionIndex, newVehicleDepartureTime);
        return insertionData;
    }

    private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
        return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);
    }

    /**
     * Gets filtered pickup positions using the position filter, or null to evaluate all.
     */
    private Set<Integer> getFilteredPickupPositions(Shipment shipment, VehicleRoute route, List<TourActivity> activities) {
        if (positionFilter == null || !positionFilter.isFilteringEnabled()) {
            return null;  // No filtering
        }
        List<Integer> filtered = positionFilter.filterPickupPositions(shipment, route, activities);
        return filtered != null ? new HashSet<>(filtered) : null;
    }

    /**
     * Gets filtered delivery positions using the position filter, or null to evaluate all.
     */
    private Set<Integer> getFilteredDeliveryPositions(Shipment shipment, VehicleRoute route, List<TourActivity> activities, int pickupPos) {
        if (positionFilter == null || !positionFilter.isFilteringEnabled()) {
            return null;  // No filtering
        }
        List<Integer> filtered = positionFilter.filterDeliveryPositions(shipment, route, activities, pickupPos);
        return filtered != null ? new HashSet<>(filtered) : null;
    }

    /**
     * Checks if cached timing from route activities can be used.
     * This is valid when the vehicle and departure time match the route.
     */
    private boolean canUseCachedTiming(VehicleRoute route, Vehicle newVehicle, double newDepartureTime) {
        if (route.isEmpty()) {
            return true;
        }
        Vehicle routeVehicle = route.getVehicle();
        if (routeVehicle.equals(newVehicle)) {
            return true;
        }
        return route.getDepartureTime() == newDepartureTime &&
                Objects.equals(routeVehicle.getType(), newVehicle.getType()) &&
                Objects.equals(routeVehicle.getStartLocation(), newVehicle.getStartLocation()) &&
                Objects.equals(routeVehicle.getEndLocation(), newVehicle.getEndLocation());
    }

    /**
     * Checks if timing propagation is required for constraint checking.
     * Returns true unless the filter explicitly says timing is not required.
     */
    private boolean isTimingRequired() {
        if (positionFilter == null) {
            return true;
        }
        return positionFilter.isTimingRequired();
    }

    /**
     * Returns all feasible insertion positions for a shipment in the route.
     * Each position is a (pickup, delivery) index pair.
     * Used for position-based regret calculation.
     */
    @Override
    public List<InsertionData> getAllInsertionPositions(final VehicleRoute currentRoute, final Job jobToInsert,
            final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver) {

        List<InsertionData> allPositions = new ArrayList<>();

        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Shipment shipment = (Shipment) jobToInsert;
        // Call createActivities once and cache both activities
        List<AbstractActivity> shipmentActivities = activityFactory.createActivities(shipment);
        TourActivity pickupShipment = (TourActivity) shipmentActivities.get(0);
        TourActivity deliverShipment = (TourActivity) shipmentActivities.get(1);
        insertionContext.getAssociatedActivities().add(pickupShipment);
        insertionContext.getAssociatedActivities().add(deliverShipment);

        // Check hard route constraints
        InsertionData noInsertion = checkRouteConstraints(insertionContext, constraintManager);
        if (noInsertion != null) {
            return allPositions; // Empty list
        }

        // Calculate route-level costs (same for all positions)
        InsertionCostBreakdown routeBreakdown = constraintManager.getRouteCostsBreakdown(insertionContext);
        double additionalICostsAtRouteLevel = routeBreakdown.getTotal();

        double accessEgressCosts = additionalAccessEgressCalculator.getCosts(insertionContext);
        if (accessEgressCosts != 0) {
            routeBreakdown.add("AccessEgress", accessEgressCosts);
        }
        additionalICostsAtRouteLevel += accessEgressCosts;

        Start start = createStartActivity(newVehicle, newVehicleDepartureTime);
        End end = createEndActivity(newVehicle);

        ActivityContext pickupContext = new ActivityContext();
        TourActivity prevAct = start;
        double prevActEndTime = newVehicleDepartureTime;

        int i = 0;
        boolean tourEnd = false;
        List<TourActivity> activities = currentRoute.getTourActivities().getActivities();
        int activitiesSize = activities.size();

        List<HardConstraint> failedActivityConstraints = new ArrayList<>();
        // Reuse ActivityContext instances - safe because they're method-local
        ActivityContext pickupActivityContext = new ActivityContext();
        ActivityContext deliveryActivityContext = new ActivityContext();

        while (!tourEnd) {
            TourActivity nextAct;
            if (i < activitiesSize) {
                nextAct = activities.get(i);
            } else {
                nextAct = end;
                tourEnd = true;
            }

            boolean pickupInsertionNotFulfilledBreak = true;
            for (TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                pickupActivityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(pickupActivityContext);

                ConstraintsStatus pickupStatus = fulfilled(insertionContext, prevAct, pickupShipment, nextAct,
                        prevActEndTime, failedActivityConstraints, constraintManager);

                if (pickupStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                    continue;
                } else if (pickupStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    continue;
                } else if (pickupStatus.equals(ConstraintsStatus.FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                }

                InsertionCostBreakdown pickupActBreakdown = constraintManager.getActivityCostsBreakdown(
                        insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                double additionalPickupICosts = pickupActBreakdown.getTotal();
                double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);

                TourActivity prevAct_deliveryLoop = pickupShipment;
                double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(
                        prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime())
                        + activityCosts.getActivityDuration(prevAct, pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);

                pickupContext.setArrivalTime(shipmentPickupArrTime);
                pickupContext.setEndTime(shipmentPickupEndTime);
                pickupContext.setInsertionIndex(i);
                insertionContext.setRelatedActivityContext(pickupContext);

                double prevActEndTime_deliveryLoop = shipmentPickupEndTime;

                // Delivery loop
                int j = i;
                boolean tourEnd_deliveryLoop = false;
                while (!tourEnd_deliveryLoop) {
                    TourActivity nextAct_deliveryLoop;
                    if (j < activitiesSize) {
                        nextAct_deliveryLoop = activities.get(j);
                    } else {
                        nextAct_deliveryLoop = end;
                        tourEnd_deliveryLoop = true;
                    }

                    boolean deliveryInsertionNotFulfilledBreak = true;
                    for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                        deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                        deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                        deliveryActivityContext.setInsertionIndex(j);
                        insertionContext.setActivityContext(deliveryActivityContext);

                        ConstraintsStatus deliveryStatus = fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment,
                                nextAct_deliveryLoop, prevActEndTime_deliveryLoop, failedActivityConstraints, constraintManager);

                        if (deliveryStatus.equals(ConstraintsStatus.FULFILLED)) {
                            InsertionCostBreakdown deliveryActBreakdown = constraintManager.getActivityCostsBreakdown(
                                    insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                            double additionalDeliveryICosts = deliveryActBreakdown.getTotal();
                            double deliveryAIC = calculate(insertionContext, prevAct_deliveryLoop, deliverShipment,
                                    nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                            double totalCost = pickupAIC + deliveryAIC + additionalICostsAtRouteLevel
                                    + additionalPickupICosts + additionalDeliveryICosts;

                            // Create InsertionData for this (pickup, delivery) position
                            InsertionData insertionData = new InsertionData(totalCost, i, j, newVehicle, newDriver);

                            // Build breakdown
                            InsertionCostBreakdown breakdown = new InsertionCostBreakdown();
                            breakdown.merge(routeBreakdown);
                            breakdown.merge(pickupActBreakdown);
                            breakdown.merge(deliveryActBreakdown);
                            breakdown.add("PickupInsertion", pickupAIC);
                            breakdown.add("DeliveryInsertion", deliveryAIC);
                            insertionData.setCostBreakdown(breakdown);

                            // Create fresh activities for this position's events
                            TourActivity pickupForPosition = activityFactory.createActivities(shipment).get(0);
                            TourActivity deliveryForPosition = activityFactory.createActivities(shipment).get(1);
                            pickupForPosition.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                            pickupForPosition.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                            deliveryForPosition.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                            deliveryForPosition.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());

                            insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
                            addActivitiesAndVehicleSwitch(insertionData, currentRoute, newVehicle,
                                    pickupForPosition, i, deliveryForPosition, j, newVehicleDepartureTime);

                            allPositions.add(insertionData);
                            deliveryInsertionNotFulfilledBreak = false;
                        } else if (deliveryStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                            deliveryInsertionNotFulfilledBreak = false;
                        }
                    }
                    if (deliveryInsertionNotFulfilledBreak) {
                        break;
                    }

                    double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(
                            prevAct_deliveryLoop.getLocation(), nextAct_deliveryLoop.getLocation(),
                            prevActEndTime_deliveryLoop, newDriver, newVehicle);
                    prevActEndTime_deliveryLoop = Math.max(nextActArrTime, nextAct_deliveryLoop.getTheoreticalEarliestOperationStartTime())
                            + activityCosts.getActivityDuration(prevAct_deliveryLoop, nextAct_deliveryLoop, nextActArrTime, newDriver, newVehicle);
                    prevAct_deliveryLoop = nextAct_deliveryLoop;
                    j++;
                }
            }
            if (pickupInsertionNotFulfilledBreak) {
                break;
            }

            double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(
                    prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
            prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime())
                    + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
            prevAct = nextAct;
            i++;
        }

        return allPositions;
    }
}
