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
//package algorithms;
//
//import static org.hamcrest.CoreMatchers.is;
//import static org.junit.Assert.assertThat;
//
//import java.util.ArrayList;
//import java.util.Collection;
//
//import org.junit.Before;
//import org.junit.Test;
//
//import util.ManhattanDistanceCalculator;
//import algorithms.FindCheaperVehicleAlgo;
//import algorithms.TourStateUpdater;
//import basics.Coordinate;
//import basics.Driver;
//import basics.Service;
//import basics.TimeWindow;
//import basics.Tour;
//import basics.TourBuilder;
//import basics.Vehicle;
//import basics.VehicleFleetManager;
//import basics.VehicleFleetManagerImpl;
//import basics.VehicleImpl;
//import basics.VehicleRoute;
//import basics.VehicleRoutingCosts;
//import basics.VehicleImpl.Type;
//
//
//public class FindCheaperVehicleTest {
//	
//	Tour tour;
//
//	Vehicle heavyVehicle;
//
//	Vehicle lightVehicle;
//	
//	VehicleRoutingCosts cost;
//
//	@Before
//	public void setUp(){
//		
//		cost = new VehicleRoutingCosts() {
//			
//			@Override
//			public double getBackwardTransportTime(String fromId, String toId,
//					double arrivalTime, Driver driver, Vehicle vehicle) {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//			
//			@Override
//			public double getBackwardTransportCost(String fromId, String toId,
//					double arrivalTime, Driver driver, Vehicle vehicle) {
//				// TODO Auto-generated method stub
//				return 0;
//			}
//			
//			@Override
//			public double getTransportCost(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
//				
//				String[] fromTokens = fromId.split(",");
//				String[] toTokens = toId.split(",");
//				double fromX = Double.parseDouble(fromTokens[0]);
//				double fromY = Double.parseDouble(fromTokens[1]);
//				
//				double toX = Double.parseDouble(toTokens[0]);
//				double toY = Double.parseDouble(toTokens[1]);
//				
//				return vehicle.getType().vehicleCostParams.perDistanceUnit*ManhattanDistanceCalculator.calculateDistance(new Coordinate(fromX, fromY), new Coordinate(toX, toY));
//			}
//			
//			@Override
//			public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {		
//				return 0;
//			}
//		};
//		
//		Type lightType = VehicleImpl.TypeBuilder.newInstance().setId("light").setCapacity(10).setFixedCost(1.0).setCostPerDistance(1.0).build();
//		lightVehicle = VehicleImpl.VehicleBuilder.newInstance("light").setLocationId("0,0").setType(lightType).build();
//		
//		Type heavyType = VehicleImpl.TypeBuilder.newInstance().setId("heavy").setCapacity(10).setFixedCost(2.0).setCostPerDistance(2.0).build();
//		heavyVehicle = VehicleImpl.VehicleBuilder.newInstance("heavy").setLocationId("0,0").setType(heavyType).build();
//	}
//	
//	@Test
//	public void runCheaperVehicle_lightIsCheaperThanHeavy_changeVehicle(){
//		TourStateUpdater tourStateCalculator = new TourStateUpdater(cost, new ExampleTransportCostFunction());
//		
//		TourBuilder tourBuilder = new TourBuilder();
//		Service firstShipment = getService("10,0");
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.scheduleDeliveryService(firstShipment);
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		Tour tour = tourBuilder.build();
//		
//		VehicleRoute route = new VehicleRoute(tour,new Driver(){},heavyVehicle);
//		tourStateCalculator.updateTour(route);
//		
//		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
//		vehicles.add(lightVehicle);
//		vehicles.add(heavyVehicle);
//		VehicleFleetManager fleetManager = new VehicleFleetManagerImpl(vehicles);
//		fleetManager.lock(heavyVehicle);
//		
//		FindCheaperVehicleAlgo findCheaperVehicle = new FindCheaperVehicleAlgo(fleetManager, tourStateCalculator);
//		VehicleRoute newRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
//
//		assertThat(lightVehicle, is(newRoute.getVehicle()));
//	}
//	
//	@Test
//	public void runCheaperVehicle_costComparisonBetweenHeavyAndLight_keepHeavy(){
//		
//
//		Type lightType = VehicleImpl.TypeBuilder.newInstance().setId("light").setCapacity(10).setFixedCost(1.0).setCostPerDistance(1.0).build();
//		lightVehicle = VehicleImpl.VehicleBuilder.newInstance("light").setLocationId("0,0").setType(lightType).build();
//		
//		Type heavyType = VehicleImpl.TypeBuilder.newInstance().setId("heavy").setCapacity(10).setFixedCost(2.0).setCostPerDistance(1.0).build();
//		heavyVehicle = VehicleImpl.VehicleBuilder.newInstance("heavy").setLocationId("0,0").setType(heavyType).build();
//		
//		
//		TourStateUpdater tourStateCalculator = new TourStateUpdater(cost, new ExampleTransportCostFunction());
//		
//		TourBuilder tourBuilder = new TourBuilder();
//		Service firstShipment = getService("10,0");
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.scheduleDeliveryService(firstShipment);
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		Tour tour = tourBuilder.build();
//		
//		
//		VehicleRoute route = new VehicleRoute(tour,new Driver(){},heavyVehicle);
//		tourStateCalculator.updateTour(route);
//		
//		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
//		vehicles.add(lightVehicle);
//		vehicles.add(heavyVehicle);
//		VehicleFleetManager fleetManager = new VehicleFleetManagerImpl(vehicles);
//		fleetManager.lock(heavyVehicle);
//		
//		FindCheaperVehicleAlgo findCheaperVehicle = new FindCheaperVehicleAlgo(fleetManager, tourStateCalculator);
//		findCheaperVehicle.setWeightFixCosts(0.0);
//		VehicleRoute newRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
//		
//		assertThat(heavyVehicle, is(newRoute.getVehicle()));
//			
//	}
//	
//	@Test
//	public void runCheaperVehicle_lightIsTheCheapest_doNotChangeVehicle(){
//		TourBuilder tourBuilder = new TourBuilder();
//		Service firstShipment = getService("10,0");
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.scheduleDeliveryService(firstShipment);
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		VehicleRoute route = new VehicleRoute(tourBuilder.build(),new Driver(){},lightVehicle);
//		
//		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
//		vehicles.add(lightVehicle);
//		vehicles.add(heavyVehicle);
//		VehicleFleetManager fleetManager = new VehicleFleetManagerImpl(vehicles);
//		fleetManager.lock(heavyVehicle);
//		
//		TourStateUpdater tourStateCalculator = new TourStateUpdater(cost, new ExampleTransportCostFunction());
//		FindCheaperVehicleAlgo findCheaperVehicle = new FindCheaperVehicleAlgo(fleetManager, tourStateCalculator);
//		VehicleRoute newRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
//		
//		assertThat(lightVehicle, is(newRoute.getVehicle()));
//		
//		
//	}
//	
//	@Test
//	public void runCheaperVehicle_noAlterativeVehicle_doNotChangeVehicle(){
//		TourBuilder tourBuilder = new TourBuilder();
//		Service firstShipment = getService("10,0");
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.scheduleDeliveryService(firstShipment);
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		VehicleRoute route = new VehicleRoute(tourBuilder.build(),new Driver(){},heavyVehicle);
//		
//		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
////		vehicles.add(lightVehicle);
//		vehicles.add(heavyVehicle);
//		VehicleFleetManager fleetManager = new VehicleFleetManagerImpl(vehicles);
//		fleetManager.lock(heavyVehicle);
//		
//		TourStateUpdater tourStateCalculator = new TourStateUpdater(cost, new ExampleTransportCostFunction());
//		FindCheaperVehicleAlgo findCheaperVehicle = new FindCheaperVehicleAlgo(fleetManager, tourStateCalculator);
//		VehicleRoute newRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
//		
//		
//		assertThat(heavyVehicle, is(newRoute.getVehicle()));
//		
//	}
//	
//	@Test
//	public void runCheaperVehicle_noTour_throwException(){
//		TourBuilder tourBuilder = new TourBuilder();
//		Service firstShipment = getService("10,0");
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.scheduleDeliveryService(firstShipment);
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		VehicleRoute route = new VehicleRoute(null,null,heavyVehicle);
//		
//		Collection<Vehicle> vehicles = new ArrayList<Vehicle>();
////		vehicles.add(lightVehicle);
//		vehicles.add(heavyVehicle);
//		VehicleFleetManager fleetManager = new VehicleFleetManagerImpl(vehicles);
//		fleetManager.lock(heavyVehicle);
//		
//		TourStateUpdater tourStateCalculator = new TourStateUpdater(cost, new ExampleTransportCostFunction());
//		FindCheaperVehicleAlgo findCheaperVehicle = new FindCheaperVehicleAlgo(fleetManager, tourStateCalculator);
//		VehicleRoute newRoute = findCheaperVehicle.runAndGetVehicleRoute(route);
//		
//		assertThat(heavyVehicle, is(newRoute.getVehicle())); 
//	}
//	
//	private Service getService(String to, double serviceTime) {
//		Service s = Service.Builder.newInstance("s", 0).setLocationId(to).setServiceTime(serviceTime).setTimeWindow(TimeWindow.newInstance(0.0, 20.0)).build(); 
//		return s;
//	}
//	
//	private Service getService(String to) {
//		Service s = getService(to, 0.0);
//		return s;
//	}
//	
//	
//	
//		
//	
//}
