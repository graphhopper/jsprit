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
//package algorithms;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//
//import junit.framework.TestCase;
//
//import org.junit.Test;
//
//import util.EuclideanDistanceCalculator;
//import basics.Coordinate;
//import basics.Driver;
//import basics.Job;
//import basics.Service;
//import basics.TimeWindow;
//import basics.Tour;
//import basics.TourActivity;
//import basics.TourBuilder;
//import basics.Vehicle;
//import basics.VehicleRoutingTransportCosts;

//
//
//public class TestAuxilliaryCostCalculatorWithServices extends TestCase{
//	
//	AuxilliaryCostCalculator costCalc;
//	
//	Tour tour;
//	
//	public void setUp(){
//		
//		VehicleRoutingTransportCosts cost = new VehicleRoutingTransportCosts() {
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
//				String[] fromTokens = fromId.split(",");
//				String[] toTokens = toId.split(",");
//				double fromX = Double.parseDouble(fromTokens[0]);
//				double fromY = Double.parseDouble(fromTokens[1]);
//				
//				double toX = Double.parseDouble(toTokens[0]);
//				double toY = Double.parseDouble(toTokens[1]);
//				
//				return EuclideanDistanceCalculator.calculateDistance(new Coordinate(fromX, fromY), new Coordinate(toX, toY));
//			}
//			
//			@Override
//			public double getTransportTime(String fromId, String toId, double departureTime, Driver driver, Vehicle vehicle) {
//				
//				return 0;
//			}
//		};
//		
//		costCalc = AuxilliaryCostCalculator.newInstance(cost, new ExampleTransportCostFunction());
//		
//		Service firstService = Service.Builder.newInstance("1", 0).setLocationId("10,0").setTimeWindow(TimeWindow.newInstance(0, 20)).build();
//		Service secondService = Service.Builder.newInstance("2", 0).setLocationId("0,10").setTimeWindow(TimeWindow.newInstance(0, 20)).build();
//		
//		Collection<Job> services = new ArrayList<Job>();
//		services.add(firstService);
//		services.add(secondService);
//		
//		ActivityStates states = new ActivityStates();
//		states.initialiseStateOfJobs(services);
//		
//		TourBuilder tourBuilder = new TourBuilder();
//		
//		tourBuilder.scheduleStart("0,0", 0.0, Double.MAX_VALUE);
//		tourBuilder.addActivity(states.getActivity(firstService,true));
//		tourBuilder.addActivity(states.getActivity(secondService,true));
//		tourBuilder.scheduleEnd("0,0", 0.0, Double.MAX_VALUE);
//		
//		tour = tourBuilder.build();
//		costCalc.setActivityStates(states);
//		
//	}
//
////	@Test
////	public void testGetPath(){
////		List<TourActivity> path = AuxilliaryCostCalculator.getPath(tour,tour.getStart(), tour.getActivities().get(1));
////		assertEquals(3,path.size());
////	}
////	
////	@Test
////	public void testGetPath_withEnd(){
////		List<TourActivity> path = AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(0), tour.getEnd());
////		assertEquals(3,path.size());
////	}
//	
//	
////	public void testCalcTourCost(){
////		List<TourActivity> path = AuxilliaryCostCalculator.getPath(tour,tour.getStart(), tour.getActivities().get(1));
////		assertEquals(0.0, costCalc.costOfPath(path,0.0,null,null));
////	}
//	
////	public void testCalcTourCost2(){
////		assertEquals(10.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(0), tour.getActivities().get(2)),0.0,null,null));
////	}
////	
////	public void testCalcTourCost3(){
////		assertEquals(20.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(2), tour.getActivities().get(6)),0.0,null,null));
////	}
////	
////	public void testCalcTourCost4(){
////		assertEquals(30.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(0), tour.getActivities().get(6)),0.0,null,null));
////	}
////	
////	public void testCalcTourCost5(){
////		assertEquals(40.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(1), tour.getActivities().get(7)),0.0,null,null));
////	}
//	
////	public void testCalcTourCost6(){
////		assertEquals(0.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(1), tour.getActivities().get(1)),0.0,null,null));
////	}
////	
////	public void testCalcTourCost7(){
////		try{
////			double c =costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(1), tour.getActivities().get(0)),0.0,null,null);
////			assertTrue(false);
////		}
////		catch(AssertionError e){
////			assertTrue(true);
////		}
////		catch(IllegalArgumentException e){
////			assertTrue(true);
////		}
////	}
////	
////	public void testCalcTourCost8(){
////		try{
////			Shipment s = getShipment("10,10","0,10");
////			TourActivity pickup = new Pickup(s);
////			
////			double c = costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(0), pickup),0.0,null,null);
////			assertTrue(false);
////		}
////		catch(AssertionError e){
////			assertTrue(true);
////		}
////		catch(IllegalArgumentException e){
////			assertTrue(true);
////		}
////	}
////	
////	public void testBoundary1(){
////		assertEquals(40.0, costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(1), tour.getActivities().get(tour.getActivities().size()-1)),0.0,null,null));
////	}
////	
////	public void testBoundary2(){
////		try{
////			costCalc.costOfPath(AuxilliaryCostCalculator.getPath(tour,tour.getActivities().get(tour.getActivities().size()-1), tour.getActivities().get(0)),0.0,null,null);
////			assertTrue(false);
////		}
////		catch(AssertionError e){
////			assertTrue(true);
////		}
////		catch(IllegalArgumentException e){
////			assertTrue(true);
////		}
////	}
////	
//////	public void testBoundary3(){
//////		assertEquals(40.0, costCalc.calculateCost(tour, tour.getActivities().getFirst(), tour.getActivities().getLast(), Double.MAX_VALUE, null, null));
//////	}
//////	
//////	public void testBoundary4(){
//////		try{
//////			costCalc.calculateCost(tour, tour.getActivities().getFirst(), tour.getActivities().getLast(), (-1)*Double.MAX_VALUE, null, null);
//////			assertTrue(false);
//////		}
//////		catch(AssertionError e){
//////			assertTrue(true);
//////		}
//////	}
////	
////	
////	
////	private Shipment getShipment(String string, String string2) {
////		Shipment s = Shipment.Builder.newInstance("first", 0).setFromId(string).setToId(string2).setPickupTW(TimeWindow.newInstance(0.0, 20.0)).setDeliveryTW(TimeWindow.newInstance(0.0, 20.0)).build();
////		return s;
////	}
////
//}
