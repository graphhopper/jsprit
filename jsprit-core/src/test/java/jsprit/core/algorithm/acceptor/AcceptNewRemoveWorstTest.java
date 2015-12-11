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
package jsprit.core.algorithm.acceptor;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class AcceptNewRemoveWorstTest {

    @Test
    public void whenHavingNewSolAndLimitedMemory_removeWorstAndAddNew() {

        VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
        VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);
        when(sol1.getCost()).thenReturn(1.0);
        when(sol2.getCost()).thenReturn(2.0);

        List<VehicleRoutingProblemSolution> solList = new ArrayList<VehicleRoutingProblemSolution>();
        solList.add(sol1);
        solList.add(sol2);

        VehicleRoutingProblemSolution sol3 = mock(VehicleRoutingProblemSolution.class);

        new GreedyAcceptance(2).acceptSolution(solList, sol3);

        assertEquals(2, solList.size());
        assertThat(sol3, is(solList.get(1)));
    }

}
