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
import static org.junit.Assert.assertFalse;
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
		assertEquals(4,vrp.getVehicles().size());
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
	public void whenReadingJobs_nuOfJobsIsReadThemCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		assertEquals(4, vrp.getJobs().size());
	}
	
	@Test
	public void whenReadingServices_itReadsThemCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
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
		int shipCounter = 0;
		for(Job j : vrp.getJobs().values()){
			if(j instanceof Shipment) shipCounter++;
		}
		assertEquals(2,shipCounter);
	}
	
	@Test
	public void whenReadingServices_capOfService1IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Service s1 = (Service) vrp.getJobs().get("1");
		assertEquals(1,s1.getCapacityDemand());
	}
	
	@Test
	public void whenReadingServices_durationOfService1IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Service s1 = (Service) vrp.getJobs().get("1");
		assertEquals(10.0,s1.getServiceDuration(),0.01);
	}
	
	@Test
	public void whenReadingServices_twOfService1IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Service s1 = (Service) vrp.getJobs().get("1");
		assertEquals(0.0,s1.getTimeWindow().getStart(),0.01);
		assertEquals(4000.0,s1.getTimeWindow().getEnd(),0.01);
	}
	
	@Test
	public void whenReadingServices_typeOfService1IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Service s1 = (Service) vrp.getJobs().get("1");
		assertEquals("service",s1.getType());
	}
	
	@Test
	public void whenReadingFile_v2MustNotReturnToDepot(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v2",vrp.getVehicles());
		assertFalse(v.isReturnToDepot());
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectStartLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals("startLoc",v3.getStartLocationId());
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectEndLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals("endLoc",v3.getEndLocationId());
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectEndLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals(1000.0,v3.getEndLocationCoordinate().getX(),0.01);
		assertEquals(2000.0,v3.getEndLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectStartLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals(10.0,v3.getStartLocationCoordinate().getX(),0.01);
		assertEquals(100.0,v3.getStartLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals(10.0,v3.getCoord().getX(),0.01);
		assertEquals(100.0,v3.getCoord().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v3HasTheCorrectLocationId(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v3 = getVehicle("v3",vrp.getVehicles());
		assertEquals("startLoc",v3.getLocationId());
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectStartLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals("startLoc",v.getStartLocationId());
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectEndLocation(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals("endLoc",v.getEndLocationId());
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectEndLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals(1000.0,v.getEndLocationCoordinate().getX(),0.01);
		assertEquals(2000.0,v.getEndLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectStartLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals(10.0,v.getStartLocationCoordinate().getX(),0.01);
		assertEquals(100.0,v.getStartLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectLocationCoordinate(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals(10.0,v.getCoord().getX(),0.01);
		assertEquals(100.0,v.getCoord().getY(),0.01);
	}
	
	@Test
	public void whenReadingFile_v4HasTheCorrectLocationId(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Vehicle v = getVehicle("v4",vrp.getVehicles());
		assertEquals("startLoc",v.getStartLocationId());
	}
	
	@Test
	public void whenReadingJobs_capOfShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(10,s.getCapacityDemand());
	}
	
	@Test
	public void whenReadingJobs_pickupServiceTimeOfShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(10.0,s.getPickupServiceTime(),0.01);
	}
	
	@Test
	public void whenReadingJobs_pickupTimeWindowOfShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(1000.0,s.getPickupTimeWindow().getStart(),0.01);
		assertEquals(4000.0,s.getPickupTimeWindow().getEnd(),0.01);
	}
	
	@Test
	public void whenReadingJobs_deliveryTimeWindowOfShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(6000.0,s.getDeliveryTimeWindow().getStart(),0.01);
		assertEquals(10000.0,s.getDeliveryTimeWindow().getEnd(),0.01);
	}
	
	@Test
	public void whenReadingJobs_deliveryServiceTimeOfShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(100.0,s.getDeliveryServiceTime(),0.01);
	}
	
	@Test
	public void whenReadingJobs_deliveryCoordShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(10.0,s.getDeliveryCoord().getX(),0.01);
		assertEquals(0.0,s.getDeliveryCoord().getY(),0.01);
	}
	
	@Test
	public void whenReadingJobs_pickupCoordShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals(10.0,s.getPickupCoord().getX(),0.01);
		assertEquals(10.0,s.getPickupCoord().getY(),0.01);
	}
	
	@Test
	public void whenReadingJobs_deliveryIdShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals("i(9,9)",s.getDeliveryLocation());
	}
	
	@Test
	public void whenReadingJobs_pickupIdShipment3IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("3");
		assertEquals("i(3,9)",s.getPickupLocation());
	}
	
	@Test
	public void whenReadingJobs_pickupLocationIdShipment4IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("4");
		assertEquals("[x=10.0][y=10.0]",s.getPickupLocation());
	}
	
	@Test
	public void whenReadingJobs_deliveryLocationIdShipment4IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("4");
		assertEquals("[x=10.0][y=0.0]",s.getDeliveryLocation());
	}
	
	@Test
	public void whenReadingJobs_pickupServiceTimeOfShipment4IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("4");
		assertEquals(0.0,s.getPickupServiceTime(),0.01);
	}
	
	@Test
	public void whenReadingJobs_deliveryServiceTimeOfShipment4IsReadCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(builder, null).read(inFileName);
		VehicleRoutingProblem vrp = builder.build();
		Shipment s = (Shipment) vrp.getJobs().get("4");
		assertEquals(100.0,s.getDeliveryServiceTime(),0.01);
	}

}
