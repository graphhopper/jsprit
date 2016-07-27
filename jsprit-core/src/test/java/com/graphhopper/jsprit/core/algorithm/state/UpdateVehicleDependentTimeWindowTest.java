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
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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


}
