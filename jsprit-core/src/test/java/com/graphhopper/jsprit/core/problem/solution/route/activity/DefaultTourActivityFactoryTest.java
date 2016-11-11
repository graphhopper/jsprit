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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;

public class DefaultTourActivityFactoryTest {

    @Test
    public void whenCreatingActivityWithService_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Service service = new Service.Builder("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupServiceDEPRECATED);
    }

    @Test
    public void whenCreatingActivityWithPickup_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Pickup service = new Pickup.Builder("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupServiceDEPRECATED);
    }

    @Test
    public void whenCreatingActivityWithDelivery_itShouldReturnDeliverService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Delivery service = new Delivery.Builder("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof DeliverServiceDEPRECATED);
    }

}
