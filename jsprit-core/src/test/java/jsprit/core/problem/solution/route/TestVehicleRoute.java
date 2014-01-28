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
package jsprit.core.problem.solution.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.driver.DriverImpl.NoDriver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;

import org.junit.Before;
import org.junit.Test;


public class TestVehicleRoute {
	
	private VehicleImpl vehicle;
	private NoDriver driver;

	@Before
	public void doBefore(){
		vehicle = VehicleImpl.Builder.newInstance("v").setLocationId("loc").setType(VehicleTypeImpl.Builder.newInstance("yo", 0).build()).build();
		driver = DriverImpl.noDriver();
	}

	@Test
	public void whenBuildingEmptyRouteCorrectly_go(){
		VehicleRoute route = VehicleRoute.Builder.newInstance(VehicleImpl.createNoVehicle(),DriverImpl.noDriver()).build();
		assertTrue(route!=null);
	}
	
	@Test
	public void whenBuildingEmptyRouteCorrectlyV2_go(){
		VehicleRoute route = VehicleRoute.emptyRoute();
		assertTrue(route!=null);
	}
	
	@Test
	public void whenBuildingEmptyRoute_ActivityIteratorIteratesOverZeroActivities(){
		VehicleRoute route = VehicleRoute.emptyRoute();
		Iterator<TourActivity> iter = route.getTourActivities().iterator();
		int count=0;
		while(iter.hasNext()){
			iter.next();
			count++;
		}
		assertEquals(0,count);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenBuildingRouteWithNulls_itThrowsException(){
		@SuppressWarnings("unused")
		VehicleRoute route = VehicleRoute.Builder.newInstance(null, null).build();
	}
	
	@Test
	public void whenBuildingANonEmptyTour2Times_tourIterIteratesOverActivitiesCorrectly(){
		VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
		routeBuilder.addService(Service.Builder.newInstance("2", 30).setLocationId("1").build());
		VehicleRoute route = routeBuilder.build();
		
		{
			Iterator<TourActivity> iter = route.getTourActivities().iterator();
			int count = 0;
			while(iter.hasNext()){
				@SuppressWarnings("unused")
				TourActivity act = iter.next();
				count++;
			}
			assertEquals(1,count);
		}
		{
			route.getTourActivities().addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("3", 30).setLocationId("1").build()));
			Iterator<TourActivity> iter = route.getTourActivities().iterator();
			int count = 0;
			while(iter.hasNext()){
				@SuppressWarnings("unused")
				TourActivity act = iter.next();
				count++;
			}
			assertEquals(2,count);
		}
	}
	
	@Test
	public void whenBuildingANonEmptyTour_tourReverseIterIteratesOverActivitiesCorrectly(){
		VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver).build();
		Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
		int count = 0;
		while(iter.hasNext()){
			@SuppressWarnings("unused")
			TourActivity act = iter.next();
			count++;
		}
		assertEquals(0,count);
	}
	
	@Test
	public void whenBuildingANonEmptyTourV2_tourReverseIterIteratesOverActivitiesCorrectly(){
		VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
		routeBuilder.addService(Service.Builder.newInstance("2", 30).setLocationId("1").build());
		VehicleRoute route = routeBuilder.build();
		Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
		int count = 0;
		while(iter.hasNext()){
			@SuppressWarnings("unused")
			TourActivity act = iter.next();
			count++;
		}
		assertEquals(1,count);
	}
	
	@Test
	public void whenBuildingANonEmptyTour2Times_tourReverseIterIteratesOverActivitiesCorrectly(){
		VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(vehicle, driver);
		routeBuilder.addService(Service.Builder.newInstance("2", 30).setLocationId("1").build());
		routeBuilder.addService(Service.Builder.newInstance("3", 30).setLocationId("2").build());
		VehicleRoute route = routeBuilder.build();
		{
			Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
			int count = 0;
			while(iter.hasNext()){
				TourActivity act = iter.next();
				if(count==0) {
					assertEquals("2",act.getLocationId());
				}
				count++;
			}
			assertEquals(2,count);
		}
		{
			Iterator<TourActivity> secondIter = route.getTourActivities().reverseActivityIterator();
			int count = 0;
			while(secondIter.hasNext()){
				TourActivity act = secondIter.next();
				if(count==0) {
					assertEquals("2",act.getLocationId());
				}
				count++;
			}
			assertEquals(2,count);
		}
	}
	
	public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectStartLocation(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getStart().getLocationId().equals("start"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectEndLocation(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getEnd().getLocationId().equals("end"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectStartLocation(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("start").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getStart().getLocationId().equals("start"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectEndLocation(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("start").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getEnd().getLocationId().equals("start"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectStartLocationV2(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setLocationId("start").setEndLocationId("start").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getStart().getLocationId().equals("start"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasSameStartAndEndLocation_routeMustHaveCorrectEndLocationV2(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setLocationId("start").setEndLocationId("start").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertTrue(vRoute.getEnd().getLocationId().equals("start"));
	}
	
	public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectDepartureTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertEquals(vRoute.getDepartureTime(),100.0,0.01);
		assertEquals(vRoute.getStart().getEndTime(),100.0,0.01);
	}
	
	public void whenBuildingRouteWithVehicleThatHasDifferentStartAndEndLocation_routeMustHaveCorrectEndTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		assertEquals(vRoute.getEnd().getArrTime(),100.0,0.01);
		assertEquals(vRoute.getEnd().getTheoreticalLatestOperationStartTime(),100.0,0.01);
	}
	
	public void whenSettingDepartureTimeInBetweenEarliestStartAndLatestArr_routeMustHaveCorrectDepartureTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(vehicle, 150.0);
		assertEquals(vRoute.getStart().getEndTime(),150.0,0.01);
		assertEquals(vRoute.getDepartureTime(),150.0,0.01);
	}
	
	public void whenSettingDepartureEarlierThanEarliestStart_routeMustHaveEarliestDepTimeAsDepTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(vehicle, 50.0);
		assertEquals(vRoute.getStart().getEndTime(),100.0,0.01);
		assertEquals(vRoute.getDepartureTime(),100.0,0.01);
	}
	
	public void whenSettingDepartureTimeLaterThanLatestArrival_routeMustHaveThisDepTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(vehicle, 50.0);
		assertEquals(vRoute.getStart().getEndTime(),100.0,0.01);
		assertEquals(vRoute.getDepartureTime(),100.0,0.01);
	}
	
	public void whenCreatingEmptyRoute_itMustReturnEmptyRoute(){
		@SuppressWarnings("unused")
		VehicleRoute route = VehicleRoute.emptyRoute();
		assertTrue(true);
	}
	
	public void whenIniRouteWithNewVehicle_startLocationMustBeCorrect(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocationId("new_start").setEndLocationId("new_end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
		assertEquals("new_start",vRoute.getStart().getLocationId());
	}
	
	public void whenIniRouteWithNewVehicle_endLocationMustBeCorrect(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocationId("new_start").setEndLocationId("new_end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
		assertEquals("new_end",vRoute.getStart().getLocationId());
	}

	public void whenIniRouteWithNewVehicle_depTimeMustBeEarliestDepTimeOfNewVehicle(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocationId("new_start").setEndLocationId("new_end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(new_vehicle, 50.0);
		assertEquals(1000.0,vRoute.getDepartureTime(),0.01);
	}
	
	public void whenIniRouteWithNewVehicle_depTimeMustBeSetDepTime(){
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setEarliestStart(100).setLatestArrival(200).setStartLocationId("start").setEndLocationId("end").build();
		Vehicle new_vehicle = VehicleImpl.Builder.newInstance("new_v").setEarliestStart(1000).setLatestArrival(2000).setStartLocationId("new_start").setEndLocationId("new_end").build();
		VehicleRoute vRoute = VehicleRoute.Builder.newInstance(vehicle, DriverImpl.noDriver()).build();
		vRoute.setVehicleAndDepartureTime(new_vehicle, 1500.0);
		assertEquals(1500.0,vRoute.getDepartureTime(),0.01);
	}
}
