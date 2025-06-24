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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;


public class TestTourActivities {

    private Service service;
    private ServiceActivity act;
    private TourActivities tour;

    @BeforeEach
    public void doBefore() {
        service = Service.Builder.newInstance("yo").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        act = ServiceActivity.newInstance(service);
        tour = new TourActivities();
    }

    @Test
    public void whenAddingServiceAct_serviceActIsAdded() {
        Assertions.assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        Assertions.assertTrue(tour.servesJob(service));
    }

    @Test
    public void whenAddingServiceActTwice_anExceptionIsThrown() {
        Assertions.assertFalse(tour.servesJob(service));
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            tour.addActivity(act);
            tour.addActivity(act);
        });
    }

    @Test
    public void whenAddingServiceAndRemovingItImmediately_tourShouldNotServeServiceAnymore() {
        Assertions.assertFalse(tour.servesJob(service));
        tour.addActivity(act);
        Assertions.assertTrue(tour.servesJob(service));
        tour.removeJob(service);
        Assertions.assertFalse(tour.servesJob(service));
    }

    @Test
    public void whenAddingAServiceAndThenRemovingTheServiceAgain_tourShouldNotServeItAnymore() {
        Assertions.assertEquals(0, tour.getActivities().size());
        tour.addActivity(act);
        Assertions.assertEquals(1, tour.getActivities().size());
        Service anotherServiceInstance = Service.Builder.newInstance("yo").addSizeDimension(0, 10).setLocation(Location.newInstance("loc")).build();
        Assertions.assertTrue(service.equals(anotherServiceInstance));
        boolean removed = tour.removeJob(anotherServiceInstance);
        Assertions.assertTrue(removed);
        Assertions.assertEquals(0, tour.getActivities().size());
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
        Assertions.assertTrue(tour.servesJob(s));
        Assertions.assertEquals(2, tour.getActivities().size());
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
        Assertions.assertFalse(tour.servesJob(s));
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

        Assertions.assertEquals(2, tour.getActivities().size());
        tour.removeJob(s);
        Assertions.assertEquals(0, tour.getActivities().size());
    }

    @Test
    public void removingNonJobActivityShouldWork() {
        TourActivity nonJobAct = Mockito.mock(TourActivity.class);

        tour.addActivity(nonJobAct);
        Assertions.assertTrue(tour.getActivities().contains(nonJobAct));

        tour.removeActivity(nonJobAct);

        Assertions.assertTrue(tour.isEmpty());
        Assertions.assertFalse(tour.getActivities().contains(nonJobAct));
    }

    @Test
    public void removingActivityShouldWork() {
        tour.addActivity(act);
        Assertions.assertTrue(tour.servesJob(service));
        Assertions.assertTrue(tour.getActivities().contains(act));

        tour.removeActivity(act);

        Assertions.assertTrue(tour.isEmpty());
        Assertions.assertFalse(tour.getActivities().contains(act));
        Assertions.assertFalse(tour.servesJob(service));
        Assertions.assertEquals(0, tour.jobSize());
    }

    @Test
    public void copyingSeqShouldWork() {
        tour.addActivity(act);

        Assertions.assertTrue(tour.servesJob(service));

        TourActivities acts = TourActivities.copyOf(tour);

        Assertions.assertTrue(acts.servesJob(service));
        Assertions.assertTrue(acts.getActivities().contains(act));
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

        Assertions.assertEquals(1, tour.jobSize());
        Assertions.assertEquals(2, tour.getActivities().size());
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(deliverShipment));

        tour.removeActivity(pickupShipment);

        Assertions.assertEquals(1, tour.jobSize());
        Assertions.assertEquals(1, tour.getActivities().size());
        Assertions.assertTrue(tour.getActivities().contains(deliverShipment));
        Assertions.assertFalse(tour.getActivities().contains(pickupShipment));
        Assertions.assertFalse(tour.getActivities().contains(pickupShipment));

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

        Assertions.assertEquals(1, tour.jobSize());
        Assertions.assertEquals(2, tour.getActivities().size());
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        Assertions.assertEquals(1, copiedTour.jobSize());
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

        Assertions.assertEquals(1, tour.jobSize());
        Assertions.assertEquals(2, tour.getActivities().size());
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        Assertions.assertEquals(2, copiedTour.getActivities().size());
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

        Assertions.assertEquals(1, tour.jobSize());
        Assertions.assertEquals(2, tour.getActivities().size());
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(pickupShipment));
        Assertions.assertTrue(tour.getActivities().contains(deliverShipment));

        TourActivities copiedTour = TourActivities.copyOf(tour);

        Assertions.assertTrue(copiedTour.servesJob(s));
    }

}
