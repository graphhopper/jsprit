package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public class FixCostPerVehicle extends RouteLevelSolutionCostComponent {

    public static final String COMPONENT_ID = "VehicleFix";

    public FixCostPerVehicle() {
        super(COMPONENT_ID);
    }

    @Override
    protected double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route) {
        return route.getVehicle().getType().getVehicleCostParams().fix;
    }

}
