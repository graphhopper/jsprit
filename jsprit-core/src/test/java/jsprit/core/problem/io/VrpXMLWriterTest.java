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

import java.util.Collection;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("deprecation")
public class VrpXMLWriterTest {
	
	private String infileName;
	
	@Before
	public void doBefore(){
		infileName = "src/test/resources/infiniteWriterV2Test.xml";
	}
	
	
	@Test
	public void whenWritingInfiniteVrp_itWritesCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.INFINITE);
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		Vehicle vehicle = VehicleImpl.Builder.newInstance("myVehicle").setStartLocationId("loc").setType(type).build();
		builder.addVehicle(vehicle);
		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp, null).write(infileName);
	}
	
	@Test
	public void whenWritingFiniteVrp_itWritesCorrectly(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.FINITE);
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp, null).write(infileName);
	}
	
	@Test
	public void t(){
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.setFleetSize(FleetSize.FINITE);
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
	}
	
	@Test
	public void whenWritingServices_itWritesThemCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		Service s1_read = (Service) vrp.getJobs().get("1");
		assertEquals("1", s1_read.getId());
		assertEquals("loc", s1_read.getLocationId());
		assertEquals("service", s1_read.getType());
		assertEquals(2.0,s1_read.getServiceDuration(),0.01);
	}
	
	@Test
	public void whenWritingServicesWithSeveralCapacityDimensions_itWritesThemCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		Service s1 = Service.Builder.newInstance("1")
				.addSizeDimension(0, 20)
				.addSizeDimension(1, 200)
				.setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		Service s1_read = (Service) vrp.getJobs().get("1");
		
		assertEquals(2, s1_read.getSize().getNuOfDimensions());
		assertEquals(20, s1_read.getSize().get(0));
		assertEquals(200, s1_read.getSize().get(1));
		
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdsOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupLocation("pickLoc").setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals("pickLoc",((Shipment)readVrp.getJobs().get("1")).getPickupLocation());
		assertEquals("delLoc",((Shipment)readVrp.getJobs().get("1")).getDeliveryLocation());
		
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenPickupTimeWindowsOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupLocation("pickLoc").setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals(1.0,((Shipment)readVrp.getJobs().get("1")).getPickupTimeWindow().getStart(),0.01);
		assertEquals(2.0,((Shipment)readVrp.getJobs().get("1")).getPickupTimeWindow().getEnd(),0.01);
		
		
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryTimeWindowsOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupLocation("pickLoc").setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals(3.0,((Shipment)readVrp.getJobs().get("1")).getDeliveryTimeWindow().getStart(),0.01);
		assertEquals(4.0,((Shipment)readVrp.getJobs().get("1")).getDeliveryTimeWindow().getEnd(),0.01);
		
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryServiceTimeOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupLocation("pickLoc").setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals(100.0,((Shipment)readVrp.getJobs().get("1")).getPickupServiceTime(),0.01);
		assertEquals(50.0,((Shipment)readVrp.getJobs().get("1")).getDeliveryServiceTime(),0.01);
		
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupCoord(Coordinate.newInstance(1, 2)).setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals("[x=1.0][y=2.0]",((Shipment)readVrp.getJobs().get("1")).getPickupLocation());
	}
	
	@Test
	public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationCoordOfS1(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Shipment s1 = Shipment.Builder.newInstance("1", 10).setPickupCoord(Coordinate.newInstance(1, 2)).setDeliveryCoord(Coordinate.newInstance(5, 6)).setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		assertEquals(2,readVrp.getJobs().size());
		
		assertEquals(1.0,((Shipment)readVrp.getJobs().get("1")).getPickupCoord().getX(),0.01);
		assertEquals(2.0,((Shipment)readVrp.getJobs().get("1")).getPickupCoord().getY(),0.01);
		
		assertEquals(5.0,((Shipment)readVrp.getJobs().get("1")).getDeliveryCoord().getX(),0.01);
		assertEquals(6.0,((Shipment)readVrp.getJobs().get("1")).getDeliveryCoord().getY(),0.01);
	}
	
	@Test
	public void whenWritingShipmentWithSeveralCapacityDimension_itShouldWriteAndReadItCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		Shipment s1 = Shipment.Builder.newInstance("1")
				.setPickupCoord(Coordinate.newInstance(1, 2)).setDeliveryCoord(Coordinate.newInstance(5, 6)).setDeliveryLocation("delLoc").setPickupTimeWindow(TimeWindow.newInstance(1, 2))
				.setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50)
				.addSizeDimension(0, 10)
				.addSizeDimension(2, 100)
				.build();
		
		Shipment s2 = Shipment.Builder.newInstance("2", 20).setPickupLocation("pickLocation").setDeliveryLocation("delLocation").setPickupTimeWindow(TimeWindow.newInstance(5, 6))
				.setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		assertEquals(3,((Shipment)readVrp.getJobs().get("1")).getSize().getNuOfDimensions());
		assertEquals(10,((Shipment)readVrp.getJobs().get("1")).getSize().get(0));
		assertEquals(0,((Shipment)readVrp.getJobs().get("1")).getSize().get(1));
		assertEquals(100,((Shipment)readVrp.getJobs().get("1")).getSize().get(2));
		
		assertEquals(1,((Shipment)readVrp.getJobs().get("2")).getSize().getNuOfDimensions());
		assertEquals(20,((Shipment)readVrp.getJobs().get("2")).getSize().get(0));
	}
	
	@Test
	public void whenWritingVehicleV1_itsStartLocationMustBeWrittenCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v1",readVrp.getVehicles());
		assertEquals("loc",v.getStartLocationId());
		assertEquals("loc",v.getEndLocationId());
		
	}
	
	@Test
	public void whenWritingVehicleV1_itDoesNotReturnToDepotMustBeWrittenCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v1",readVrp.getVehicles());
		assertFalse(v.isReturnToDepot());
	}
	
	@Test
	public void whenWritingVehicleV1_readingAgainAssignsCorrectType(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v1",readVrp.getVehicles());
		assertEquals("vehType",v.getType().getTypeId());
	}
	
	@Test
	public void whenWritingVehicleV2_readingAgainAssignsCorrectType(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("loc").setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v2",readVrp.getVehicles());
		assertEquals("vehType2",v.getType().getTypeId());
		assertEquals(200,v.getType().getCapacity());
		
	}
	
	@Test
	public void whenWritingVehicleV2_readingItsLocationsAgainReturnsCorrectLocations(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(1, 2))
				.setEndLocationId("endLoc").setEndLocationCoordinate(Coordinate.newInstance(4, 5)).setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v2",readVrp.getVehicles());
		assertEquals("startLoc",v.getStartLocationId());
		assertEquals("endLoc",v.getEndLocationId());
	}
	
	@Test
	public void whenWritingVehicleV2_readingItsLocationsCoordsAgainReturnsCorrectLocationsCoords(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType", 20).build();
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2", 200).build();
		Vehicle v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocationId("loc").setType(type1).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v2").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(1, 2))
				.setEndLocationId("endLoc").setEndLocationCoordinate(Coordinate.newInstance(4, 5)).setType(type2).build();
		
		builder.addVehicle(v1);
		builder.addVehicle(v2);
		
		Service s1 = Service.Builder.newInstance("1", 1).setLocationId("loc").setServiceTime(2.0).build();
		Service s2 = Service.Builder.newInstance("2", 1).setLocationId("loc2").setServiceTime(4.0).build();
		
		VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v2",readVrp.getVehicles());
		assertEquals(1.0,v.getStartLocationCoordinate().getX(),0.01);
		assertEquals(2.0,v.getStartLocationCoordinate().getY(),0.01);
		
		assertEquals(4.0,v.getEndLocationCoordinate().getX(),0.01);
		assertEquals(5.0,v.getEndLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectly(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type", 200)
				.addCapacityDimension(0, 100)
				.addCapacityDimension(1, 1000)
				.addCapacityDimension(2, 10000)
				.build();
		
		Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(1, 2))
				.setEndLocationId("endLoc").setEndLocationCoordinate(Coordinate.newInstance(4, 5)).setType(type2).build();
		builder.addVehicle(v2);

		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v",readVrp.getVehicles());
		assertEquals(3,v.getType().getCapacityDimensions().getNuOfDimensions());
		assertEquals(100,v.getType().getCapacityDimensions().get(0));
		assertEquals(1000,v.getType().getCapacityDimensions().get(1));
		assertEquals(10000,v.getType().getCapacityDimensions().get(2));
	}
	
	@Test
	public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectlyV2(){
		Builder builder = VehicleRoutingProblem.Builder.newInstance();
		
		VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type", 200)
				.addCapacityDimension(0, 100)
				.addCapacityDimension(1, 1000)
				.addCapacityDimension(10, 10000)
				.build();
		
		Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setStartLocationCoordinate(Coordinate.newInstance(1, 2))
				.setEndLocationId("endLoc").setEndLocationCoordinate(Coordinate.newInstance(4, 5)).setType(type2).build();
		builder.addVehicle(v2);

		VehicleRoutingProblem vrp = builder.build();
		new VrpXMLWriter(vrp, null).write(infileName);
		
		VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
		VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
		
		Vehicle v = getVehicle("v",readVrp.getVehicles());
		assertEquals(11,v.getType().getCapacityDimensions().getNuOfDimensions());
		assertEquals(0,v.getType().getCapacityDimensions().get(9));
		assertEquals(10000,v.getType().getCapacityDimensions().get(10));
	}
	
	private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
		for(Vehicle v : vehicles) if(string.equals(v.getId())) return v;
		return null;
	}
	

}
