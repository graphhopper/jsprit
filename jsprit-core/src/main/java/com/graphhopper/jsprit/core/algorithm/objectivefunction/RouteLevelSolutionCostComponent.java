package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

/**
 * An implementation of solution cost component which calculates and keeps track
 * of cost per route level.
 *
 * @author balage
 *
 */
public abstract class RouteLevelSolutionCostComponent extends SolutionCostComponent {

    /**
     * Constructor.
     *
     * @param id
     *            The unique id of the component.
     */
    public RouteLevelSolutionCostComponent(String id) {
        super(id);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation calls the
     * {@linkplain #calculateRouteLevelCost(VehicleRoutingProblem, VehicleRoute)}
     * for each route in the solution and stores the calculated cost values,
     * then returns the sum of them.
     * </p>
     */
    @Override
    public RouteLevelComponentValue calculateCost(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        RouteLevelComponentValue cv = new RouteLevelComponentValue(getId());
        for (VehicleRoute route : solution.getRoutes()) {
            double val = calculateRouteLevelCost(problem, route);
            cv.setRouteValue(route.getId(), val);
        }
        return cv;
    }

    /**
     * Calculates the cost of a route.
     *
     * @param problem
     *            The problem the calculation will run on.
     * @param route
     *            The route to calculate the cost of.
     * @return The cost value of the route.
     */
    protected abstract double calculateRouteLevelCost(VehicleRoutingProblem problem, VehicleRoute route);
}
