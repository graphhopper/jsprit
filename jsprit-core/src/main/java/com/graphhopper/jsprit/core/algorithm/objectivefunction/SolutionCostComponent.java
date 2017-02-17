package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

/**
 * Base class for solution cost calculator components.
 *
 * <p>
 * Two components regarded as equal if their id is equal.
 * </p>
 *
 * @author balage
 * @see {@linkplain RouteLevelSolutionCostComponent}
 */
public abstract class SolutionCostComponent {

    // The unique id of the component
    private String id;

    // The maximum costs variable
    private double maxCosts;

    /**
     * Constructor.
     *
     * @param id
     *            The unique id of the component.
     */
    public SolutionCostComponent(String id) {
        super();
        this.id = id;
    }

    /**
     * Initialization of the component.
     * <p>
     * This implementation stores the maximum costs, therefore all overridden
     * implementations should call the super implementation!
     * </p>
     *
     * @param problem
     *            The problem the calculation will run on.
     * @param maxCosts
     *            The maximum cost value.
     */
    public void beforeRun(VehicleRoutingProblem problem, double maxCosts) {
        this.maxCosts = maxCosts;
    }

    /**
     * Initilization called before each calculation on a solution.
     * <p>
     * This implementation does nothing.
     * </p>
     *
     * @param problem
     *            The problem the calculation will run on.
     */
    public void beforeSolution(VehicleRoutingProblem problem) {
    }

    /**
     * Calculates the component cost of the solution.
     *
     * @param problem
     *            The problem the calculation will run on.
     * @param solution
     *            The solution to calculate on.
     * @return The calculated value.
     */
    public abstract ComponentValue calculateCost(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution);


    /**
     * @return The unique id of the component.
     */
    public String getId() {
        return id;
    }

    /**
     * @return The maximum travel cost of the problem.
     */
    public double getMaxCosts() {
        return maxCosts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SolutionCostComponent other = (SolutionCostComponent) obj;
        if (id == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

}
