/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.*;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.cost.WaitingTimeCosts;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.*;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.CostFactory;
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
	public void doBefore(){
		
		vehicle = mock(Vehicle.class);
		route = mock(VehicleRoute.class);
		when(route.isEmpty()).thenReturn(false);
		when(route.getVehicle()).thenReturn(vehicle);
		
		jic = mock(JobInsertionContext.class);
		when(jic.getRoute()).thenReturn(route);
		when(jic.getNewVehicle()).thenReturn(vehicle);
		
		tpCosts = mock(VehicleRoutingTransportCosts.class);
		when(tpCosts.getTransportCost(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(2.0);
		when(tpCosts.getTransportTime(loc("i"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
		when(tpCosts.getTransportCost(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(3.0);
		when(tpCosts.getTransportTime(loc("i"), loc("k"), 0.0, null, vehicle)).thenReturn(0.0);
		when(tpCosts.getTransportCost(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(3.0);
		when(tpCosts.getTransportTime(loc("k"), loc("j"), 0.0, null, vehicle)).thenReturn(0.0);
		
		actCosts = mock(VehicleRoutingActivityCosts.class);
		calc = new LocalActivityInsertionCostsCalculator(tpCosts, actCosts, null);
	}

    private Location loc(String i) {
        return Location.Builder.newInstance().setId(i).build();
    }

    @Test
	public void whenInsertingActBetweenTwoRouteActs_itCalcsMarginalTpCosts(){
		TourActivity prevAct = mock(TourActivity.class);
		when(prevAct.getLocation()).thenReturn(loc("i"));
		TourActivity nextAct = mock(TourActivity.class);
		when(nextAct.getLocation()).thenReturn(loc("j"));
		TourActivity newAct = mock(TourActivity.class);
		when(newAct.getLocation()).thenReturn(loc("k"));
		
		when(vehicle.isReturnToDepot()).thenReturn(true);
		
		double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
		assertEquals(4.0,costs,0.01);
	}
	
	@Test
	public void whenInsertingActBetweenLastActAndEnd_itCalcsMarginalTpCosts(){
		TourActivity prevAct = mock(TourActivity.class);
		when(prevAct.getLocation()).thenReturn(loc("i"));
		End nextAct = End.newInstance("j", 0.0, 0.0);
		TourActivity newAct = mock(TourActivity.class);
		when(newAct.getLocation()).thenReturn(loc("k"));
		
		when(vehicle.isReturnToDepot()).thenReturn(true);
		
		double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
		assertEquals(4.0,costs,0.01);
	}

	@Test
	public void whenInsertingActBetweenTwoRouteActsAndRouteIsOpen_itCalcsMarginalTpCosts(){
		TourActivity prevAct = mock(TourActivity.class);
		when(prevAct.getLocation()).thenReturn(loc("i"));
		TourActivity nextAct = mock(TourActivity.class);
		when(nextAct.getLocation()).thenReturn(loc("j"));
		TourActivity newAct = mock(TourActivity.class);
		when(newAct.getLocation()).thenReturn(loc("k"));
		
		when(vehicle.isReturnToDepot()).thenReturn(false);
		
		double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
		assertEquals(4.0,costs,0.01);
	}
	
	@Test
	public void whenInsertingActBetweenLastActAndEndAndRouteIsOpen_itCalculatesTpCostsFromPrevToNewAct(){
		TourActivity prevAct = mock(TourActivity.class);
		when(prevAct.getLocation()).thenReturn(loc("i"));
		End nextAct = End.newInstance("j", 0.0, 0.0);
		TourActivity newAct = mock(TourActivity.class);
		when(newAct.getLocation()).thenReturn(loc("k"));
		
		when(vehicle.isReturnToDepot()).thenReturn(false);
		
		double costs = calc.getCosts(jic, prevAct, nextAct, newAct, 0.0);
		assertEquals(3.0,costs,0.01);
	}

	@Test
	public void test(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();
		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setStartLocation(Location.newInstance(0, 0)).build();
		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(60, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,50)).build();
		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(prevS).addService(nextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(), new StateManager(mock(VehicleRoutingProblem.class)));
		calc.setSolutionCompletenessRatio(1.);
		calc.setVariableStartTimeFactor(.8);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,0);
		assertEquals(40.,c,0.01);
	}

	@Test
	public void whenAddingNewBetweenStartAndAct_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30, 0))
				.setTimeWindow(TimeWindow.newInstance(40, 50)).build();
		Start prevAct = new Start(Location.newInstance(0,0),0,100);

		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(nextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),new StateManager(mock(VehicleRoutingProblem.class)) );
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,0);
		assertEquals(-10.,c,0.01);
	}

	@Test
	public void whenAddingNewBetweenStartAndAct2_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0))
				.setTimeWindow(TimeWindow.newInstance(140,150)).build();
		Start prevAct = new Start(Location.newInstance(0,0),0,100);
//		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
		VehicleRoute route = VehicleRoute.Builder.newInstance(v2).addService(nextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v2,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(), null);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,0);
		assertEquals(-10.,c,0.01);
	}

	@Test
	public void whenAddingNewInEmptyRoute_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).setTimeWindow(TimeWindow.newInstance(100, 150)).build();
//		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,50)).build();
		Start prevAct = new Start(Location.newInstance(0,0),0,100);
