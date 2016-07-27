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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.Test;

import static org.junit.Assert.assertTrue;


public class TestMixedServiceAndShipmentsProblemOnRouteLevel {


    @Test(expected = UnsupportedOperationException.class)
    public void whenHavingShipmentsAndServicesInOneProblem_andInsertionShouldBeMadeOnRouteLevel_throwException() {
        /* get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
         */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();

		/*
         * build shipments at the required locations, each with a capacity-demand of 1.
		 * 4 shipments
		 * 1: (5,7)->(6,9)
		 * 2: (5,13)->(6,11)
		 * 3: (15,7)->(14,9)
		 * 4: (15,13)->(14,11)
		 */

        Shipment shipment1 = Shipment.Builder.newInstance("1").addSizeDimension(0, 1).setPickupLocation(TestUtils.loc(Coordinate.newInstance(5, 7))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(6, 9))).build();
        Shipment shipment2 = Shipment.Builder.newInstance("2").addSizeDimension(0, 1).setPickupLocation(TestUtils.loc(Coordinate.newInstance(5, 13))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(6, 11))).build();

        Shipment shipment3 = Shipment.Builder.newInstance("3").addSizeDimension(0, 1).setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 7))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(14, 9))).build();
        Shipment shipment4 = Shipment.Builder.newInstance("4").addSizeDimension(0, 1).setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 13))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(14, 11))).build();

		/*
         * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
        Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5").addSizeDimension(0, 1).setLocation(TestUtils.loc(Coordinate.newInstance(4, 8))).build();
        Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6").addSizeDimension(0, 1).setLocation(TestUtils.loc(Coordinate.newInstance(4, 12))).build();
        Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7").addSizeDimension(0, 1).setLocation(TestUtils.loc(Coordinate.newInstance(16, 8))).build();
        Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8").addSizeDimension(0, 1).setLocation(TestUtils.loc(Coordinate.newInstance(16, 12))).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle);
        vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
            .addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();

        VehicleRoutingProblem vrp = vrpBuilder.build();

        final StateManager stateManager = new StateManager(vrp);


        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addLoadConstraint();
        constraintManager.addTimeWindowConstraint();

        VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager, constraintManager);
        bestIBuilder.setRouteLevel(2, 2);
        @SuppressWarnings("unused")
        InsertionStrategy bestInsertion = bestIBuilder.build();

    }

    @Test
    public void whenHavingOnlyServicesInOneProblem_andInsertionShouldBeMadeOnRouteLevel_itShouldAssertTrue() {
        /* get a vehicle type-builder and build a type with the typeId "vehicleType" and a capacity of 2
         */
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType").addCapacityDimension(0, 2);
        VehicleType vehicleType = vehicleTypeBuilder.build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */
        VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("vehicle");
        vehicleBuilder.setStartLocation(Location.newInstance(10, 10));
        vehicleBuilder.setType(vehicleType);
        VehicleImpl vehicle = vehicleBuilder.build();


		/*
         * build deliveries, (implicitly picked up in the depot)
		 * 1: (4,8)
		 * 2: (4,12)
		 * 3: (16,8)
		 * 4: (16,12)
		 */
        Delivery delivery1 = (Delivery) Delivery.Builder.newInstance("5").addSizeDimension(0, 1).setLocation(Location.newInstance(4, 8)).build();
        Delivery delivery2 = (Delivery) Delivery.Builder.newInstance("6").addSizeDimension(0, 1).setLocation(Location.newInstance(4, 12)).build();
        Delivery delivery3 = (Delivery) Delivery.Builder.newInstance("7").addSizeDimension(0, 1).setLocation(Location.newInstance(16, 8)).build();
        Delivery delivery4 = (Delivery) Delivery.Builder.newInstance("8").addSizeDimension(0, 1).setLocation(Location.newInstance(16, 12)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.addVehicle(vehicle)
//		vrpBuilder.addJob(shipment1).addJob(shipment2).addJob(shipment3).addJob(shipment4)
            .addJob(delivery1).addJob(delivery2).addJob(delivery3).addJob(delivery4).build();

        VehicleRoutingProblem vrp = vrpBuilder.build();

        final StateManager stateManager = new StateManager(vrp);

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addLoadConstraint();
        constraintManager.addTimeWindowConstraint();

        VehicleFleetManager fleetManager = new InfiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        BestInsertionBuilder bestIBuilder = new BestInsertionBuilder(vrp, fleetManager, stateManager, constraintManager);
        bestIBuilder.setRouteLevel(2, 2);
        @SuppressWarnings("unused")
        InsertionStrategy bestInsertion = bestIBuilder.build();

        assertTrue(true);

    }

}
