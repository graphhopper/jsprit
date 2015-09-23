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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.CrowFlyCosts;
import jsprit.core.util.Solutions;
import jsprit.core.util.TestUtils;

import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class CalcWithTimeSchedulingTest {


    public void timeScheduler() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("myVehicle").setEarliestStart(0.0).setLatestArrival(100.0).
            setStartLocation(TestUtils.loc("0,0", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("myType").addCapacityDimension(0, 20).setCostPerDistance(1.0).build()).build();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(Service.Builder.newInstance("myService").addSizeDimension(0, 2)
            .setLocation(TestUtils.loc("0,20", Coordinate.newInstance(0, 20))).build());
        vrpBuilder.setFleetSize(FleetSize.INFINITE);
        vrpBuilder.setRoutingCost(getTpCosts(new CrowFlyCosts(vrpBuilder.getLocations())));
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/testConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        VehicleRoutingProblemSolution sol = Solutions.bestOf(solutions);
        assertEquals(40.0, sol.getCost(), 0.01);
        assertEquals(1, sol.getRoutes().size());
        VehicleRoute route = sol.getRoutes().iterator().next();
        assertEquals(50.0, route.getStart().getEndTime(), 0.01);
    }

    private VehicleRoutingTransportCosts getTpCosts(final VehicleRoutingTransportCosts baseCosts) {
        return new VehicleRoutingTransportCosts() {

            @Override
            public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                if (departureTime < 50) {
                    return baseCosts.getTransportCost(from, to, departureTime, driver, vehicle) * 2.0;
                }
                return baseCosts.getTransportCost(from, to, departureTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportTime(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, driver, vehicle);
            }
        };
    }

}
