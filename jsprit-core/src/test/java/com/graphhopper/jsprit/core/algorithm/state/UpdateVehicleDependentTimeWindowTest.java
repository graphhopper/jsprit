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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * unit tests to test vehicle dependent time window updater
 */
public class UpdateVehicleDependentTimeWindowTest {

    private StateManager stateManager;

    private VehicleRoute route;

    private VehicleImpl vehicle;

    private VehicleImpl vehicle2;

    private VehicleImpl vehicle3;

    private VehicleImpl equivalentOf3;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private VehicleFleetManager fleetManager;

    private VehicleRoutingProblem vrp;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        routingCosts = CostFactory.createEuclideanCosts();
        activityCosts = new WaitingTimeCosts();
        vrpBuilder.setRoutingCost(routingCosts);

        vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        vehicle2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0.).setLatestArrival(60.).build();

        vehicle3 = VehicleImpl.Builder.newInstance("v3").setStartLocation(Location.newInstance("40,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        equivalentOf3 = VehicleImpl.Builder.newInstance("v4").setStartLocation(Location.newInstance("40,0")).setEarliestStart(0.).setLatestArrival(100.).build();

        vrpBuilder.addVehicle(vehicle).addVehicle(vehicle2).addVehicle(vehicle3).addVehicle(equivalentOf3);

        Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
        vehicles.add(vehicle);
        vehicles.add(vehicle2);
        vehicles.add(vehicle3);


        fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();

        Service service = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).build();

        vrpBuilder.addJob(service).addJob(service2).addJob(service3);
        vrp = vrpBuilder.build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        }).addService(service).addService(service2).addService(service3).build();


        stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, routingCosts, activityCosts);
        updater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

            @Override
            public Collection<Vehicle> get(VehicleRoute route) {
                Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
                vehicles.add(route.getVehicle());
                vehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                return vehicles;
            }

        });
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    public void whenSwitchIsNotAllowed_itShouldCalOnlyStatesOfCurrentVehicle() {
        stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, routingCosts, activityCosts);

        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
        assertTrue(stateManager.hasActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME));
        assertFalse(stateManager.hasActivityState(route.getActivities().get(0), vehicle2, InternalStates.LATEST_OPERATION_START_TIME));
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(2), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);

    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3_v2() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(2), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3WithVehicle2() {
        assertEquals(30., stateManager.getActivityState(route.getActivities().get(2), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3WithVehicle3() {
        assertEquals(90., stateManager.getActivityState(route.getActivities().get(2), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2() {
        assertEquals(60., stateManager.getActivityState(route.getActivities().get(1), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2_v2() {
        assertEquals(60., stateManager.getActivityState(route.getActivities().get(1), vehicle,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithVehicle2() {
        assertEquals(20., stateManager.getActivityState(route.getActivities().get(1), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithVehicle3() {
        assertEquals(80., stateManager.getActivityState(route.getActivities().get(1), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2WithEquivalentOfVehicle3() {
        assertEquals(80., stateManager.getActivityState(route.getActivities().get(1), equivalentOf3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct1WithVehicle2() {
        assertEquals(10., stateManager.getActivityState(route.getActivities().get(0), vehicle2,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct1WithVehicle3() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(0), vehicle3,
            InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }


    @Test
    public void twUpdateShouldWorkWithMultipleTWs(){
        //
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setEarliestStart(0.).setLatestArrival(100.).build();
        Service service = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0"))
                .addTimeWindow(10,20).addTimeWindow(30,40).build();
        Service service2 = Service.Builder.newInstance("s2")
                .addTimeWindow(20,30).addTimeWindow(40,60).addTimeWindow(70,80).setLocation(Location.newInstance("20,0")).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addJob(service2).addVehicle(vehicle)
                .setRoutingCost(routingCosts).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory())
                .addService(service).addService(service2, TimeWindow.newInstance(70, 80)).build();

        StateManager stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager,routingCosts,activityCosts);
        updater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

            @Override
            public Collection<Vehicle> get(VehicleRoute route) {
                Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
                vehicles.add(route.getVehicle());
//                vehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                return vehicles;
            }

        });
        stateManager.addStateUpdater(updater);
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());

        assertEquals(80.,stateManager.getActivityState(route.getActivities().get(1),vehicle,
                InternalStates.LATEST_OPERATION_START_TIME, Double.class),0.01);
    }

    @Test
    public void updateOfOpenRoutesShouldBeDoneCorrectly(){
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v")
            .setReturnToDepot(false)
            .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setLatestArrival(51)
            .build();

        Service service = Service.Builder.newInstance("s")
            .setLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(50, 0)).build()).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(service).addVehicle(vehicle).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle)
            .setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).build();

        stateManager = new StateManager(vrp);
        UpdateVehicleDependentPracticalTimeWindows updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, vrp.getTransportCosts(), vrp.getActivityCosts());
        stateManager.addStateUpdater(updater);
        stateManager.reCalculateStates(route);

        Double activityState = stateManager.getActivityState(route.getActivities().get(0),route.getVehicle(), InternalStates.LATEST_OPERATION_START_TIME, Double.class);
        Assert.assertEquals(51d, activityState, 0.01);

    }

    Random random = new Random();

    @Test
    public void testSquash () {
        Location pickupLocation = Location.newInstance(random.nextDouble(), random.nextDouble());
        final double fixedDurationInSameLocation = random.nextDouble(), activityDuration = 10 + random.nextDouble(), travelTime = random.nextDouble();

        Pickup pickup = Pickup.Builder.newInstance("pick1").setLocation(pickupLocation).setTimeWindow(TimeWindow.newInstance(0, 40)).setServiceTime(activityDuration).build();
        Pickup pickup2 = Pickup.Builder.newInstance("pick2").setLocation(pickupLocation).setTimeWindow(TimeWindow.newInstance(0, 40)).setServiceTime(activityDuration).build();

        Delivery delivery = Delivery.Builder.newInstance("del1").setLocation(Location.newInstance(random.nextDouble(), random.nextDouble())).setTimeWindow(TimeWindow.newInstance(0, 40)).setServiceTime(activityDuration).build();
        Delivery delivery2 = Delivery.Builder.newInstance("del2").setLocation(Location.newInstance(random.nextDouble(), random.nextDouble())).setTimeWindow(TimeWindow.newInstance(0, 40)).setServiceTime(activityDuration).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(random.nextDouble(), random.nextDouble())).setType(VehicleTypeImpl.Builder.newInstance("type").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(pickup).addJob(pickup2).addJob(delivery).addJob(delivery2).addVehicle(vehicle).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        }).addService(pickup).addService(pickup2).addService(delivery).addService(delivery2).build();



        VehicleRoutingTransportCosts transportCosts = getTransportCosts(travelTime);

        activityCosts = new WaitingTimeCosts() {
            @Override
            public double getActivityDuration(TourActivity prevAct, TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return prevAct != null && prevAct.getLocation().getCoordinate().equals(tourAct.getLocation().getCoordinate()) ? fixedDurationInSameLocation : tourAct.getOperationTime();
            }
        };

        stateManager = new StateManager(vrp);

        final UpdateVehicleDependentPracticalTimeWindows timeWindowsUpdater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, transportCosts, activityCosts);
        timeWindowsUpdater.visit(route);

        double latestArrivalAt4Act = 40,
            latestArrivalAt3Act = latestArrivalAt4Act - activityDuration - travelTime,
            latestArrivalAt2Act = latestArrivalAt3Act - fixedDurationInSameLocation - travelTime,
            latestArrivalAt1Act = latestArrivalAt2Act - activityDuration;

        assertEquals(stateManager.getActivityState(route.getActivities().get(3), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), latestArrivalAt4Act, 0.001);
        assertEquals(stateManager.getActivityState(route.getActivities().get(2), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), latestArrivalAt3Act, 0.001);
        assertEquals(stateManager.getActivityState(route.getActivities().get(1), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), latestArrivalAt2Act, 0.001);
        assertEquals(stateManager.getActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), latestArrivalAt1Act, 0.001);
    }

    public VehicleRoutingTransportCosts getTransportCosts(final double travelTime) {
        return  new VehicleRoutingTransportCosts() {

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return from.getCoordinate().equals(to.getCoordinate()) ? 0 : travelTime;
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return from.getCoordinate().equals(to.getCoordinate()) ? 0 : travelTime;
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return from.getCoordinate().equals(to.getCoordinate()) ? 0 : travelTime;
            }

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return from.getCoordinate().equals(to.getCoordinate()) ? 0 : travelTime;
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return from.getCoordinate().equals(to.getCoordinate()) ? 0 : travelTime;
            }
        };
    }

}
