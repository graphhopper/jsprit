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
import com.graphhopper.jsprit.core.problem.job.Shipment;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultShipmentActivityFactoryTest {

    @Test
    public void whenCreatingPickupActivityWithShipment_itShouldReturnPickupShipment() {
        DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("pLoc").build()).setDeliveryLocation(Location.newInstance("dLoc")).build();
        TourActivity act = factory.createPickup(shipment);
        assertNotNull(act);
        assertTrue(act instanceof PickupShipment);
    }

    @Test
    public void whenCreatingDeliverActivityWithShipment_itShouldReturnDeliverShipment() {
        DefaultShipmentActivityFactory factory = new DefaultShipmentActivityFactory();
        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setId("pLoc").build()).setDeliveryLocation(Location.newInstance("dLoc")).build();
        TourActivity act = factory.createDelivery(shipment);
        assertNotNull(act);
        assertTrue(act instanceof DeliverShipment);
    }
}
