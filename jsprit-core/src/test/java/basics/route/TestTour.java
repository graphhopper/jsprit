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
package basics.route;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import basics.Service;
import basics.Service.Builder;
import basics.route.ServiceActivity;
import basics.route.TourActivities;


public class TestTour {
	
	private Service service;
	private ServiceActivity act;
	private TourActivities tour;

	@Before
	public void doBefore(){
		service = Service.Builder.newInstance("yo", 10).setLocationId("loc").build();
		act = ServiceActivity.newInstance(service);
		tour = new TourActivities();
	}
	
	@Test
	public void whenAddingServiceAct_serviceActIsAdded(){
		assertFalse(tour.servesJob(service));
		tour.addActivity(act);
		assertTrue(tour.servesJob(service));
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenAddingServiceActTwice_serviceActIsAdded(){
		assertFalse(tour.servesJob(service));
		tour.addActivity(act);
		tour.addActivity(act);
	}
	
	@Test
	public void whenAddingServiceAndRemoveIt_tourShouldNotServeService(){
		assertFalse(tour.servesJob(service));
		tour.addActivity(act);
		assertTrue(tour.servesJob(service));
		tour.removeJob(service);
		assertFalse(tour.servesJob(service));
	}
	
	@Test
	public void noNameYet(){
		assertEquals(0, tour.getActivities().size());
		tour.addActivity(act);
		assertEquals(1, tour.getActivities().size());
		Service anotherServiceInstance = Service.Builder.newInstance("yo", 10).setLocationId("loc").build();
		assertTrue(service.equals(anotherServiceInstance));
		boolean removed = tour.removeJob(anotherServiceInstance);
		assertTrue(removed);
//		assertEquals(0, tour.getActivities().size());
	}
	
	
}
