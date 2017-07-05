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
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateFutureWaitingTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVehicleDependentPracticalTimeWindows;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestLocalActivityInsertionCostsCalculator {

    VehicleRoutingTransportCosts tpCosts;

    VehicleRoutingActivityCosts actCosts;

    LocalActivityInsertionCostsCalculator calc;

    Vehicle vehicle;

    VehicleRoute route;

    JobInsertionContext jic;

    @Before
    public void doBefore() {

        vehicle = mock(Vehicle.class);
        route = mock(VehicleRoute.class);
        when(route.isEmpty()).thenReturn(false);
        when(route.getVehicle()).thenReturn(vehicle);

        jic = mock(JobInsertionContext.class);
        when(jic.getRoute()).thenReturn(route);
        when(jic.getNewVehicle()).thenReturn(vehicle);
        when(vehicle.getType()).thenReturn(VehicleTypeImpl.Builder.newInstance("type").build());

        tpCosts = mock(VehicleRoutingTransportCosts.class);
        when(tpCosts.getTransportCost(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(2.0);
        when(tpCosts.getTransportTime(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.getTransportCost(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.getTransportTime(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(0.0);
        when(tpCosts.getTransportCost(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(3.0);
        when(tpCosts.getTransportTime(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);

        actCosts = new WaitingTimeCosts();
        calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts, mock(StateManager.class));
    }

    private Location loc(String i) {
        return Location.Builder.newInstance().setId(i).build();
    }

    @Test
    public void whenAddingServiceBetweenDiffStartAndEnd_costMustBeCorrect() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance(0, 0))
            .setEndLocation(Location.newInstance(20, 0))
            .build();
        Service s = Service.Builder.newInstance("s")
            .setLocation(Location.newInstance(10, 0))
            .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v)
            .addJob(s)
            .build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext jobInsertionContext =
            new JobInsertionContext(route, s, v, null, 0);
        LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator =
            new LocalActivityInsertionCostsCalculator(
                vrp.getTransportCosts(),
                vrp.getActivityCosts(),
                new StateManager(vrp));
        double cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            new Start(v.getStartLocation(), 0, Double.MAX_VALUE),
            new End(v.getEndLocation(), 0, Double.MAX_VALUE),
            vrp.getActivities(s).get(0),
            0);
        assertEquals(20., cost, Math.ulp(20.));
    }

    @Test
    public void whenAddingShipmentBetweenDiffStartAndEnd_costMustBeCorrect() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v")
            .setStartLocation(Location.newInstance(0, 0))
            .setEndLocation(Location.newInstance(20, 0))
            .build();
        Shipment s = Shipment.Builder.newInstance("p")
            .setPickupLocation(Location.newInstance(10, 0))
            .setDeliveryLocation(Location.newInstance(10, 7.5))
            .build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(v)
            .addJob(s)
            .build();
        VehicleRoute route = VehicleRoute.emptyRoute();
        JobInsertionContext jobInsertionContext =
            new JobInsertionContext(route, s, v, null, 0);
        LocalActivityInsertionCostsCalculator localActivityInsertionCostsCalculator =
            new LocalActivityInsertionCostsCalculator(
                vrp.getTransportCosts(),
                vrp.getActivityCosts(),
                new StateManager(vrp));
        double cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            new Start(v.getStartLocation(), 0, Double.MAX_VALUE),
            new End(v.getEndLocation(), 0, Double.MAX_VALUE),
            vrp.getActivities(s).get(0),
            0);
        assertEquals(20., cost, Math.ulp(20.));
        cost = localActivityInsertionCostsCalculator.getCosts(
            jobInsertionContext,
            vrp.getActivities(s).get(0),
            new End(v.getEndLocation(), 0, Double.MAX_VALUE),
            vrp.getActivities(s).get(1),
            0);
        assertEquals(10, cost, Math.ulp(10.));
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActs_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        when(prevAct.getIndex()).thenReturn(1);
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        when(nextAct.getIndex()).thenReturn(1);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));
        when(newAct.getIndex()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEnd_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        when(prevAct.getIndex()).thenReturn(1);
        End nextAct = End.newInstance("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));
        when(newAct.getIndex()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(true);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenTwoRouteActsAndRouteIsOpen_itCalcsMarginalTpCosts() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        when(prevAct.getIndex()).thenReturn(1);
        TourActivity nextAct = mock(TourActivity.class);
        when(nextAct.getLocation()).thenReturn(loc("j"));
        when(nextAct.getIndex()).thenReturn(1);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));
        when(newAct.getIndex()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(4.0, costs, 0.01);
    }

    @Test
    public void whenInsertingActBetweenLastActAndEndAndRouteIsOpen_itCalculatesTpCostsFromPrevToNewAct() {
        TourActivity prevAct = mock(TourActivity.class);
        when(prevAct.getLocation()).thenReturn(loc("i"));
        when(prevAct.getIndex()).thenReturn(1);
        End nextAct = End.newInstance("j", 0.0, 0.0);
        TourActivity newAct = mock(TourActivity.class);
        when(newAct.getLocation()).thenReturn(loc("k"));
        when(newAct.getIndex()).thenReturn(1);

        when(vehicle.isReturnToDepot()).thenReturn(false);

        double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
        assertEquals(3.0, costs, 0.01);
    }

    @Test
    public void test() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(60, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 80)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(80);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrpMock));
        calc.setSolutionCompletenessRatio(1.);

        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(50., c, 0.01);

		/*
        new: dist = 90 & wait = 0
		old: dist = 30 & wait = 10
		c = new - old = 90 - 40 = 50
		 */
    }

    @Test
    public void whenAddingNewBetweenStartAndAct_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0))
            .setTimeWindow(TimeWindow.newInstance(40, 50)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(newS).addJob(nextS).addVehicle(v).build();

        Start prevAct = new Start(Location.newInstance(0, 0), 0, 100);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(50);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewBetweenStartAndAct2_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0))
            .setTimeWindow(TimeWindow.newInstance(140, 150)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(newS).addJob(nextS).addVehicle(v2).build();

        Start prevAct = new Start(Location.newInstance(0, 0), 0, 100);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(140);
        nextAct.setTheoreticalLatestOperationStartTime(150);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v2).setJobActivityFactory(vrp.getJobActivityFactory()).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v2, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewInEmptyRoute_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();

        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).setTimeWindow(TimeWindow.newInstance(100, 150)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(newS).addVehicle(v).build();

        Start prevAct = new Start(Location.newInstance(0, 0), 0, 100);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(150);

        End nextAct = new End(Location.newInstance(0, 0), 0, 100);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 0);
        assertEquals(110., c, 0.01);
    }

    @Test
    public void whenAddingNewBetweenTwoActs_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 50)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(50);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(120);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), new StateManager(vrp));
        calc.setSolutionCompletenessRatio(0.5);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(35., c, 0.01);
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs2_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(400, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(afterNextS).addJob(prevS).addJob(newS).addJob(nextS).addVehicle(v).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(120);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(400);
        nextAct.setTheoreticalLatestOperationStartTime(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).build();

        StateManager stateManager = getStateManager(vrp, route);

        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
        //
        //old: dist: 0, waiting: 10 + 350 = 360
        //new: dist: 0, waiting: 80 + 270 = 350
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs3_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(120);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterNextAct = vrp.getActivities(afterNextS).get(0);
        afterNextAct.setTheoreticalEarliestOperationStartTime(80);
        afterNextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterAfterNextAct = vrp.getActivities(afterAfterNextS).get(0);
        afterAfterNextAct.setTheoreticalEarliestOperationStartTime(100);
        afterAfterNextAct.setTheoreticalLatestOperationStartTime(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();

        StateManager stateManager = getStateManager(vrp, route);

        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);
        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(20., c, 0.01);
        //start-delay = new - old = 120 - 40 = 80 > future waiting time savings = 30 + 20 + 10
        //ref: 10 + 50 + 20 = 80
        //new: 80 - 10 - 30 - 20 = 20
        /*
        w(new) + w(next) - w_old(next) - min{start_delay(next),future_waiting}
		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs4_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50, 0)).setTimeWindow(TimeWindow.newInstance(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(120);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterNextAct = vrp.getActivities(afterNextS).get(0);
        afterNextAct.setTheoreticalEarliestOperationStartTime(80);
        afterNextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterAfterNextAct = vrp.getActivities(afterAfterNextS).get(0);
        afterAfterNextAct.setTheoreticalEarliestOperationStartTime(100);
        afterAfterNextAct.setTheoreticalLatestOperationStartTime(500);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(30., c, 0.01);
        //ref: 10 + 30 + 10 = 50
        //new: 50 - 50 = 0

		/*
        activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
        /*
		old = 10 + 30 + 10 = 50
		new = 80 + 0 - 10 - min{80,40} = 30
		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs4WithVarStart_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 500)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(80, 500)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50, 0)).setTimeWindow(TimeWindow.newInstance(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);
        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(100);
        newAct.setTheoreticalLatestOperationStartTime(120);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterNextAct = vrp.getActivities(afterNextS).get(0);
        afterNextAct.setTheoreticalEarliestOperationStartTime(80);
        afterNextAct.setTheoreticalLatestOperationStartTime(500);

        TourActivity afterAfterNextAct = vrp.getActivities(afterAfterNextS).get(0);
        afterAfterNextAct.setTheoreticalEarliestOperationStartTime(100);
        afterAfterNextAct.setTheoreticalLatestOperationStartTime(500);


        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(30., c, 0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = 10 + 30 + 10 = 50
		new = 80
		new - old = 80 - 40 = 40

		 */
    }

    @Test
    public void whenAddingNewWithTWBetweenTwoActs3WithVarStart_itShouldCalcInsertionCostsCorrectly() {
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

        Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
        Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(50, 70)).setLocation(Location.newInstance(20, 0)).build();
        Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0)).setTimeWindow(TimeWindow.newInstance(40, 70)).build();

        Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40, 0)).setTimeWindow(TimeWindow.newInstance(50, 100)).build();
        Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50, 0)).setTimeWindow(TimeWindow.newInstance(100, 500)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
            .addJob(afterNextS).addJob(afterAfterNextS).build();

        TourActivity prevAct = vrp.getActivities(prevS).get(0);

        TourActivity newAct = vrp.getActivities(newS).get(0);
        newAct.setTheoreticalEarliestOperationStartTime(50);
        newAct.setTheoreticalLatestOperationStartTime(70);

        TourActivity nextAct = vrp.getActivities(nextS).get(0);
        nextAct.setTheoreticalEarliestOperationStartTime(40);
        nextAct.setTheoreticalLatestOperationStartTime(70);

        TourActivity afterNextAct = vrp.getActivities(afterNextS).get(0);
        afterNextAct.setTheoreticalEarliestOperationStartTime(50);
        afterNextAct.setTheoreticalEarliestOperationStartTime(100);

        TourActivity afterAfterNextAct = vrp.getActivities(afterAfterNextS).get(0);
        afterAfterNextAct.setTheoreticalEarliestOperationStartTime(100);
        afterAfterNextAct.setTheoreticalEarliestOperationStartTime(500);

        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
        JobInsertionContext context = new JobInsertionContext(route, newS, v, null, 0.);

        StateManager stateManager = getStateManager(vrp, route);
        stateManager.updateTimeWindowStates();
        stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());

        LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(), new WaitingTimeCosts(), stateManager);
        calc.setSolutionCompletenessRatio(1.);
        double c = calc.getCosts(context, prevAct, nextAct, newAct, 10);
        assertEquals(-10., c, 0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = 10 + 40 = 50
		new = 30 + 10 = 40
		 */
    }


    private StateManager getStateManager(VehicleRoutingProblem vrp, VehicleRoute route) {
        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager, vrp.getTransportCosts(), actCosts));
        stateManager.addStateUpdater(new UpdateFutureWaitingTimes(stateManager, vrp.getTransportCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), new ArrayList<Job>());
        return stateManager;
    }
}
