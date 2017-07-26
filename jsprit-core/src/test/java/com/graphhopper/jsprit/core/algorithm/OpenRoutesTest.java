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
package com.graphhopper.jsprit.core.algorithm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.recreate.NoSolutionFoundException;
import com.graphhopper.jsprit.core.distance.SphericalDistanceCalculator;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.DefaultCosts;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.TestUtils;

public class OpenRoutesTest {

    @Test
    public void whenDealingWithOpenRouteAndShipments_insertionShouldNotRequireRouteToBeClosed() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(11.)
                        .setType(type).setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();

        ShipmentJob shipment = new ShipmentJob.Builder("s").setPickupLocation(TestUtils.loc(Coordinate.newInstance(5, 0)))
                        .setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(10, 0))).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        try {
            @SuppressWarnings("unused")
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (NoSolutionFoundException e) {
            assertFalse(true);
        }

    }

    @Test
    public void whenDealingWithOpenRoute_insertionShouldNotRequireRouteToBeClosed() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(9.)
                        .setType(type).setReturnToDepot(false)
                        .setStartLocation(TestUtils.loc(Coordinate.newInstance(0, 0)))
                        .build();

        ServiceJob service = new ServiceJob.Builder("s")
                        .setLocation(TestUtils.loc(Coordinate.newInstance(5, 0)))
                        .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        try {
            @SuppressWarnings("unused")
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (NoSolutionFoundException e) {
            assertFalse(true);
        }

    }


    @Test
    public void whenDealingWithOpenRouteAndShipments_algorithmShouldCalculateCorrectCosts() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(20.)
                        .setType(type).setReturnToDepot(false).setStartLocation(Location.Builder.newInstance()
                                        .setCoordinate(Coordinate.newInstance(0, 0)).build()).build();

        ShipmentJob shipment = new ShipmentJob.Builder("s")
                        .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(5, 0)).build())
                        .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).build())
                        .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(10., Solutions.bestOf(solutions).getCost(), 0.01);

    }

    @Test
    public void whenDealingWithOpenRoute_algorithmShouldCalculateCorrectCosts() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(10.)
                        .setType(type).setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();

        ServiceJob service = new ServiceJob.Builder("s")
                        .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(5, 0)).build()).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(5., Solutions.bestOf(solutions).getCost(), 0.01);

    }

    @Test
    public void whenDealingWithOpenRouteAndGreatCircleCost_algorithmShouldRunWithoutException() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
                        .setType(type).setReturnToDepot(false)
                        .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                        .build();

        ServiceJob service = new ServiceJob.Builder("s")
                        .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(50, 0)).build()).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                        .addJob(service).addVehicle(vehicle)
                        .setRoutingCost(new DefaultCosts(SphericalDistanceCalculator.getInstance()))
                        .build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        try {
            @SuppressWarnings({ "UnusedDeclaration", "unused" })
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }


    }


}