//		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		End nextAct = new End(Location.newInstance(0,0),0,100);
//		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
//		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
		VehicleRoute route = VehicleRoute.Builder.newInstance(v).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),null );
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,0);
		assertEquals(110.,c,0.01);
	}

	@Test
	public void whenAddingNewBetweenTwoActs_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10, 0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,50)).build();

		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(prevS).addService(nextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),null );
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(-10.,c,0.01);
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100, 120)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,500)).build();

		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(prevS).addService(nextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),null );
		calc.setSolutionCompletenessRatio(0.5);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(35.,c,0.01);
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs2_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100,120)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,500)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(400,500)).build();

		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
		ServiceActivity newAct = ServiceActivity.newInstance(newS);
		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(prevS).addService(nextS).addService(afterNextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),null );
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(-90.,c,0.01);
		//360
		//270
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs3_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100,120)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,500)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(80,500)).build();
		Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(100,500)).build();

		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
				.addJob(afterNextS).addJob(afterAfterNextS).build();

		TourActivity prevAct = vrp.getActivities(prevS).get(0);
		TourActivity newAct = vrp.getActivities(newS).get(0);
		TourActivity nextAct = vrp.getActivities(nextS).get(0);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();

		StateManager stateManager = getStateManager(vrp,route);

		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),stateManager);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(20.,c,0.01);
		//start-delay = new - old = 120 - 40 = 80 > future waiting time savings = 30 + 20 + 10
		//ref: 10 + 50 + 20 = 80
		//new: 80 - 10 - 30 - 20 = 20
		/*
		w(new) + w(next) - w_old(next) - min{start_delay(next),future_waiting}
		 */
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs4_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100,120)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,500)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(80,500)).build();
		Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50,0)).setTimeWindow(TimeWindow.newInstance(100,500)).build();

		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
				.addJob(afterNextS).addJob(afterAfterNextS).build();

		TourActivity prevAct = vrp.getActivities(prevS).get(0);
		TourActivity newAct = vrp.getActivities(newS).get(0);
		TourActivity nextAct = vrp.getActivities(nextS).get(0);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);

		StateManager stateManager = getStateManager(vrp, route);

		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),stateManager);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(30.,c,0.01);
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
	public void whenAddingNewWithTWBetweenTwoActs4WithVarStart_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(100,120)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,500)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(80,500)).build();
		Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50,0)).setTimeWindow(TimeWindow.newInstance(100,500)).build();

		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
				.addJob(afterNextS).addJob(afterAfterNextS).build();

		TourActivity prevAct = vrp.getActivities(prevS).get(0);
		TourActivity newAct = vrp.getActivities(newS).get(0);
		TourActivity nextAct = vrp.getActivities(nextS).get(0);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);

		StateManager stateManager = getStateManager(vrp, route);

		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),stateManager);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(0.,c,0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = 10 + 30 + 10 = 50
		new = 80 + 0 - 10 - min{80,40} = 30
		 */
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs3WithVarStart_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(50,70)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,70)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(50,100)).build();
		Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50,0)).setTimeWindow(TimeWindow.newInstance(100,500)).build();

		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
				.addJob(afterNextS).addJob(afterAfterNextS).build();

		TourActivity prevAct = vrp.getActivities(prevS).get(0);
		TourActivity newAct = vrp.getActivities(newS).get(0);
		TourActivity nextAct = vrp.getActivities(nextS).get(0);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);

		StateManager stateManager = getStateManager(vrp, route);

		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),stateManager);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(-10.,c,0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = s:40,act1:50,act2:60,act3:70,act4:80 --> w=20
		new = s:30,act1:40,act2:50,act3:70,act4:80,act5:90 --> w=10 => -10
		 */
	}

	@Test
	public void whenAddingNewWithTWBetweenTwoActs2WithVarStart_itShouldCalcInsertionCostsCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setTimeWindow(TimeWindow.newInstance(10,20)).setLocation(Location.newInstance(20, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(40,70)).build();

		Service afterNextS = Service.Builder.newInstance("afterNext").setLocation(Location.newInstance(40,0)).setTimeWindow(TimeWindow.newInstance(50,100)).build();
		Service afterAfterNextS = Service.Builder.newInstance("afterAfterNext").setLocation(Location.newInstance(50,0)).setTimeWindow(TimeWindow.newInstance(100,500)).build();

		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(prevS).addJob(newS).addJob(nextS)
				.addJob(afterNextS).addJob(afterAfterNextS).build();

		TourActivity prevAct = vrp.getActivities(prevS).get(0);
		TourActivity newAct = vrp.getActivities(newS).get(0);
		TourActivity nextAct = vrp.getActivities(nextS).get(0);

		VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(prevS).addService(nextS).addService(afterNextS).addService(afterAfterNextS).build();
		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);

		StateManager stateManager = getStateManager(vrp, route);

		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),stateManager);
		calc.setSolutionCompletenessRatio(1.);
		double c = calc.getCosts(context,prevAct,nextAct,newAct,10);
		assertEquals(20.,c,0.01);
		/*
		activity start time delay at next act = start-time-old - start-time-new is always bigger than subsequent waiting time savings
		 */
		/*
		old = s:40,act1:50,act2:60,act3:70,act4:80 --> w=20
		new = s:0,act1:10,act2:20,act3:40,act4:50,act5:60 --> w=40 => 20
		 */
	}

	private StateManager getStateManager(VehicleRoutingProblem vrp, VehicleRoute route) {
		StateManager stateManager = new StateManager(vrp);
		stateManager.addStateUpdater(new UpdateTimeSlack(stateManager,vrp.getTransportCosts()));
		stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts()));
		stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager,vrp.getTransportCosts()));
		stateManager.addStateUpdater(new UpdateFutureWaitingTimes(stateManager,vrp.getTransportCosts()));
		stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
		stateManager.informInsertionStarts(Arrays.asList(route),new ArrayList<Job>());
		return stateManager;
	}
}

