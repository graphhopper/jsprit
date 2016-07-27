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
package com.graphhopper.jsprit.core.algorithm.selector;

import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SelectBestTest {

    @Test
    public void whenHaving2Solutions_selectBest() {
        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);
        when(sol1.getCost()).thenReturn(1.0);
        when(sol2.getCost()).thenReturn(2.0);
        assertThat(new SelectBest().selectSolution(Arrays.asList(sol1, sol2)), is(sol1));
    }

    @Test
    public void whenHavingOnly1Solutions_selectThisOne() {
        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        when(sol1.getCost()).thenReturn(1.0);
        assertThat(new SelectBest().selectSolution(Arrays.asList(sol1)), is(sol1));
    }

    @Test
    public void whenHavingNoSolutions_returnNull() {
        assertNull(new SelectBest().selectSolution(Collections.<VehicleRoutingProblemSolution>emptyList()));
    }

}
