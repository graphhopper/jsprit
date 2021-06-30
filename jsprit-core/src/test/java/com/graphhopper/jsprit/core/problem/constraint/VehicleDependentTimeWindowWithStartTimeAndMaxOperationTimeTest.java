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
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

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

    /*
     * driver TW: 5 - 10
     * prev task 0 - 5
     * new and next acts at same location, tw 5-10, activity duration 4
     *     |--- newAct ---|
     *  |--- nextAct ---|
     */

    Random random = new Random();
    @Test
    public void testSquashNotEnd () {
        final double fixedCostAtSameLocation = random.nextDouble(),
            travelTime = random.nextDouble(),
            firstActDuration = 5, nextActivitiesDuration = 4.0,
            vehicleLatestArrival = firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 3;

        Location locationFirst = Location.newInstance(random.nextDouble(), random.nextDouble());
        Location locationSecond = Location.newInstance(random.nextDouble(), random.nextDouble());

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatestArrival()).thenReturn(vehicleLatestArrival);

        JobInsertionContext iFacts = mock(JobInsertionContext.class);
        when(iFacts.getNewVehicle()).thenReturn(vehicle);

        RouteAndActivityStateGetter routeAndActivityStateGetter = mock(RouteAndActivityStateGetter.class);

        TourActivity prevAct = getTourActivity(.0, firstActDuration, firstActDuration, locationFirst);
        TourActivity newAct = getTourActivity(firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2, nextActivitiesDuration, locationSecond);
        TourActivity nextAct = getTourActivity(firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2, nextActivitiesDuration, locationSecond);

        when(routeAndActivityStateGetter.getActivityState(nextAct, vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class)).thenReturn(vehicleLatestArrival - travelTime - nextActivitiesDuration);

        final VehicleDependentTimeWindowConstraints timeWindowConstraints = spy(new VehicleDependentTimeWindowConstraints(routeAndActivityStateGetter, getTransportCosts(travelTime), getActivityCost(fixedCostAtSameLocation)));
        doReturn(HardActivityConstraint.ConstraintsStatus.FULFILLED).when(timeWindowConstraints).validateNotLateToActivityAfterNext(iFacts, nextAct, 5+travelTime+nextActivitiesDuration+0+fixedCostAtSameLocation);
        assertEquals(timeWindowConstraints.fulfilled(iFacts, prevAct, newAct, nextAct, 5.0), HardActivityConstraint.ConstraintsStatus.FULFILLED);
    }

    @Test
    public void testSquashNotEndLateToNext () {
        final double fixedCostAtSameLocation = random.nextDouble(),
            travelTime = random.nextDouble(),
            firstActDuration = 5, nextActivitiesDuration = 4.0,
            vehicleLatestArrival = firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 3;

        Location locationFirst = Location.newInstance(random.nextDouble(), random.nextDouble());
        Location locationSecond = Location.newInstance(random.nextDouble(), random.nextDouble());

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatestArrival()).thenReturn(vehicleLatestArrival);

        JobInsertionContext iFacts = mock(JobInsertionContext.class);
        when(iFacts.getNewVehicle()).thenReturn(vehicle);

        RouteAndActivityStateGetter routeAndActivityStateGetter = mock(RouteAndActivityStateGetter.class);

        TourActivity prevAct = getTourActivity(.0, firstActDuration, firstActDuration, locationFirst);
        TourActivity newAct = getTourActivity(firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2, nextActivitiesDuration, locationSecond);
        TourActivity nextAct = getTourActivity(firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2, nextActivitiesDuration, locationSecond);

        when(routeAndActivityStateGetter.getActivityState(nextAct, vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class)).thenReturn(vehicleLatestArrival - travelTime - nextActivitiesDuration);

        final VehicleDependentTimeWindowConstraints timeWindowConstraints = spy(new VehicleDependentTimeWindowConstraints(routeAndActivityStateGetter, getTransportCosts(travelTime), getActivityCost(fixedCostAtSameLocation)));
        doReturn(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED).when(timeWindowConstraints).validateNotLateToActivityAfterNext(iFacts, nextAct, 5+travelTime+nextActivitiesDuration+0+fixedCostAtSameLocation);
        assertEquals(timeWindowConstraints.fulfilled(iFacts, prevAct, newAct, nextAct, 5.0), HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED);
    }

    @Test
    public void testNoSquashEnd () {
        final double fixedCostAtSameLocation = random.nextDouble(),
            travelTime = random.nextDouble(),
            firstActDuration = 5, nextActivitiesDuration = 4.0,
            vehicleLatestArrival = firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 3;

        Location locationFirst = Location.newInstance(random.nextDouble(), random.nextDouble());
        Location locationSecond = Location.newInstance(random.nextDouble(), random.nextDouble());

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatestArrival()).thenReturn(vehicleLatestArrival);
        when(vehicle.isReturnToDepot()).thenReturn(false);

        JobInsertionContext iFacts = mock(JobInsertionContext.class);
        when(iFacts.getNewVehicle()).thenReturn(vehicle);

        TourActivity prevAct = getTourActivity(.0, firstActDuration, firstActDuration, locationFirst);
        TourActivity newAct = getTourActivity(firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2, nextActivitiesDuration, locationSecond);
        End end = new End(locationSecond, firstActDuration, firstActDuration + nextActivitiesDuration + fixedCostAtSameLocation + travelTime * 2);

        final VehicleDependentTimeWindowConstraints timeWindowConstraints = new VehicleDependentTimeWindowConstraints(mock(RouteAndActivityStateGetter.class), getTransportCosts(travelTime), getActivityCost(fixedCostAtSameLocation));

        assertEquals(timeWindowConstraints.fulfilled(iFacts, prevAct, newAct, end, 5.0), HardActivityConstraint.ConstraintsStatus.FULFILLED);
    }

    @Test
    public void testSquashEndReturnToDepot () {
        final double fixedCostAtSameLocation = random.nextDouble(),
            travelTime = random.nextDouble(),
            firstActDuration = 5.0, newActivityDuration = 4.0,
            vehicleLatestArrival = firstActDuration + fixedCostAtSameLocation + travelTime;

        Location location = Location.newInstance(random.nextDouble(), random.nextDouble());
        Location depot = Location.newInstance(random.nextDouble(), random.nextDouble());

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatestArrival()).thenReturn(vehicleLatestArrival);
        when(vehicle.isReturnToDepot()).thenReturn(true);
        when(vehicle.getEndLocation()).thenReturn(depot);

        JobInsertionContext iFacts = mock(JobInsertionContext.class);
        when(iFacts.getNewVehicle()).thenReturn(vehicle);

        TourActivity prevAct = getTourActivity(.0, firstActDuration, firstActDuration, location);
        TourActivity newAct = getTourActivity(firstActDuration, firstActDuration + fixedCostAtSameLocation, newActivityDuration, location);
        End end = new End(depot, firstActDuration, firstActDuration + fixedCostAtSameLocation + travelTime);

        final VehicleDependentTimeWindowConstraints timeWindowConstraints = new VehicleDependentTimeWindowConstraints(mock(RouteAndActivityStateGetter.class), getTransportCosts(travelTime), getActivityCost(fixedCostAtSameLocation));

        assertEquals(timeWindowConstraints.fulfilled(iFacts, prevAct, newAct, end, 5.0), HardActivityConstraint.ConstraintsStatus.FULFILLED);
    }

    @Test
    public void testSquashEndNoReturnToDepot () {
        final double fixedCostAtSameLocation = random.nextDouble(),
            travelTime = random.nextDouble(),
            firstActDuration = 5.0, newActivityDuration = 4.0,
            vehicleLatestArrival = firstActDuration + fixedCostAtSameLocation;

        Location location = Location.newInstance(random.nextDouble(), random.nextDouble());
        Location depot = Location.newInstance(random.nextDouble(), random.nextDouble());

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getLatestArrival()).thenReturn(vehicleLatestArrival);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getEndLocation()).thenReturn(depot);

        JobInsertionContext iFacts = mock(JobInsertionContext.class);
        when(iFacts.getNewVehicle()).thenReturn(vehicle);

        TourActivity prevAct = getTourActivity(.0, firstActDuration, firstActDuration, location);
        TourActivity newAct = getTourActivity(firstActDuration, firstActDuration + fixedCostAtSameLocation, newActivityDuration, location);
        End end = new End(depot, firstActDuration, firstActDuration + fixedCostAtSameLocation);

        final VehicleDependentTimeWindowConstraints timeWindowConstraints = new VehicleDependentTimeWindowConstraints(mock(RouteAndActivityStateGetter.class), getTransportCosts(travelTime), getActivityCost(fixedCostAtSameLocation));

        assertEquals(timeWindowConstraints.fulfilled(iFacts, prevAct, newAct, end, 5.0), HardActivityConstraint.ConstraintsStatus.FULFILLED);
    }

    private TourActivity getTourActivity(double start, double end, double activityDuration, Location location) {
        TourActivity act = mock(DeliveryActivity.class);
        when(act.getTheoreticalEarliestOperationStartTime()).thenReturn(start);
        when(act.getTheoreticalLatestOperationStartTime()).thenReturn(end);
        when(act.getOperationTime()).thenReturn(activityDuration);
        when(act.getLocation()).thenReturn(location);
        return act;
    }

    private VehicleRoutingActivityCosts getActivityCost(final double fixedCostAtSameLocation) {
        return new VehicleRoutingActivityCosts() {
            @Override
            public double getActivityCost(TourActivity prevAct, TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getActivityDuration(prevAct, tourAct, arrivalTime, driver, vehicle);
            }

            @Override
            public double getActivityDuration(TourActivity from, TourActivity to, double startTime, Driver driver, Vehicle vehicle) {
                if (from != null && !(to instanceof BreakActivity || from instanceof BreakActivity) && from.getLocation().getCoordinate().equals(to.getLocation().getCoordinate())) {
                    return fixedCostAtSameLocation;
                }

                return to.getOperationTime();
            }
        };
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
