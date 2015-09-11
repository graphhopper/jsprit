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


public class SolomonReaderTest {

    @Test
    public void whenReadingSolomonInstance_nuOfCustomersIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(100, vrp.getJobs().values().size());
    }

    private String getPath() {
        URL resource = getClass().getClassLoader().getResource("C101_solomon.txt");
        if (resource == null) throw new IllegalStateException("file C101_solomon.txt does not exist");
        return resource.getPath();
    }

    @Test
    public void whenReadingSolomonInstance_fleetSizeIsInfinite() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(FleetSize.INFINITE, vrp.getFleetSize());
    }

    @Test
    public void whenReadingSolomonInstance_vehicleCapacitiesAreCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(200, v.getType().getCapacityDimensions().get(0));
        }
    }

    @Test
    public void whenReadingSolomonInstance_vehicleLocationsAreCorrect_and_correspondToDepotLocation() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        for (Vehicle v : vrp.getVehicles()) {
            assertEquals(40.0, v.getStartLocation().getCoordinate().getX(), 0.01);
            assertEquals(50.0, v.getStartLocation().getCoordinate().getY(), 0.01);
        }
    }

    @Test
    public void whenReadingSolomonInstance_demandOfCustomerOneIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(10, vrp.getJobs().get("1").getSize().get(0));
    }

    @Test
    public void whenReadingSolomonInstance_serviceDurationOfCustomerTwoIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(90, ((Service) vrp.getJobs().get("2")).getServiceDuration(), 0.1);
    }

    @Test
    public void whenReadingSolomonInstance_earliestServiceStartTimeOfCustomerSixtyTwoIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(262.0, ((Service) vrp.getJobs().get("62")).getTimeWindow().getStart(), 0.1);
    }

    @Test
    public void whenReadingSolomonInstance_latestServiceStartTimeOfCustomerEightySevenIsCorrect() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(builder).read(getPath());
        VehicleRoutingProblem vrp = builder.build();
        assertEquals(144.0, ((Service) vrp.getJobs().get("87")).getTimeWindow().getEnd(), 0.1);
    }


}
