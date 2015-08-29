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
package jsprit.core.algorithm.ruin.distance;

import jsprit.core.problem.Location;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;


public class TestJobDistanceAvgCosts {

    public static void main(String[] args) {
        VehicleRoutingTransportCosts costs = new VehicleRoutingTransportCosts() {

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {

                return 0;
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to,
                                                   double arrivalTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getTransportCost(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                @SuppressWarnings("unused")
                String vehicleId = vehicle.getId();
                return 0;
            }

            @Override
            public double getTransportTime(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                return 0;
            }
        };
        AvgServiceDistance c = new AvgServiceDistance(costs);
        c.getDistance(Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance("foo")).build(), Service.Builder.newInstance("2").addSizeDimension(0, 2).setLocation(Location.newInstance("foo")).build());
    }

    @Test(expected = NullPointerException.class)
    public void whenVehicleAndDriverIsNull_And_CostsDoesNotProvideAMethodForThis_throwException() {
//		(expected=NullPointerException.class)
        VehicleRoutingTransportCosts costs = new VehicleRoutingTransportCosts() {

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {

                return 0;
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to,
                                                   double arrivalTime, Driver driver, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getTransportCost(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                @SuppressWarnings("unused")
                String vehicleId = vehicle.getId();
                return 0;
            }

            @Override
            public double getTransportTime(Location from, Location to,
                                           double departureTime, Driver driver, Vehicle vehicle) {
                return 0;
            }
        };
        AvgServiceDistance c = new AvgServiceDistance(costs);
        c.getDistance(Service.Builder.newInstance("1").addSizeDimension(0, 1).setLocation(Location.newInstance("loc")).build(), Service.Builder.newInstance("2").addSizeDimension(0, 2).setLocation(Location.newInstance("loc")).build());
    }

}
