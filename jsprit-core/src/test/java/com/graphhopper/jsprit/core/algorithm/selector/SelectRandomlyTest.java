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
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SelectRandomlyTest {

    @Test
    public void whenHaving2Solutions_selectSecond() {
        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);

        when(sol1.getCost()).thenReturn(1.0);
        when(sol2.getCost()).thenReturn(2.0);

        Random random = mock(Random.class);
        when(random.nextInt(2)).thenReturn(1);

        SelectRandomly selectRandomly = new SelectRandomly();
        selectRandomly.setRandom(random);

        assertThat(selectRandomly.selectSolution(Arrays.asList(sol1, sol2)), is(sol2));
    }

    @Test
    public void whenHaving2Solutions_selectFirst() {

        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);

        when(sol1.getCost()).thenReturn(1.0);
        when(sol2.getCost()).thenReturn(2.0);

        Random random = mock(Random.class);
        when(random.nextInt(2)).thenReturn(0);

        SelectRandomly selectRandomly = new SelectRandomly();
        selectRandomly.setRandom(random);

        assertThat(selectRandomly.selectSolution(Arrays.asList(sol1, sol2)), is(sol1));
    }

    @Test
    public void whenHavingNoSolutions_returnNull() {
        Random random = mock(Random.class);
        when(random.nextInt(2)).thenReturn(0);

        SelectRandomly selectRandomly = new SelectRandomly();
        selectRandomly.setRandom(random);

        assertNull(selectRandomly.selectSolution(Collections.<VehicleRoutingProblemSolution>emptyList()));
    }
}
