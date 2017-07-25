/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.ServiceJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;


public class TestTourActivities {

    private ServiceJob service;
    private ServiceActivity act;
    private TourActivities tour;

    @Before
    public void doBefore() {
        Location loc = Location.newInstance("loc");
        service = new ServiceJob.Builder("yo").setLocation(loc).build();
        act = new ServiceActivity(service, "s1",
                        loc, 0d, SizeDimension.Builder.newInstance().addDimension(0, 10).build(),
                        TimeWindows.ANY_TIME.getTimeWindows());
        tour = new TourActivities();
    }

    @Test
    public void whenAddingServiceAct_serviceActIsAdded() {
        assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        assertTrue(tour.servesJob(service));
    }

    @Test(expected = IllegalArgumentException.class)
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
        ServiceJob anotherServiceInstance = new ServiceJob.Builder("yo").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        assertTrue(service.equals(anotherServiceInstance));
        boolean removed = tour.removeJob(anotherServiceInstance);
        assertTrue(removed);
        assertEquals(0, tour.getActivities().size());
    }

    @Test
    public void whenAddingAShipmentActivity_tourShouldServeShipment() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1).setDeliveryLocation(Location.newInstance("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);
        assertTrue(tour.servesJob(s));
        assertEquals(2, tour.getActivities().size());
    }


    @Test
    public void whenRemovingShipment_tourShouldNotServiceItAnymore() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build())
                        .build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        tour.removeJob(s);
        assertFalse(tour.servesJob(s));
    }


    @Test
    public void whenRemovingShipment_theirCorrespondingActivitiesShouldBeRemoved() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
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
        assertTrue(tour.getActivities().contains(act));

        tour.removeActivity(act);

        assertTrue(tour.isEmpty());
        assertFalse(tour.getActivities().contains(act));
        assertFalse(tour.servesJob(service));
        assertEquals(0, tour.jobSize());
    }

    @Test
    public void copyingSeqShouldWork() {
        tour.addActivity(act);

        assertTrue(tour.servesJob(service));

        TourActivities acts = TourActivities.copyOf(tour);

        assertTrue(acts.servesJob(service));
        assertTrue(acts.getActivities().contains(act));
    }

    @Test
    public void removingShipmentActivityShouldWork() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(deliverShipment));

        tour.removeActivity(pickupShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(1, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(deliverShipment));
        assertFalse(tour.getActivities().contains(pickupShipment));
        assertFalse(tour.getActivities().contains(pickupShipment));

    }

    @Test
    public void whenCopyingShipmentActivitySeq_jobSizeShouldBeCorrect() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc"))
                        .setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertEquals(1, copiedTour.jobSize());
    }

    @Test
    public void whenCopyingShipmentActivitySeq_noActivitiesShouldBeCorrect() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertEquals(2, copiedTour.getActivities().size());
    }

    @Test
    public void whenCopyingShipmentActivitySeq_itShouldContaintPickupAct() {
        ShipmentJob s = ShipmentJob.Builder.newInstance("s").addSizeDimension(0, 1)
                        .setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        TourActivity pickupShipment = s.getPickupActivity();
        TourActivity deliverShipment = s.getDeliveryActivity();
        tour.addActivity(pickupShipment);
        tour.addActivity(deliverShipment);

        assertEquals(1, tour.jobSize());
        assertEquals(2, tour.getActivities().size());
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(pickupShipment));
        assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        assertTrue(copiedTour.servesJob(s));
    }

}
