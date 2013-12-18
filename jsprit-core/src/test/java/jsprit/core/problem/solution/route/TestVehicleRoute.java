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
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivities;
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
		VehicleRoute route = VehicleRoute.newInstance(TourActivities.emptyTour(),DriverImpl.noDriver(),VehicleImpl.noVehicle());
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
	
	@Test(expected=IllegalStateException.class)
	public void whenBuildingEmptyRoute_(){
		@SuppressWarnings("unused")
		VehicleRoute route = VehicleRoute.newInstance(null,null,null);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenBuildingRouteWithNonEmptyTour_throwException(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("jo", 10).build()));
		@SuppressWarnings("unused")
		VehicleRoute route = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),VehicleImpl.noVehicle());
	}
	
	@Test
	public void whenBuildingEmptyTour_tourIterIteratesOverAnEmptyList(){
		TourActivities tour = new TourActivities();
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").setType(VehicleTypeImpl.Builder.newInstance("yo", 0).build()).build();
		VehicleRoute route = VehicleRoute.newInstance(tour,DriverImpl.noDriver(),v);
		Iterator<TourActivity> iter = route.getTourActivities().iterator();
		int count = 0;
		while(iter.hasNext()){
			@SuppressWarnings("unused")
			TourActivity act = iter.next();
			count++;
		}
		assertEquals(0,count);
	}
	
	@Test
	public void whenBuildingANonEmptyTour_tourIterIteratesOverActivitiesCorrectly(){
		TourActivities tour = new TourActivities();
		tour.addActivity(Start.newInstance("", 0, 0));
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
		Iterator<TourActivity> iter = route.getTourActivities().iterator();
		int count = 0;
		while(iter.hasNext()){
			@SuppressWarnings("unused")
			TourActivity act = iter.next();
			count++;
		}
		assertEquals(1,count);
	}
	
	
	@Test
	public void whenBuildingANonEmptyTour2Times_tourIterIteratesOverActivitiesCorrectly(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("2", 30).setLocationId("1").build()));
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
		
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
			tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("3", 30).setLocationId("1").build()));
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
		TourActivities tour = new TourActivities();
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
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
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("2", 30).setLocationId("1").build()));
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
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
	public void whenBuildingANonEmptyTourV3_tourReverseIterIteratesOverActivitiesCorrectly(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("2", 30).setLocationId("1").build()));
		ServiceActivity del = ServiceActivity.newInstance(Service.Builder.newInstance("3", 30).setLocationId("1").build());
		tour.addActivity(del);
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
		Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
		int count = 0;
		TourActivity memAct = null;
		while(iter.hasNext()){
			TourActivity act = iter.next();
			if(count==0) memAct = act;
			count++;
		}
		assertEquals(memAct,del);	
	}
	
	@Test
	public void whenBuildingANonEmptyTourV4_tourReverseIterIteratesOverActivitiesCorrectly(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("2", 30).setLocationId("1").build()));
		ServiceActivity del = ServiceActivity.newInstance(Service.Builder.newInstance("3", 30).setLocationId("1").build());
		tour.addActivity(del);
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
		Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
		int count = 0;
		TourActivity memAct = null;
		while(iter.hasNext()){
			TourActivity act = iter.next();
			if(count==0) memAct = act;
			count++;
		}
		assertEquals(memAct,del);
		assertEquals(2,count);
	}
	
	@Test
	public void whenBuildingANonEmptyTour2Times_tourReverseIterIteratesOverActivitiesCorrectly(){
		TourActivities tour = new TourActivities();
		tour.addActivity(ServiceActivity.newInstance(Service.Builder.newInstance("2", 30).setLocationId("1").build()));
		ServiceActivity del = ServiceActivity.newInstance(Service.Builder.newInstance("3", 30).setLocationId("1").build());
		tour.addActivity(del);
		VehicleRoute route = VehicleRoute.newInstance(tour, driver, vehicle);
		{
			Iterator<TourActivity> iter = route.getTourActivities().reverseActivityIterator();
			int count = 0;
			TourActivity memAct = null;
			while(iter.hasNext()){
				TourActivity act = iter.next();
				if(count==0) memAct = act;
				count++;
			}
			assertEquals(memAct,del);
			assertEquals(2,count);
		}
		{
			Iterator<TourActivity> secondIter = route.getTourActivities().reverseActivityIterator();
			int count = 0;
			TourActivity memAct = null;
			while(secondIter.hasNext()){
				TourActivity act = secondIter.next();
				if(count==0) memAct = act;
				count++;
			}
			assertEquals(memAct,del);
			assertEquals(2,count);
		}
	}

}
