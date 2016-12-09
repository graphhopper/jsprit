package com.graphhopper.jsprit.core.algorithm.objectivefunction;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

public class UnassignedJobs extends SolutionCostComponent {

    public static final String COMPONENT_ID = "Unassigned";

    public UnassignedJobs() {
        super(COMPONENT_ID);
    }


    @Override
    public ComponentValue calculateCost(VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        double costs = 0d;
        for (Job j : solution.getUnassignedJobs()) {
            costs += getMaxCosts() * 2 * (4 - j.getPriority());
        }
        return new ComponentValue(COMPONENT_ID, costs);
    }


}
