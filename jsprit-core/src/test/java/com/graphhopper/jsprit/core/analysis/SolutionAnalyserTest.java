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
package com.graphhopper.jsprit.core.analysis;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.ManhattanCosts;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Solution Analyser Test")
class SolutionAnalyserTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    @BeforeEach
    void doBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(-5, 0)).addSkill("skill1").addSkill("skill2").build();
        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(5, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(10, 20)).setLocation(Location.newInstance(-10, 1)).addSizeDimension(0, 2).addRequiredSkill("skill1").build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(-10, 10)).addSizeDimension(0, 3).addRequiredSkill("skill2").addRequiredSkill("skill1").build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1").setPickupLocation(TestUtils.loc(Coordinate.newInstance(-15, 2))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(-16, 5))).addSizeDimension(0, 10).setPickupServiceTime(20.).setDeliveryServiceTime(20.).addRequiredSkill("skill3").build();
        Service s3 = Service.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(10, 20)).setLocation(TestUtils.loc(Coordinate.newInstance(10, 1))).addSizeDimension(0, 2).build();
        Service s4 = Service.Builder.newInstance("s4").setLocation(TestUtils.loc(Coordinate.newInstance(10, 10))).addSizeDimension(0, 3).build();
        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 2))).setPickupServiceTime(20.).setDeliveryServiceTime(20.).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(16, 5))).addSizeDimension(0, 10).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addVehicle(vehicle2).addJob(s1).addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s1).addPickup(shipment1).addDelivery(shipment1).addService(s2).build();
        VehicleRoute route2 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s3).addPickup(shipment2).addDelivery(shipment2).addService(s4).build();
        solution = new VehicleRoutingProblemSolution(Arrays.asList(route1, route2), 42);
    }

    public void buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(-5, 0)).setLatestArrival(150.).build();
        Pickup s1 = Pickup.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(10, 20)).setLocation(Location.newInstance(-10, 1)).addSizeDimension(0, 10).build();
        Delivery s2 = Delivery.Builder.newInstance("s2").setLocation(Location.newInstance(-10, 10)).setTimeWindow(TimeWindow.newInstance(10, 20)).addSizeDimension(0, 20).build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1").setPickupLocation(TestUtils.loc(Coordinate.newInstance(-15, 2))).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(-16, 5))).addSizeDimension(0, 15).setPickupServiceTime(20.).setDeliveryServiceTime(20.).setPickupTimeWindow(TimeWindow.newInstance(10, 20)).setDeliveryTimeWindow(TimeWindow.newInstance(10, 20)).build();
        Pickup s3 = Pickup.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(10, 20)).setLocation(TestUtils.loc(Coordinate.newInstance(10, 1))).addSizeDimension(0, 10).build();
        Delivery s4 = Delivery.Builder.newInstance("s4").setLocation(Location.newInstance(10, 10)).addSizeDimension(0, 20).setTimeWindow(TimeWindow.newInstance(10, 20)).build();
        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 2))).setPickupServiceTime(20.).setDeliveryServiceTime(20.).setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(16, 5))).setPickupTimeWindow(TimeWindow.newInstance(10, 20)).setDeliveryTimeWindow(TimeWindow.newInstance(10, 20)).addSizeDimension(0, 15).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(s1).addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addPickup(s3).addPickup(shipment2).addDelivery(shipment2).addDelivery(s4).addDelivery(s2).addPickup(shipment1).addDelivery(shipment1).addPickup(s1).build();
        solution = new VehicleRoutingProblemSolution(Collections.singletonList(route), 300);
    }

    /**
     * Test the last transport costs at an activity are correct.
     */
    @Test
    @DisplayName("Last Transport Costs Of Route 1 Should Work")
    void lastTransportCostsOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_COST);
    }

    /**
     * Test the last transport distance at an activity are correct.
     */
    @Test
    @DisplayName("Last Transport Distance Of Route 1 Should Work")
    void lastTransportDistanceOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_DISTANCE);
    }

    /**
     * Test the last transport time at an activity are correct.
     */
    @Test
    @DisplayName("Last Transport Time Of Route 1 Should Work")
    void lastTransportTimeOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_TIME);
    }

    /**
     * Test the last transport time at an activity are correct.
     */
    @Test
    @DisplayName("Transport Time At Activity Of Route 1 Should Work")
    void transportTimeAtActivityOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.TRANSPORT_TIME_AT_ACTIVITY);
    }

    private enum TransportCostsTestType {

        LAST_COST, LAST_TIME, LAST_DISTANCE, TRANSPORT_TIME_AT_ACTIVITY
    }

    /**
     * Run multiple different tests for transport costs
     */
    private void testTransportCosts(TransportCostsTestType type) {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        // this should be the path taken by route 1 including depots
        Coordinate[] route1Path = new Coordinate[]{Coordinate.newInstance(-5, 0), Coordinate.newInstance(-10, 1), Coordinate.newInstance(-15, 2), Coordinate.newInstance(-16, 5), Coordinate.newInstance(-10, 10), Coordinate.newInstance(-5, 0)};
        VehicleRoute route1 = solution.getRoutes().iterator().next();
        // get route 1 activities
        List<TourActivity> activities = route1.getActivities();
        assertEquals(activities.size(), 4);
        // utility class to calculate manhattan distance
        @DisplayName("Manhattan Distance")
        class ManhattanDistance {

            private double calc(Coordinate from, Coordinate to) {
                return Math.abs(from.getX() - to.getX()) + Math.abs(from.getY() - to.getY());
            }
        }
        ManhattanDistance md = new ManhattanDistance();
        // loop over all activities on route and do tests
        double totalTime = 0;
        for (int i = 0; i < activities.size(); i++) {
            TourActivity activity = activities.get(i);
            Coordinate last = route1Path[i];
            Coordinate current = route1Path[i + 1];
            // calculate last distance and time (Manhattan uses speed  = 1 so distance = time)
            double dist = md.calc(last, current);
            // test last distance
            if (type == TransportCostsTestType.LAST_DISTANCE) {
                double savedDist = analyser.getLastTransportDistanceAtActivity(activity, route1);
                assertEquals(dist, savedDist, 1E-10);
            }
            // test last time
            if (type == TransportCostsTestType.LAST_TIME) {
                double savedTime = analyser.getLastTransportTimeAtActivity(activity, route1);
                assertEquals(dist, savedTime, 1E-10);
            }
            // test last cost
            if (type == TransportCostsTestType.LAST_COST) {
                double perDistanceUnit = 1;
                Vehicle vehicle = route1.getVehicle();
                if (vehicle != null) {
                    if (vehicle.getType() != null) {
                        perDistanceUnit = vehicle.getType().getVehicleCostParams().perDistanceUnit;
                    }
                }
                double cost = dist * perDistanceUnit;
                double savedCost = analyser.getLastTransportCostAtActivity(activity, route1);
                assertEquals(cost, savedCost, 1E-10);
            }
            // test total transport time at activity
            if (type == TransportCostsTestType.TRANSPORT_TIME_AT_ACTIVITY) {
                totalTime += dist;
                double savedTransportTime = analyser.getTransportTimeAtActivity(activity, route1);
                assertEquals(totalTime, savedTransportTime, 1E-10);
            }
        }
    }

    @Test
    @DisplayName("Construction Should Work")
    void constructionShouldWork() {
        new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertTrue(true);
    }

    @Test
    @DisplayName("Load At Beginning Of Route 1 Should Work")
    void loadAtBeginningOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    @DisplayName("Load At Beginning Of Route 2 Should Work")
    void loadAtBeginningOfRoute2ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();
        assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    @DisplayName("Load At End _ Of Route 1 Should Work")
    void loadAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    @DisplayName("Load At End _ Of Route 2 Should Work")
    void loadAtEnd_OfRoute2ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();
        assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of Start Act Of Route 1 Should Work")
    void loadAfterActivity_ofStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getLoadRightAfterActivity(route.getStart(), route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of Act 1 of Route 1 Should Work")
    void loadAfterActivity_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of Act 2 of Route 1 Should Work")
    void loadAfterActivity_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(12, analyser.getLoadRightAfterActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of Act 3 of Route 1 Should Work")
    void loadAfterActivity_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of Act 4 of Route 1 Should Work")
    void loadAfterActivity_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(5, analyser.getLoadRightAfterActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    @DisplayName("Load After Activity _ of End Act Of Route 1 Should Work")
    void loadAfterActivity_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(5, analyser.getLoadRightAfterActivity(route.getEnd(), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of Start Act Of Route 1 Should Work")
    void loadBeforeActivity_ofStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getLoadJustBeforeActivity(route.getStart(), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of Act 1 of Route 1 Should Work")
    void loadBeforeActivity_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getLoadJustBeforeActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of Act 2 of Route 1 Should Work")
    void loadBeforeActivity_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of Act 3 of Route 1 Should Work")
    void loadBeforeActivity_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(12, analyser.getLoadJustBeforeActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of Act 4 of Route 1 Should Work")
    void loadBeforeActivity_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    @DisplayName("Load Before Activity _ of End Act Of Route 1 Should Work")
    void loadBeforeActivity_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(5, analyser.getLoadJustBeforeActivity(route.getEnd(), route).get(0));
    }

    @Test
    @DisplayName("Max Load _ Of Route 1 Should Work")
    void maxLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(12, analyser.getMaxLoad(route).get(0));
    }

    @Test
    @DisplayName("Pickup Count _ Of Route 1 Should Work")
    void pickupCount_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(3, analyser.getNumberOfPickups(route), 0.01);
    }

    @Test
    @DisplayName("Pickup Count At Beginning _ Of Route 1 Should Work")
    void pickupCountAtBeginning_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getNumberOfPickupsAtBeginning(route), 0.01);
    }

    @Test
    @DisplayName("Pickup Count _ Of Route 1 Of Another Solution Should Work")
    void pickupCount_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(4, analyser.getNumberOfPickups(route), 0.01);
    }

    @Test
    @DisplayName("Pickup Count At Beginning _ Of Route 1 Of Another Solution Should Work")
    void pickupCountAtBeginning_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getNumberOfPickupsAtBeginning(route), 0.01);
    }

    @Test
    @DisplayName("Pickup Count _ on Solution Should Work")
    void pickupCount_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(6, analyser.getNumberOfPickups(), 0.01);
    }

    @Test
    @DisplayName("Pickup Count At Beginning _ on Solution Should Work")
    void pickupCountAtBeginning_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(0, analyser.getNumberOfPickupsAtBeginning(), 0.01);
    }

    @Test
    @DisplayName("Pickup Count _ on Another Solution Should Work")
    void pickupCount_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(4, analyser.getNumberOfPickups(), 0.01);
    }

    @Test
    @DisplayName("Pickup Count At Beginning _ on Another Solution Should Work")
    void pickupCountAtBeginning_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(2, analyser.getNumberOfPickupsAtBeginning(), 0.01);
    }

    @Test
    @DisplayName("Pickup Load _ Of Route 1 Should Work")
    void pickupLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(15, analyser.getLoadPickedUp(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load At Beginning _ Of Route 1 Should Work")
    void pickupLoadAtBeginning_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getLoadAtBeginning(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load _ Of Route 1 Of Another Should Work")
    void pickupLoad_OfRoute1OfAnotherShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(50, analyser.getLoadPickedUp(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load At Beginning _ Of Route 1 Of Another Should Work")
    void pickupLoadAtBeginning_OfRoute1OfAnotherShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(40, analyser.getLoadAtBeginning(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load _ on Solution Should Work")
    void pickupLoad_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(30, analyser.getLoadPickedUp().get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load At Beginning _ on Solution Should Work")
    void pickupLoadAtBeginning_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(0, analyser.getLoadAtBeginning().get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load _ on Another Solution Should Work")
    void pickupLoad_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(50, analyser.getLoadPickedUp().get(0), 0.01);
    }

    @Test
    @DisplayName("Pickup Load At Beginning _ on Another Solution Should Work")
    void pickupLoadAtBeginning_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(40, analyser.getLoadAtBeginning().get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Count _ Of Route 1 Should Work")
    void deliveryCount_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(1, analyser.getNumberOfDeliveries(route), 0.01);
    }

    @Test
    @DisplayName("Delivery Count At End _ Of Route 1 Should Work")
    void deliveryCountAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(route), 0.01);
    }

    @Test
    @DisplayName("Delivery Count _ Of Route 1 Of Another Solution Should Work")
    void deliveryCount_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(4, analyser.getNumberOfDeliveries(route), 0.01);
    }

    @Test
    @DisplayName("Delivery Count At End _ Of Route 1 Of Another Solution Should Work")
    void deliveryCountAtEnd_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(route), 0.01);
    }

    @Test
    @DisplayName("Delivery Count _ on Solution Should Work")
    void deliveryCount_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(2, analyser.getNumberOfDeliveries(), 0.01);
    }

    @Test
    @DisplayName("Delivery Count At End _ on Solution Should Work")
    void deliveryCountAtEnd_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(4, analyser.getNumberOfDeliveriesAtEnd(), 0.01);
    }

    @Test
    @DisplayName("Delivery Count _ on Another Solution Should Work")
    void deliveryCount_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(4, analyser.getNumberOfDeliveries(), 0.01);
    }

    @Test
    @DisplayName("Delivery Count At End _ on Another Solution Should Work")
    void deliveryCountAtEnd_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(), 0.01);
    }

    @Test
    @DisplayName("Delivery Load _ Of Route 1 Should Work")
    void deliveryLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(10, analyser.getLoadDelivered(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load At End _ Of Route 1 Should Work")
    void deliveryLoadAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(5, analyser.getLoadAtEnd(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load _ Of Route 1 Of Another Solution Should Work")
    void deliveryLoad_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(70, analyser.getLoadDelivered(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load At End _ Of Route 1 Of Another Solution Should Work")
    void deliveryLoadAtEnd_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(20, analyser.getLoadAtEnd(route).get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load _ on Solution Should Work")
    void deliveryLoad_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(20, analyser.getLoadDelivered().get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load At End _ on Solution Should Work")
    void deliveryLoadAtEnd_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(10, analyser.getLoadAtEnd().get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load _ on Another Solution Should Work")
    void deliveryLoad_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(70, analyser.getLoadDelivered().get(0), 0.01);
    }

    @Test
    @DisplayName("Delivery Load At End _ on Another Solution Should Work")
    void deliveryLoadAtEnd_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        assertEquals(20, analyser.getLoadAtEnd().get(0), 0.01);
    }

    @Test
    @DisplayName("Operation Time _ Of Route 1 Should Work")
    void operationTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(46. + 40., analyser.getOperationTime(route), 0.01);
    }

    @Test
    @DisplayName("When Setting Start Time _ operation Time Should Work")
    void whenSettingStartTime_operationTimeShouldWork() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v2").setType(type).setEarliestStart(100).setStartLocation(Location.newInstance(5, 0)).build();
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance(20, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service).setRoutingCost(new ManhattanCosts()).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).build();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, new VehicleRoutingProblemSolution(Collections.singletonList(route), 300), vrp.getTransportCosts());
        assertEquals(30, analyser.getOperationTime(), 0.01);
        assertEquals(30, analyser.getOperationTime(route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ Of Route 1 Should Work")
    void waitingTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(4., analyser.getWaitingTime(route), 0.01);
    }

    @Test
    @DisplayName("Transport Time _ Of Route 1 Should Work")
    void transportTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(42., analyser.getTransportTime(route), 0.01);
    }

    @Test
    @DisplayName("Service Time _ Of Route 1 Should Work")
    void serviceTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(40., analyser.getServiceTime(route), 0.01);
    }

    @Test
    @DisplayName("Distance _ Of Route 1 Should Work")
    void distance_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(42., analyser.getDistance(route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ at Start Act Of Route 1 Should Work")
    void waitingTime_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getWaitingTimeAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ of Act 1 of Route 1 Should Work")
    void waitingTime_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(4., analyser.getWaitingTimeAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ of Act 2 of Route 1 Should Work")
    void waitingTime_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ of Act 3 of Route 1 Should Work")
    void waitingTime_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ of Act 4 of Route 1 Should Work")
    void waitingTime_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    @DisplayName("Waiting Time _ of End Act Of Route 1 Should Work")
    void waitingTime_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getWaitingTimeAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ at Start Act Of Route 1 Should Work")
    void distance_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getDistanceAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ of Act 1 of Route 1 Should Work")
    void distance_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(6., analyser.getDistanceAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ of Act 2 of Route 1 Should Work")
    void distance_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(12., analyser.getDistanceAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ of Act 3 of Route 1 Should Work")
    void distance_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(16., analyser.getDistanceAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ of Act 4 of Route 1 Should Work")
    void distance_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(27., analyser.getDistanceAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    @DisplayName("Distance _ of End Act Of Route 1 Should Work")
    void distance_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(42., analyser.getDistanceAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ at Start Act Of Route 1 Should Work")
    void lateArrivalTimes_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getTimeWindowViolationAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ of Act 1 of Route 1 Should Work")
    void lateArrivalTimes_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ of Act 2 of Route 1 Should Work")
    void lateArrivalTimes_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ of Act 3 of Route 1 Should Work")
    void lateArrivalTimes_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ of Act 4 of Route 1 Should Work")
    void lateArrivalTimes_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    @DisplayName("Late Arrival Times _ of End Act Of Route 1 Should Work")
    void lateArrivalTimes_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    @DisplayName("Late Arr Times _ Of Route 1 Should Work")
    void lateArrTimes_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0., analyser.getTimeWindowViolation(route), 0.01);
    }

    @Test
    @DisplayName("Variable Transport Costs _ Of Route 1 Should Work")
    void variableTransportCosts_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(84., analyser.getVariableTransportCosts(route), 0.01);
    }

    @Test
    @DisplayName("Fixed Costs _ Of Route 1 Should Work")
    void fixedCosts_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(100., analyser.getFixedCosts(route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ at Start Act Of Route 1 Should Work")
    void transportCosts_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(0, analyser.getVariableTransportCostsAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ of Act 1 of Route 1 Should Work")
    void transportCosts_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(6. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ of Act 2 of Route 1 Should Work")
    void transportCosts_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(12. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ of Act 3 of Route 1 Should Work")
    void transportCosts_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(16. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ of Act 4 of Route 1 Should Work")
    void transportCosts_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(27. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    @DisplayName("Transport Costs _ of End Act Of Route 1 Should Work")
    void transportCosts_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        assertEquals(42. * 2., analyser.getVariableTransportCostsAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    @DisplayName("Capacity Violation At Beginning _ should Work")
    void capacityViolationAtBeginning_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atBeginning = analyser.getCapacityViolationAtBeginning(route);
        for (int i = 0; i < atBeginning.getNuOfDimensions(); i++) {
            assertEquals(0, atBeginning.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation At End _ should Work")
    void capacityViolationAtEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        for (int i = 0; i < atEnd.getNuOfDimensions(); i++) {
            assertEquals(0, atEnd.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation On Route _ should Work When Violated")
    void capacityViolationOnRoute_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolation(route);
        assertEquals(50, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation At End _ should Work When Violated")
    void capacityViolationAtEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        assertEquals(5, atEnd.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Start _ should Work")
    void capacityViolationAfterStart_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getStart();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation At Beginning _ should Work When Violated")
    void capacityViolationAtBeginning_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAtBeginning(route);
        assertEquals(25, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Start _ should Work When Violated")
    void capacityViolationAfterStart_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getStart(), route);
        assertEquals(25, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 1 _ should Work When Violated")
    void capacityViolationAfterAct1_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(0), route);
        assertEquals(35, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 2 _ should Work When Violated")
    void capacityViolationAfterAct2_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(1), route);
        assertEquals(50, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 3 _ should Work When Violated")
    void capacityViolationAfterAct3_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(2), route);
        assertEquals(35, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 4 _ should Work When Violated")
    void capacityViolationAfterAct4_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(3), route);
        assertEquals(15, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 5 _ should Work When Violated")
    void capacityViolationAfterAct5_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(4), route);
        assertEquals(0, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 6 _ should Work When Violated")
    void capacityViolationAfterAct6_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(5), route);
        assertEquals(10, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 7 _ should Work When Violated")
    void capacityViolationAfterAct7_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(6), route);
        assertEquals(0, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 8 _ should Work When Violated")
    void capacityViolationAfterAct8_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(7), route);
        assertEquals(5, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After End _ should Work When Violated")
    void capacityViolationAfterEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getEnd(), route);
        assertEquals(5, cap.get(0));
    }

    @Test
    @DisplayName("Capacity Violation After Act 1 _ should Work")
    void capacityViolationAfterAct1_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(0);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation After Act 2 _ should Work")
    void capacityViolationAfterAct2_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(1);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation After Act 3 _ should Work")
    void capacityViolationAfterAct3_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(2);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation After Act 4 _ should Work")
    void capacityViolationAfterAct4_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(3);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Capacity Violation After End _ should Work")
    void capacityViolationAfterEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getEnd();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertEquals(0, cap.get(i));
        }
    }

    @Test
    @DisplayName("Time Window Violation _ should Work")
    void timeWindowViolation_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation _ should Work When Violated")
    void timeWindowViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals((2 + 26 + 57 + 77 + 90 + 114 + 144 + 20), violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Start _ should Work When Violated")
    void timeWindowViolationAtStart_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getStart(), route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 1 _ should Work When Violated")
    void timeWindowViolationAtAct1_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(0), route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 2 _ should Work When Violated")
    void timeWindowViolationAtAct2_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(1), route);
        assertEquals(2., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 3 _ should Work When Violated")
    void timeWindowViolationAtAct3_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(2), route);
        assertEquals(26., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 4 _ should Work When Violated")
    void timeWindowViolationAtAct4_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(3), route);
        assertEquals(57., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 5 _ should Work When Violated")
    void timeWindowViolationAtAct5_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(4), route);
        assertEquals(77., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 6 _ should Work When Violated")
    void timeWindowViolationAtAct6_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(5), route);
        assertEquals(90., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 7 _ should Work When Violated")
    void timeWindowViolationAtAct7_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(6), route);
        assertEquals(114., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At Act 8 _ should Work When Violated")
    void timeWindowViolationAtAct8_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(7), route);
        assertEquals(144., violation, 0.01);
    }

    @Test
    @DisplayName("Time Window Violation At End _ should Work When Violated")
    void timeWindowViolationAtEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getEnd(), route);
        assertEquals(20., violation, 0.01);
    }

    @Test
    @DisplayName("Backhaul Violation _ should Work When Violated")
    void backhaulViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Start _ should Work")
    void backhaulViolationAtStart_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 1 _ should Work")
    void backhaulViolationAtAct1_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 2 _ should Work")
    void backhaulViolationAtAct2_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 3 _ should Work")
    void backhaulViolationAtAct3_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 4 _ should Work")
    void backhaulViolationAtAct4_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 5 _ should Work")
    void backhaulViolationAtAct5_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(4), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 6 _ should Work")
    void backhaulViolationAtAct6_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(5), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 7 _ should Work")
    void backhaulViolationAtAct7_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(6), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At Act 8 _ should Work")
    void backhaulViolationAtAct8_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(7), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Backhaul Violation At End _ should Work")
    void backhaulViolationAtEnd_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Start _ should Work")
    void shipmentViolationAtStart_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasShipmentConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 1 _ should Work")
    void shipmentViolationAtAct1_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 2 _ should Work")
    void shipmentViolationAtAct2_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 3 _ should Work")
    void shipmentViolationAtAct3_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 4 _ should Work")
    void shipmentViolationAtAct4_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 5 _ should Work")
    void shipmentViolationAtAct5_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(4), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 6 _ should Work")
    void shipmentViolationAtAct6_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(5), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 7 _ should Work")
    void shipmentViolationAtAct7_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(6), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Act 8 _ should Work")
    void shipmentViolationAtAct8_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(7), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation At End _ should Work")
    void shipmentViolationAtEnd_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation _ should Work")
    void shipmentViolation_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertFalse(violation);
    }

    @Test
    @DisplayName("Shipment Violation _ should Work When Violated")
    void shipmentViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity deliverShipment = route.getActivities().get(2);
        route.getTourActivities().removeActivity(deliverShipment);
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Activity _ should Work When Removing Delivery")
    void shipmentViolationAtActivity_shouldWorkWhenRemovingDelivery() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity deliverShipment = route.getActivities().get(2);
        route.getTourActivities().removeActivity(deliverShipment);
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation _ should Work When Removing Delivery")
    void shipmentViolation_shouldWorkWhenRemovingDelivery() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity deliverShipment = route.getActivities().get(2);
        route.getTourActivities().removeActivity(deliverShipment);
        assertFalse(route.getActivities().contains(deliverShipment));
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation At Activity _ should Work When Removing Pickup")
    void shipmentViolationAtActivity_shouldWorkWhenRemovingPickup() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity pickupShipment = route.getActivities().get(1);
        route.getTourActivities().removeActivity(pickupShipment);
        assertFalse(route.getActivities().contains(pickupShipment));
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation On Route _ should Work When Removing Pickup")
    void shipmentViolationOnRoute_shouldWorkWhenRemovingPickup() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity pickupShipment = route.getActivities().get(1);
        route.getTourActivities().removeActivity(pickupShipment);
        assertFalse(route.getActivities().contains(pickupShipment));
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    @DisplayName("Shipment Violation On Solution _ should Work When Removing Pickup")
    void shipmentViolationOnSolution_shouldWorkWhenRemovingPickup() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity pickupShipment = route.getActivities().get(1);
        route.getTourActivities().removeActivity(pickupShipment);
        assertFalse(route.getActivities().contains(pickupShipment));
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violation = analyser.hasShipmentConstraintViolation();
        assertTrue(violation);
    }

    @Test
    @DisplayName("Skill Violation On Route _ should Work When Violated")
    void skillViolationOnRoute_shouldWorkWhenViolated() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolation(route);
        assertTrue(violated);
    }

    @Test
    @DisplayName("Skill Violation At Start _ should Work")
    void skillViolationAtStart_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation At Act 1 _ should Work")
    void skillViolationAtAct1_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation At Act 2 _ should Work")
    void skillViolationAtAct2_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertTrue(violated);
    }

    @Test
    @DisplayName("Skill Violation At Act 3 _ should Work")
    void skillViolationAtAct3_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertTrue(violated);
    }

    @Test
    @DisplayName("Skill Violation At Act 4 _ should Work")
    void skillViolationAtAct4_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation At End _ should Work")
    void skillViolationAtEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation On Route _ should Work When Not Violated")
    void skillViolationOnRoute_shouldWorkWhenNotViolated() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();
        Boolean violated = analyser.hasSkillConstraintViolation(route);
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation On Solution _ should Work")
    void skillViolationOnSolution_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasSkillConstraintViolation();
        assertTrue(violated);
    }

    @Test
    @DisplayName("Backhaul Violation On Solution _ should Work")
    void backhaulViolationOnSolution_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasBackhaulConstraintViolation();
        assertFalse(violated);
    }

    @Test
    @DisplayName("Backhaul Violation On Solution _ should Work When Violated")
    void backhaulViolationOnSolution_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasBackhaulConstraintViolation();
        assertTrue(violated);
    }

    @Test
    @DisplayName("Shipment Violation On Solution _ should Work")
    void shipmentViolationOnSolution_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasShipmentConstraintViolation();
        assertFalse(violated);
    }

    @Test
    @DisplayName("Skill Violation On Solution _ should Work When Not Violated")
    void skillViolationOnSolution_shouldWorkWhenNotViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasSkillConstraintViolation();
        assertFalse(violated);
    }

    @Test
    @DisplayName("Should Work With Route Without Activities")
    void shouldWorkWithRouteWithoutActivities() {
        try {
            Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance(0, 0)).setEndLocation(Location.newInstance(10, 0)).build();
            VehicleRoute vehicleRoute = VehicleRoute.Builder.newInstance(vehicle).build();
            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).build();
            VehicleRoutingProblemSolution solution = new VehicleRoutingProblemSolution(Collections.singletonList(vehicleRoute), 0);
            new SolutionAnalyser(vrp, solution, (from, to, departureTime, vehicle1) -> 100);
            assertTrue(true);
        } catch (Exception e) {
            fail();
        }
    }
}
