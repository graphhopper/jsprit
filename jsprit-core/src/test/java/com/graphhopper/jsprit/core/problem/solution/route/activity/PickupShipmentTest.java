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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;

@DisplayName("Pickup Shipment Test")
class PickupShipmentTest {

    private PickupShipment pickup;

    @BeforeEach
    void doBefore() {
        Shipment shipment = Shipment.Builder.newInstance("shipment").setPickupLocation(Location.Builder.newInstance().setId("pickupLoc").build()).setDeliveryLocation(Location.newInstance("deliveryLoc")).setPickupTimeWindow(TimeWindow.newInstance(1., 2.)).setDeliveryTimeWindow(TimeWindow.newInstance(3., 4.)).addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        pickup = new PickupShipment(shipment);
        pickup.setTheoreticalEarliestOperationStartTime(shipment.getPickupTimeWindow().getStart());
        pickup.setTheoreticalLatestOperationStartTime(shipment.getPickupTimeWindow().getEnd());
    }

    @Test
    @DisplayName("When Calling Capacity _ it Should Return Correct Capacity")
    void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        Assertions.assertEquals(10, pickup.getSize().get(0));
        Assertions.assertEquals(100, pickup.getSize().get(1));
        Assertions.assertEquals(1000, pickup.getSize().get(2));
    }

    @Test
    @DisplayName("When Start Is Ini With Earliest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithEarliestStart_itShouldBeSetCorrectly() {
        Assertions.assertEquals(1., pickup.getTheoreticalEarliestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Start Is Ini With Latest Start _ it Should Be Set Correctly")
    void whenStartIsIniWithLatestStart_itShouldBeSetCorrectly() {
        Assertions.assertEquals(2., pickup.getTheoreticalLatestOperationStartTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting Arr Time _ it Should Be Set Correctly")
    void whenSettingArrTime_itShouldBeSetCorrectly() {
        pickup.setArrTime(4.0);
        Assertions.assertEquals(4., pickup.getArrTime(), 0.01);
    }

    @Test
    @DisplayName("When Setting End Time _ it Should Be Set Correctly")
    void whenSettingEndTime_itShouldBeSetCorrectly() {
        pickup.setEndTime(5.0);
        Assertions.assertEquals(5., pickup.getEndTime(), 0.01);
    }

    @Test
    @DisplayName("When Ini Location Id _ it Should Be Set Correctly")
    void whenIniLocationId_itShouldBeSetCorrectly() {
        Assertions.assertEquals(pickup.getLocation().getId(), "pickupLoc");
    }

    @Test
    @DisplayName("When Copying Start _ it Should Be Done Correctly")
    void whenCopyingStart_itShouldBeDoneCorrectly() {
        PickupShipment copy = (PickupShipment) pickup.duplicate();
        Assertions.assertEquals(1., copy.getTheoreticalEarliestOperationStartTime(), 0.01);
        Assertions.assertEquals(2., copy.getTheoreticalLatestOperationStartTime(), 0.01);
        Assertions.assertEquals(copy.getLocation().getId(), "pickupLoc");
        Assertions.assertEquals(10, copy.getSize().get(0));
        Assertions.assertEquals(100, copy.getSize().get(1));
        Assertions.assertEquals(1000, copy.getSize().get(2));
        Assertions.assertNotSame(copy, pickup);
    }

    @Test
    @DisplayName("When Getting Capacity _ it Should Return It Correctly")
    void whenGettingCapacity_itShouldReturnItCorrectly() {
        Shipment shipment = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).addSizeDimension(0, 10).addSizeDimension(1, 100).build();
        PickupShipment pick = new PickupShipment(shipment);
        Assertions.assertEquals(10, pick.getSize().get(0));
        Assertions.assertEquals(100, pick.getSize().get(1));
    }
}
