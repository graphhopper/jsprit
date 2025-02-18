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
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Pickup;
import com.graphhopper.jsprit.core.problem.job.Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Default Tour Activity Factory Test")
class DefaultTourActivityFactoryTest {

    @Test
    @DisplayName("When Creating Activity With Service _ it Should Return Pickup Service")
    void whenCreatingActivityWithService_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Service service = Service.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupService);
    }

    @Test
    @DisplayName("When Creating Activity With Pickup _ it Should Return Pickup Service")
    void whenCreatingActivityWithPickup_itShouldReturnPickupService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Pickup service = (Pickup) Pickup.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof PickupService);
    }

    @Test
    @DisplayName("When Creating Activity With Delivery _ it Should Return Deliver Service")
    void whenCreatingActivityWithDelivery_itShouldReturnDeliverService() {
        DefaultTourActivityFactory factory = new DefaultTourActivityFactory();
        Delivery service = (Delivery) Delivery.Builder.newInstance("service").setLocation(Location.newInstance("loc")).build();
        TourActivity act = factory.createActivity(service);
        assertNotNull(act);
        assertTrue(act instanceof DeliverService);
    }
}
