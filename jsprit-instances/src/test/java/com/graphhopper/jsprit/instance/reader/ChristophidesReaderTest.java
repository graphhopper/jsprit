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


public class ChristophidesReaderTest {

    @Test
    public void whenReadingInstance_nuOfCustomersIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc1.txt"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(50, vrp.getJobs().values().size());
    }

    private String getPath(String string) {
        URL resource = this.getClass().getClassLoader().getResource(string);
        if (resource == null) throw new IllegalStateException("resource " + string + " does not exist");
        return resource.getPath();
    }

    @Test
    public void whenReadingInstance_fleetSizeIsInfinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc1.txt"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.INFINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingInstance_vehicleCapacitiesAreCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc1.txt"));
        VehicleRoutingProblem vrp = builder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(160, v.getType().getCapacityDimensions().get(0));
        }
    }

    @Test
    public void whenReadingInstance_vehicleLocationsAreCorrect_and_correspondToDepotLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc1.txt"));
        VehicleRoutingProblem vrp = builder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(30.0, v.getStartLocation().getCoordinate().getX(), 0.01);
            assertEquals(40.0, v.getStartLocation().getCoordinate().getY(), 0.01);
        }
    }

    @Test
    public void whenReadingInstance_vehicleDurationsAreCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc13.txt"));
        VehicleRoutingProblem vrp = builder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(0.0, v.getEarliestDeparture(), 0.01);
            assertEquals(720.0, v.getLatestArrival() - v.getEarliestDeparture(), 0.01);
        }
    }

    @Test
    public void whenReadingInstance_demandOfCustomerOneIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc1.txt"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(7, vrp.getJobs().get("1").getSize().get(0));
    }

    @Test
    public void whenReadingInstance_serviceDurationOfCustomerTwoIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new ChristofidesReader(builder).read(getPath("vrpnc13.txt"));
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(50.0, ((Service) vrp.getJobs().get("2")).getServiceDuration(), 0.1);
    }


}
