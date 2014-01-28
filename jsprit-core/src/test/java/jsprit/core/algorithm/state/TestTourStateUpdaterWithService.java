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
package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivities;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.ManhattanDistanceCalculator;

import org.junit.Before;
import org.junit.Test;



public class TestTourStateUpdaterWithService {

	TourActivities tour;

	Driver driver;

	Vehicle vehicle;

	TourActivities anotherTour;

	
	StateManager states;

	private VehicleRoute vehicleRoute;

	private ServiceActivity act1;

	private ServiceActivity act2;

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
		
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("test", 10).build();
		vehicle = VehicleImpl.Builder.newInstance("testvehicle").setType(type).setLocationId("0,0")
				.setEarliestStart(0.0).setLatestArrival(50.0).build();
		
		
		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addAllJobs(services).addVehicle(vehicle).setRoutingCost(cost).build();
		
		states = new StateManager(vrp);
		states.updateLoadStates();
		states.updateTimeWindowStates();
		states.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), states));
		states.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts()));
		
		act1 = ServiceActivity.newInstance(firstService);
		act2 = ServiceActivity.newInstance(secondService);
		
		vehicleRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();//.newInstance(tour,DriverImpl.noDriver(),vehicle);
		vehicleRoute.getTourActivities().addActivity(act1);
		vehicleRoute.getTourActivities().addActivity(act2);
	}
	
	@Test
	public void testCalculatedCost() {
		states.informInsertionStarts(Arrays.asList(vehicleRoute), null);
		assertEquals(40.0, states.getRouteState(vehicleRoute,StateFactory.COSTS).toDouble(), 0.05);
		assertEquals(10, states.getRouteState(vehicleRoute, StateFactory.LOAD_AT_END).toDouble(), 0.05);
	}
	
	@Test
	public void testStatesOfAct0(){
		states.informInsertionStarts(Arrays.asList(vehicleRoute), null);
		assertEquals(0.0, vehicleRoute.getStart().getEndTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLocationId(), vehicleRoute.getStart().getLocationId());
		assertEquals(vehicleRoute.getVehicle().getEarliestDeparture(), vehicleRoute.getStart().getTheoreticalEarliestOperationStartTime(),0.05);
		assertEquals(Double.MAX_VALUE, vehicleRoute.getStart().getTheoreticalLatestOperationStartTime(),0.05);
		
	}
	
	@Test
	public void testStatesOfAct1(){
		states.informInsertionStarts(Arrays.asList(vehicleRoute), null);
		assertEquals(10.0, states.getActivityState(act1, StateFactory.COSTS).toDouble(),0.05);
		assertEquals(5.0, states.getActivityState(act1, StateFactory.LOAD).toDouble(),0.05);
		assertEquals(20.0, states.getActivityState(act1, StateFactory.LATEST_OPERATION_START_TIME).toDouble(),0.05);
	}
	
	@Test
	public void testStatesOfAct2(){
		states.informInsertionStarts(Arrays.asList(vehicleRoute), null);
		assertEquals(30.0, states.getActivityState(act2, StateFactory.COSTS).toDouble(),0.05);
		assertEquals(10.0, states.getActivityState(act2, StateFactory.LOAD).toDouble(),0.05);
		assertEquals(40.0, states.getActivityState(act2, StateFactory.LATEST_OPERATION_START_TIME).toDouble(),0.05);
	}
	
	@Test
	public void testStatesOfAct3(){
		states.informInsertionStarts(Arrays.asList(vehicleRoute), null);
		assertEquals(40.0, states.getRouteState(vehicleRoute, StateFactory.COSTS).toDouble(), 0.05);
		assertEquals(40.0, vehicleRoute.getEnd().getArrTime(),0.05);
		assertEquals(50.0, vehicleRoute.getEnd().getTheoreticalLatestOperationStartTime(),0.05);
	}


}
