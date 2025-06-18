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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Greedy Acceptance Test")
class GreedyAcceptanceTest {

    @Test
    @DisplayName("When Having New Sol And Limited Memory _ remove Worst And Add New")
    void whenHavingNewSolAndLimitedMemory_removeWorstAndAddNew() {
        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);
        when(sol1.getCost()).thenReturn(1.0);
        when(sol2.getCost()).thenReturn(2.0);
        List<VehicleRoutingProblemSolution> solList = new ArrayList<VehicleRoutingProblemSolution>();
        solList.add(sol1);
        solList.add(sol2);
        VehicleRoutingProblemSolution sol3 = mock(VehicleRoutingProblemSolution.class);
        new GreedyAcceptance(2).acceptSolution(solList, sol3);
        Assertions.assertEquals(2, solList.size());
        Assertions.assertSame(sol3, solList.get(1));
    }
}
