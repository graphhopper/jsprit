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
package com.graphhopper.jsprit.core.algorithm.ruin.distance;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;


public class TestJobDistanceAvgCosts {

    public static void main(String[] args) {
        VehicleRoutingTransportCosts costs = new VehicleRoutingTransportCosts() {

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return 0;
            }

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
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return 0;
            }

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
