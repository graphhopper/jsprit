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
package basics.route;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import basics.Service;
import basics.Shipment;


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
	public void whenAddingServiceActTwice_anExceptionIsThrown(){
		assertFalse(tour.servesJob(service));
		tour.addActivity(act);
		tour.addActivity(act);
	}
	
	@Test
	public void whenAddingServiceAndRemovingItImmediately_tourShouldNotServeServiceAnymore(){
		assertFalse(tour.servesJob(service));
		tour.addActivity(act);
		assertTrue(tour.servesJob(service));
		tour.removeJob(service);
		assertFalse(tour.servesJob(service));
	}
	
	@Test
	public void whenAddingAServiceAndThenRemovingTheServiceAgain_tourShouldNotServeItAnymore(){
		assertEquals(0, tour.getActivities().size());
		tour.addActivity(act);
		assertEquals(1, tour.getActivities().size());
		Service anotherServiceInstance = Service.Builder.newInstance("yo", 10).setLocationId("loc").build();
		assertTrue(service.equals(anotherServiceInstance));
		boolean removed = tour.removeJob(anotherServiceInstance);
		assertTrue(removed);
		assertEquals(0, tour.getActivities().size());
	}
	
	@Test
	public void whenAddingAShipmentActivity_tourShouldServeShipment(){
		Shipment s = Shipment.Builder.newInstance("s", 1).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
		TourActivity pickupShipment = fac.createPickup(s);
		TourActivity deliverShipment = fac.createDelivery(s);
		tour.addActivity(pickupShipment);
		tour.addActivity(deliverShipment);
		assertTrue(tour.servesJob(s));
		assertEquals(2,tour.getActivities().size());
	}
	
	
	
	@Test
	public void whenRemovingShipment_tourShouldNotServiceItAnymore(){
		Shipment s = Shipment.Builder.newInstance("s", 1).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
		TourActivity pickupShipment = fac.createPickup(s);
		TourActivity deliverShipment = fac.createDelivery(s);
		tour.addActivity(pickupShipment);
		tour.addActivity(deliverShipment);
		
		tour.removeJob(s);
		assertFalse(tour.servesJob(s));
	}

	
	@Test
	public void whenRemovingShipment_theirCorrespondingActivitiesShouldBeRemoved(){
		Shipment s = Shipment.Builder.newInstance("s", 1).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
		TourActivity pickupShipment = fac.createPickup(s);
		TourActivity deliverShipment = fac.createDelivery(s);
		tour.addActivity(pickupShipment);
		tour.addActivity(deliverShipment);
		
		assertEquals(2, tour.getActivities().size());
		tour.removeJob(s);
		assertEquals(0, tour.getActivities().size());
	}
	
}
