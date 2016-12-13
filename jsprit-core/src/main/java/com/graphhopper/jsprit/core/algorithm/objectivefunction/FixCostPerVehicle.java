package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

/**
 * A cost calculator component for a per vehicle static cost.
 * <p>
 * This cost calculator returns the fixed cost of the vehicle type.
 * </p>
 * <p>
 * This is a route level component.
 * </p>
 *
 * @author balage
 *
 */
public class FixCostPerVehicle extends RouteLevelSolutionCostComponent {

    /**
     * The unique id of the component.
     */
    public static final String COMPONENT_ID = "VehicleFix";

    public FixCostPerVehicle() {
        super(COMPONENT_ID);
    }

    @Override
    protected double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route) {
        return route.getVehicle().getType().getVehicleCostParams().fix;
    }

}
