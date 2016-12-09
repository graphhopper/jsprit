package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

public abstract class RouteLevelSolutionCostComponent extends SolutionCostComponent {

    public RouteLevelSolutionCostComponent(String id) {
        super(id);
    }

    @Override
    public RouteLevelComponentValue calculateCost(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        RouteLevelComponentValue cv = new RouteLevelComponentValue(getId());
        for (VehicleRoute route : solution.getRoutes()) {
            double val = calculateRouteLevelCost(problem, route);
            cv.setRouteValue(route.getId(), val);
        }
        return cv;
    }

    protected abstract double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route);
}
