package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Enhanced AbstractInsertionCalculator with more common functionality moved up
 * to simplify implementation of concrete calculators.
 */
public abstract class AbstractInsertionCalculator implements JobInsertionCostsCalculator {

    /**
     * Check if route constraints are fulfilled
     */
    protected InsertionData checkRouteConstraints(JobInsertionContext insertionContext, ConstraintManager constraintManager) {
        for (HardRouteConstraint hardRouteConstraint : constraintManager.getHardRouteConstraints()) {
            if (!hardRouteConstraint.fulfilled(insertionContext)) {
                InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
                emptyInsertionData.addFailedConstrainName(hardRouteConstraint.getClass().getSimpleName());
                return emptyInsertionData;
            }
        }
        return null;
    }

    /**
     * Check if activity constraints are fulfilled
     */
    protected ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, Collection<HardConstraint> failedActivityConstraints, ConstraintManager constraintManager) {
        return fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, failedActivityConstraints, constraintManager, new ArrayList<>());
    }

    /**
     * @param failedInTier scratch list for the constraints that failed within the tier being evaluated; cleared here,
     *                     handed in by the caller so that it is allocated once per insertion evaluation and not per position
     */
    protected ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, Collection<HardConstraint> failedActivityConstraints, ConstraintManager constraintManager, List<HardConstraint> failedInTier) {
        if (!constraintManager.hasHardActivityConstraints()) return ConstraintsStatus.FULFILLED;
        ConstraintsStatus notFulfilled = null;
        List<HardConstraint> failed = failedInTier;
        if (!failed.isEmpty()) failed.clear();
        // indexed loops over the live list views: no iterator allocation per position
        List<HardActivityConstraint> critical = constraintManager.getCriticalHardActivityConstraints();
        for (int i = 0, n = critical.size(); i < n; i++) {
            HardActivityConstraint c = critical.get(i);
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                failedActivityConstraints.add(c);
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    failed.add(c);
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) {
            failedActivityConstraints.addAll(failed);
            return notFulfilled;
        }

        List<HardActivityConstraint> highPrio = constraintManager.getHighPrioHardActivityConstraints();
        for (int i = 0, n = highPrio.size(); i < n; i++) {
            HardActivityConstraint c = highPrio.get(i);
            ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                failedActivityConstraints.add(c);
                return status;
            } else {
                if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    failed.add(c);
                    notFulfilled = status;
                }
            }
        }
        if (notFulfilled != null) {
            failedActivityConstraints.addAll(failed);
            return notFulfilled;
        }

        List<HardActivityConstraint> lowPrio = constraintManager.getLowPrioHardActivityConstraints();
        for (int i = 0, n = lowPrio.size(); i < n; i++) {
            HardActivityConstraint constraint = lowPrio.get(i);
            ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            if (status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                failedActivityConstraints.add(constraint);
                return status;
            }
        }
        return ConstraintsStatus.FULFILLED;
    }

    /**
     * Creates a Start activity for a vehicle at a given departure time
     */
    protected Start createStartActivity(Vehicle vehicle, double departureTime) {
        Start start = new Start(vehicle.getStartLocation(), vehicle.getEarliestDeparture(), vehicle.getLatestArrival());
        start.setEndTime(departureTime);
        return start;
    }

    /**
     * Creates an End activity for a vehicle
     */
    protected End createEndActivity(Vehicle vehicle) {
        return new End(vehicle.getEndLocation(), 0.0, vehicle.getLatestArrival());
    }

    /**
     * Creates a NoInsertionFound result with failed constraint information
     */
    protected InsertionData createNoInsertionFoundResult(Collection<HardConstraint> failedConstraints) {
        InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
        addFailedConstraintNames(emptyInsertionData, failedConstraints);
        return emptyInsertionData;
    }

    /**
     * The failed list holds one entry per failed position, so on a route where a constraint fails everywhere it is
     * hundreds of references to the same few constraints in runs; the name is resolved once per run instead of once per
     * entry (getSimpleName goes through the class's reflection data). The names list keeps one entry per failed position,
     * so the unassigned-reason frequencies are unchanged.
     */
    static void addFailedConstraintNames(InsertionData insertionData, Collection<HardConstraint> failedConstraints) {
        HardConstraint previous = null;
        String name = null;
        for (HardConstraint failed : failedConstraints) {
            if (failed != previous) {
                name = failed.getClass().getSimpleName();
                previous = failed;
            }
            insertionData.addFailedConstrainName(name);
        }
    }

    /**
     * Adds events to insertion data for a job that requires two activities (like Shipment)
     */
    protected void addActivitiesAndVehicleSwitch(InsertionData insertionData, VehicleRoute route,
                                                 Vehicle vehicle, TourActivity firstActivity, int firstActivityIndex,
                                                 TourActivity secondActivity, int secondActivityIndex,
                                                 double departureTime) {
        // Order matters here - we need to insert second activity before first to maintain indices
        insertionData.getEvents().add(new InsertActivity(route, vehicle, secondActivity, secondActivityIndex));
        insertionData.getEvents().add(new InsertActivity(route, vehicle, firstActivity, firstActivityIndex));
        insertionData.getEvents().add(new SwitchVehicle(route, vehicle, departureTime));
    }

    /**
     * Adds events to insertion data for a job that requires a single activity (like Service)
     */
    protected void addActivityAndVehicleSwitch(InsertionData insertionData, VehicleRoute route,
                                               Vehicle vehicle, TourActivity activity, int activityIndex,
                                               double departureTime) {
        insertionData.getEvents().add(new InsertActivity(route, vehicle, activity, activityIndex));
        insertionData.getEvents().add(new SwitchVehicle(route, vehicle, departureTime));
    }
}