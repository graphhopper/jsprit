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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.VehicleDependentTraveledDistance;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by schroeder on 18/05/16.
 */
@DisplayName("Vehicle Dependent Traveled Distance Test")
class VehicleDependentTraveledDistanceTest {

    StateManager stateManager;

    VehicleRoute route;

    StateId traveledDistanceId;

    Vehicle vehicle;

    Vehicle vehicle2;

    VehicleRoutingProblem vrp;

    Delivery d1, d2, newDelivery;

    Pickup pickup;

    Shipment s1;

    Map<Vehicle, Double> maxDistanceMap;

    @BeforeEach
    void doBefore() {
        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(10, 10)).build();
        maxDistanceMap = new HashMap<>();
        maxDistanceMap.put(vehicle, 200d);
        maxDistanceMap.put(vehicle2, 200d);
        d1 = Delivery.Builder.newInstance("d1").setLocation(Location.newInstance(10, 10)).build();
        d2 = Delivery.Builder.newInstance("d2").setLocation(Location.newInstance(20, 15)).build();
        pickup = Pickup.Builder.newInstance("pickup").setLocation(Location.newInstance(50, 50)).build();
        s1 = Shipment.Builder.newInstance("s1").setPickupLocation(Location.newInstance(35, 30)).setDeliveryLocation(Location.newInstance(20, 25)).build();
        newDelivery = Delivery.Builder.newInstance("new").setLocation(Location.newInstance(-10, 10)).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().setRoutingCost(new ManhattanCosts()).addVehicle(vehicle).addVehicle(vehicle2).addJob(d1).addJob(d2).addJob(s1).addJob(pickup).addJob(newDelivery).build();
        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addDelivery(d1).addDelivery(d2).addPickup(s1).addPickup(pickup).addDelivery(s1).build();
        stateManager = new StateManager(vrp);
        traveledDistanceId = stateManager.createStateId("traveledDistance");
        VehicleDependentTraveledDistance traveledDistance = new VehicleDependentTraveledDistance(vrp.getTransportCosts(), stateManager, traveledDistanceId, Arrays.asList(vehicle, vehicle2));
        stateManager.addStateUpdater(traveledDistance);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    @DisplayName("When End Location Is Set _ constraint Should Work")
    void whenEndLocationIsSet_constraintShouldWork() {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).setEndLocation(Location.newInstance(10, 0)).build();
        Pickup pickup = Pickup.Builder.newInstance("pickup").setLocation(Location.newInstance(10, 0)).build();
        vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(pickup).build();
        route = VehicleRoute.emptyRoute();
        maxDistanceMap = new HashMap<>();
        maxDistanceMap.put(vehicle, 5d);
        MaxDistanceConstraint maxDistanceConstraint = new MaxDistanceConstraint(new StateManager(vrp), traveledDistanceId, vrp.getTransportCosts(), maxDistanceMap);
        JobInsertionContext context = new JobInsertionContext(route, pickup, vehicle, null, 0);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, new Start(vehicle.getStartLocation(), 0, Double.MAX_VALUE), vrp.getActivities(pickup).get(0), new End(vehicle.getEndLocation(), 0, Double.MAX_VALUE), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
    }

    /*
    vehicle: 200.0
vehicle (max distance): 200.0
vehicle2: 160.0
vehicle2 (max distance): 180.0
     */
    @Test
    @DisplayName("Insert New In Vehicle Should Fail")
    void insertNewInVehicleShouldFail() {
        MaxDistanceConstraint maxDistanceConstraint = new MaxDistanceConstraint(stateManager, traveledDistanceId, vrp.getTransportCosts(), maxDistanceMap);
        JobInsertionContext context = new JobInsertionContext(route, newDelivery, vehicle, null, 0);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, route.getStart(), newAct(), act(0), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(0), newAct(), act(1), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(1), newAct(), act(2), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(2), newAct(), act(3), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(3), newAct(), act(4), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(4), newAct(), route.getEnd(), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
    }

    @Test
    @DisplayName("Insert New In Vehicle 2 Should Be Correct")
    void insertNewInVehicle2ShouldBeCorrect() {
        // current distance vehicle2: 160 allowed: 200
        MaxDistanceConstraint maxDistanceConstraint = new MaxDistanceConstraint(stateManager, traveledDistanceId, vrp.getTransportCosts(), maxDistanceMap);
        JobInsertionContext context = new JobInsertionContext(route, newDelivery, vehicle2, null, 0);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, route.getStart(), newAct(), act(0), 0).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
        // additional distance: 20+35-15=40
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(0), newAct(), act(1), 0).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
        // additional distance: 35+65-30=70
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(1), newAct(), act(2), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        // additional distance: 65+100-35
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(2), newAct(), act(3), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        // additional distance: 100+45-55
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(3), newAct(), act(4), 0).equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED));
        // additional distance: 45+20-25
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, act(4), newAct(), route.getEnd(), 0).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
    }

    private TourActivity act(int i) {
        return route.getActivities().get(i);
    }

    private TourActivity newAct() {
        return vrp.getActivities(newDelivery).get(0);
    }

    @Test
    @DisplayName("Traveled Distance Should Be Correct")
    void traveledDistanceShouldBeCorrect() {
        Assertions.assertEquals(20d, stateManager.getActivityState(route.getActivities().get(0), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(35d, stateManager.getActivityState(route.getActivities().get(1), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(65d, stateManager.getActivityState(route.getActivities().get(2), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(100d, stateManager.getActivityState(route.getActivities().get(3), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(155d, stateManager.getActivityState(route.getActivities().get(4), vehicle, traveledDistanceId, Double.class), 0.01);
    }

    @Test
    @DisplayName("Traveled Distance With Vehicle 2 Should Be Correct")
    void traveledDistanceWithVehicle2ShouldBeCorrect() {
        Assertions.assertEquals(0d, stateManager.getActivityState(route.getActivities().get(0), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(15d, stateManager.getActivityState(route.getActivities().get(1), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(45d, stateManager.getActivityState(route.getActivities().get(2), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(80d, stateManager.getActivityState(route.getActivities().get(3), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(135d, stateManager.getActivityState(route.getActivities().get(4), vehicle2, traveledDistanceId, Double.class), 0.01);
    }

    @Test
    @DisplayName("Distance Of Shipment In Route")
    void distanceOfShipmentInRoute() {
        double traveledDistanceBeforePickup = stateManager.getActivityState(route.getActivities().get(2), vehicle, traveledDistanceId, Double.class);
        double traveledDistanceBeforeDelivery = stateManager.getActivityState(route.getActivities().get(4), vehicle, traveledDistanceId, Double.class);
        Assertions.assertEquals(90d, traveledDistanceBeforeDelivery - traveledDistanceBeforePickup, 0.01);
    }

    @Test
    @DisplayName("Distance Of Shipment In Route Vehicle 2")
    void distanceOfShipmentInRouteVehicle2() {
        double traveledDistanceBeforePickup = stateManager.getActivityState(route.getActivities().get(2), vehicle2, traveledDistanceId, Double.class);
        double traveledDistanceBeforeDelivery = stateManager.getActivityState(route.getActivities().get(4), vehicle2, traveledDistanceId, Double.class);
        Assertions.assertEquals(90d, traveledDistanceBeforeDelivery - traveledDistanceBeforePickup, 0.01);
    }

    @Test
    @DisplayName("Distance Of Pickup In Route")
    void distanceOfPickupInRoute() {
        double traveledDistanceBeforePickup = stateManager.getActivityState(route.getActivities().get(3), vehicle, traveledDistanceId, Double.class);
        double total = stateManager.getRouteState(route, vehicle, traveledDistanceId, Double.class);
        Assertions.assertEquals(100d, total - traveledDistanceBeforePickup, 0.01);
    }

    @Test
    @DisplayName("Distance Of Pickup In Route Vehicle 2")
    void distanceOfPickupInRouteVehicle2() {
        double traveledDistanceBeforePickup = stateManager.getActivityState(route.getActivities().get(3), vehicle2, traveledDistanceId, Double.class);
        double total = stateManager.getRouteState(route, vehicle2, traveledDistanceId, Double.class);
        Assertions.assertEquals(80d, total - traveledDistanceBeforePickup, 0.01);
    }

    @Test
    @DisplayName("Distance To Travel Should Be Correct")
    void distanceToTravelShouldBeCorrect() {
        double total = stateManager.getRouteState(route, vehicle, traveledDistanceId, Double.class);
        Assertions.assertEquals(180d, total - stateManager.getActivityState(route.getActivities().get(0), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(165d, total - stateManager.getActivityState(route.getActivities().get(1), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(135d, total - stateManager.getActivityState(route.getActivities().get(2), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(100d, total - stateManager.getActivityState(route.getActivities().get(3), vehicle, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(45d, total - stateManager.getActivityState(route.getActivities().get(4), vehicle, traveledDistanceId, Double.class), 0.01);
    }

    @Test
    @DisplayName("Distance To Travel Should Be Correct Vehicle 2")
    void distanceToTravelShouldBeCorrectVehicle2() {
        double total = stateManager.getRouteState(route, vehicle2, traveledDistanceId, Double.class);
        Assertions.assertEquals(160d, total - stateManager.getActivityState(route.getActivities().get(0), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(145d, total - stateManager.getActivityState(route.getActivities().get(1), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(115d, total - stateManager.getActivityState(route.getActivities().get(2), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(80d, total - stateManager.getActivityState(route.getActivities().get(3), vehicle2, traveledDistanceId, Double.class), 0.01);
        Assertions.assertEquals(25d, total - stateManager.getActivityState(route.getActivities().get(4), vehicle2, traveledDistanceId, Double.class), 0.01);
    }

    @Test
    @DisplayName("When Adding Deliver Shipment _ constraint Should Work")
    void whenAddingDeliverShipment_constraintShouldWork() {
        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance(0, 3)).setDeliveryLocation(Location.newInstance(4, 0)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext context = new JobInsertionContext(route, shipment, vehicle, null, 0);
        context.getAssociatedActivities().add(vrp.getActivities(shipment).get(0));
        context.getAssociatedActivities().add(vrp.getActivities(shipment).get(1));
        maxDistanceMap = new HashMap<>();
        maxDistanceMap.put(vehicle, 12d);
        StateManager stateManager = new StateManager(vrp);
        MaxDistanceConstraint maxDistanceConstraint = new MaxDistanceConstraint(stateManager, traveledDistanceId, vrp.getTransportCosts(), maxDistanceMap);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, new Start(vehicle.getStartLocation(), 0, Double.MAX_VALUE), vrp.getActivities(shipment).get(0), new End(vehicle.getEndLocation(), 0, Double.MAX_VALUE), 0).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
        ActivityContext pickupContext = new ActivityContext();
        pickupContext.setArrivalTime(3);
        pickupContext.setEndTime(3);
        pickupContext.setInsertionIndex(0);
        context.setRelatedActivityContext(pickupContext);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, vrp.getActivities(shipment).get(0), vrp.getActivities(shipment).get(1), new End(vehicle.getEndLocation(), 0, Double.MAX_VALUE), 3).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
    }

    @Test
    @DisplayName("When Adding Deliver Shipment With Veh Diff Start End Locs _ constraint Should Work")
    void whenAddingDeliverShipmentWithVehDiffStartEndLocs_constraintShouldWork() {
        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(Location.newInstance(0, 1)).setDeliveryLocation(Location.newInstance(4, 1)).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).setEndLocation(Location.newInstance(0, 4)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext context = new JobInsertionContext(route, shipment, vehicle, null, 0);
        context.getAssociatedActivities().add(vrp.getActivities(shipment).get(0));
        context.getAssociatedActivities().add(vrp.getActivities(shipment).get(1));
        maxDistanceMap = new HashMap<>();
        maxDistanceMap.put(vehicle, 10d);
        StateManager stateManager = new StateManager(vrp);
        MaxDistanceConstraint maxDistanceConstraint = new MaxDistanceConstraint(stateManager, traveledDistanceId, vrp.getTransportCosts(), maxDistanceMap);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, new Start(vehicle.getStartLocation(), 0, Double.MAX_VALUE), vrp.getActivities(shipment).get(0), new End(vehicle.getEndLocation(), 0, Double.MAX_VALUE), 0).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
        ActivityContext pickupContext = new ActivityContext();
        pickupContext.setArrivalTime(1);
        pickupContext.setEndTime(1);
        pickupContext.setInsertionIndex(0);
        context.setRelatedActivityContext(pickupContext);
        Assertions.assertTrue(maxDistanceConstraint.fulfilled(context, vrp.getActivities(shipment).get(0), vrp.getActivities(shipment).get(1), new End(vehicle.getEndLocation(), 0, Double.MAX_VALUE), 1).equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
    }
}
