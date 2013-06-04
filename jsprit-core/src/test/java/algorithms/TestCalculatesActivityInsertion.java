/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import algorithms.RouteStates.ActivityState;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;



public class TestCalculatesActivityInsertion {
	
	VehicleRoutingTransportCosts costs;
	
	Vehicle newVehicle;

	private RouteStates states;

	private CalculatesActivityInsertionWithHardTimeWindows insertionCalculator;
	
	@Before
	public void setup(){
		costs = mock(VehicleRoutingTransportCosts.class);
		newVehicle = mock(Vehicle.class);
		
		when(costs.getTransportCost("depot", "1", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("depot", "2", 0.0, null, null)).thenReturn(20.0);
		when(costs.getTransportCost("depot", "3", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("1", "2", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("1", "3", 0.0, null, null)).thenReturn(20.0);
		when(costs.getTransportCost("2", "3", 0.0, null, null)).thenReturn(10.0);
		
		when(costs.getTransportCost("1", "depot", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("2", "depot", 0.0, null, null)).thenReturn(20.0);
		when(costs.getTransportCost("3", "depot", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("2", "1", 0.0, null, null)).thenReturn(10.0);
		when(costs.getTransportCost("3", "1", 0.0, null, null)).thenReturn(20.0);
		when(costs.getTransportCost("3", "2", 0.0, null, null)).thenReturn(10.0);
		
		when(costs.getTransportCost("depot", "1", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("depot", "2", 0.0, null, newVehicle)).thenReturn(40.0);
		when(costs.getTransportCost("depot", "3", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("1", "2", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("1", "3", 0.0, null, newVehicle)).thenReturn(40.0);
		when(costs.getTransportCost("2", "3", 0.0, null, newVehicle)).thenReturn(20.0);
		
		when(costs.getTransportCost("1", "depot", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("2", "depot", 0.0, null, newVehicle)).thenReturn(40.0);
		when(costs.getTransportCost("3", "depot", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("2", "1", 0.0, null, newVehicle)).thenReturn(20.0);
		when(costs.getTransportCost("3", "1", 0.0, null, newVehicle)).thenReturn(40.0);
		when(costs.getTransportCost("3", "2", 0.0, null, newVehicle)).thenReturn(20.0);
		
		states = new RouteStates();
		
		insertionCalculator = new CalculatesActivityInsertionWithHardTimeWindows(states,costs,activityCosts());
		
	}

	private ExampleActivityCostFunction activityCosts() {
		return new ExampleActivityCostFunction();
	}
	
	public TourActivity getActivityMock(String id, double earliestOperationStart, double currCost){
		TourActivity act = mock(TourActivity.class);
		when(act.getLocationId()).thenReturn(id);
		states.getActivityStates().put(act, new ActivityState(act));
		states.getState(act).setEarliestOperationStart(earliestOperationStart);
		states.getState(act).setCurrentCost(currCost);
//		when(act.getEarliestOperationStartTime()).thenReturn(earliestOperationStart);
//		when(act.getCurrentCost()).thenReturn(currCost);
		return act;
	}
	
	@Test
	public void whenInsertingANewJob_itCalculatesMarginalCostChanges(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity prevAct = getActivityMock("1", 0.0, 10.0);
		TourActivity nextAct = getActivityMock("3", 0.0, 30.0);
		TourActivity act2insert = getActivityMock("2", 0.0, 0.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(prevAct);
		vehicleRoute.getTourActivities().addActivity(nextAct);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,prevAct,nextAct,end);
		when(tour.getActivities()).thenReturn(activities);
//		when(states.getRouteState(vehicleRoute).getCosts()).thenReturn(40.0);
		
		double c = insertionCalculator.calculate(vehicleRoute, prevAct, nextAct, act2insert, null, null);
		assertEquals(0.0,c,0.2);
	}

	@Test
	public void whenInsertingANewJob_itCalculatesMarginalCostChanges2(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
		when(tour.getActivities()).thenReturn(activities);
		
		double c = insertionCalculator.calculate(vehicleRoute, act1, act2, act3, null, null);
		assertEquals(20.0,c,0.2);
	}
	
	@Test
	public void whenInsertingANewJob_itCalculatesMarginalCostChanges3(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 0.0);
		TourActivity end = getActivityMock("depot", 0.0, 20.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,act1,end);
		when(tour.getActivities()).thenReturn(activities);
		
		double c = insertionCalculator.calculate(vehicleRoute, start, act1, act3, null, null);
		assertEquals(20.0,c,0.2);
	}
	
	@Test
	public void whenInsertingANewJobWithANewVehicle_itCalculatesLocalMarginalCostChanges(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
		vehicleRoute.getTourActivities().addActivity(end);
		
		
		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
		when(tour.getActivities()).thenReturn(activities);
		
		double c = insertionCalculator.calculate(vehicleRoute, act1, act2, act3, null, newVehicle);
		assertEquals(50.0,c,0.2);
	}
	
	@Test
	public void whenInsertingANewJobWithANewVehicle_itCalculatesLocalMarginalCostChangesAndAfterInsertionCostChanges(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
		when(tour.getActivities()).thenReturn(activities);
			
		double c = insertionCalculator.calculate(vehicleRoute, act1, act2, act3, null, newVehicle);
		assertEquals(50.0,c,0.2);
	}

//already on route-level
//	@Test
//	public void whenInsertingANewJobWithANewVehicle_itCalculatesTotalMarginalCostChanges(){
//		Tour tour = mock(Tour.class);
//		TourActivity start = getActivityMock("depot", 0.0, 0.0);
//		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
//		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
//		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
//		TourActivity end = getActivityMock("depot", 0.0, 40.0);
//		
//		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
//		when(tour.getActivities()).thenReturn(activities);
//		
//		double c = insertionCalculator.calculate(tour, act1, act2, act3, null, newVehicle);
//		assertEquals(80.0,c,0.2);
//	}
	
	@Test
	public void whenInsertingANewJobWithANewVehicle_itCalculatesTotalMarginalCostChanges2(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
		when(tour.getActivities()).thenReturn(activities);
			
		double c = insertionCalculator.calculate(vehicleRoute, act2, end, act3, null, newVehicle);
		assertEquals(20.0,c,0.2);
	}
	
	@Test
	public void whenInsertingANewJobWithANewVehicle_itCalculatesTotalMarginalCostChanges3(){
		VehicleRoute vehicleRoute = VehicleRoute.emptyRoute();
		
		TourActivities tour = mock(TourActivities.class);
		TourActivity start = getActivityMock("depot", 0.0, 0.0);
		TourActivity act1 = getActivityMock("1", 0.0, 10.0);
		TourActivity act3 = getActivityMock("3", 0.0, 0.0);
		TourActivity act2 = getActivityMock("2", 0.0, 20.0);
		TourActivity end = getActivityMock("depot", 0.0, 40.0);
		
		vehicleRoute.getTourActivities().addActivity(start);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
		vehicleRoute.getTourActivities().addActivity(end);
		
		List<TourActivity> activities = Arrays.asList(start,act1,act2,end);
		when(tour.getActivities()).thenReturn(activities);
		
		double c = insertionCalculator.calculate(vehicleRoute, start, act1, act3, null, newVehicle);
		assertEquals(50.0,c,0.2);
	}
	
}
