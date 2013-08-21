/*******************************************************************************
 * Copyright (C) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.DriverImpl;
import basics.route.ServiceActivity;
import basics.route.TimeWindow;
import basics.route.TourActivities;
import basics.route.Vehicle;
import basics.route.VehicleImpl;
import basics.route.VehicleRoute;
import basics.route.VehicleType;
import basics.route.VehicleTypeImpl;


public class TestTourStateUpdaterWithService {

	TourActivities tour;

	Driver driver;

	Vehicle vehicle;

	TourActivities anotherTour;

	UpdateStates updateStates;
	
	StatesContainerImpl states;

	private VehicleRoute vehicleRoute;

	@Before
	public void setUp() {

		VehicleRoutingTransportCosts cost = new VehicleRoutingTransportCosts() {

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
		
		states = new StatesContainerImpl();
		
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("test", 0).build();
		vehicle = VehicleImpl.Builder.newInstance("testvehicle").setType(type).setLocationId("0,0")
				.setEarliestStart(0.0).setLatestArrival(50.0).build();
		
		tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(firstService));
		tour.addActivity(ServiceActivity.newInstance(secondService));
	
		
		updateStates = new UpdateStates(states, cost, new ExampleActivityCostFunction());
		
		vehicleRoute = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),vehicle);
	}
	
	@Test
	public void testCalculatedCost() {
		updateStates.update(vehicleRoute);
		assertEquals(40.0, states.getRouteState(vehicleRoute,StateTypes.COSTS).toDouble(), 0.05);
		assertEquals(10, states.getRouteState(vehicleRoute, StateTypes.LOAD).toDouble(), 0.05);
	}
	
	@Test
	public void testStatesOfAct0(){
		updateStates.update(vehicleRoute);
		assertEquals(0.0, vehicleRoute.getStart().getEndTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLocationId(), vehicleRoute.getStart().getLocationId());
		assertEquals(vehicleRoute.getVehicle().getEarliestDeparture(), vehicleRoute.getStart().getTheoreticalEarliestOperationStartTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLatestArrival(), vehicleRoute.getStart().getTheoreticalLatestOperationStartTime(),0.05);
		
	}
	
	@Test
	public void testStatesOfAct1(){
		updateStates.update(vehicleRoute);
		assertEquals(10.0, states.getActivityState(tour.getActivities().get(0), StateTypes.COSTS).toDouble(),0.05);
		assertEquals(5.0, states.getActivityState(tour.getActivities().get(0), StateTypes.LOAD).toDouble(),0.05);
//		assertEquals(10.0, states.getActivityState(tour.getActivities().get(0), StateTypes.EARLIEST_OPERATION_START_TIME).toDouble(),0.05);
		assertEquals(20.0, states.getActivityState(tour.getActivities().get(0), StateTypes.LATEST_OPERATION_START_TIME).toDouble(),0.05);
	}
	
	@Test
	public void testStatesOfAct2(){
		updateStates.update(vehicleRoute);
		
		assertEquals(30.0, states.getActivityState(tour.getActivities().get(1), StateTypes.COSTS).toDouble(),0.05);
		assertEquals(10.0, states.getActivityState(tour.getActivities().get(1), StateTypes.LOAD).toDouble(),0.05);
//		assertEquals(10.0, states.getActivityState(tour.getActivities().get(0), StateTypes.EARLIEST_OPERATION_START_TIME).toDouble(),0.05);
		assertEquals(40.0, states.getActivityState(tour.getActivities().get(1), StateTypes.LATEST_OPERATION_START_TIME).toDouble(),0.05);
	}
	
	@Test
	public void testStatesOfAct3(){
		updateStates.update(vehicleRoute);
		
		assertEquals(40.0, states.getRouteState(vehicleRoute, StateTypes.COSTS).toDouble(), 0.05);
		assertEquals(40.0, vehicleRoute.getEnd().getEndTime(),0.05);
		assertEquals(50.0, vehicleRoute.getEnd().getTheoreticalLatestOperationStartTime(),0.05);
	}


}
