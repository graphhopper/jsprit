/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.selector;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
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
