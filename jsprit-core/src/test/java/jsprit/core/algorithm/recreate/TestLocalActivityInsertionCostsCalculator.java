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

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.cost.WaitingTimeCosts;
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

//	@Test
//	public void test_(){
//		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();
//		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0, 0)).build();
//		Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
//		Service newS = Service.Builder.newInstance("new").setLocation(Location.newInstance(40, 0)).build();
//		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0)).setTimeWindow(TimeWindow.newInstance(140,150)).build();
//		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
//		ServiceActivity newAct = ServiceActivity.newInstance(newS);
//		ServiceActivity nextAct = ServiceActivity.newInstance(nextS);
//		VehicleRoute route = VehicleRoute.Builder.newInstance(v).addService(prevS).addService(nextS).build();
//		JobInsertionContext context = new JobInsertionContext(route,newS,v,null,0.);
//		LocalActivityInsertionCostsCalculator calc = new LocalActivityInsertionCostsCalculator(CostFactory.createEuclideanCosts(),new WaitingTimeCosts(),new StateManager(mock(VehicleRoutingProblem.class)) );
//		calc.setSolutionCompletenessRatio(1.);
////		calc.setVariableStartTimeFactor(1.);
//		double c = calc.getCosts(context,prevAct,nextAct,newAct,0);
//		assertEquals(0.,c,0.01);
//	}

	@Test
	public void test2(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//
// Service prevS = Service.Builder.newInstance("prev").setLocation(Location.newInstance(10,0)).build();
		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).build();
		Service nextS = Service.Builder.newInstance("next").setLocation(Location.newInstance(30,0))
				.setTimeWindow(TimeWindow.newInstance(40,50)).build();
		Start prevAct = new Start(Location.newInstance(0,0),0,100);
//		ServiceActivity prevAct = ServiceActivity.newInstance(prevS);
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
	public void test3(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

//		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
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
	public void test4(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").setCostPerWaitingTime(1.).build();

		VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(false).setType(type).setStartLocation(Location.newInstance(0,0)).build();
//		VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setHasVariableDepartureTime(true).setType(type).setStartLocation(Location.newInstance(0,0)).build();

		Service newS = Service.Builder.newInstance("new").setServiceTime(10).setLocation(Location.newInstance(10, 0)).setTimeWindow(TimeWindow.newInstance(100,150)).build();
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
}

