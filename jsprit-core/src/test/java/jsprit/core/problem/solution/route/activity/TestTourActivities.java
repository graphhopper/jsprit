/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Location;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class TestTourActivities {

    private Service service;
    private ServiceActivity act;
    private TourActivities tour;

    @Before
    public void doBefore() {
        service = Service.Builder.newInstance("yo").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        act = ServiceActivity.newInstance(service);
        tour = new TourActivities();
    }

    @Test
    public void whenAddingServiceAct_serviceActIsAdded() {
        assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        assertTrue(tour.servesJob(service));
    }

    @Test(expected = IllegalStateException.class)
    public void whenAddingServiceActTwice_anExceptionIsThrown() {
        assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        tour.addActivity(act);
    }

    @Test
    public void whenAddingServiceAndRemovingItImmediately_tourShouldNotServeServiceAnymore() {
        assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        assertTrue(tour.servesJob(service));
        tour.removeJob(service);
        assertFalse(tour.servesJob(service));
    }

    @Test
    public void whenAddingAServiceAndThenRemovingTheServiceAgain_tourShouldNotServeItAnymore() {
        assertEquals(0, tour.getActivities().size());
        tour.addActivity(act);
        assertEquals(1, tour.getActivities().size());
        Service anotherServiceInstance = Service.Builder.newInstance("yo").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        assertTrue(service.equals(anotherServiceInstance));
        boolean removed = tour.removeJob(anotherServiceInstance);
        assertTrue(removed);
        assertEquals(0, tour.getActivities().size());
    }

    @Test
    public void whenAddingAShipmentActivity_tourShouldServeShipment() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setDeliveryLocation(Location.newInstance("delLoc"))
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);
        assertTrue(tour.servesJob(s));
        assertEquals(2, tour.getActivities().size());
    }


    @Test
    public void whenRemovingShipment_tourShouldNotServiceItAnymore() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1).setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        tour.removeJob(s);
        assertFalse(tour.servesJob(s));
    }


    @Test
    public void whenRemovingShipment_theirCorrespondingActivitiesShouldBeRemoved() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setDeliveryLocation(Location.newInstance("delLoc"))
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(2, tour.getActivities().size());
        tour.removeJob(s);
        assertEquals(0, tour.getActivities().size());
    }

    @Test
    public void removingActivityShouldWork() {
        tour.addActivity(act);
        assertTrue(tour.servesJob(service));
        assertTrue(tour.hasActivity(act));

        tour.removeActivity(act);

        assertTrue(tour.isEmpty());
        assertFalse(tour.hasActivity(act));
        assertFalse(tour.servesJob(service));
        assertEquals(0, tour.jobSize());
    }

    @Test
    public void copyingSeqShouldWork() {
        tour.addActivity(act);

        assertTrue(tour.servesJob(service));
        assertTrue(tour.hasActivity(act));

        TourActivities acts = TourActivities.copyOf(tour);

        assertTrue(acts.servesJob(service));
        assertTrue(acts.hasActivity(act));
    }

    @Test
    public void removingShipmentActivityShouldWork() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setDeliveryLocation(Location.newInstance("delLoc"))
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.hasActivity(pickupShipment));
        assertTrue(tour.hasActivity(deliverShipment));

        tour.removeActivity(pickupShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(1, tour.getActivities().size());
        assertTrue(tour.hasActivity(deliverShipment));
        assertFalse(tour.hasActivity(pickupShipment));
        assertFalse(tour.getActivities().contains(pickupShipment));

    }

    @Test
    public void whenCopyingShipmentActivitySeq_jobSizeShouldBeCorrect() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setDeliveryLocation(Location.newInstance("delLoc"))
            .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.hasActivity(pickupShipment));
        assertTrue(tour.hasActivity(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertEquals(1, copiedTour.jobSize());
    }

    @Test
    public void whenCopyingShipmentActivitySeq_noActivitiesShouldBeCorrect() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.hasActivity(pickupShipment));
        assertTrue(tour.hasActivity(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertEquals(2, copiedTour.getActivities().size());
    }

    @Test
    public void whenCopyingShipmentActivitySeq_itShouldContaintPickupAct() {
        Shipment s = Shipment.Builder.newInstance("s").addSizeDimension(0, 1)
            .setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourShipmentActivityFactory fac = new DefaultShipmentActivityFactory();
        TourActivity pickupShipment = fac.createPickup(s);
        TourActivity deliverShipment = fac.createDelivery(s);
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.hasActivity(pickupShipment));
        assertTrue(tour.hasActivity(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertTrue(copiedTour.servesJob(s));
    }

}
