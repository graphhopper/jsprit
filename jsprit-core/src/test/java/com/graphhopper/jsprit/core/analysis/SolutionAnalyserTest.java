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
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class SolutionAnalyserTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;


    @Before
    public void doBefore() {

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type)
            .setStartLocation(Location.newInstance(-5, 0))
            .addSkill("skill1").addSkill("skill2")
            .build();

        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setType(type)
            .setStartLocation(Location.newInstance(5, 0)).build();

        Service s1 = Service.Builder.newInstance("s1")
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .setLocation(Location.newInstance(-10, 1)).addSizeDimension(0, 2)
            .addRequiredSkill("skill1")
            .build();
        Service s2 = Service.Builder.newInstance("s2")
            .setLocation(Location.newInstance(-10, 10))
            .addSizeDimension(0, 3)
            .addRequiredSkill("skill2").addRequiredSkill("skill1")
            .build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1")
            .setPickupLocation(TestUtils.loc(Coordinate.newInstance(-15, 2)))
            .setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(-16, 5)))
            .addSizeDimension(0, 10)
            .setPickupServiceTime(20.)
            .setDeliveryServiceTime(20.)
            .addRequiredSkill("skill3")
            .build();

        Service s3 = Service.Builder.newInstance("s3")
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .setLocation(TestUtils.loc(Coordinate.newInstance(10, 1))).addSizeDimension(0, 2).build();

        Service s4 = Service.Builder.newInstance("s4").setLocation(TestUtils.loc(Coordinate.newInstance(10, 10))).addSizeDimension(0, 3).build();

        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 2)))
            .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
            .setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(16, 5))).addSizeDimension(0, 10).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle)
            .addVehicle(vehicle2)
            .addJob(s1)
            .addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();

        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addService(s1).addPickup(shipment1).addDelivery(shipment1).addService(s2).build();

        VehicleRoute route2 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addService(s3).addPickup(shipment2).addDelivery(shipment2).addService(s4).build();

        solution = new VehicleRoutingProblemSolution(Arrays.asList(route1, route2), 42);
    }


    public void buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type)
            .setStartLocation(Location.newInstance(-5, 0))
            .setLatestArrival(150.)
            .build();

        Pickup s1 = Pickup.Builder.newInstance("s1")
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .setLocation(Location.newInstance(-10, 1))
            .addSizeDimension(0, 10)
            .build();
        Delivery s2 = Delivery.Builder.newInstance("s2")
            .setLocation(Location.newInstance(-10, 10))
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .addSizeDimension(0, 20)
            .build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1").setPickupLocation(TestUtils.loc(Coordinate.newInstance(-15, 2)))
            .setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(-16, 5)))
            .addSizeDimension(0, 15)
            .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
            .setPickupTimeWindow(TimeWindow.newInstance(10, 20)).setDeliveryTimeWindow(TimeWindow.newInstance(10, 20))
            .build();

        Pickup s3 = Pickup.Builder.newInstance("s3")
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .setLocation(TestUtils.loc(Coordinate.newInstance(10, 1)))
            .addSizeDimension(0, 10)
            .build();
        Delivery s4 = Delivery.Builder.newInstance("s4").setLocation(Location.newInstance(10, 10))
            .addSizeDimension(0, 20)
            .setTimeWindow(TimeWindow.newInstance(10, 20))
            .build();
        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupLocation(TestUtils.loc(Coordinate.newInstance(15, 2)))
            .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
            .setDeliveryLocation(TestUtils.loc(Coordinate.newInstance(16, 5)))
            .setPickupTimeWindow(TimeWindow.newInstance(10, 20)).setDeliveryTimeWindow(TimeWindow.newInstance(10, 20))
            .addSizeDimension(0, 15).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle)
            .addJob(s1)
            .addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
            .addPickup(s3)
            .addPickup(shipment2).addDelivery(shipment2)
            .addDelivery(s4)
            .addDelivery(s2)
            .addPickup(shipment1).addDelivery(shipment1)
            .addPickup(s1)
            .build();

        solution = new VehicleRoutingProblemSolution(Arrays.asList(route), 300);
    }

    /**
     * Test the last transport costs at an activity are correct.
     */
    @Test
    public void lastTransportCostsOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_COST);
    }

    /**
     * Test the last transport distance at an activity are correct.
     */
    @Test
    public void lastTransportDistanceOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_DISTANCE);
    }


    /**
     * Test the last transport time at an activity are correct.
     */
    @Test
    public void lastTransportTimeOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.LAST_TIME);
    }

    /**
     * Test the last transport time at an activity are correct.
     */
    @Test
    public void transportTimeAtActivityOfRoute1ShouldWork() {
        testTransportCosts(TransportCostsTestType.TRANSPORT_TIME_AT_ACTIVITY);
    }

    private enum TransportCostsTestType {
        LAST_COST,
        LAST_TIME,
        LAST_DISTANCE,
        TRANSPORT_TIME_AT_ACTIVITY,
    }

    /**
     * Run multiple different tests for transport costs
     *
     * @param type
     */
    private void testTransportCosts(TransportCostsTestType type) {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        // this should be the path taken by route 1 including depots
        Coordinate[] route1Path = new Coordinate[]{
            Coordinate.newInstance(-5, 0),
            Coordinate.newInstance(-10, 1),
            Coordinate.newInstance(-15, 2),
            Coordinate.newInstance(-16, 5),
            Coordinate.newInstance(-10, 10),
            Coordinate.newInstance(-5, 0)

        };

        VehicleRoute route1 = solution.getRoutes().iterator().next();

        // get route 1 activities
        List<TourActivity> activities = route1.getActivities();
        Assert.assertEquals(activities.size(), 4);

        // utility class to calculate manhattan distance
        class ManhattanDistance {
            private double calc(Coordinate from, Coordinate to) {
                return Math.abs(from.getX() - to.getX())
                    + Math.abs(from.getY() - to.getY());
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
            double time = dist;

            // test last distance
            if (type == TransportCostsTestType.LAST_DISTANCE) {
                double savedDist = analyser.getLastTransportDistanceAtActivity(activity, route1);
                Assert.assertEquals(dist, savedDist, 1E-10);
            }

            // test last time
            if (type == TransportCostsTestType.LAST_TIME) {
                double savedTime = analyser.getLastTransportTimeAtActivity(activity, route1);
                Assert.assertEquals(time, savedTime, 1E-10);
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
                Assert.assertEquals(cost, savedCost, 1E-10);
            }

            // test total transport time at activity
            if (type == TransportCostsTestType.TRANSPORT_TIME_AT_ACTIVITY) {
                totalTime += time;
                double savedTransportTime = analyser.getTransportTimeAtActivity(activity, route1);
                Assert.assertEquals(totalTime, savedTransportTime, 1E-10);
            }
        }
    }

    @Test
    public void constructionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertTrue(true);
    }

    @Test
    public void loadAtBeginningOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    public void loadAtBeginningOfRoute2ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();

        Assert.assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    public void loadAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    public void loadAtEnd_OfRoute2ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();

        Assert.assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    public void loadAfterActivity_ofStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadRightAfterActivity(route.getStart(), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getLoadRightAfterActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadRightAfterActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadRightAfterActivity(route.getEnd(), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadJustBeforeActivity(route.getStart(), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadJustBeforeActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getLoadJustBeforeActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadJustBeforeActivity(route.getEnd(), route).get(0));
    }

    @Test
    public void maxLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getMaxLoad(route).get(0));
    }

    @Test
    public void pickupCount_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(3, analyser.getNumberOfPickups(route), 0.01);
    }

    @Test
    public void pickupCountAtBeginning_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getNumberOfPickupsAtBeginning(route), 0.01);
    }

    @Test
    public void pickupCount_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4, analyser.getNumberOfPickups(route), 0.01);
    }

    @Test
    public void pickupCountAtBeginning_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getNumberOfPickupsAtBeginning(route), 0.01);
    }

    @Test
    public void pickupCount_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(6, analyser.getNumberOfPickups(), 0.01);
    }

    @Test
    public void pickupCountAtBeginning_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(0, analyser.getNumberOfPickupsAtBeginning(), 0.01);
    }

    @Test
    public void pickupCount_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(4, analyser.getNumberOfPickups(), 0.01);
    }

    @Test
    public void pickupCountAtBeginning_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(2, analyser.getNumberOfPickupsAtBeginning(), 0.01);
    }

    @Test
    public void pickupLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(15, analyser.getLoadPickedUp(route).get(0), 0.01);
    }

    @Test
    public void pickupLoadAtBeginning_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadAtBeginning(route).get(0), 0.01);
    }

    @Test
    public void pickupLoad_OfRoute1OfAnotherShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(50, analyser.getLoadPickedUp(route).get(0), 0.01);
    }

    @Test
    public void pickupLoadAtBeginning_OfRoute1OfAnotherShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(40, analyser.getLoadAtBeginning(route).get(0), 0.01);
    }

    @Test
    public void pickupLoad_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(30, analyser.getLoadPickedUp().get(0), 0.01);
    }

    @Test
    public void pickupLoadAtBeginning_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(0, analyser.getLoadAtBeginning().get(0), 0.01);
    }

    @Test
    public void pickupLoad_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(50, analyser.getLoadPickedUp().get(0), 0.01);
    }

    @Test
    public void pickupLoadAtBeginning_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(40, analyser.getLoadAtBeginning().get(0), 0.01);
    }

    @Test
    public void deliveryCount_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(1, analyser.getNumberOfDeliveries(route), 0.01);
    }

    @Test
    public void deliveryCountAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(route), 0.01);
    }

    @Test
    public void deliveryCount_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4, analyser.getNumberOfDeliveries(route), 0.01);
    }

    @Test
    public void deliveryCountAtEnd_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(route), 0.01);
    }

    @Test
    public void deliveryCount_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(2, analyser.getNumberOfDeliveries(), 0.01);
    }

    @Test
    public void deliveryCountAtEnd_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(4, analyser.getNumberOfDeliveriesAtEnd(), 0.01);
    }

    @Test
    public void deliveryCount_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(4, analyser.getNumberOfDeliveries(), 0.01);
    }

    @Test
    public void deliveryCountAtEnd_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(2, analyser.getNumberOfDeliveriesAtEnd(), 0.01);
    }

    @Test
    public void deliveryLoad_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(10, analyser.getLoadDelivered(route).get(0), 0.01);
    }

    @Test
    public void deliveryLoadAtEnd_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadAtEnd(route).get(0), 0.01);
    }

    @Test
    public void deliveryLoad_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(70, analyser.getLoadDelivered(route).get(0), 0.01);
    }

    @Test
    public void deliveryLoadAtEnd_OfRoute1OfAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(20, analyser.getLoadAtEnd(route).get(0), 0.01);
    }

    @Test
    public void deliveryLoad_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(20, analyser.getLoadDelivered().get(0), 0.01);
    }

    @Test
    public void deliveryLoadAtEnd_onSolutionShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(10, analyser.getLoadAtEnd().get(0), 0.01);
    }

    @Test
    public void deliveryLoad_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(70, analyser.getLoadDelivered().get(0), 0.01);
    }

    @Test
    public void deliveryLoadAtEnd_onAnotherSolutionShouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Assert.assertEquals(20, analyser.getLoadAtEnd().get(0), 0.01);
    }

    @Test
    public void operationTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(46. + 40., analyser.getOperationTime(route), 0.01);
    }

    @Test
    public void waitingTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4., analyser.getWaitingTime(route), 0.01);
    }

    @Test
    public void transportTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getTransportTime(route), 0.01);
    }

    @Test
    public void serviceTime_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(40., analyser.getServiceTime(route), 0.01);
    }

    @Test
    public void distance_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getDistance(route), 0.01);
    }

    @Test
    public void waitingTime_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getWaitingTimeAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4., analyser.getWaitingTimeAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void waitingTime_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void distance_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getDistanceAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void distance_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(6., analyser.getDistanceAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void distance_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12., analyser.getDistanceAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void distance_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(16., analyser.getDistanceAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void distance_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(27., analyser.getDistanceAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void distance_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getDistanceAtActivity(route.getEnd(), route), 0.01);
    }


    @Test
    public void lateArrivalTimes_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getTimeWindowViolationAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolationAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void lateArrTimes_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getTimeWindowViolation(route), 0.01);
    }

    @Test
    public void variableTransportCosts_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(84., analyser.getVariableTransportCosts(route), 0.01);
    }

    @Test
    public void fixedCosts_OfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(100., analyser.getFixedCosts(route), 0.01);
    }

    @Test
    public void transportCosts_atStartActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getVariableTransportCostsAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct1ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(6. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct2ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct3ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(16. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct4ofRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(27. * 2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void transportCosts_ofEndActOfRoute1ShouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42. * 2., analyser.getVariableTransportCostsAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void capacityViolationAtBeginning_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atBeginning = analyser.getCapacityViolationAtBeginning(route);
        for (int i = 0; i < atBeginning.getNuOfDimensions(); i++) {
            assertTrue(atBeginning.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAtEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        for (int i = 0; i < atEnd.getNuOfDimensions(); i++) {
            assertTrue(atEnd.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationOnRoute_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolation(route);
        assertEquals(50, cap.get(0));
    }

    @Test
    public void capacityViolationAtEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        assertEquals(5, atEnd.get(0));
    }

    @Test
    public void capacityViolationAfterStart_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getStart();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }


    @Test
    public void capacityViolationAtBeginning_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAtBeginning(route);
        assertEquals(25, cap.get(0));
    }


    @Test
    public void capacityViolationAfterStart_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getStart(), route);
        assertEquals(25, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct1_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(0), route);
        assertEquals(35, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct2_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(1), route);
        assertEquals(50, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct3_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(2), route);
        assertEquals(35, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct4_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(3), route);
        assertEquals(15, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct5_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(4), route);
        assertEquals(0, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct6_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(5), route);
        assertEquals(10, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct7_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(6), route);
        assertEquals(0, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct8_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(7), route);
        assertEquals(5, cap.get(0));
    }

    @Test
    public void capacityViolationAfterEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getEnd(), route);
        assertEquals(5, cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct1_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(0);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct2_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(1);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct3_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(2);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct4_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(3);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getEnd();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act, route);
        for (int i = 0; i < cap.getNuOfDimensions(); i++) {
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void timeWindowViolation_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    public void timeWindowViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals((2 + 26 + 57 + 77 + 90 + 114 + 144 + 20), violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtStart_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getStart(), route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct1_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(0), route);
        assertEquals(0., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct2_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(1), route);
        assertEquals(2., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct3_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(2), route);
        assertEquals(26., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct4_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(3), route);
        assertEquals(57., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct5_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(4), route);
        assertEquals(77., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct6_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(5), route);
        assertEquals(90., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct7_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(6), route);
        assertEquals(114., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtAct8_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(7), route);
        assertEquals(144., violation, 0.01);
    }

    @Test
    public void timeWindowViolationAtEnd_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getEnd(), route);
        assertEquals(20., violation, 0.01);
    }

    @Test
    public void backhaulViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    public void backhaulViolationAtStart_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct1_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();


        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct2_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct3_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct4_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertTrue(violation);
    }

    @Test
    public void backhaulViolationAtAct5_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(4), route);
        assertTrue(violation);
    }

    @Test
    public void backhaulViolationAtAct6_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(5), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct7_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(6), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtAct8_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(7), route);
        assertFalse(violation);
    }

    @Test
    public void backhaulViolationAtEnd_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtStart_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasShipmentConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct1_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct2_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct3_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct4_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertTrue(violation);
    }

    @Test
    public void shipmentViolationAtAct5_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(4), route);
        assertTrue(violation);
    }

    @Test
    public void shipmentViolationAtAct6_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(5), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct7_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(6), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtAct8_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getActivities().get(7), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolationAtEnd_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasBackhaulConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolation_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertFalse(violation);
    }

    @Test
    public void shipmentViolation_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity deliverShipment = route.getActivities().get(2);
        route.getTourActivities().removeActivity(deliverShipment);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        Boolean violation = analyser.hasShipmentConstraintViolation(route);
        assertTrue(violation);
    }

    @Test
    public void shipmentViolationAtActivity_shouldWorkWhenRemovingDelivery() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity deliverShipment = route.getActivities().get(2);
        route.getTourActivities().removeActivity(deliverShipment);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        Boolean violation = analyser.hasShipmentConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertTrue(violation);
    }

    @Test
    public void shipmentViolation_shouldWorkWhenRemovingDelivery() {
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
    public void shipmentViolationAtActivity_shouldWorkWhenRemovingPickup() {
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
    public void shipmentViolationOnRoute_shouldWorkWhenRemovingPickup() {
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
    public void shipmentViolationOnSolution_shouldWorkWhenRemovingPickup() {
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
    public void skillViolationOnRoute_shouldWorkWhenViolated() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolation(route);
        assertTrue(violated);
    }

    @Test
    public void skillViolationAtStart_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getStart(), route);
        assertFalse(violated);
    }

    @Test
    public void skillViolationAtAct1_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(0), route);
        assertFalse(violated);
    }

    @Test
    public void skillViolationAtAct2_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(1), route);
        assertTrue(violated);
    }

    @Test
    public void skillViolationAtAct3_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(2), route);
        assertTrue(violated);
    }

    @Test
    public void skillViolationAtAct4_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getActivities().get(3), route);
        assertFalse(violated);
    }

    @Test
    public void skillViolationAtEnd_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        VehicleRoute route = solution.getRoutes().iterator().next();
        Boolean violated = analyser.hasSkillConstraintViolationAtActivity(route.getEnd(), route);
        assertFalse(violated);
    }


    @Test
    public void skillViolationOnRoute_shouldWorkWhenNotViolated() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());

        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();
        Boolean violated = analyser.hasSkillConstraintViolation(route);
        assertFalse(violated);
    }

    @Test
    public void skillViolationOnSolution_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasSkillConstraintViolation();
        assertTrue(violated);
    }

    @Test
    public void backhaulViolationOnSolution_shouldWork() {
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasBackhaulConstraintViolation();
        assertFalse(violated);
    }

    @Test
    public void backhaulViolationOnSolution_shouldWorkWhenViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasBackhaulConstraintViolation();
        assertTrue(violated);
    }

    @Test
    public void shipmentViolationOnSolution_shouldWork() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasShipmentConstraintViolation();
        assertFalse(violated);
    }

    @Test
    public void skillViolationOnSolution_shouldWorkWhenNotViolated() {
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionAnalyser analyser = new SolutionAnalyser(vrp, solution, vrp.getTransportCosts());
        Boolean violated = analyser.hasSkillConstraintViolation();
        assertFalse(violated);
    }

}
