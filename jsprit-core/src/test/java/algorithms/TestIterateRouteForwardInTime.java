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

import java.util.ArrayList;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;

import util.Coordinate;
import util.ManhattanDistanceCalculator;
import basics.Job;
import basics.Service;
import basics.costs.DefaultVehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.DriverImpl;
import basics.route.ServiceActivity;
import basics.route.TimeWindow;
import basics.route.TourActivities;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleRoute;
import basics.route.VehicleTypeImpl;

public class TestIterateRouteForwardInTime {

	TourActivities tour;

	Driver driver;

	Vehicle vehicle;

	TourActivities anotherTour;

	private VehicleRoute vehicleRoute;
	
	private VehicleRoutingTransportCosts cost;
	
	ServiceActivity firstAct;
	
	ServiceActivity secondAct;
	
	StateManagerImpl stateManager;
	
	@Before
	public void setUp(){
		cost = new VehicleRoutingTransportCosts() {

			@Override
			public double getBackwardTransportTime(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
			}

			@Override
			public double getBackwardTransportCost(String fromId, String toId,
					double arrivalTime, Driver driver, Vehicle vehicle) {
				return getTransportCost(fromId, toId, arrivalTime, driver, vehicle);
			}

			@Override
			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
				String[] fromTokens = fromId.split(",");
				String[] toTokens = toId.split(",");
				double fromX = Double.parseDouble(fromTokens[0]);
				double fromY = Double.parseDouble(fromTokens[1]);

				double toX = Double.parseDouble(toTokens[0]);
				double toY = Double.parseDouble(toTokens[1]);

				return ManhattanDistanceCalculator.calculateDistance(new Coordinate(fromX, fromY), new Coordinate(toX, toY));
			}

			@Override
			public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
				return getTransportCost(fromId, toId, departureTime, driver, vehicle);
			}
		};

		Service firstService = Service.Builder.newInstance("1", 5).setLocationId("10,0").setTimeWindow(TimeWindow.newInstance(0, 20)).build();
		Service secondService = Service.Builder.newInstance("2", 5).setLocationId("0,10").setTimeWindow(TimeWindow.newInstance(0, 50)).build();
		
		Collection<Job> services = new ArrayList<Job>();
		services.add(firstService);
		services.add(secondService);
		
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("test", 0).build();
		vehicle = VehicleImpl.Builder.newInstance("testvehicle").setType(type).setLocationId("0,0")
				.setEarliestStart(0.0).setLatestArrival(50.0).build();
		
		tour = new TourActivities();
		firstAct = ServiceActivity.newInstance(firstService);
		tour.addActivity(firstAct);
		secondAct = ServiceActivity.newInstance(secondService);
		tour.addActivity(secondAct);
	
		vehicleRoute = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),vehicle);
		
		stateManager = new StateManagerImpl();
	}
	
	@Test
	public void whenIteratingWithoutUpdate_itShouldUpdateNothing() {
		IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(cost);
		forwardInTime.iterate(vehicleRoute);
		
		assertEquals(0.0,firstAct.getArrTime(),0.1);
		assertEquals(0.0,firstAct.getEndTime(),0.1);
		
		assertEquals(0.0,secondAct.getArrTime(),0.1);
		assertEquals(0.0,secondAct.getEndTime(),0.1);
	}
	
	@Test
	public void whenIteratingWithActivityTimeUpdater_itShouldUpdateActivityTimes() {
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		forwardInTime.addActivityVisitor(new UpdateActivityTimes(cost));
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(10.0,firstAct.getArrTime(),0.1);
		assertEquals(10.0,firstAct.getEndTime(),0.1);
		
		assertEquals(30.0,secondAct.getArrTime(),0.1);
		assertEquals(30.0,secondAct.getEndTime(),0.1);
	}
	
	@Test
	public void whenIteratingWithLoadUpdateAtActLocations_itShouldUpdateLoad() {
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		forwardInTime.addActivityVisitor(new UpdateLoadAtAllLevels(stateManager));
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(5.0, stateManager.getActivityState(firstAct,StateIdFactory.LOAD).toDouble(), 0.01);
		assertEquals(10.0, stateManager.getActivityState(secondAct,StateIdFactory.LOAD).toDouble(), 0.01);
	}
	
	
	@Test
	public void testStatesOfAct0(){
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(0.0, vehicleRoute.getStart().getEndTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLocationId(), vehicleRoute.getStart().getLocationId());
		assertEquals(vehicleRoute.getVehicle().getEarliestDeparture(), vehicleRoute.getStart().getTheoreticalEarliestOperationStartTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLatestArrival(), vehicleRoute.getStart().getTheoreticalLatestOperationStartTime(),0.05);
		
	}
	
	@Test
	public void testStatesOfAct1(){
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		forwardInTime.addActivityVisitor(new UpdateLoadAtAllLevels(stateManager));
		forwardInTime.addActivityVisitor(new UpdateEarliestStartTimeWindowAtActLocations(stateManager, cost));
		forwardInTime.addActivityVisitor(new UpdateCostsAtAllLevels(new DefaultVehicleRoutingActivityCosts(), cost, stateManager));
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(10.0, stateManager.getActivityState(firstAct, StateIdFactory.COSTS).toDouble(),0.05);
		assertEquals(5.0, stateManager.getActivityState(firstAct, StateIdFactory.LOAD).toDouble(),0.05);
		assertEquals(10.0, stateManager.getActivityState(firstAct, StateIdFactory.EARLIEST_OPERATION_START_TIME).toDouble(),0.05);
//		assertEquals(20.0, states.getState(tour.getActivities().get(0)).getLatestOperationStart(),0.05);
	}
	
	@Test
	public void testStatesOfAct2(){
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		
		forwardInTime.addActivityVisitor(new UpdateLoadAtAllLevels(stateManager));
		forwardInTime.addActivityVisitor(new UpdateEarliestStartTimeWindowAtActLocations(stateManager, cost));
		forwardInTime.addActivityVisitor(new UpdateCostsAtAllLevels(new DefaultVehicleRoutingActivityCosts(), cost, stateManager));
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(30.0, stateManager.getActivityState(secondAct, StateIdFactory.COSTS).toDouble(),0.05);
		assertEquals(10.0, stateManager.getActivityState(secondAct, StateIdFactory.LOAD).toDouble(),0.05);
		assertEquals(30.0, stateManager.getActivityState(secondAct, StateIdFactory.EARLIEST_OPERATION_START_TIME).toDouble(),0.05);
//		assertEquals(40.0, states.getState(tour.getActivities().get(1)).getLatestOperationStart(),0.05);
	}
	
	@Test
	public void testStatesOfAct3(){
		RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
		
		forwardInTime.addActivityVisitor(new UpdateActivityTimes(cost));
		forwardInTime.addActivityVisitor(new UpdateCostsAtAllLevels(new DefaultVehicleRoutingActivityCosts(), cost, stateManager));
		forwardInTime.visit(vehicleRoute);
		
		assertEquals(40.0, stateManager.getRouteState(vehicleRoute,StateIdFactory.COSTS).toDouble(), 0.05);
		assertEquals(40.0, vehicleRoute.getEnd().getArrTime(),0.05);
		assertEquals(50.0, vehicleRoute.getEnd().getTheoreticalLatestOperationStartTime(),0.05);
	}
	
}
