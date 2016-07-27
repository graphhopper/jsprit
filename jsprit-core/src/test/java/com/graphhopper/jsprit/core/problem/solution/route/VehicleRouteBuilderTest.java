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
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class VehicleRouteBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryIsAddedBeforePickup_throwsException() {
        Shipment s = mock(Shipment.class);
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addDelivery(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupIsAddedTwice_throwsException() {
        Shipment s = mock(Shipment.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().build());
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0., 10.));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsPickedDeliveredAndDeliveredAgain_throwsException() {
        Shipment s = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(s.getSize()).thenReturn(capacity);
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addDelivery(s);
        builder.addDelivery(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsPickedUpThoughButHasNotBeenDeliveredAndRouteIsBuilt_throwsException() {
        Shipment s = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        Shipment s2 = mock(Shipment.class);
        when(s2.getSize()).thenReturn(capacity);
        when(s.getSize()).thenReturn(capacity);
        when(s2.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.build();
    }

    @Test
    public void whenTwoShipmentsHaveBeenAdded_nuOfActivitiesMustEqualFour() {
        Shipment s = mock(Shipment.class);
        Shipment s2 = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(s.getSize()).thenReturn(capacity);
        when(s2.getSize()).thenReturn(capacity);
        when(s2.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.addDelivery(s2);
        VehicleRoute route = builder.build();
        assertEquals(4, route.getTourActivities().getActivities().size());
    }

    @Test
    public void whenBuildingClosedRoute_routeEndShouldHaveLocationOfVehicle() {
        Shipment s = mock(Shipment.class);
        Shipment s2 = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(s.getSize()).thenReturn(capacity);
        when(s2.getSize()).thenReturn(capacity);
        when(s2.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("vehLoc")).setEndLocation(Location.newInstance("vehLoc"))
            .build();

        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.addDelivery(s2);
        VehicleRoute route = builder.build();
        assertEquals("vehLoc", route.getEnd().getLocation().getId());
    }

    @Test
    public void whenBuildingOpenRoute_routeEndShouldHaveLocationOfLastActivity() {
        Shipment s = mock(Shipment.class);
        Shipment s2 = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(s.getSize()).thenReturn(capacity);
        when(s2.getSize()).thenReturn(capacity);
        when(s2.getDeliveryLocation()).thenReturn(loc("delLoc"));
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.addDelivery(s2);
        VehicleRoute route = builder.build();
        assertEquals(route.getEnd().getLocation().getId(), s2.getDeliveryLocation().getId());
    }

    private Location loc(String delLoc) {
        return Location.Builder.newInstance().setId(delLoc).build();
    }

    @Test
    public void whenSettingDepartureTime() {
        Shipment s = mock(Shipment.class);
        Shipment s2 = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(s.getSize()).thenReturn(capacity);
        when(s2.getSize()).thenReturn(capacity);
        when(s2.getDeliveryLocation()).thenReturn(Location.Builder.newInstance().setId("delLoc").build());
        when(s.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        when(s2.getPickupTimeWindow()).thenReturn(TimeWindow.newInstance(0., 10.));
        when(s2.getDeliveryTimeWindow()).thenReturn(TimeWindow.newInstance(0.,10.));
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getStartLocation()).thenReturn(Location.Builder.newInstance().setId("vehLoc").build());
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.addDelivery(s2);
        builder.setDepartureTime(100);
        VehicleRoute route = builder.build();
        assertEquals(100.0, route.getDepartureTime(), 0.01);
        assertEquals(100.0, route.getStart().getEndTime(), 0.01);
    }



}
