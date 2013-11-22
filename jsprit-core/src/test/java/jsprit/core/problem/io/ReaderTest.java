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
package jsprit.core.problem.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.activity.TourActivity;

import org.junit.Test;


public class ReaderTest {
	

	@Test
	public void testRead_ifReaderIsCalled_itReadsSuccessfully(){	 
		new VrpXMLReader(VehicleRoutingProblem.Builder.newInstance(), new ArrayList<VehicleRoutingProblemSolution>()).read("src/test/resources/lui-shen-solution.xml");
	}
	
	@Test
	public void testRead_ifReaderIsCalled_itReadsSuccessfullyV2(){	 
		Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		VehicleRoutingProblem vrp = vrpBuilder.build();
		ArrayList<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
		new VrpXMLReader(vrpBuilder, solutions).read("src/test/resources/finiteVrpWithShipmentsAndSolution.xml");
		assertEquals(3,vrp.getJobs().size());
		assertEquals(1,solutions.size());
		
		assertEquals(1,solutions.get(0).getRoutes().size());
		List<TourActivity> activities = solutions.get(0).getRoutes().iterator().next().getTourActivities().getActivities();
		assertEquals(4,activities.size());
		assertTrue(activities.get(0) instanceof PickupService);
		assertTrue(activities.get(1) instanceof PickupService);
		assertTrue(activities.get(2) instanceof PickupShipment);
		assertTrue(activities.get(3) instanceof DeliverShipment);
	}

}
