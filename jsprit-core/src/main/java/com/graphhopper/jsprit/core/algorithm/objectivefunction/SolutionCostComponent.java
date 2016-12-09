package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public abstract class SolutionCostComponent {

    private String id;

    private double maxCosts;

    public SolutionCostComponent(String id) {
        super();
        this.id = id;
    }

    public void beforeRun(VehicleRoutingProblem problem, double maxCosts) {
        this.maxCosts = maxCosts;
    }

    public void beforeSolution(VehicleRoutingProblem problem) {
    }

    public abstract ComponentValue calculateCost(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution);


    public String getId() {
        return id;
    }

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
