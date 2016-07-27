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
