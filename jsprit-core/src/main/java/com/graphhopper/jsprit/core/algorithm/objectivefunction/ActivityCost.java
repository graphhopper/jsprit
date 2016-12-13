package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * A cost calculator component for a all activity (operation) costs.
 * <p>
 * This cost calculator returns the sum of the activity cost of all the
 * activities on the route.
 * </p>
 * <p>
 * This is a route level component.
 * </p>
 *
 * @author balage
 *
 */
public class ActivityCost extends RouteLevelSolutionCostComponent {

    public static final String COMPONENT_ID = "Activity";

    public ActivityCost() {
        super(COMPONENT_ID);
    }

    @Override
    protected double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route) {
        double costs = 0;
        for (TourActivity act : route.getActivities()) {
            costs += problem.getActivityCosts().getActivityCost(act, act.getArrTime(), route.getDriver(), route.getVehicle());
        }
        return costs;
    }

}
