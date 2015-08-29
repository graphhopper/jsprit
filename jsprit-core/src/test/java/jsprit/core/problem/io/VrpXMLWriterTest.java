/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.Builder;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import jsprit.core.util.TestUtils;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class VrpXMLWriterTest {

    private String infileName;

    @Before
    public void doBefore() {
        infileName = "src/test/resources/infiniteWriterV2Test.xml";
    }

    @Test
    public void whenWritingInfiniteVrp_itWritesCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(FleetSize.INFINITE);
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("myVehicle").setStartLocation(TestUtils.loc("loc")).setType(type).build();
        builder.addVehicle(vehicle);
        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);
    }

    @Test
    public void whenWritingFiniteVrp_itWritesCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(FleetSize.FINITE);
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();
        builder.addVehicle(v1);
        builder.addVehicle(v2);
        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);
    }

    @Test
    public void t() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(FleetSize.FINITE);
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();
        builder.addVehicle(v1);
        builder.addVehicle(v2);
        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
    }

    @Test
    public void whenWritingServices_itWritesThemCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);


        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        Service s1_read = (Service) vrp.getJobs().get("1");
        assertEquals("1", s1_read.getId());
        assertEquals("loc", s1_read.getLocation().getId());
        assertEquals("service", s1_read.getType());
        assertEquals(2.0, s1_read.getServiceDuration(), 0.01);
    }

    @Test
    public void shouldWriteNameOfService() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        Service s1 = Service.Builder.newInstance("1").setName("cleaning").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Service s1_read = (Service) readVrp.getJobs().get("1");
        assertTrue(s1_read.getName().equals("cleaning"));
    }

    @Test
    public void shouldWriteNameOfShipment() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        Location pickLocation = Location.Builder.newInstance().setId("pick").setIndex(1).build();
        Shipment s1 = Shipment.Builder.newInstance("1").setName("cleaning")
            .setPickupLocation(pickLocation)
            .setDeliveryLocation(TestUtils.loc("del")).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Shipment s1_read = (Shipment) readVrp.getJobs().get("1");
        assertTrue(s1_read.getName().equals("cleaning"));
        assertEquals(1, s1_read.getPickupLocation().getIndex());
    }

    @Test
    public void whenWritingServicesWithSeveralCapacityDimensions_itWritesThemCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Service s1 = Service.Builder.newInstance("1")
            .addSizeDimension(0, 20)
            .addSizeDimension(1, 200)
            .setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        Service s1_read = (Service) vrp.getJobs().get("1");

        assertEquals(2, s1_read.getSize().getNuOfDimensions());
        assertEquals(20, s1_read.getSize().get(0));
        assertEquals(200, s1_read.getSize().get(1));

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdsOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals("pickLoc", ((Shipment) readVrp.getJobs().get("1")).getPickupLocation().getId());
        assertEquals("delLoc", ((Shipment) readVrp.getJobs().get("1")).getDeliveryLocation().getId());

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenPickupTimeWindowsOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals(1.0, ((Shipment) readVrp.getJobs().get("1")).getPickupTimeWindow().getStart(), 0.01);
        assertEquals(2.0, ((Shipment) readVrp.getJobs().get("1")).getPickupTimeWindow().getEnd(), 0.01);


    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryTimeWindowsOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals(3.0, ((Shipment) readVrp.getJobs().get("1")).getDeliveryTimeWindow().getStart(), 0.01);
        assertEquals(4.0, ((Shipment) readVrp.getJobs().get("1")).getDeliveryTimeWindow().getEnd(), 0.01);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryServiceTimeOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
            .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals(100.0, ((Shipment) readVrp.getJobs().get("1")).getPickupServiceTime(), 0.01);
        assertEquals(50.0, ((Shipment) readVrp.getJobs().get("1")).getDeliveryServiceTime(), 0.01);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2))).setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals("[x=1.0][y=2.0]", ((Shipment) readVrp.getJobs().get("1")).getPickupLocation().getId());
    }

    @Test
    public void whenWritingVehicles_vehShouldHave2Skills() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").addSkill("SKILL5").addSkill("skill1").addSkill("Skill2")
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Vehicle veh1 = getVehicle("v1", readVrp);

        assertEquals(3, veh1.getSkills().values().size());
    }

    @Test
    public void whenWritingVehicles_vehShouldContain_skill5() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").addSkill("SKILL5").addSkill("skill1").addSkill("Skill2")
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Vehicle veh1 = getVehicle("v1", readVrp);

        assertTrue(veh1.getSkills().containsSkill("skill5"));
    }

    @Test
    public void whenWritingVehicles_vehShouldContain_skill1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").addSkill("SKILL5").addSkill("skill1").addSkill("Skill2")
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Vehicle veh1 = getVehicle("v1", readVrp);

        assertTrue(veh1.getSkills().containsSkill("skill1"));
    }

    @Test
    public void whenWritingVehicles_vehShouldContain_skill2() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").addSkill("SKILL5").addSkill("skill1").addSkill("Skill2")
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Vehicle veh1 = getVehicle("v1", readVrp);

        assertTrue(veh1.getSkills().containsSkill("skill2"));
    }

    @Test
    public void whenWritingVehicles_vehShouldHave0Skills() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        Vehicle veh = getVehicle("v1", readVrp);

        assertEquals(0, veh.getSkills().values().size());
    }

    private Vehicle getVehicle(String v1, VehicleRoutingProblem readVrp) {
        for (Vehicle v : readVrp.getVehicles()) {
            if (v.getId().equals(v1)) return v;
        }
        return null;
    }

    @Test
    public void whenWritingShipments_shipmentShouldHaveCorrectNuSkills() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Shipment s = Shipment.Builder.newInstance("1").addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3")
            .addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertEquals(3, readVrp.getJobs().get("1").getRequiredSkills().values().size());
    }

    @Test
    public void whenWritingShipments_shipmentShouldContain_skill1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Shipment s = Shipment.Builder.newInstance("1").addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3")
            .addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertTrue(readVrp.getJobs().get("1").getRequiredSkills().containsSkill("skill1"));
    }

    @Test
    public void whenWritingShipments_shipmentShouldContain_skill2() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Shipment s = Shipment.Builder.newInstance("1").addRequiredSkill("skill1").addRequiredSkill("Skill2").addRequiredSkill("skill3")
            .addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertTrue(readVrp.getJobs().get("1").getRequiredSkills().containsSkill("skill2"));
    }

    @Test
    public void whenWritingShipments_shipmentShouldContain_skill3() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Shipment s = Shipment.Builder.newInstance("1").addRequiredSkill("skill1").addRequiredSkill("Skill2").addRequiredSkill("skill3")
            .addSizeDimension(0, 10)
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertTrue(readVrp.getJobs().get("1").getRequiredSkills().containsSkill("skill3"));
    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationCoordinateOfS1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Shipment s1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 10).setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation"))
            .setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals(1.0, ((Shipment) readVrp.getJobs().get("1")).getPickupLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, ((Shipment) readVrp.getJobs().get("1")).getPickupLocation().getCoordinate().getY(), 0.01);

        assertEquals(5.0, ((Shipment) readVrp.getJobs().get("1")).getDeliveryLocation().getCoordinate().getX(), 0.01);
        assertEquals(6.0, ((Shipment) readVrp.getJobs().get("1")).getDeliveryLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenWritingShipmentWithSeveralCapacityDimension_itShouldWriteAndReadItCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Shipment s1 = Shipment.Builder.newInstance("1")
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
            .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
            .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
            .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50)
            .addSizeDimension(0, 10)
            .addSizeDimension(2, 100)
            .build();

        Shipment s2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 20)
            .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
            .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
            .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertEquals(3, (readVrp.getJobs().get("1")).getSize().getNuOfDimensions());
        assertEquals(10, (readVrp.getJobs().get("1")).getSize().get(0));
        assertEquals(0, (readVrp.getJobs().get("1")).getSize().get(1));
        assertEquals(100, (readVrp.getJobs().get("1")).getSize().get(2));

        assertEquals(1, (readVrp.getJobs().get("2")).getSize().getNuOfDimensions());
        assertEquals(20, (readVrp.getJobs().get("2")).getSize().get(0));
    }

    @Test
    public void whenWritingVehicleV1_itsStartLocationMustBeWrittenCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v1", readVrp.getVehicles());
        assertEquals("loc", v.getStartLocation().getId());
        assertEquals("loc", v.getEndLocation().getId());

    }

    @Test
    public void whenWritingService_itShouldHaveTheCorrectNuSkills() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Service s = Service.Builder.newInstance("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
            .setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertEquals(2, readVrp.getJobs().get("1").getRequiredSkills().values().size());
    }

    @Test
    public void whenWritingService_itShouldContain_skill1() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Service s = Service.Builder.newInstance("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
            .setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertTrue(readVrp.getJobs().get("1").getRequiredSkills().containsSkill("skill1"));
    }

    @Test
    public void whenWritingService_itShouldContain_skill2() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        Service s = Service.Builder.newInstance("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
            .setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        assertTrue(readVrp.getJobs().get("1").getRequiredSkills().containsSkill("skill2"));
    }

    @Test
    public void whenWritingVehicleV1_itDoesNotReturnToDepotMustBeWrittenCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc"))
            .setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v1", readVrp.getVehicles());
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenWritingVehicleV1_readingAgainAssignsCorrectType() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v1", readVrp.getVehicles());
        assertEquals("vehType", v.getType().getTypeId());
    }

    @Test
    public void whenWritingVehicleV2_readingAgainAssignsCorrectType() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v2", readVrp.getVehicles());
        assertEquals("vehType2", v.getType().getTypeId());
        assertEquals(200, v.getType().getCapacityDimensions().get(0));

    }

    @Test
    public void whenWritingVehicleV2_readingItsLocationsAgainReturnsCorrectLocations() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v2", readVrp.getVehicles());
        assertEquals("startLoc", v.getStartLocation().getId());
        assertEquals("endLoc", v.getEndLocation().getId());
    }

    @Test
    public void whenWritingVehicleV2_readingItsLocationsCoordsAgainReturnsCorrectLocationsCoords() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false)
            .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
            .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v2", readVrp.getVehicles());
        assertEquals(1.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, v.getStartLocation().getCoordinate().getY(), 0.01);

        assertEquals(4.0, v.getEndLocation().getCoordinate().getX(), 0.01);
        assertEquals(5.0, v.getEndLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type")
            .addCapacityDimension(0, 100)
            .addCapacityDimension(1, 1000)
            .addCapacityDimension(2, 10000)
            .build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();
        builder.addVehicle(v2);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v", readVrp.getVehicles());
        assertEquals(3, v.getType().getCapacityDimensions().getNuOfDimensions());
        assertEquals(100, v.getType().getCapacityDimensions().get(0));
        assertEquals(1000, v.getType().getCapacityDimensions().get(1));
        assertEquals(10000, v.getType().getCapacityDimensions().get(2));
    }

    @Test
    public void whenWritingVehicleWithSeveralCapacityDimensions_itShouldBeWrittenAndRereadCorrectlyV2() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("type")
            .addCapacityDimension(0, 100)
            .addCapacityDimension(1, 1000)
            .addCapacityDimension(10, 10000)
            .build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
            .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();
        builder.addVehicle(v2);

        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();

        Vehicle v = getVehicle("v", readVrp.getVehicles());
        assertEquals(11, v.getType().getCapacityDimensions().getNuOfDimensions());
        assertEquals(0, v.getType().getCapacityDimensions().get(9));
        assertEquals(10000, v.getType().getCapacityDimensions().get(10));
    }

    private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
        for (Vehicle v : vehicles) if (string.equals(v.getId())) return v;
        return null;
    }

    @Test
    public void whenWritingAndReadingInitialRouteWithShipment4_thisShipmentShouldNotAppearInJobMap() { //since it is not part of the problem anymore
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read("src/test/resources/finiteVrpWithInitialSolutionForReaderTest.xml");
        VehicleRoutingProblem vrp = builder.build();

        new VrpXMLWriter(vrp).write("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");

        VehicleRoutingProblem.Builder newBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(newBuilder).read("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");
        VehicleRoutingProblem newVrp = newBuilder.build();

        assertFalse(newVrp.getJobs().containsKey("4"));
    }

    @Test
    public void whenReadingInitialRouteWithDepTime10_departureTimeOfRouteShouldBeReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read("src/test/resources/finiteVrpWithInitialSolutionForReaderTest.xml");
        VehicleRoutingProblem vrp = builder.build();

        new VrpXMLWriter(vrp).write("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");

        VehicleRoutingProblem.Builder newBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(newBuilder).read("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");
        VehicleRoutingProblem newVrp = newBuilder.build();

        assertEquals(10., newVrp.getInitialVehicleRoutes().iterator().next().getDepartureTime(), 0.01);
    }

    @Test
    public void whenReadingInitialRoute_nuInitialRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read("src/test/resources/finiteVrpWithInitialSolutionForReaderTest.xml");
        VehicleRoutingProblem vrp = builder.build();

        new VrpXMLWriter(vrp).write("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");

        VehicleRoutingProblem.Builder newBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(newBuilder).read("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");
        VehicleRoutingProblem newVrp = newBuilder.build();


        assertEquals(1, newVrp.getInitialVehicleRoutes().size());
    }

    @Test
    public void whenReadingInitialRoute_nuActivitiesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read("src/test/resources/finiteVrpWithInitialSolutionForReaderTest.xml");
        VehicleRoutingProblem vrp = builder.build();

        new VrpXMLWriter(vrp).write("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");

        VehicleRoutingProblem.Builder newBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(newBuilder).read("src/test/resources/finiteVrpWithInitialSolutionForWriterTest.xml");
        VehicleRoutingProblem newVrp = newBuilder.build();


        assertEquals(2, newVrp.getInitialVehicleRoutes().iterator().next().getActivities().size());
    }

    @Test
    public void solutionWithoutUnassignedJobsShouldBeWrittenCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).addService(s2).build();
        List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        solutions.add(solution);

        new VrpXMLWriter(vrp, solutions).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<VehicleRoutingProblemSolution> solutionsToRead = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpToReadBuilder, solutionsToRead).read(infileName);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).getCost(), 0.01);
        assertTrue(Solutions.bestOf(solutionsToRead).getUnassignedJobs().isEmpty());
    }

    @Test
    public void solutionWithUnassignedJobsShouldBeWrittenCorrectly() {
        Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        Service s1 = Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        Service s2 = Service.Builder.newInstance("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).build();
        List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        solution.getUnassignedJobs().add(s2);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<VehicleRoutingProblemSolution>();
        solutions.add(solution);

        new VrpXMLWriter(vrp, solutions).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<VehicleRoutingProblemSolution> solutionsToRead = new ArrayList<VehicleRoutingProblemSolution>();
        new VrpXMLReader(vrpToReadBuilder, solutionsToRead).read(infileName);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).getCost(), 0.01);
        assertEquals(1, Solutions.bestOf(solutionsToRead).getUnassignedJobs().size());
        assertEquals("2", Solutions.bestOf(solutionsToRead).getUnassignedJobs().iterator().next().getId());
    }

}
