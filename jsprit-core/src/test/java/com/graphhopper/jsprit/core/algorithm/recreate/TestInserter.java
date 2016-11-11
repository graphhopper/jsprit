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
package com.graphhopper.jsprit.core.algorithm.recreate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipmentDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupServiceDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipmentDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;

public class TestInserter {

    private Service.Builder createService(String name, Location loc) {
        TimeWindow tw = TimeWindow.newInstance(0, 10);

        return new Service.Builder(name)
                .setTimeWindow(tw)
                .setLocation(loc);
    }


    protected Shipment.Builder createStandardShipment(String name) {
        Location loc = Location.Builder.newInstance().setId("delLoc").build();
        TimeWindow tw = TimeWindow.newInstance(0, 10);
        return Shipment.Builder.newInstance(name)
                .addSizeDimension(0, 10)
                .setPickupTimeWindow(tw)
                .setDeliveryTimeWindow(tw)
                .setPickupLocation(loc)
                .setDeliveryLocation(loc);
    }

    @Test
    public void whenInsertingServiceAndRouteIsClosed_itInsertsCorrectly() {
//        Service service = mock(Service.class);
//        when(service.getActivityList()).thenReturn(new SequentialJobActivityList(service));
//        when(service.getTimeWindow()).thenReturn(mock(TimeWindow.class));

        Location loc = Location.Builder.newInstance().setId("loc").build();
        Service service = createService("s1", loc).build();

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.getEndLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.isReturnToDepot()).thenReturn(true);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
        //start - pick(shipment) - del(shipment) - end
        Service serviceToInsert = createService("toInsert", loc).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getDeliveryInsertionIndex()).thenReturn(1);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        List<JobActivity> acts = new ArrayList<>();
        PickupServiceDEPRECATED act = new PickupServiceDEPRECATED(serviceToInsert);
        acts.add(act);
        when(vehicleRoutingProblem.copyAndGetActivities(serviceToInsert)).thenReturn(acts);
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(serviceToInsert, iData, route);

        assertEquals(2, route.getTourActivities().getActivities().size());
        assertEquals(route.getTourActivities().getActivities().get(1).getLocation().getId(), serviceToInsert.getLocation().getId());
        assertEquals(route.getEnd().getLocation().getId(), vehicle.getEndLocation().getId());
    }

    private Location loc(String vehLoc) {
        return Location.Builder.newInstance().setId(vehLoc).build();
    }

    @Test
    public void whenInsertingServiceAndRouteIsOpen_itInsertsCorrectlyAndSwitchesEndLocation() {
        Service service = createService("alma", Location.newInstance("loc")).build();
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getStartLocation()).thenReturn(Location.newInstance("vehLoc"));
        when(vehicle.getEndLocation()).thenReturn(Location.newInstance("vehLoc"));
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
        Service serviceToInsert = createService("alma", Location.newInstance("delLoc")).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getDeliveryInsertionIndex()).thenReturn(1);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
