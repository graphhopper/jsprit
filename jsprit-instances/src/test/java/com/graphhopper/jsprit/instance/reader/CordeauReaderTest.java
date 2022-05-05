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
package com.graphhopper.jsprit.instance.reader;

import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class CordeauReaderTest {

    @Test
    public void testCordeauReader() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        vrpBuilder.build();

    }

    private String getPath(String string) {
        URL resource = this.getClass().getClassLoader().getResource(string);
        if (resource == null) throw new IllegalStateException("resource " + string + " does not exist");
        return resource.getPath();
    }

    @Test
    public void whenReadingInstance_fleetSizeIsFinite() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(FleetSize.FINITE, vrp.getFleetSize());
    }

    @Test
    public void testNuOfVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(16, vrp.getVehicles().size());
    }

    @Test
    public void whenReadingCordeauInstance_vehiclesHaveTheCorrectCapacity() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(80, v.getType().getCapacityDimensions().get(0));
        }
    }

    @Test
    public void whenReadingCordeauInstance_vehiclesHaveTheCorrectDuration() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p08"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(0.0, v.getEarliestDeparture(), 0.1);
            assertEquals(310.0, v.getLatestArrival() - v.getEarliestDeparture(), 0.1);
        }
    }

    @Test
    public void whenReadingCustomersCordeauInstance_customerOneShouldHaveCorrectCoordinates() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        Service service = (Service) vrp.getJobs().get("1");
        assertEquals(37.0, service.getLocation().getCoordinate().getX(), 0.1);
        assertEquals(52.0, service.getLocation().getCoordinate().getY(), 0.1);
    }

    @Test
    public void whenReadingCustomersCordeauInstance_customerTwoShouldHaveCorrectServiceDuration() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        Service service = (Service) vrp.getJobs().get("2");
        assertEquals(0.0, service.getServiceDuration(), 0.1);
    }

    @Test
    public void whenReadingCustomersCordeauInstance_customerThreeShouldHaveCorrectDemand() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        Service service = (Service) vrp.getJobs().get("3");
        assertEquals(16.0, service.getSize().get(0), 0.1);
    }

    @Test
    public void whenReadingCustomersCordeauInstance_customerFortySevenShouldHaveCorrectDemand() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        Service service = (Service) vrp.getJobs().get("47");
        assertEquals(25.0, service.getSize().get(0), 0.1);
    }

    @Test
    public void testLocationsAndCapOfVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        boolean capacityOk = true;
        boolean loc1ok = false;
        boolean loc2ok = false;
        boolean loc3ok = false;
        boolean loc4ok = false;
        for (Vehicle v : vrp.getVehicles()) {
            if (v.getType().getCapacityDimensions().get(0) != 80) capacityOk = false;
            if (v.getStartLocation().getCoordinate().getX() == 20.0 && v.getStartLocation().getCoordinate().getY() == 20.0)
                loc1ok = true;
            if (v.getStartLocation().getCoordinate().getX() == 30.0 && v.getStartLocation().getCoordinate().getY() == 40.0)
                loc2ok = true;
            if (v.getStartLocation().getCoordinate().getX() == 50.0 && v.getStartLocation().getCoordinate().getY() == 30.0)
                loc3ok = true;
            if (v.getStartLocation().getCoordinate().getX() == 60.0 && v.getStartLocation().getCoordinate().getY() == 50.0)
                loc4ok = true;
        }
        assertTrue(capacityOk);
        assertTrue(loc1ok);
        assertTrue(loc2ok);
        assertTrue(loc3ok);
        assertTrue(loc4ok);
    }

    @Test
    public void testNuOfCustomers() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new CordeauReader(vrpBuilder).read(getPath("p01"));
        VehicleRoutingProblem vrp = vrpBuilder.build();
        assertEquals(50, vrp.getJobs().values().size());
    }
}
