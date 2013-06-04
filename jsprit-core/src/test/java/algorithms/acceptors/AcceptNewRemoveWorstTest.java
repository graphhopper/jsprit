/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms.acceptors;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;



public class AcceptNewRemoveWorstTest {
	
	@Test
	public void whenHavingNewSolAndLimitedMemory_removeWorstAndAddNew(){
		
		VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
		VehicleRoutingProblemSolution sol1 = mock(VehicleRoutingProblemSolution.class);
		VehicleRoutingProblemSolution sol2 = mock(VehicleRoutingProblemSolution.class);
		when(sol1.getCost()).thenReturn(1.0);
		when(sol2.getCost()).thenReturn(2.0);
		
		List<VehicleRoutingProblemSolution> solList = new ArrayList<VehicleRoutingProblemSolution>();
		solList.add(sol1);
		solList.add(sol2);
		
		VehicleRoutingProblemSolution sol3 = mock(VehicleRoutingProblemSolution.class);
		
		new AcceptNewIfBetterThanWorst(2).acceptSolution(solList, sol3);
		
		assertEquals(2,solList.size());
		assertThat(sol3,is(solList.get(1)));
	}

}
