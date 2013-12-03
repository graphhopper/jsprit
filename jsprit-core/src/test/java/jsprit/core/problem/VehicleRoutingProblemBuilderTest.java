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
package jsprit.core.problem;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.constraint.Constraint;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;

import org.junit.Test;


public class VehicleRoutingProblemBuilderTest {
	
	@Test
	public void buildsProblemWithInfiniteVehiclesCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.INFINITE);
//		Depot depot = new Depot("depot1", Coordinate.newInstance(0, 0));
//		builder.assignVehicleType(depot, VehicleType.Builder.newInstance("t1", 20).build());
//		builder.assignVehicleType(depot, VehicleType.Builder.newInstance("t2", 200).build());
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.INFINITE,vrp.getFleetSize());
		
	}

	@Test
	public void buildsProblemWithFiniteVehiclesCorrectly_checkVehiclesAndTypesSizes(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.FINITE);
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("t1", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("t2", 200).build();
		
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setLocationId("yo").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setLocationId("yo").setType(type1).build();
		Vehicle v3 = VehicleImpl.Builder.newInstance("v3").setLocationId("yo").setType(type2).build();
		Vehicle v4 = VehicleImpl.Builder.newInstance("v4").setLocationId("yo").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		builder.addVehicle(v3);
		builder.addVehicle(v4);
		
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.FINITE,vrp.getFleetSize());
		assertEquals(4,vrp.getVehicles().size());
		assertEquals(2,vrp.getTypes().size());

	}
	
	@Test
	public void whenShipmentsAreAdded_theyShouldBePartOfTheProblem(){
		Shipment s = Shipment.Builder.newInstance("s", 10).setPickupLocation("foofoo").setDeliveryLocation("foo").build();
		Shipment s2 = Shipment.Builder.newInstance("s2", 100).setPickupLocation("foofoo").setDeliveryLocation("foo").build();
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		vrpBuilder.addJob(s);
		vrpBuilder.addJob(s2);
		VehicleRoutingProblem vrp = vrpBuilder.build();
		assertEquals(2,vrp.getJobs().size());
		
		Job j = vrp.getJobs().get("s");
		assertEquals(s,j);
		assertEquals(s2,vrp.getJobs().get("s2"));
	}
	
	@Test
	public void whenConstraintsAdded_theyShouldAppearInConstraintCollection(){
		Constraint c1 = mock(Constraint.class);
		Constraint c2 = mock(Constraint.class);
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.addConstraint(c1).addConstraint(c2);
		VehicleRoutingProblem problem = builder.build();
		assertEquals(2,problem.getConstraints().size());
	}
	

}
