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
package com.graphhopper.jsprit.core.problem.solution.route;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VehicleRouteBuilderTest {

    private Driver driver;
    private VehicleRoute.Builder builder;
    private Capacity emptyCapacity;
    private TimeWindow defaultTimeWindow;

    @Before
    public void setUp() {
        Vehicle vehicle = mock(Vehicle.class);
        driver = mock(Driver.class);
        builder = VehicleRoute.Builder.newInstance(vehicle, driver);
        emptyCapacity = Capacity.Builder.newInstance().build();
        defaultTimeWindow = TimeWindow.newInstance(0., 10.);
    }

    private Shipment createMockShipment(String deliveryLocationId) {
        Shipment shipment = mock(Shipment.class);
        when(shipment.getJobType()).thenReturn(Job.Type.SHIPMENT);
        when(shipment.getSize()).thenReturn(emptyCapacity);
        when(shipment.getPickupTimeWindow()).thenReturn(defaultTimeWindow);
        when(shipment.getDeliveryTimeWindow()).thenReturn(defaultTimeWindow);
        if (deliveryLocationId != null) {
            when(shipment.getDeliveryLocation()).thenReturn(loc(deliveryLocationId));
        }
        return shipment;
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryIsAddedBeforePickup_throwsException() {
        Shipment s = mock(Shipment.class);
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addDelivery(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_whenPickupIsAddedTwice() {
        Shipment shipment = createMockShipment(null);

        builder.addPickup(shipment);
        builder.addPickup(shipment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_whenShipmentIsDeliveredTwice() {
        Shipment shipment = createMockShipment(null);

        builder.addPickup(shipment);
        builder.addDelivery(shipment);
        builder.addDelivery(shipment);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowException_whenBuildingRouteWithUndeliveredShipment() {
        Shipment shipment1 = createMockShipment(null);
        Shipment shipment2 = createMockShipment(null);

        builder.addPickup(shipment1);
        builder.addPickup(shipment2);
        builder.addDelivery(shipment1);

        builder.build();
    }

    @Test
    public void shouldHaveFourActivities_whenTwoShipmentsAreAdded() {
        Shipment shipment1 = createMockShipment(null);
        Shipment shipment2 = createMockShipment(null);

        builder.addPickup(shipment1)
            .addPickup(shipment2)
            .addDelivery(shipment1)
            .addDelivery(shipment2);

        VehicleRoute route = builder.build();

        assertEquals(4, route.getTourActivities().getActivities().size());
    }

    @Test
    public void shouldUseVehicleLocation_whenBuildingClosedRoute() {
        Shipment s = createMockShipment(null);
        Shipment s2 = createMockShipment(null);

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("vehLoc")).setEndLocation(Location.newInstance("vehLoc"))
            .build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, driver)
            .addPickup(s)
            .addPickup(s2)
            .addDelivery(s)
            .addDelivery(s2)
            .build();

        assertEquals("vehLoc", route.getEnd().getLocation().getId());
    }

    @Test
    public void shouldUseLastActivityLocation_whenBuildingOpenRoute() {
        Shipment s = createMockShipment(null);
        Shipment s2 = createMockShipment("vehLoc");

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class))
            .addPickup(s)
            .addPickup(s2)
            .addDelivery(s)
            .addDelivery(s2)
            .build();

        assertEquals(route.getEnd().getLocation().getId(), s2.getDeliveryLocation().getId());
    }

    private Location loc(String delLoc) {
        return Location.Builder.newInstance().setId(delLoc).build();
    }

    @Test
    public void houldSetDepartureTimeCorrectly() {
        Shipment s = createMockShipment(null);
        Shipment s2 = createMockShipment("delLoc");

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getStartLocation()).thenReturn(Location.Builder.newInstance().setId("vehLoc").build());

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class))
            .addPickup(s)
            .addPickup(s2)
            .addDelivery(s)
            .addDelivery(s2)
            .setDepartureTime(100)
            .build();

        assertEquals(100.0, route.getDepartureTime(), 0.01);
        assertEquals(100.0, route.getStart().getEndTime(), 0.01);
    }



}
