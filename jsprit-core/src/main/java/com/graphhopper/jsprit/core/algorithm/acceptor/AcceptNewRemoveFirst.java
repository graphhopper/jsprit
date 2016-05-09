package com.graphhopper.jsprit.core.algorithm.acceptor;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.Collection;

/**
 * Created by schroeder on 09/02/16.
 */
public class AcceptNewRemoveFirst implements SolutionAcceptor {

    private final int solutionMemory;

    public AcceptNewRemoveFirst(int solutionMemory) {
        this.solutionMemory = solutionMemory;
    }

    /**
     * Accepts every solution if solution memory allows. If memory occupied, than accepts new solution only if better than the worst in memory.
     * Consequently, the worst solution is removed from solutions, and the new solution added.
     * <p>
     * <p>Note that this modifies Collection<VehicleRoutingProblemSolution> solutions.
     */
    @Override
    public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
        if (solutions.size() >= solutionMemory) {
            solutions.remove(solutions.iterator().next());
        }
        solutions.add(newSolution);
        return true;
    }

    @Override
    public String toString() {
        return "[name=acceptNewRemoveFirst]";
    }
}
