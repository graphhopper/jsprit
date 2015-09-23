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
package jsprit.instance.reader;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
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
