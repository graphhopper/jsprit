package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.HardConstraint;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

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
        List<HardRouteConstraint> routeConstraints = constraintManager.getHardRouteConstraints();
        for (int i = 0, n = routeConstraints.size(); i < n; i++) {
            HardRouteConstraint hardRouteConstraint = routeConstraints.get(i);
            if (!hardRouteConstraint.fulfilled(insertionContext)) {
                InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
                emptyInsertionData.addFailedConstrainName(hardRouteConstraint.getClass().getSimpleName());
                return emptyInsertionData;
            }
        }
        return null;
    }

    /**
     * The tiered hard activity constraint check of {@link ConstraintManager#fulfilled(JobInsertionContext, TourActivity, TourActivity, TourActivity, double, Collection, List)};
     * {@code failedInTier} is the caller's scratch list, kept across positions.
     */
    protected ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime, Collection<HardConstraint> failedActivityConstraints, ConstraintManager constraintManager, List<HardConstraint> failedInTier) {
        return constraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime, failedActivityConstraints, failedInTier);
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