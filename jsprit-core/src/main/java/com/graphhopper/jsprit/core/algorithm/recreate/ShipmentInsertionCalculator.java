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

    private ParallelInsertionScan parallelScan;

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
     * Set by the builders when the algorithm runs on a ForkJoinPool with more than one thread: the pickup
     * positions of a long route are then scanned in parallel, see {@link ParallelInsertionScan}.
     */
    public void setParallelScan(ParallelInsertionScan parallelScan) {
        this.parallelScan = parallelScan;
    }

    /**
     * Calculates the marginal cost of inserting job i locally. This is based on the
     * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
     */
    @Override
    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
        LOGGER.trace("shipment-id: {} starting insertion evaluation into vehicle {} with departure time {}", jobToInsert.getId(), newVehicle.getId(), newVehicleDepartureTime);

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

        Start start = createStartActivity(newVehicle, newVehicleDepartureTime);
        End end = createEndActivity(newVehicle);
        List<TourActivity> activities = currentRoute.getTourActivities().getActivities();

        Scan scan = new Scan(currentRoute, shipment, activities, start, end, newVehicle, newDriver, newVehicleDepartureTime,
            routeBreakdown, additionalICostsAtRouteLevel, bestKnownCosts,
            getFilteredPickupPositions(shipment, currentRoute, activities),
            canUseCachedTiming(currentRoute, newVehicle, newVehicleDepartureTime), isTimingRequired());
        int positions = activities.size() + 1;
        int tasks = parallelScan == null ? 1 : parallelScan.shipmentTasks(positions);
        Range best;
        if (tasks > 1) {
            // the bound every range prunes with is fixed before the ranges start, so what a range skips does not
            // depend on how far the other ranges have got
            List<Range> seeds = seedInsertions(scan, positions);
            double seedCost = bestKnownCosts;
            for (Range seed : seeds) {
                if (seed.pickupInsertionIndex != InsertionData.NO_INDEX && seed.bestCost < seedCost) seedCost = seed.bestCost;
            }
            final double bound = seedCost;
            ParallelInsertionScan.Shared shared = new ParallelInsertionScan.Shared();
            List<Range> ranges = parallelScan.run(ParallelInsertionScan.triangularBounds(positions, tasks), (from, to) -> {
                List<AbstractActivity> copies = activityFactory.createActivities(shipment);
                Range range = new Range(scan, copies.get(0), copies.get(1), from, to);
                range.shared = shared;
                range.bound = bound;
                range.scan();
                return range;
            });
            best = Range.mergeInOrder(ranges);
            for (Range seed : seeds) best.takeIfBetter(seed);
        } else {
            best = new Range(scan, pickupShipment, deliverShipment, 0, positions);
            best.scan();
        }

        if (best.pickupInsertionIndex == InsertionData.NO_INDEX) {
            LOGGER.trace("Position cost: {}, feasible: {}", -1, false);
            return createNoInsertionFoundResult(best.failedActivityConstraints);
        }
        InsertionData insertionData = new InsertionData(best.bestCost, best.pickupInsertionIndex, best.deliveryInsertionIndex, newVehicle, newDriver);
        insertionData.setCostBreakdown(best.bestBreakdown);
        insertionData.setSelectedPickupTimeWindow(best.bestPickupTimeWindow);
        insertionData.setSelectedTimeWindow(best.bestDeliveryTimeWindow);
        pickupShipment.setTheoreticalEarliestOperationStartTime(best.bestPickupTimeWindow.getStart());
        pickupShipment.setTheoreticalLatestOperationStartTime(best.bestPickupTimeWindow.getEnd());
        deliverShipment.setTheoreticalEarliestOperationStartTime(best.bestDeliveryTimeWindow.getStart());
        deliverShipment.setTheoreticalLatestOperationStartTime(best.bestDeliveryTimeWindow.getEnd());
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        addActivitiesAndVehicleSwitch(insertionData, currentRoute, newVehicle, pickupShipment, best.pickupInsertionIndex, deliverShipment, best.deliveryInsertionIndex, newVehicleDepartureTime);
        return insertionData;
    }

    /** Pickup positions that are evaluated completely before a parallel scan starts; their cheapest insertion is the bound of the ranges. */
    private static final int SEED_POSITIONS = 2;

    /**
     * The start of a parallel scan, on the calling thread: the pickup part of the insertion costs at every position
     * (no delivery is tried, so this is one cheap pass) and the complete evaluation of the {@link #SEED_POSITIONS}
     * positions where the pickup is cheapest. Their best insertion bounds the ranges: a pickup that alone costs more
     * is skipped. The ranges used to tell each other their best insertion while they ran; what a range skipped then
     * depended on the timing of the threads, and with equally cheap insertions the result did, too.
     *
     * @return the evaluated seed positions in position order, empty if no pickup is possible anywhere
     */
    private List<Range> seedInsertions(Scan scan, int positions) {
        double[] pickupCosts = new double[positions];
        java.util.Arrays.fill(pickupCosts, Double.MAX_VALUE);
        List<AbstractActivity> activities = activityFactory.createActivities(scan.shipment);
        Range pickups = new Range(scan, activities.get(0), activities.get(1), 0, positions);
        pickups.pickupCosts = pickupCosts;
        pickups.scan();
        int[] chosen = new int[SEED_POSITIONS];
        int n = 0;
        while (n < SEED_POSITIONS) {
            int cheapest = -1;
            for (int i = 0; i < positions; i++) {
                if (pickupCosts[i] >= scan.bestKnownCosts || contains(chosen, n, i)) continue;
                if (cheapest < 0 || pickupCosts[i] < pickupCosts[cheapest]) cheapest = i;
            }
            if (cheapest < 0) break;
            chosen[n++] = cheapest;
        }
        java.util.Arrays.sort(chosen, 0, n);
        List<Range> seeds = new ArrayList<>(n);
        for (int k = 0; k < n; k++) {
            List<AbstractActivity> copies = activityFactory.createActivities(scan.shipment);
            Range seed = new Range(scan, copies.get(0), copies.get(1), chosen[k], chosen[k] + 1);
            seed.scan();
            seeds.add(seed);
        }
        return seeds;
    }

    private static boolean contains(int[] values, int length, int value) {
        for (int i = 0; i < length; i++) if (values[i] == value) return true;
        return false;
    }

    /** What every range of one shipment evaluation shares, all of it read-only while the ranges run. */
    private record Scan(VehicleRoute route, Shipment shipment, List<TourActivity> activities, Start start, End end,
                        Vehicle vehicle, Driver driver, double departureTime,
                        InsertionCostBreakdown routeBreakdown, double additionalICostsAtRouteLevel, double bestKnownCosts,
                        Set<Integer> filteredPickupPositions, boolean useCachedTiming, boolean timingRequired) {
    }

    /**
     * The scan of the pickup positions {@code from} (inclusive) to {@code to} (exclusive) with its own scratch
     * objects and its result. The whole route is one range when the scan runs sequentially.
     */
    private final class Range {
        private final Scan s;
        private final int from, to;
        private final TourActivity pickupShipment, deliverShipment;
        private final JobInsertionContext insertionContext;
        private final ActivityContext pickupContext = new ActivityContext();
        private final ActivityContext pickupActivityContext = new ActivityContext();
        private final ActivityContext deliveryActivityContext = new ActivityContext();
        private final double[] pickupActCosts, deliveryActCosts;

        final List<HardConstraint> failedActivityConstraints = new ArrayList<>();
        double bestCost;
        InsertionCostBreakdown bestBreakdown = null;
        int pickupInsertionIndex = InsertionData.NO_INDEX;
        int deliveryInsertionIndex = InsertionData.NO_INDEX;
        TimeWindow bestPickupTimeWindow = null;
        TimeWindow bestDeliveryTimeWindow = null;
        /** The pickup position at which every time window answered NOT_FULFILLED_BREAK and the loop stopped, or -1. */
        int brokeAt = -1;
        /** What the ranges of one parallel evaluation tell each other (see {@link ParallelInsertionScan.Shared}); null when the range is the whole route. */
        ParallelInsertionScan.Shared shared;
        /** Costs of an insertion known before the scan started (see {@link #seedInsertions}); a pickup that alone costs more is skipped. */
        double bound = Double.MAX_VALUE;
        /** Pickup-only pass: the cheapest pickup part per position is written here and no delivery is tried; null otherwise. */
        double[] pickupCosts;

        Range(Scan s, TourActivity pickupShipment, TourActivity deliverShipment, int from, int to) {
            this.s = s;
            this.from = from;
            this.to = to;
            this.pickupShipment = pickupShipment;
            this.deliverShipment = deliverShipment;
            this.bestCost = s.bestKnownCosts;
            this.insertionContext = new JobInsertionContext(s.route, s.shipment, s.vehicle, s.driver, s.departureTime);
            insertionContext.getAssociatedActivities().add(pickupShipment);
            insertionContext.getAssociatedActivities().add(deliverShipment);
            this.pickupActCosts = constraintManager.newActivityCostsScratch();
            this.deliveryActCosts = constraintManager.newActivityCostsScratch();
        }

        /**
         * Merges the ranges in position order into the first one, exactly as the sequential loop would have
         * seen them: a cheaper insertion replaces, an equally expensive one does not (it lies at a later
         * position), and nothing behind the position where a range broke is looked at.
         */
        static Range mergeInOrder(List<Range> ranges) {
            Range merged = ranges.get(0);
            for (int k = 1; k < ranges.size() && merged.brokeAt < 0; k++) {
                Range other = ranges.get(k);
                merged.failedActivityConstraints.addAll(other.failedActivityConstraints);
                if (other.pickupInsertionIndex != InsertionData.NO_INDEX && other.bestCost < merged.bestCost) {
                    merged.bestCost = other.bestCost;
                    merged.bestBreakdown = other.bestBreakdown;
                    merged.pickupInsertionIndex = other.pickupInsertionIndex;
                    merged.deliveryInsertionIndex = other.deliveryInsertionIndex;
                    merged.bestPickupTimeWindow = other.bestPickupTimeWindow;
                    merged.bestDeliveryTimeWindow = other.bestDeliveryTimeWindow;
                }
                merged.brokeAt = other.brokeAt;
            }
            return merged;
        }

        /**
         * Takes the insertion of a seed position if it is cheaper, or equally expensive at an earlier pickup position.
         * A seed whose delivery lowers the costs is skipped by the ranges (its pickup alone costs more than the bound
         * it sets), so it is merged in here.
         */
        void takeIfBetter(Range seed) {
            if (seed.pickupInsertionIndex == InsertionData.NO_INDEX) return;
            if (brokeAt >= 0 && seed.pickupInsertionIndex > brokeAt) return;
            boolean earlierTie = seed.bestCost == bestCost && pickupInsertionIndex != InsertionData.NO_INDEX && seed.pickupInsertionIndex < pickupInsertionIndex;
            if (seed.bestCost < bestCost || earlierTie) {
                bestCost = seed.bestCost;
                bestBreakdown = seed.bestBreakdown;
                pickupInsertionIndex = seed.pickupInsertionIndex;
                deliveryInsertionIndex = seed.deliveryInsertionIndex;
                bestPickupTimeWindow = seed.bestPickupTimeWindow;
                bestDeliveryTimeWindow = seed.bestDeliveryTimeWindow;
            }
        }

        void scan() {
            final Shipment shipment = s.shipment;
            final List<TourActivity> activities = s.activities;
            final int activitiesSize = activities.size();
            final Vehicle newVehicle = s.vehicle;
            final Driver newDriver = s.driver;
            final boolean useCachedTiming = s.useCachedTiming;
            final boolean timingRequired = s.timingRequired;
            final Set<Integer> filteredPickupPositions = s.filteredPickupPositions;
            final double additionalICostsAtRouteLevel = s.additionalICostsAtRouteLevel;

            // the timing at the first position of the range: the vehicle's departure, the cached end time of the
            // activity before it, or, when the route's timing does not apply to this vehicle, propagated from the start
            TourActivity prevAct = s.start;
            double prevActEndTime = s.departureTime;
            if (from > 0) {
                if (useCachedTiming) {
                    prevAct = activities.get(from - 1);
                    prevActEndTime = prevAct.getEndTime();
                } else {
                    for (int k = 0; k < from; k++) {
                        TourActivity nextAct = activities.get(k);
                        double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
                        prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                        prevAct = nextAct;
                    }
                }
            }

            // Pickup loop - iterate through positions, evaluating only filtered ones
            for (int i = from; i < to; i++) {
                // a range before this one has hit the position where the sequential loop stops: everything from
                // here on would be discarded by the merge, so stop
                if (shared != null && shared.stopsBefore(i)) return;
                TourActivity nextAct = i < activitiesSize ? activities.get(i) : s.end;

                // Check if we should evaluate this pickup position
                boolean shouldEvaluatePickup = (filteredPickupPositions == null || filteredPickupPositions.contains(i));

                // If using cached timing and filtering, we can jump directly
                if (useCachedTiming && filteredPickupPositions != null && !shouldEvaluatePickup) {
                    // Skip this position entirely - timing is cached on activities
                    continue;
                }

                // If timing is required and we can't use cached timing, we must track it even for skipped positions
                if (!shouldEvaluatePickup) {
                    // Update timing but don't evaluate
                    double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
                    prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                    prevAct = nextAct;
                    continue;
                }

                // If using cached timing, get timing from previous activity
                if (useCachedTiming && i > 0) {
                    prevAct = activities.get(i - 1);
                    prevActEndTime = prevAct.getEndTime();
                }

                boolean pickupInsertionNotFulfilledBreak = true;
                for (TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                    pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                    pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                    pickupActivityContext.setInsertionIndex(i);
                    insertionContext.setRelatedActivityContext(null);
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
                    double additionalPickupICosts = constraintManager.getActivityCosts(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime, pickupActCosts);
                    double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                    if (pickupCosts != null) {
                        double pickupPart = pickupAIC + additionalPickupICosts + additionalICostsAtRouteLevel;
                        if (pickupPart < pickupCosts[i]) pickupCosts[i] = pickupPart;
                        continue;
                    }

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
                    // the insertion found before the ranges started is strictly cheaper than this pickup alone: skip
                    // the delivery loop (an equally expensive candidate is still evaluated, so the earlier position wins)
                    if (bound < pickupAIC + additionalPickupICosts + additionalICostsAtRouteLevel) {
                        continue;
                    }

                    // Get filtered delivery positions for this pickup position
                    Set<Integer> filteredDeliveryPositions = getFilteredDeliveryPositions(shipment, s.route, activities, i);

                    // Delivery loop - must iterate sequentially for timing, but only evaluate filtered positions
                    int j = i;
                    boolean tourEnd_deliveryLoop = false;
                    while (!tourEnd_deliveryLoop) {
                        TourActivity nextAct_deliveryLoop;
                        if (j < activitiesSize) {
                            nextAct_deliveryLoop = activities.get(j);
                        } else {
                            nextAct_deliveryLoop = s.end;
                            tourEnd_deliveryLoop = true;
                        }

                        // Check if we should evaluate this delivery position
                        boolean shouldEvaluateDelivery = (filteredDeliveryPositions == null || filteredDeliveryPositions.contains(j));

                        if (shouldEvaluateDelivery) {

                            boolean deliveryInsertionNotFulfilledBreak = true;
                            for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                                deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                                deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                                deliveryActivityContext.setInsertionIndex(j);
                                insertionContext.setActivityContext(deliveryActivityContext);
                                ConstraintsStatus deliverShipmentConstraintStatus = fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop, failedActivityConstraints, constraintManager);
                                if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                                    double additionalDeliveryICosts = constraintManager.getActivityCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop, deliveryActCosts);
                                    double deliveryAIC = calculate(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                                    double totalActivityInsertionCosts = pickupAIC + deliveryAIC
                                            + additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;

                                    if (totalActivityInsertionCosts < bestCost) {
                                        bestCost = totalActivityInsertionCosts;
                                        pickupInsertionIndex = i;
                                        deliveryInsertionIndex = j;
                                        bestPickupTimeWindow = pickupTimeWindow;
                                        bestDeliveryTimeWindow = deliveryTimeWindow;
                                        // Build complete breakdown for this position
                                        bestBreakdown = new InsertionCostBreakdown();
                                        bestBreakdown.merge(s.routeBreakdown);
                                        bestBreakdown.merge(constraintManager.activityBreakdownOf(pickupActCosts));
                                        bestBreakdown.merge(constraintManager.activityBreakdownOf(deliveryActCosts));
                                        bestBreakdown.add("PickupInsertion", pickupAIC);
                                        bestBreakdown.add("DeliveryInsertion", deliveryAIC);
                                    }
                                    deliveryInsertionNotFulfilledBreak = false;
                                } else if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                                    deliveryInsertionNotFulfilledBreak = false;
                                }
                            }
                            if (deliveryInsertionNotFulfilledBreak) {
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
                    brokeAt = i;
                    if (shared != null) shared.stopAt(i);
                    return;
                }
                // Update timing for pickup loop; with cached timing the next position reads it from the route instead
                if (!useCachedTiming) {
                    double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
                    prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                    prevAct = nextAct;
                }
            }
        }
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
        double[] pickupActCosts = constraintManager.newActivityCostsScratch();
        double[] deliveryActCosts = constraintManager.newActivityCostsScratch();
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
                insertionContext.setRelatedActivityContext(null);
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

                double additionalPickupICosts = constraintManager.getActivityCosts(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime, pickupActCosts);
                InsertionCostBreakdown pickupActBreakdown = constraintManager.activityBreakdownOf(pickupActCosts);
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
                            double additionalDeliveryICosts = constraintManager.getActivityCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop, deliveryActCosts);
                            InsertionCostBreakdown deliveryActBreakdown = constraintManager.activityBreakdownOf(deliveryActCosts);
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
                            insertionData.setSelectedPickupTimeWindow(pickupTimeWindow);
                            insertionData.setSelectedTimeWindow(deliveryTimeWindow);

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
