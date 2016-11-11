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

import java.util.Collection;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;


/**
 * Acceptor that decides whether the newSolution is accepted or not.
 *
 * @author stefan
 */
public interface SolutionAcceptor {

    /**
     * Accepts solution or not, and returns true if a new solution has been accepted.
     * <p>
     * <p>If the solution is accepted, it is added to solutions, i.e. the solutions-collections is modified.
     *
     * @param solutions   collection of existing solutions
     * @param newSolution new solution to be evaluated
     * @return true if solution accepted
     */
    public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution);

}
