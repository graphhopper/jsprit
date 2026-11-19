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

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Calculator that calculates the best insertion position for a {@link Service}.
 *
 * @author schroeder
 */
final class ServiceInsertionCalculator extends AbstractInsertionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInsertionCalculator.class);

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private final JobActivityFactory activityFactory;

    private final AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private final ConstraintManager constraintManager;

    private ParallelInsertionScan parallelScan;

    public ServiceInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager, JobActivityFactory activityFactory) {
        super();
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = activityFactory;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=calculatesServiceInsertion]";
    }

    /**
     * Set by the builders when the algorithm runs on a ForkJoinPool with more than one thread: the positions
     * of a long route are then scanned in parallel, see {@link ParallelInsertionScan}.
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
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Service service = (Service) jobToInsert;

        TourActivity deliveryAct2Insert = activityFactory.createActivities(service).getFirst();
        insertionContext.getAssociatedActivities().add(deliveryAct2Insert);

        /*
        check hard constraints at route level
         */
        InsertionData noInsertion = checkRouteConstraints(insertionContext, constraintManager);
        if (noInsertion != null) return noInsertion;

        /*
        check soft constraints at route level
         */
        InsertionCostBreakdown routeBreakdown = constraintManager.getRouteCostsBreakdown(insertionContext);
        double additionalICostsAtRouteLevel = routeBreakdown.getTotal();

        double accessEgressCosts = additionalAccessEgressCalculator.getCosts(insertionContext);
        if (accessEgressCosts != 0) {
            routeBreakdown.add("AccessEgress", accessEgressCosts);
        }
        additionalICostsAtRouteLevel += accessEgressCosts;

        /*
        generate new start and end for new vehicle
         */
        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
        start.setEndTime(newVehicleDepartureTime);
        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        List<TourActivity> activities = currentRoute.getActivities();
        Scan scan = new Scan(currentRoute, service, activities, start, end, newVehicle, newDriver, newVehicleDepartureTime,
            routeBreakdown, additionalICostsAtRouteLevel, bestKnownCosts);
        int positions = activities.size() + 1;
        int tasks = parallelScan == null ? 1 : parallelScan.serviceTasks(positions);
        Range best;
        if (tasks > 1) {
            ParallelInsertionScan.Shared shared = new ParallelInsertionScan.Shared();
            List<Range> ranges = parallelScan.run(ParallelInsertionScan.linearBounds(positions, tasks), (from, to) -> {
                Range range = new Range(scan, activityFactory.createActivities(service).getFirst(), from, to);
                range.shared = shared;
                range.scan();
                return range;
            });
            best = Range.mergeInOrder(ranges);
        } else {
            best = new Range(scan, deliveryAct2Insert, 0, positions);
            best.scan();
        }

        if (best.insertionIndex == InsertionData.NO_INDEX) {
            InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
            addFailedConstraintNames(emptyInsertionData, best.failedActivityConstraints);
            return emptyInsertionData;
        }
        InsertionData insertionData = new InsertionData(best.bestCost, InsertionData.NO_INDEX, best.insertionIndex, newVehicle, newDriver);
        insertionData.setCostBreakdown(best.bestBreakdown);
        insertionData.setSelectedTimeWindow(best.bestTimeWindow);
        deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(best.bestTimeWindow.getStart());
        deliveryAct2Insert.setTheoreticalLatestOperationStartTime(best.bestTimeWindow.getEnd());
        insertionData.getEvents().add(new InsertActivity(currentRoute, newVehicle, deliveryAct2Insert, best.insertionIndex));
        insertionData.getEvents().add(new SwitchVehicle(currentRoute,newVehicle,newVehicleDepartureTime));
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        return insertionData;
    }

    /** What every range of one service evaluation shares, all of it read-only while the ranges run. */
    private record Scan(VehicleRoute route, Service service, List<TourActivity> activities, Start start, End end,
                        Vehicle vehicle, Driver driver, double departureTime,
                        InsertionCostBreakdown routeBreakdown, double additionalICostsAtRouteLevel, double bestKnownCosts) {

        /**
         * Whether the end times the state manager cached on the route's activities are the times this
         * evaluation propagates: same vehicle, same departure. Then a range can start at any position.
         */
        boolean cachedTimingApplies() {
            return !route.isEmpty() && route.getVehicle() == vehicle && route.getDepartureTime() == departureTime;
        }
    }

    /**
     * The scan of the positions {@code from} (inclusive) to {@code to} (exclusive) with its own scratch objects
     * and its result. The whole route is one range when the scan runs sequentially.
     */
    private final class Range {
        private final Scan s;
        private final int from, to;
        private final TourActivity deliveryAct2Insert;
        private final JobInsertionContext insertionContext;
        private final ActivityContext activityContext = new ActivityContext();
        private final double[] actCosts;

        final Collection<HardConstraint> failedActivityConstraints = new ArrayList<>();
        double bestCost;
        InsertionCostBreakdown bestBreakdown = null;
        TimeWindow bestTimeWindow = null;
        int insertionIndex = InsertionData.NO_INDEX;
        /** The position at which every time window answered NOT_FULFILLED_BREAK and the loop stopped, or -1. */
        int brokeAt = -1;
        /** What the ranges of one parallel evaluation tell each other (see {@link ParallelInsertionScan.Shared}); null when the range is the whole route. */
        ParallelInsertionScan.Shared shared;

        Range(Scan s, TourActivity deliveryAct2Insert, int from, int to) {
            this.s = s;
            this.from = from;
            this.to = to;
            this.deliveryAct2Insert = deliveryAct2Insert;
            this.bestCost = s.bestKnownCosts;
            this.insertionContext = new JobInsertionContext(s.route, s.service, s.vehicle, s.driver, s.departureTime);
            insertionContext.getAssociatedActivities().add(deliveryAct2Insert);
            this.actCosts = constraintManager.newActivityCostsScratch();
        }

        /** Merges the ranges in position order into the first one, exactly as the sequential loop would have seen them. */
        static Range mergeInOrder(List<Range> ranges) {
            Range merged = ranges.get(0);
            for (int k = 1; k < ranges.size() && merged.brokeAt < 0; k++) {
                Range other = ranges.get(k);
                merged.failedActivityConstraints.addAll(other.failedActivityConstraints);
                if (other.insertionIndex != InsertionData.NO_INDEX && other.bestCost < merged.bestCost) {
                    merged.bestCost = other.bestCost;
                    merged.bestBreakdown = other.bestBreakdown;
                    merged.bestTimeWindow = other.bestTimeWindow;
                    merged.insertionIndex = other.insertionIndex;
                }
                merged.brokeAt = other.brokeAt;
            }
            return merged;
        }

        void scan() {
            final Service service = s.service;
            final List<TourActivity> activities = s.activities;
            final int activitiesSize = activities.size();
            final Vehicle newVehicle = s.vehicle;
            final Driver newDriver = s.driver;
            final double additionalICostsAtRouteLevel = s.additionalICostsAtRouteLevel;

            // the timing at the first position of the range: the vehicle's departure, the cached end time of the
            // activity before it, or, when the route's timing does not apply to this vehicle, propagated from the start
            TourActivity prevAct = s.start;
            double prevActStartTime = s.departureTime;
            if (from > 0) {
                if (s.cachedTimingApplies()) {
                    prevAct = activities.get(from - 1);
                    prevActStartTime = prevAct.getEndTime();
                } else {
                    for (int k = 0; k < from; k++) {
                        TourActivity nextAct = activities.get(k);
                        double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
                        prevActStartTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                        prevAct = nextAct;
                    }
                }
            }

            for (int actIndex = from; actIndex < to; actIndex++) {
                // a range before this one has hit the position where the sequential loop stops: stop too, the merge discards the rest
                if (shared != null && shared.stopsBefore(actIndex)) return;
                TourActivity nextAct = actIndex < activitiesSize ? activities.get(actIndex) : s.end;
                boolean not_fulfilled_break = true;
                for (TimeWindow timeWindow : service.getTimeWindows()) {
                    deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                    deliveryAct2Insert.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                    activityContext.setInsertionIndex(actIndex);
                    insertionContext.setActivityContext(activityContext);
                    ConstraintsStatus status = fulfilled(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, failedActivityConstraints, constraintManager);
                    if (status.equals(ConstraintsStatus.FULFILLED)) {
                        double additionalICostsAtActLevel = constraintManager.getActivityCosts(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, actCosts);
                        double additionalTransportationCosts = activityInsertionCostsCalculator.getCosts(insertionContext, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
                        double totalCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;
                        if (totalCost < bestCost) {
                            bestCost = totalCost;
                            insertionIndex = actIndex;
                            bestTimeWindow = timeWindow;
                            // Build complete breakdown for this position
                            bestBreakdown = new InsertionCostBreakdown();
                            bestBreakdown.merge(s.routeBreakdown);
                            bestBreakdown.merge(constraintManager.activityBreakdownOf(actCosts));
                            bestBreakdown.add("ActivityInsertion", additionalTransportationCosts);
                        }
                        not_fulfilled_break = false;
                    } else if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                        not_fulfilled_break = false;
                    }
                }
                if (not_fulfilled_break) {
                    brokeAt = actIndex;
                    if (shared != null) shared.stopAt(actIndex);
                    return;
                }
                double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
                prevActStartTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
                prevAct = nextAct;
            }
        }
    }

    /**
     * Returns all feasible insertion positions for a service in the route.
     * Used for position-based regret calculation.
     */
    @Override
    public List<InsertionData> getAllInsertionPositions(final VehicleRoute currentRoute, final Job jobToInsert,
            final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver) {

        List<InsertionData> allPositions = new ArrayList<>();

        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Service service = (Service) jobToInsert;

        TourActivity deliveryAct2Insert = activityFactory.createActivities(service).getFirst();
        insertionContext.getAssociatedActivities().add(deliveryAct2Insert);

        // Check hard constraints at route level
        InsertionData noInsertion = checkRouteConstraints(insertionContext, constraintManager);
        if (noInsertion != null) {
            return allPositions; // Empty list - route constraints not satisfied
        }

        Collection<HardConstraint> failedActivityConstraints = new ArrayList<>();
        double[] actCosts = constraintManager.newActivityCostsScratch();

        // Calculate route-level costs (same for all positions)
        InsertionCostBreakdown routeBreakdown = constraintManager.getRouteCostsBreakdown(insertionContext);
        double additionalICostsAtRouteLevel = routeBreakdown.getTotal();

        double accessEgressCosts = additionalAccessEgressCalculator.getCosts(insertionContext);
        if (accessEgressCosts != 0) {
            routeBreakdown.add("AccessEgress", accessEgressCosts);
        }
        additionalICostsAtRouteLevel += accessEgressCosts;

        // Generate start and end for new vehicle
        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
        start.setEndTime(newVehicleDepartureTime);
        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        TourActivity prevAct = start;
        double prevActStartTime = newVehicleDepartureTime;
        int actIndex = 0;
        Iterator<TourActivity> activityIterator = currentRoute.getActivities().iterator();
        boolean tourEnd = false;
        // Reuse single ActivityContext instance - safe because it's method-local
        ActivityContext activityContext = new ActivityContext();

        while (!tourEnd) {
            TourActivity nextAct;
            if (activityIterator.hasNext()) {
                nextAct = activityIterator.next();
            } else {
                nextAct = end;
                tourEnd = true;
            }

            boolean notFulfilledBreak = true;
            for (TimeWindow timeWindow : service.getTimeWindows()) {
                deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                deliveryAct2Insert.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                activityContext.setInsertionIndex(actIndex);
                insertionContext.setActivityContext(activityContext);

                ConstraintsStatus status = fulfilled(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, failedActivityConstraints, constraintManager);

                if (status.equals(ConstraintsStatus.FULFILLED)) {
                    // Calculate costs for this position
                    double additionalICostsAtActLevel = constraintManager.getActivityCosts(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, actCosts);
                    InsertionCostBreakdown actBreakdown = constraintManager.activityBreakdownOf(actCosts);
                    double additionalTransportationCosts = activityInsertionCostsCalculator.getCosts(
                            insertionContext, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
                    double totalCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;

                    // Create InsertionData for this position
                    InsertionData insertionData = new InsertionData(totalCost, InsertionData.NO_INDEX, actIndex, newVehicle, newDriver);

                    // Build complete breakdown
                    InsertionCostBreakdown breakdown = new InsertionCostBreakdown();
                    breakdown.merge(routeBreakdown);
                    breakdown.merge(actBreakdown);
                    breakdown.add("ActivityInsertion", additionalTransportationCosts);
                    insertionData.setCostBreakdown(breakdown);
                    insertionData.setSelectedTimeWindow(timeWindow);

                    // Create fresh activity for this position's events
                    TourActivity actForPosition = activityFactory.createActivities(service).getFirst();
                    actForPosition.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                    actForPosition.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                    insertionData.getEvents().add(new InsertActivity(currentRoute, newVehicle, actForPosition, actIndex));
                    insertionData.getEvents().add(new SwitchVehicle(currentRoute, newVehicle, newVehicleDepartureTime));
                    insertionData.setVehicleDepartureTime(newVehicleDepartureTime);

                    allPositions.add(insertionData);
                    notFulfilledBreak = false;
                } else if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    notFulfilledBreak = false;
                }
            }
            if (notFulfilledBreak) break;

            double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(
                    prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
            prevActStartTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime())
                    + activityCosts.getActivityDuration(prevAct, nextAct, nextActArrTime, newDriver, newVehicle);
            prevAct = nextAct;
            actIndex++;
        }

        return allPositions;
    }

}
