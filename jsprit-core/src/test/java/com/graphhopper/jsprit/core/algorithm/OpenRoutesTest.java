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

import com.graphhopper.jsprit.core.algorithm.box.SchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.recreate.NoSolutionFoundException;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.GreatCircleCosts;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class OpenRoutesTest {

    @Test
    public void whenDealingWithOpenRouteAndShipments_insertionShouldNotRequireRouteToBeClosed() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(11.)
            .setType(type).setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();

        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(TestUtils.loc(Coordinate.newInstance(5, 0)))
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

        Service service = Service.Builder.newInstance("s")
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

        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(5, 0)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).build())
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(10., Solutions.bestOf(solutions).getCost(), 0.01);

    }

    @Test
    public void whenDealingWithOpenRoute_algorithmShouldCalculateCorrectCosts() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(10.)
            .setType(type).setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();

        Service service = Service.Builder.newInstance("s")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(5, 0)).build()).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(5., Solutions.bestOf(solutions).getCost(), 0.01);

    }

    @Test
    public void whenDealingWithOpenRouteAndGreatCircleCost_algorithmShouldRunWithoutException() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setType(type).setReturnToDepot(false)
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
            .build();

        Service service = Service.Builder.newInstance("s")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(50, 0)).build()).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(service).addVehicle(vehicle)
            .setRoutingCost(new GreatCircleCosts())
            .build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        try {
            @SuppressWarnings("UnusedDeclaration")
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(false);
        }


    }


}
