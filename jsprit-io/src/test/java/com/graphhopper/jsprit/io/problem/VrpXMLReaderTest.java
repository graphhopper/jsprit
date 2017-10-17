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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.util.Solutions;


public class VrpXMLReaderTest {

    private InputStream inputStream;

    @Before
    public void doBefore() {
        inputStream = getClass().getResourceAsStream("finiteVrpForReaderTest.xml");
    }

    @Test
    public void shouldReadNameOfService() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s = (ServiceJob) vrp.getJobs().get("1");
        assertTrue(s.getName().equals("cleaning"));
    }

    @Test
    public void shouldReadNameOfShipment() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertTrue(s.getName().equals("deliver-smth"));
    }

    @Test
    public void whenReadingVrp_problemTypeIsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingVrp_vehiclesAreReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(5, vrp.getVehicles().size());
        assertTrue(idsInCollection(Arrays.asList("v1", "v2"), vrp.getVehicles()));
    }

    @Test
    public void whenReadingVrp_vehiclesAreReadCorrectly2() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.getVehicles());
        assertEquals(20, v1.getType().getCapacityDimensions().get(0));
        assertEquals(100.0, v1.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(0.0, v1.getEarliestDeparture(), 0.01);
        assertEquals("depotLoc2", v1.getStartLocation().getId());
        assertNotNull(v1.getType());
        assertEquals("vehType", v1.getType().getTypeId());
        assertNotNull(v1.getStartLocation());
        assertEquals(1, v1.getStartLocation().getIndex());
        assertEquals(1000.0, v1.getLatestArrival(), 0.01);
    }

    @Test
    public void whenReadingVehicles_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.getVehicles());
        assertTrue(v1.getSkills().containsSkill("skill1"));
    }

    @Test
    public void whenReadingVehicles_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.getVehicles());
        assertTrue(v1.getSkills().containsSkill("skill2"));
    }

    @Test
    public void whenReadingVehicles_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v1 = getVehicle("v1", vrp.getVehicles());
        assertEquals(2, v1.getSkills().values().size());
    }

    @Test
    public void whenReadingVehicles_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v2", vrp.getVehicles());
        assertEquals(0, v.getSkills().values().size());
    }

    private Vehicle getVehicle(String string, Collection<Vehicle> vehicles) {
        for (Vehicle v : vehicles) {
            if (string.equals(v.getId()))
                return v;
        }
        return null;
    }

    private boolean idsInCollection(List<String> asList, Collection<Vehicle> vehicles) {
        List<String> ids = new ArrayList<>(asList);
        for (Vehicle v : vehicles) {
            if (ids.contains(v.getId())) {
                ids.remove(v.getId());
            }
        }
        return ids.isEmpty();
    }

    @Test
    public void whenReadingVrp_vehicleTypesAreReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(3, vrp.getTypes().size());
    }

    @Test
    public void whenReadingVrpWithInfiniteSize_itReadsCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingJobs_nuOfJobsIsReadThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(4, vrp.getJobs().size());
    }

    @Test
    public void whenReadingServices_itReadsThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        int servCounter = 0;
        for (Job j : vrp.getJobs().values()) {
            if (j instanceof ServiceJob) {
                servCounter++;
            }
        }
        assertEquals(2, servCounter);
    }

    @Test
    public void whenReadingService1_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s = (ServiceJob) vrp.getJobs().get("1");
        assertTrue(s.getRequiredSkills().containsSkill("skill1"));
    }

    @Test
    public void whenReadingService1_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s = (ServiceJob) vrp.getJobs().get("1");
        assertTrue(s.getRequiredSkills().containsSkill("skill2"));
    }

    @Test
    public void whenReadingService1_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s = (ServiceJob) vrp.getJobs().get("1");
        assertEquals(2, s.getRequiredSkills().values().size());
    }

    @Test
    public void whenReadingService2_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s = (ServiceJob) vrp.getJobs().get("2");
        assertEquals(0, s.getRequiredSkills().values().size());
    }

    @Test
    public void whenReadingShipments_itReadsThemCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        int shipCounter = 0;
        for (Job j : vrp.getJobs().values()) {
            if (j instanceof ShipmentJob) {
                shipCounter++;
            }
        }
        assertEquals(2, shipCounter);
    }

    @Test
    public void whenReadingShipment3_skill1ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertTrue(s.getRequiredSkills().containsSkill("skill1"));
    }

    @Test
    public void whenReadingShipment3_skill2ShouldBeAssigned() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertTrue(s.getRequiredSkills().containsSkill("skill2"));
    }

    @Test
    public void whenReadingShipment3_nuSkillsShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals(2, s.getRequiredSkills().values().size());
    }

    @Test
    public void whenReadingShipment4_nuSkillsOfV2ShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("4");
        assertEquals(0, s.getRequiredSkills().values().size());
    }

    @Test
    public void whenReadingServices_capOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s1 = (ServiceJob) vrp.getJobs().get("1");
        assertEquals(1, s1.getActivity().getLoadChange().get(0));
    }

    @Test
    public void whenReadingServices_durationOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s1 = (ServiceJob) vrp.getJobs().get("1");
        assertEquals(10.0, s1.getActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenReadingServices_twOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s1 = (ServiceJob) vrp.getJobs().get("1");
        TimeWindow tw = s1.getActivity().getTimeWindows().iterator().next();
        assertEquals(0.0, tw.getStart(), 0.01);
        assertEquals(4000.0, tw.getEnd(), 0.01);
    }

    @Test
    public void whenReadingServices_typeOfService1IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ServiceJob s1 = (ServiceJob) vrp.getJobs().get("1");
        assertEquals("pickup", s1.getType());
    }

    @Test
    public void whenReadingFile_v2MustNotReturnToDepot() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v2", vrp.getVehicles());
        assertFalse(v.isReturnToDepot());
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectStartLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals("startLoc", v3.getStartLocation().getId());
        assertNotNull(v3.getEndLocation());
        assertEquals(4, v3.getEndLocation().getIndex());
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectEndLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals("endLoc", v3.getEndLocation().getId());
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectEndLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals(1000.0, v3.getEndLocation().getCoordinate().getX(), 0.01);
        assertEquals(2000.0, v3.getEndLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectStartLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals(10.0, v3.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(100.0, v3.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals(10.0, v3.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(100.0, v3.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v3HasTheCorrectLocationId() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v3 = getVehicle("v3", vrp.getVehicles());
        assertEquals("startLoc", v3.getStartLocation().getId());
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectStartLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals("startLoc", v.getStartLocation().getId());
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectEndLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals("endLoc", v.getEndLocation().getId());
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectEndLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals(1000.0, v.getEndLocation().getCoordinate().getX(), 0.01);
        assertEquals(2000.0, v.getEndLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectStartLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals(10.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(100.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectLocationCoordinate() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals(10.0, v.getStartLocation().getCoordinate().getX(), 0.01);
        assertEquals(100.0, v.getStartLocation().getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingFile_v4HasTheCorrectLocationId() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v4", vrp.getVehicles());
        assertEquals("startLoc", v.getStartLocation().getId());
    }

    @Test
    public void whenReadingJobs_capOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals(-10, s.getDeliveryActivity().getLoadChange().get(0));
    }

    @Test
    public void whenReadingJobs_pickupServiceTimeOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals(10.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_pickupTimeWindowOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        TimeWindow tw = s.getPickupActivity().getTimeWindows().iterator().next();
        assertEquals(1000.0, tw.getStart(), 0.01);
        assertEquals(4000.0, tw.getEnd(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryTimeWindowOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        TimeWindow tw = s.getDeliveryActivity().getTimeWindows().iterator().next();
        assertEquals(6000.0, tw.getStart(), 0.01);
        assertEquals(10000.0, tw.getEnd(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryServiceTimeOfShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals(100.0, s.getDeliveryActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryCoordShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        Location deliveryLocation = s.getDeliveryActivity().getLocation();
        assertEquals(10.0, deliveryLocation.getCoordinate().getX(), 0.01);
        assertEquals(0.0, deliveryLocation.getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingJobs_pickupCoordShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        Location pickupLocation = s.getPickupActivity().getLocation();
        assertEquals(10.0, pickupLocation.getCoordinate().getX(), 0.01);
        assertEquals(10.0, pickupLocation.getCoordinate().getY(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryIdShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals("i(9,9)", s.getDeliveryActivity().getLocation().getId());
    }

    @Test
    public void whenReadingJobs_pickupIdShipment3IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("3");
        assertEquals("i(3,9)", s.getPickupActivity().getLocation().getId());
    }

    @Test
    public void whenReadingJobs_pickupLocationIdShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("4");
        assertEquals("[x=10.0][y=10.0]", s.getPickupActivity().getLocation().getId());
    }

    @Test
    public void whenReadingJobs_deliveryLocationIdShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("4");
        assertEquals("[x=10.0][y=0.0]", s.getDeliveryActivity().getLocation().getId());
    }

    @Test
    public void whenReadingJobs_pickupServiceTimeOfShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("4");
        assertEquals(0.0, s.getPickupActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenReadingJobs_deliveryServiceTimeOfShipment4IsReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        ShipmentJob s = (ShipmentJob) vrp.getJobs().get("4");
        assertEquals(100.0, s.getDeliveryActivity().getOperationTime(), 0.01);
    }

    @Test
    public void whenReadingFile_v5AndItsTypeHasTheCorrectCapacityDimensionValues() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(inputStream);
        VehicleRoutingProblem vrp = builder.build();
        Vehicle v = getVehicle("v5", vrp.getVehicles());
        assertEquals(100, v.getType().getCapacityDimensions().get(0));
        assertEquals(1000, v.getType().getCapacityDimensions().get(1));
        assertEquals(10000, v.getType().getCapacityDimensions().get(2));
        assertEquals(0, v.getType().getCapacityDimensions().get(3));
        assertEquals(0, v.getType().getCapacityDimensions().get(5));
        assertEquals(100000, v.getType().getCapacityDimensions().get(10));
    }

    @Test
    public void whenReadingInitialRouteWithShipment4_thisShipmentShouldNotAppearInJobMap() { //since it is not part of the problem anymore
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertFalse(vrp.getJobs().containsKey("4"));
    }

    @Test
    public void whenReadingInitialRouteWithDepTime10_departureTimeOfRouteShouldBeReadCorrectly() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(10., vrp.getInitialVehicleRoutes().iterator().next().getDepartureTime(), 0.01);
    }

    @Test
    public void whenReadingInitialRoute_nuInitialRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(1, vrp.getInitialVehicleRoutes().size());
    }

    @Test
    public void whenReadingInitialRoute_nuActivitiesShouldBeCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(builder, null).read(getClass().getResourceAsStream("finiteVrpWithInitialSolutionForReaderTest.xml"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(2, vrp.getInitialVehicleRoutes().iterator().next().getActivities().size());
    }

    @Test
    public void testRead_ifReaderIsCalled_itReadsSuccessfullyV2() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        ArrayList<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        new VrpXMLReader(vrpBuilder, solutions).read(getClass().getResourceAsStream("finiteVrpWithShipmentsAndSolution.xml"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(4, vrp.getJobs().size());
        assertEquals(1, solutions.size());

        assertEquals(1, solutions.get(0).getRoutes().size());
        List<TourActivity> activities = solutions.get(0).getRoutes().iterator().next().getTourActivities().getActivities();
        assertEquals(4, activities.size());
        assertTrue(activities.get(0) instanceof ServiceActivity);
        assertTrue(activities.get(1) instanceof ServiceActivity);
        assertTrue(activities.get(2) instanceof PickupActivity);
        assertTrue(activities.get(3) instanceof DeliveryActivity);
    }

    @Test
    public void testRead_ifReaderIsCalled_itReadsSuccessfully() {
        new VrpXMLReader(VehicleRoutingProblem.Builder.newInstance(), new ArrayList<VehicleRoutingProblemSolution>())
        .read(getClass().getResourceAsStream("lui-shen-solution.xml"));
        assertTrue(true);
    }


    @Test
    public void unassignedJobShouldBeRead() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        ArrayList<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        new VrpXMLReader(vrpBuilder, solutions).read(getClass().getResourceAsStream("finiteVrpWithShipmentsAndSolution.xml"));

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertEquals(1, solution.getUnassignedJobs().size());
        assertEquals("4", solution.getUnassignedJobs().iterator().next().getId());
    }
}

//
