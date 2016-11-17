/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.acceptor;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.Collection;


/**
 * Acceptor that accepts solutions to be memorized only better solutions.
 * <p>
 * <p>If there is enough memory, every solution will be accepted. If there is no memory anymore and the solution
 * to be evaluated is better than the worst, the worst will be replaced by the new solution.</p>
 */
public class GreedyAcceptance implements SolutionAcceptor {

    private final int solutionMemory;

    public GreedyAcceptance(int solutionMemory) {
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
        boolean solutionAccepted = false;
        if (solutions.size() < solutionMemory) {
            solutions.add(newSolution);
            solutionAccepted = true;
        } else {
            VehicleRoutingProblemSolution worstSolution = null;
            for (VehicleRoutingProblemSolution s : solutions) {
                if (worstSolution == null) worstSolution = s;
                else if (s.getCost() > worstSolution.getCost()) worstSolution = s;
            }
            if (newSolution.getCost() < worstSolution.getCost()) {
                solutions.remove(worstSolution);
                solutions.add(newSolution);
                solutionAccepted = true;
            }
        }
        return solutionAccepted;
    }

    @Override
    public String toString() {
        return "[name=GreedyAcceptance]";
    }


}
