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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob;
import com.graphhopper.jsprit.core.problem.job.ShipmentJob.Builder;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;


public class VehicleRouteBuilderTest {

    @Test(expected = IllegalArgumentException.class)
    public void whenDeliveryIsAddedBeforePickup_throwsException() {
        ShipmentJob s = new ShipmentJob.Builder("s")
                .setDeliveryLocation(Location.newInstance("loc1")).build();
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addDelivery(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenPickupIsAddedTwice_throwsException() {
        ShipmentJob s = createStandardShipment("s1").build();
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsPickedDeliveredAndDeliveredAgain_throwsException() {
        ShipmentJob s = createStandardShipment("s1").build();

        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addDelivery(s);
        builder.addDelivery(s);
    }

    @Test(expected = IllegalArgumentException.class)
    public void whenShipmentIsPickedUpThoughButHasNotBeenDeliveredAndRouteIsBuilt_throwsException() {
        ShipmentJob s = createStandardShipment("s1").build();
        ShipmentJob s2 = createStandardShipment("s2").build();

        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.build();
    }

    @Test
    public void whenTwoShipmentsHaveBeenAdded_nuOfActivitiesMustEqualFour() {
        ShipmentJob s = createStandardShipment("s1").build();
        ShipmentJob s2 = createStandardShipment("s2").build();

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
        ShipmentJob s = createStandardShipment("s1").build();
        ShipmentJob s2 = createStandardShipment("s2").build();

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
        ShipmentJob s = createStandardShipment("s1").build();
        ShipmentJob s2 = createStandardShipment("s2").build();

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));
        VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class));
        builder.addPickup(s);
        builder.addPickup(s2);
        builder.addDelivery(s);
        builder.addDelivery(s2);
        VehicleRoute route = builder.build();
        assertEquals(route.getEnd().getLocation().getId(),
                s2.getDeliveryActivity().getLocation().getId());
    }

    private Location loc(String delLoc) {
        return Location.Builder.newInstance().setId(delLoc).build();
    }

    @Test
    public void whenSettingDepartureTime() {
        ShipmentJob s = createStandardShipment("s1").build();
        ShipmentJob s2 = createStandardShipment("s2").build();

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

    protected Builder createStandardShipment(String name) {
        Location loc = Location.Builder.newInstance().setId("delLoc").build();
        TimeWindow tw = TimeWindow.newInstance(0, 10);
        return new ShipmentJob.Builder(name)
                .addSizeDimension(0, 10)
                .setPickupTimeWindow(tw)
                .setDeliveryTimeWindow(tw)
                .setPickupLocation(loc)
                .setDeliveryLocation(loc);
    }


}
