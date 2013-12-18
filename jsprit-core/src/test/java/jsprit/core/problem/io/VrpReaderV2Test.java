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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.vehicle.Vehicle;

import org.junit.Before;
import org.junit.Test;


public class VrpReaderV2Test {
	
	private String inFileName;
	
	@Before
	public void doBefore(){
		inFileName = "src/test/resources/finiteVrpForReaderV2Test.xml";
	}
	
	@Test
	public void whenReadingVrp_problemTypeIsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.FINITE,vrp.getFleetSize());
	}
	
	@Test 
	public void whenReadingVrp_vehiclesAreReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(2,vrp.getVehicles().size());
		assertTrue(idsInCollection(Arrays.asList("v1","v2"),vrp.getVehicles()));
	}
	
	@Test
	public void whenReadingVrp_vehiclesAreReadCorrectly2(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v1 = getVehicle("v1",vrp.getVehicles());
		assertEquals(20,v1.getCapacity());
		assertEquals(100.0,v1.getCoord().getX(),0.01);
		assertEquals(0.0,v1.getEarliestDeparture(),0.01);
		assertEquals("depotLoc2",v1.getLocationId());
		assertNotNull(v1.getType());
		assertEquals("vehType", v1.getType().getTypeId());
		assertEquals(1000.0,v1.getLatestArrival(),0.01);
	}
	
	private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles) if(string.equals(v.getId())) return v;
		return null;
	}

	private boolean idsInCollection(List<String> asList, Collection<Vehicle> vehicles) {
		List<String> ids = new ArrayList<String>(asList);
		for(Vehicle v : vehicles){
			if(ids.contains(v.getId())) ids.remove(v.getId());
		}
		return ids.isEmpty();
	}

	@Test 
	public void whenReadingVrp_vehicleTypesAreReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(2,vrp.getTypes().size());
	}
	
	@Test 
	public void whenReadingVrpWithInfiniteSize_itReadsCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(FleetSize.FINITE,vrp.getFleetSize());
	}
	
	@Test
	public void whenReadingServices_itReadsThemCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(3, vrp.getJobs().size());
		int servCounter = 0;
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Service) servCounter++;
		}
		assertEquals(2,servCounter);
	}
	
	@Test
	public void whenReadingShipments_itReadsThemCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(3, vrp.getJobs().size());
		int shipCounter = 0;
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Shipment) shipCounter++;
		}
		assertEquals(1,shipCounter);
	}
	
	@Test
	public void whenReadingServices_servicesAreBuiltCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Service s1 = (Service) vrp.getJobs().get("1");
		assertEquals("service",s1.getType());
		assertEquals(1,s1.getCapacityDemand());
		assertEquals(0.0,s1.getServiceDuration(),0.01);
		assertEquals(3, vrp.getJobs().size());
	}
}
