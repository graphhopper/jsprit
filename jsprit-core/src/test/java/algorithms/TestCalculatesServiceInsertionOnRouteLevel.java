/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package algorithms;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import util.Coordinate;
import util.ManhattanDistanceCalculator;
import algorithms.StateUpdates.UpdateStates;
import basics.Job;
import basics.Service;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.DriverImpl;
import basics.route.DriverImpl.NoDriver;
import basics.route.ServiceActivity;
import basics.route.TimeWindow;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;



public class TestCalculatesServiceInsertionOnRouteLevel {
	
	ServiceInsertionOnRouteLevelCalculator serviceInsertion;
	
	VehicleRoutingTransportCosts costs;
	
	Vehicle vehicle;
	
	Vehicle newVehicle;

	private Service first;

	private Service second;

	private Service third;

	private StateManagerImpl states;

	private NoDriver driver;
	
	private UpdateStates updateStates;
	
	@Before
	public void setup(){
		Logger.getRootLogger().setLevel(Level.DEBUG);
		
		costs = mock(VehicleRoutingTransportCosts.class);
		vehicle = mock(Vehicle.class);
		when(vehicle.getCapacity()).thenReturn(1000);
		when(vehicle.getLocationId()).thenReturn("0,0");
		when(vehicle.getEarliestDeparture()).thenReturn(0.0);
		when(vehicle.getLatestArrival()).thenReturn(100.0);
		
		newVehicle = mock(Vehicle.class);
		when(newVehicle.getCapacity()).thenReturn(1000);
		when(newVehicle.getLocationId()).thenReturn("0,0");
		when(newVehicle.getEarliestDeparture()).thenReturn(0.0);
		when(newVehicle.getLatestArrival()).thenReturn(100.0);
		
		driver = DriverImpl.noDriver();

		costs = new VehicleRoutingTransportCosts() {

			@Override
			public double getBackwardTransportTime(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getBackwardTransportCost(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle veh) {
				String[] fromTokens = fromId.split(",");
				String[] toTokens = toId.split(",");
				double fromX = Double.parseDouble(fromTokens[0]);
				double fromY = Double.parseDouble(fromTokens[1]);

				double toX = Double.parseDouble(toTokens[0]);
				double toY = Double.parseDouble(toTokens[1]);

				double dist = ManhattanDistanceCalculator.calculateDistance(new Coordinate(fromX, fromY), new Coordinate(toX, toY));
				if(veh == vehicle){
					return dist;
				}
				else if(veh == newVehicle){
					return 2*dist;
				}
				throw new IllegalStateException();
			}

			@Override
			public double getTransportTime(String fromId, String toId,
					double departureTime, Driver driver, Vehicle vehicle) {
				// TODO Auto-generated method stub
				return 0;
			}
		};

		
		first = Service.Builder.newInstance("1", 0).setLocationId("0,10").setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
		second = Service.Builder.newInstance("3", 0).setLocationId("10,0").setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
		third = Service.Builder.newInstance("2", 0).setLocationId("10,10").setTimeWindow(TimeWindow.newInstance(0.0, 100.0)).build();
		Collection<Job> jobs = new ArrayList<Job>();
		jobs.add(first);
		jobs.add(second);
		jobs.add(third);
		
		states = new StateManagerImpl();
		
		ExampleActivityCostFunction activityCosts = new ExampleActivityCostFunction();
		ActivityInsertionCostsCalculator actInsertionCostCalculator = new RouteLevelActivityInsertionCostsEstimator(costs, activityCosts, states);
		serviceInsertion = new ServiceInsertionOnRouteLevelCalculator(costs,activityCosts, actInsertionCostCalculator, new HardLoadConstraint(states), new HardTimeWindowActivityLevelConstraint(states, costs));
		serviceInsertion.setNuOfActsForwardLooking(4);
		serviceInsertion.setStates(states);
		
		updateStates = new UpdateStates(states, costs, activityCosts);
		
	}
	
	public TourActivity getActivityMock(String id, double earliestOperationStart, double currCost){
		TourActivity act = mock(TourActivity.class);
		when(act.getLocationId()).thenReturn(id);
		return act;
	}
	
	@Test
	public void whenInsertingTheFirstJobInAnEmptyTourWithVehicle_itCalculatesMarginalCostChanges(){
		TourActivities tour = new TourActivities();
		
		VehicleRoute route = VehicleRoute.newInstance(tour,driver,vehicle);
		updateStates.update(route);
		
		InsertionData iData = serviceInsertion.calculate(route, first, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
		assertEquals(20.0, iData.getInsertionCost(), 0.2);
		assertEquals(0, iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingThirdJobWithVehicle_itCalculatesMarginalCostChanges(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(first));
		tour.addActivity(ServiceActivity.newInstance(second));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,driver,vehicle);
		updateStates.update(route);
		
		InsertionData iData = serviceInsertion.calculate(route, third, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
		assertEquals(0.0, iData.getInsertionCost(), 0.2);
		assertEquals(1, iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingThirdJobWithNewVehicle_itCalculatesMarginalCostChanges(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(first));
		tour.addActivity(ServiceActivity.newInstance(second));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,driver,vehicle);
		updateStates.update(route);
		
		InsertionData iData = serviceInsertion.calculate(route, third, newVehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
		assertEquals(40.0, iData.getInsertionCost(), 0.2);
		assertEquals(1, iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingASecondJobWithAVehicle_itCalculatesLocalMarginalCostChanges(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(first));
		tour.addActivity(ServiceActivity.newInstance(third));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,driver,vehicle);
		updateStates.update(route);
		
		InsertionData iData = serviceInsertion.calculate(route, second, vehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
		assertEquals(0.0, iData.getInsertionCost(), 0.2);
		assertEquals(2, iData.getDeliveryInsertionIndex());
	}
	
	@Test
	public void whenInsertingASecondJobWithANewVehicle_itCalculatesLocalMarginalCostChanges(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(first));
		tour.addActivity(ServiceActivity.newInstance(third));
		
		VehicleRoute route = VehicleRoute.newInstance(tour,driver,vehicle);
		updateStates.update(route);
		
		InsertionData iData = serviceInsertion.calculate(route, second, newVehicle, vehicle.getEarliestDeparture(), null, Double.MAX_VALUE);
		assertEquals(40.0, iData.getInsertionCost(), 0.2);
		assertEquals(2, iData.getDeliveryInsertionIndex());
	}
	
}