// REMARK - Balage1551 - This one is throwing a mockito exception, I don't understand why.
        when(vehicleRoutingProblem.copyAndGetActivities(serviceToInsert)).thenReturn(getTourActivities(serviceToInsert));

        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(serviceToInsert, iData, route);

        assertEquals(2, route.getTourActivities().getActivities().size());
        assertEquals(route.getTourActivities().getActivities().get(1).getLocation().getId(), serviceToInsert.getLocation().getId());
        assertEquals(route.getEnd().getLocation().getId(), serviceToInsert.getLocation().getId());
    }

    private List<JobActivity> getTourActivities(Service serviceToInsert) {
        List<JobActivity> acts = new ArrayList<>();
        acts.add(new PickupServiceDEPRECATED(serviceToInsert));
        return acts;
    }


    @Test
    public void whenInsertingShipmentAndRouteIsClosed_itInsertsCorrectly() {
        Shipment shipment = createStandardShipment("s1").build();

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.getEndLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.isReturnToDepot()).thenReturn(true);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
        //start - pick(shipment) - del(shipment) - end
        Shipment shipmentToInsert = Shipment.Builder.newInstance("s").setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getPickupInsertionIndex()).thenReturn(2);
        when(iData.getDeliveryInsertionIndex()).thenReturn(2);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(shipmentToInsert)).thenReturn(getTourActivities(shipmentToInsert));
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(shipmentToInsert, iData, route);

        assertEquals(4, route.getTourActivities().getActivities().size());
        assertEquals(route.getTourActivities().getActivities().get(2).getLocation().getId(), shipmentToInsert.getPickupLocation().getId());
        assertEquals(route.getTourActivities().getActivities().get(3).getLocation().getId(), shipmentToInsert.getDeliveryLocation().getId());
        assertEquals(route.getEnd().getLocation().getId(), vehicle.getEndLocation().getId());
    }

    private List<JobActivity> getTourActivities(Shipment shipmentToInsert) {
        List<JobActivity> acts = new ArrayList<>();
        acts.add(new PickupShipmentDEPRECATED(shipmentToInsert));
        acts.add(new DeliverShipmentDEPRECATED(shipmentToInsert));
        return acts;
    }

    @Test
    public void whenInsertingShipmentAndRouteIsOpen_itInsertsCorrectlyAndSwitchesEndLocation() {
        Shipment shipment = createStandardShipment("s1").build();

        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
        //start - pick(shipment) - del(shipment) - end
        Shipment shipmentToInsert = Shipment.Builder.newInstance("s").setDeliveryLocation(Location.newInstance("delLoc")).setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).build();
        InsertionData iData = mock(InsertionData.class);
        when(iData.getPickupInsertionIndex()).thenReturn(2);
        when(iData.getDeliveryInsertionIndex()).thenReturn(2);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(shipmentToInsert)).thenReturn(getTourActivities(shipmentToInsert));
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(shipmentToInsert, iData, route);

        assertEquals(4, route.getTourActivities().getActivities().size());
        assertEquals(route.getTourActivities().getActivities().get(2).getLocation().getId(), shipmentToInsert.getPickupLocation().getId());
        assertEquals(route.getTourActivities().getActivities().get(3).getLocation().getId(), shipmentToInsert.getDeliveryLocation().getId());
        assertEquals(route.getEnd().getLocation().getId(), shipmentToInsert.getDeliveryLocation().getId());
    }

    @Test
    public void whenSwitchingVehicleAndRouteIsClosed_newStartAndEndShouldBeTheLocationOfNewVehicle() {
        Shipment shipment = createStandardShipment("s1").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setStartLocation(Location.newInstance("vehLoc")).setType(mock(VehicleType.class)).build();
        Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setStartLocation(Location.newInstance("newVehLoc")).setType(mock(VehicleType.class)).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
        //start - pick(shipment) - del(shipment) - end
        Shipment shipmentToInsert = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getPickupInsertionIndex()).thenReturn(2);
        when(iData.getDeliveryInsertionIndex()).thenReturn(2);
        when(iData.getSelectedVehicle()).thenReturn(newVehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(shipmentToInsert)).thenReturn(getTourActivities(shipmentToInsert));
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(shipmentToInsert, iData, route);

        assertEquals(route.getEnd().getLocation().getId(), newVehicle.getEndLocation().getId());
    }

    @Test
    public void whenSwitchingVehicleAndRouteIsOpen_endLocationShouldBeTheLocationOfTheLastActivity() {
        Shipment shipment = createStandardShipment("s1").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setReturnToDepot(false).setStartLocation(Location.newInstance("vehLoc")).setType(mock(VehicleType.class)).build();
        Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setReturnToDepot(false).setStartLocation(Location.newInstance("newVehLoc")).setType(mock(VehicleType.class)).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
        //start - pick(shipment) - del(shipment) - end
        Shipment shipmentToInsert = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getPickupInsertionIndex()).thenReturn(2);
        when(iData.getDeliveryInsertionIndex()).thenReturn(2);
        when(iData.getSelectedVehicle()).thenReturn(newVehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(shipmentToInsert)).thenReturn(getTourActivities(shipmentToInsert));
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(shipmentToInsert, iData, route);

        assertEquals("delLoc", route.getEnd().getLocation().getId());
    }

    @Test
    public void whenInsertingShipmentAtBeginningAndSwitchingVehicleAndRouteIsOpen_endLocationShouldBeTheLocationOfTheLastActivity() {
        Shipment shipment = createStandardShipment("s1")
                .setDeliveryLocation(Location.newInstance("oldShipmentDelLoc"))
                .build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehId").setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setId("vehLoc").build()).setType(mock(VehicleType.class)).build();
        Vehicle newVehicle = VehicleImpl.Builder.newInstance("newVehId").setReturnToDepot(false).setStartLocation(Location.Builder.newInstance().setId("newVehLoc").build()).setType(mock(VehicleType.class)).build();

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addPickup(shipment).addDelivery(shipment).build();
        //start - pick(shipment) - del(shipment) - end
        Shipment shipmentToInsert = Shipment.Builder.newInstance("s").setPickupLocation(Location.Builder.newInstance().setId("pickLoc").build()).setDeliveryLocation(Location.newInstance("delLoc")).build();

        InsertionData iData = mock(InsertionData.class);
        when(iData.getPickupInsertionIndex()).thenReturn(0);
        when(iData.getDeliveryInsertionIndex()).thenReturn(0);
        when(iData.getSelectedVehicle()).thenReturn(newVehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(shipmentToInsert)).thenReturn(getTourActivities(shipmentToInsert));
        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(shipmentToInsert, iData, route);

        UpdateEndLocationIfRouteIsOpen updateEnd = new UpdateEndLocationIfRouteIsOpen();
        updateEnd.visit(route);

        assertEquals("oldShipmentDelLoc", route.getEnd().getLocation().getId());
    }

}
