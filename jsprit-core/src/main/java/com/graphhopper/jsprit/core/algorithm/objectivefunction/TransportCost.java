package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * A cost calculator component for a all transport costs.
 * <p>
 * This cost calculator returns the sum of the transport cost of all the
 * activities on the route.
 * </p>
 * <p>
 * This is a route level component.
 * </p>
 *
 * @author balage
 *
 */
public class TransportCost extends RouteLevelSolutionCostComponent {

    public static final String COMPONENT_ID = "Transport";

    public TransportCost() {
        super(COMPONENT_ID);
    }

    @Override
    protected double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route) {
        double costs = 0;
        TourActivity prevAct = route.getStart();
        for (TourActivity act : route.getActivities()) {
            costs += problem.getTransportCosts().getTransportCost(prevAct.getLocation(), act.getLocation(), prevAct.getEndTime(),
                            route.getDriver(), route.getVehicle());
            prevAct = act;
        }
        costs += problem.getTransportCosts().getTransportCost(prevAct.getLocation(), route.getEnd().getLocation(),
                        prevAct.getEndTime(), route.getDriver(), route.getVehicle());
        return costs;
    }

}
