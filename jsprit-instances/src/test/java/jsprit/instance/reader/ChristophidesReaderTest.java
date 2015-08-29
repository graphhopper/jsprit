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
package jsprit.instance.reader;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
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
