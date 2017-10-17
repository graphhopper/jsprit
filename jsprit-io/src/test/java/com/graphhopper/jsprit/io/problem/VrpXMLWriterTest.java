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
package com.graphhopper.jsprit.io.problem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.AbstractSingleActivityJob;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.io.util.TestUtils;

public class VrpXMLWriterTest {

    private String infileName;

    @Before
    public void doBefore() {
        infileName = "src/test/resources/infiniteWriterV2Test.xml";
    }

    @Test
    public void whenWritingInfiniteVrp_itWritesCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("myVehicle").setStartLocation(TestUtils.loc("loc")).setType(type).build();
        builder.addVehicle(vehicle);
        VehicleRoutingProblem vrp = builder.build();
        new VrpXMLWriter(vrp, null).write(infileName);
    }

    @Test
    public void whenWritingFiniteVrp_itWritesCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
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
        builder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);


        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        ServiceJob s1_read = (ServiceJob) vrp.getJobs().get("1");
        assertEquals("1", s1_read.getId());
        assertEquals("loc", s1_read.getActivity().getLocation().getId());
        assertEquals("pickup", s1_read.getType());
        assertEquals(2.0, s1_read.getActivity().getOperationTime(), 0.01);
    }

    @Test
    public void shouldWriteNameOfService() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        ServiceJob s1 = new ServiceJob.Builder("1").setName("cleaning").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        AbstractSingleActivityJob<?> s1_read = (AbstractSingleActivityJob<?>) readVrp.getJobs().get("1");
        assertTrue(s1_read.getName().equals("cleaning"));
    }

    @Test
    public void shouldWriteNameOfShipment() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        Location pickLocation = Location.Builder.newInstance().setId("pick").setIndex(1).build();
        ShipmentJob s1 = new ShipmentJob.Builder("1").setName("cleaning")
                .setPickupLocation(pickLocation)
                .setDeliveryLocation(TestUtils.loc("del")).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        ShipmentJob s1_read = (ShipmentJob) readVrp.getJobs().get("1");
        assertTrue(s1_read.getName().equals("cleaning"));
        assertEquals(1, s1_read.getPickupActivity().getLocation().getIndex());
    }

    @Test
    public void whenWritingServicesWithSeveralCapacityDimensions_itWritesThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ServiceJob s1 = new ServiceJob.Builder("1")
                .addSizeDimension(0, 20)
                .addSizeDimension(1, 200)
                .setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        ServiceJob s1_read = (ServiceJob) vrp.getJobs().get("1");

        SizeDimension size = s1_read.getActivity().getLoadChange();
        assertEquals(2, size.getNuOfDimensions());
        assertEquals(20, size.get(0));
        assertEquals(200, size.get(1));

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdsOfS1() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
                .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
                .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals("pickLoc", ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getLocation().getId());
        assertEquals("delLoc", ((ShipmentJob) readVrp.getJobs().get("1")).getDeliveryActivity().getLocation().getId());
    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenPickupTimeWindowsOfS1() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
                .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
                .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        TimeWindow tw = ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getTimeWindows().iterator()
            .next();
        assertEquals(1.0, tw.getStart(), 0.01);
        assertEquals(2.0, tw.getEnd(), 0.01);
    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryTimeWindowsOfS1() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
                .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
                .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        TimeWindow tw = ((ShipmentJob) readVrp.getJobs().get("1")).getDeliveryActivity().getTimeWindows().iterator()
            .next();
        assertEquals(3.0, tw.getStart(), 0.01);
        assertEquals(4.0, tw.getEnd(), 0.01);
    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenDeliveryServiceTimeOfS1() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                .setDeliveryLocation(TestUtils.loc("delLoc")).setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
                .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
                .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals(100.0, ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getOperationTime(), 0.01);
        assertEquals(50.0, ((ShipmentJob) readVrp.getJobs().get("1")).getDeliveryActivity().getOperationTime(), 0.01);

    }

    @Test
    public void whenWritingShipments_readingThemAgainMustReturnTheWrittenLocationIdOfS1() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10)
                .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2))).setDeliveryLocation(TestUtils.loc("delLoc"))
                .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
                .setPickupLocation(Location.Builder.newInstance().setId("pickLocation").build())
                .setDeliveryLocation(TestUtils.loc("delLocation")).setPickupTimeWindow(TimeWindow.newInstance(5, 6))
                .setDeliveryTimeWindow(TimeWindow.newInstance(7, 8)).build();


        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();
        new VrpXMLWriter(vrp, null).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpToReadBuilder, null).read(infileName);
        VehicleRoutingProblem readVrp = vrpToReadBuilder.build();
        assertEquals(2, readVrp.getJobs().size());

        assertEquals("[x=1.0][y=2.0]", ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getLocation().getId());
    }

    @Test
    public void whenWritingVehicles_vehShouldHave2Skills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
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
            if (v.getId().equals(v1))
                return v;
        }
        return null;
    }

    @Test
    public void whenWritingShipments_shipmentShouldHaveCorrectNuSkills() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ShipmentJob s = new ShipmentJob.Builder("1").addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3")
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ShipmentJob s = new ShipmentJob.Builder("1").addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3")
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ShipmentJob s = new ShipmentJob.Builder("1").addRequiredSkill("skill1").addRequiredSkill("Skill2").addRequiredSkill("skill3")
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ShipmentJob s = new ShipmentJob.Builder("1").addRequiredSkill("skill1").addRequiredSkill("Skill2").addRequiredSkill("skill3")
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ShipmentJob s1 = new ShipmentJob.Builder("1").addSizeDimension(0, 10).setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
                .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
                .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50).build();
        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
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

        assertEquals(1.0, ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(2.0, ((ShipmentJob) readVrp.getJobs().get("1")).getPickupActivity().getLocation().getCoordinate().getY(), 0.01);

        assertEquals(5.0, ((ShipmentJob) readVrp.getJobs().get("1")).getDeliveryActivity().getLocation().getCoordinate().getX(), 0.01);
        assertEquals(6.0, ((ShipmentJob) readVrp.getJobs().get("1")).getDeliveryActivity().getLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenWritingShipmentWithSeveralCapacityDimension_itShouldWriteAndReadItCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ShipmentJob s1 = new ShipmentJob.Builder("1")
                .setPickupLocation(TestUtils.loc(Coordinate.newInstance(1, 2)))
                .setDeliveryLocation(TestUtils.loc("delLoc", Coordinate.newInstance(5, 6)))
                .setPickupTimeWindow(TimeWindow.newInstance(1, 2))
                .setDeliveryTimeWindow(TimeWindow.newInstance(3, 4)).setPickupServiceTime(100).setDeliveryServiceTime(50)
                .addSizeDimension(0, 10)
                .addSizeDimension(2, 100)
                .build();

        ShipmentJob s2 = new ShipmentJob.Builder("2").addSizeDimension(0, 20)
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ServiceJob s = new ServiceJob.Builder("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ServiceJob s = new ServiceJob.Builder("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        ServiceJob s = new ServiceJob.Builder("1").addRequiredSkill("sKill1").addRequiredSkill("skill2").addSizeDimension(0, 1)
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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc"))
                .setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(TestUtils.loc("loc")).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false).setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
                .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleTypeImpl type2 = VehicleTypeImpl.Builder.newInstance("vehType2").addCapacityDimension(0, 200).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setReturnToDepot(false)
                .setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(TestUtils.loc("startLoc", Coordinate.newInstance(1, 2)))
                .setEndLocation(TestUtils.loc("endLoc", Coordinate.newInstance(4, 5))).setType(type2).build();

        builder.addVehicle(v1);
        builder.addVehicle(v2);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

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
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

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
        for (Vehicle v : vehicles) {
            if (string.equals(v.getId()))
                return v;
        }
        return null;
    }

    @Test
    public void solutionWithoutUnassignedJobsShouldBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).addService(s2).build();
        List<VehicleRoute> routes = new ArrayList<>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        solutions.add(solution);

        new VrpXMLWriter(vrp, solutions).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<VehicleRoutingProblemSolution> solutionsToRead = new ArrayList<>();
        new VrpXMLReader(vrpToReadBuilder, solutionsToRead).read(infileName);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).getCost(), 0.01);
        assertTrue(Solutions.bestOf(solutionsToRead).getUnassignedJobs().isEmpty());
    }

    @Test
    public void solutionWithUnassignedJobsShouldBeWrittenCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();

        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("vehType").addCapacityDimension(0, 20).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(TestUtils.loc("loc")).setType(type1).build();
        builder.addVehicle(v1);

        ServiceJob s1 = new ServiceJob.Builder("1").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc")).setServiceTime(2.0).build();
        ServiceJob s2 = new ServiceJob.Builder("2").addSizeDimension(0, 1).setLocation(TestUtils.loc("loc2")).setServiceTime(4.0).build();

        VehicleRoutingProblem vrp = builder.addJob(s1).addJob(s2).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(v1).addService(s1).build();
        List<VehicleRoute> routes = new ArrayList<>();
        routes.add(route);
        VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(routes, 10.);
        solution.getUnassignedJobs().add(s2);
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        solutions.add(solution);

        new VrpXMLWriter(vrp, solutions).write(infileName);

        VehicleRoutingProblem.Builder vrpToReadBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<VehicleRoutingProblemSolution> solutionsToRead = new ArrayList<>();
        new VrpXMLReader(vrpToReadBuilder, solutionsToRead).read(infileName);

        assertEquals(1, solutionsToRead.size());
        assertEquals(10., Solutions.bestOf(solutionsToRead).getCost(), 0.01);
        assertEquals(1, Solutions.bestOf(solutionsToRead).getUnassignedJobs().size());
        assertEquals("2", Solutions.bestOf(solutionsToRead).getUnassignedJobs().iterator().next().getId());
    }

}
