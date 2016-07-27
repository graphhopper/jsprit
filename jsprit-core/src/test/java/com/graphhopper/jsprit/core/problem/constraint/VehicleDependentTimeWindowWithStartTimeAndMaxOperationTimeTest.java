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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVehicleDependentPracticalTimeWindows;
import com.graphhopper.jsprit.core.problem.*;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

/**
 * unit tests to test vehicle dependent time-windows
 */
public class VehicleDependentTimeWindowWithStartTimeAndMaxOperationTimeTest {

    private StateManager stateManager;

    private VehicleRoute route;

    private AbstractVehicle vehicle;

    private AbstractVehicle v2;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private VehicleImpl v3;
    private VehicleImpl v4;
    private VehicleImpl v5;
    private VehicleImpl v6;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        routingCosts = CostFactory.createEuclideanCosts();
        activityCosts = new WaitingTimeCosts();
        vrpBuilder.setRoutingCost(routingCosts);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        vehicle = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEarliestStart(0.).setLatestArrival(100.).build();

        v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEarliestStart(0.).setLatestArrival(60.).build();

        v3 = VehicleImpl.Builder.newInstance("v3").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEarliestStart(0.).setLatestArrival(50.).build();

        v4 = VehicleImpl.Builder.newInstance("v4").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEarliestStart(0.).setLatestArrival(10.).build();

        v5 = VehicleImpl.Builder.newInstance("v5").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEarliestStart(60.).setLatestArrival(100.).build();

        v6 = VehicleImpl.Builder.newInstance("v6").setType(type).setStartLocation(Location.newInstance("0,0"))
            .setEndLocation(Location.newInstance("40,0")).setEarliestStart(0.).setLatestArrival(40.).build();

        vrpBuilder.addVehicle(vehicle).addVehicle(v2).addVehicle(v3).addVehicle(v4).addVehicle(v5).addVehicle(v6);

        Service service = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).build();

        vrpBuilder.addJob(service).addJob(service2).addJob(service3);
        final VehicleRoutingProblem vrp = vrpBuilder.build();

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(new JobActivityFactory() {

            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }

        }).addService(service).addService(service2).addService(service3).build();

        stateManager = new StateManager(vrp);

        Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
        vehicles.add(vehicle);
        vehicles.add(v2);
        vehicles.add(v3);
        vehicles.add(v4);
        vehicles.add(v5);
        vehicles.add(v6);

        final VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vehicles).createFleetManager();
//        stateManager.updateTimeWindowStates();
        UpdateVehicleDependentPracticalTimeWindows timeWindow_updater = new UpdateVehicleDependentPracticalTimeWindows(stateManager, routingCosts, activityCosts);
        timeWindow_updater.setVehiclesToUpdate(new UpdateVehicleDependentPracticalTimeWindows.VehiclesToUpdate() {

            @Override
            public Collection<Vehicle> get(VehicleRoute route) {
                List<Vehicle> vehicles = new ArrayList<Vehicle>();
                vehicles.add(route.getVehicle());
                vehicles.addAll(fleetManager.getAvailableVehicles(route.getVehicle()));
                return vehicles;
            }

        });
        stateManager.addStateUpdater(timeWindow_updater);
        stateManager.addStateUpdater(new UpdateActivityTimes(routingCosts,activityCosts));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct3() {
        assertEquals(70., stateManager.getActivityState(route.getActivities().get(2),
            vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct2() {
        assertEquals(60., stateManager.getActivityState(route.getActivities().get(1),
            vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void stateManagerShouldHaveMemorizedCorrectLatestEndOfAct1() {
        assertEquals(50., stateManager.getActivityState(route.getActivities().get(0),
            vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void whenNewJobIsInsertedWithOldVeh_itJustShouldReturnTrue() {

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("50,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, vehicle, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);
        assertTrue(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenNewJobIsInsertedWithOldVeh_itJustShouldReturnFalse() {

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("1000,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, vehicle, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenNewJobIsInsertedInBetweenAct1And2WithOldVeh_itJustShouldReturnTrue() {

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("50,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, vehicle, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);
        /*
        driverTime = 10 + 10 + 30 + 20 + 30 = 100
         */
//        System.out.println("latest act1 " + stateManager.getActivityState());
        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(1), serviceAct, route.getActivities().get(2), 20.);
        assertTrue(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenNewJobIsInsertedInBetweenAct1And2WithOldVeh_itJustShouldReturnFalse() {

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("51,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, vehicle, route.getDriver(), 0.);

        /*
        driverTime = 10 + 10 + 31 + 21 + 30 = 102
         */

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(1), serviceAct, route.getActivities().get(2), 20.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithNewVehicleThatNeedsToBeHomeAt60_itShouldReturnFalse() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v2, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);

        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithNewVehicleThatNeedsToBeHomeAt50_itShouldReturnFalse() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v3, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithNewVehicleThatNeedsToBeHomeAt10_itShouldReturnFalse() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v4, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithV6BetweenS2AndS3_itShouldReturnFalse() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v6, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(1), serviceAct, route.getActivities().get(2), 30.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithV6BetweenS1AndS2_itShouldReturnFalse() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v6, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(0), serviceAct, route.getActivities().get(1), 10.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }

    @Test
    public void whenJobIsInsertedAlongWithV6AtTheEndOfRoute_itShouldReturnTrue() {

        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v6, route.getDriver(), 0.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 30.);
        assertTrue(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));
    }

    @Test
    public void whenJobIsInsertedAlongWithNewVehicleThatCanOnlyStartAt60_itShouldReturnFalse() {
        assertEquals(60., route.getEnd().getArrTime(), 0.01);

        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("40,0")).build();
        PickupService serviceAct = new PickupService(s4);

        JobInsertionContext insertionContext = new JobInsertionContext(route, s4, v5, route.getDriver(), 60.);

        HardActivityConstraint twConstraint = new VehicleDependentTimeWindowConstraints(stateManager, routingCosts, activityCosts);

        HardActivityConstraint.ConstraintsStatus status = twConstraint.fulfilled(insertionContext, route.getActivities().get(2), serviceAct, route.getEnd(), 90.);
        assertFalse(status.equals(HardActivityConstraint.ConstraintsStatus.FULFILLED));

    }


}
