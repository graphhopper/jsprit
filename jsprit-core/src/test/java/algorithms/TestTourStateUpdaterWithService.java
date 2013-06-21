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

	TourStateUpdater tdTourStatusProcessor;
	
	RouteStates states;

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
		
		states = new RouteStates();
		states.initialiseStateOfJobs(services);
		
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("test", 0).build();
		vehicle = VehicleImpl.Builder.newInstance("testvehicle").setType(type).setLocationId("0,0")
				.setEarliestStart(0.0).setLatestArrival(50.0).build();
		
		tour = new TourActivities();
		tour.addActivity(states.getActivity(firstService,true));
		tour.addActivity(states.getActivity(secondService,true));
	
		tdTourStatusProcessor = new TourStateUpdater(states, cost, new ExampleActivityCostFunction());
		
		
		vehicleRoute = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),vehicle);
	}
	
	@Test
	public void testCalculatedCost() {
		tdTourStatusProcessor.updateRoute(vehicleRoute);
		assertEquals(40.0, states.getRouteState(vehicleRoute).getCosts(), 0.05);
		assertEquals(10, states.getRouteState(vehicleRoute).getLoad());
	}
	
	@Test
	public void testStatesOfAct0(){
		tdTourStatusProcessor.updateRoute(vehicleRoute);
		assertEquals(0.0, vehicleRoute.getStart().getEndTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLocationId(), vehicleRoute.getStart().getLocationId());
		assertEquals(vehicleRoute.getVehicle().getEarliestDeparture(), vehicleRoute.getStart().getTheoreticalEarliestOperationStartTime(),0.05);
		assertEquals(vehicleRoute.getVehicle().getLatestArrival(), vehicleRoute.getStart().getTheoreticalLatestOperationStartTime(),0.05);
		
	}
	
	@Test
	public void testStatesOfAct1(){
		tdTourStatusProcessor.updateRoute(vehicleRoute);
		assertEquals(10.0, states.getState(tour.getActivities().get(0)).getCurrentCost(),0.05);
		assertEquals(5.0, states.getState(tour.getActivities().get(0)).getCurrentLoad(),0.05);
		assertEquals(10.0, states.getState(tour.getActivities().get(0)).getEarliestOperationStart(),0.05);
		assertEquals(20.0, states.getState(tour.getActivities().get(0)).getLatestOperationStart(),0.05);
	}
	
	@Test
	public void testStatesOfAct2(){
		tdTourStatusProcessor.updateRoute(vehicleRoute);
		assertEquals(30.0, states.getState(tour.getActivities().get(1)).getCurrentCost(),0.05);
		assertEquals(10.0, states.getState(tour.getActivities().get(1)).getCurrentLoad(),0.05);
		assertEquals(30.0, states.getState(tour.getActivities().get(1)).getEarliestOperationStart(),0.05);
		assertEquals(40.0, states.getState(tour.getActivities().get(1)).getLatestOperationStart(),0.05);
	}
	
	@Test
	public void testStatesOfAct3(){
		tdTourStatusProcessor.updateRoute(vehicleRoute);
		assertEquals(40.0, states.getRouteState(vehicleRoute).getCosts(), 0.05);
		assertEquals(40.0, vehicleRoute.getEnd().getEndTime(),0.05);
		assertEquals(50.0, vehicleRoute.getEnd().getTheoreticalLatestOperationStartTime(),0.05);
	}

//	public void testEarliestArrStart() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(0.0, tour.getActivities().get(0)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testLatestArrStart() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(0.0, tour.getActivities().get(0)
//				.getLatestOperationStartTime());
//	}
//
//	public void testEarliestArrAtFirstPickup() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(10.0, tour.getActivities().get(1)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testEarliestArrAtFirstPickupWithTDCost() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(10.0, tour.getActivities().get(1)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testLatestArrAtFirstPickup() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(10.0, tour.getActivities().get(1)
//				.getLatestOperationStartTime());
//	}
//
//	public void testLatestArrAtFirstPickupWithTDCost() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(12.0, tour.getActivities().get(1)
//				.getLatestOperationStartTime());
//	}
//
//	public void testEarliestArrAtSecondPickup() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(30.0, tour.getActivities().get(2)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testEarliestArrAtSecondPickupWithTDCosts() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(30.0, tour.getActivities().get(2)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testLatestArrAtSecondPickup() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(30.0, tour.getActivities().get(2)
//				.getLatestOperationStartTime());
//	}
//
//	public void testLatestArrAtSecondPickupWithTDCosts() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(30.0, tour.getActivities().get(2)
//				.getLatestOperationStartTime());
//	}
//
//	public void testEarliestArrAtEnd() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(40.0, tour.getActivities().get(5)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testEarliestArrAtEndWithTDCosts() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(35.0, tour.getActivities().get(5)
//				.getEarliestOperationStartTime());
//	}
//
//	public void testLatestArrAtEnd() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(Double.MAX_VALUE, tour.getActivities().get(5)
//				.getLatestOperationStartTime());
//	}
//
//	public void testLatestArrAtEndWithTDCosts() {
//		tdTourStatusProcessor.calculate(tour, vehicle, driver);
//		assertEquals(Double.MAX_VALUE, tour.getActivities().get(5)
//				.getLatestOperationStartTime());
//	}

}
