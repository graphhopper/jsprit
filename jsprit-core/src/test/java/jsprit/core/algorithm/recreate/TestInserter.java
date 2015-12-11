/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestInserter {


    @Test
    public void whenInsertingServiceAndRouteIsClosed_itInsertsCorrectly() {
        Service service = mock(Service.class);
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getStartLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.getEndLocation()).thenReturn(loc("vehLoc"));
        when(vehicle.isReturnToDepot()).thenReturn(true);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
        //start - pick(shipment) - del(shipment) - end
        Service serviceToInsert = mock(Service.class);
        when(serviceToInsert.getLocation()).thenReturn(loc("delLoc"));

        InsertionData iData = mock(InsertionData.class);
        when(iData.getDeliveryInsertionIndex()).thenReturn(1);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
        PickupService act = new PickupService(serviceToInsert);
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
        Service service = mock(Service.class);
        Vehicle vehicle = mock(Vehicle.class);
        when(vehicle.getStartLocation()).thenReturn(Location.newInstance("vehLoc"));
        when(vehicle.getEndLocation()).thenReturn(Location.newInstance("vehLoc"));
        when(vehicle.isReturnToDepot()).thenReturn(false);
        when(vehicle.getId()).thenReturn("vehId");

        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).addService(service).build();
        Service serviceToInsert = mock(Service.class);
        when(serviceToInsert.getLocation()).thenReturn(Location.Builder.newInstance().setId("delLoc").build());

        InsertionData iData = mock(InsertionData.class);
        when(iData.getDeliveryInsertionIndex()).thenReturn(1);
        when(iData.getSelectedVehicle()).thenReturn(vehicle);

        VehicleRoutingProblem vehicleRoutingProblem = mock(VehicleRoutingProblem.class);
        when(vehicleRoutingProblem.copyAndGetActivities(serviceToInsert)).thenReturn(getTourActivities(serviceToInsert));

        Inserter inserter = new Inserter(mock(InsertionListeners.class), vehicleRoutingProblem);
        inserter.insertJob(serviceToInsert, iData, route);

        assertEquals(2, route.getTourActivities().getActivities().size());
        assertEquals(route.getTourActivities().getActivities().get(1).getLocation().getId(), serviceToInsert.getLocation().getId());
        assertEquals(route.getEnd().getLocation().getId(), serviceToInsert.getLocation().getId());
    }

    private List<AbstractActivity> getTourActivities(Service serviceToInsert) {
        List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
        acts.add(new PickupService(serviceToInsert));
        return acts;
    }


    @Test
    public void whenInsertingShipmentAndRouteIsClosed_itInsertsCorrectly() {
        Shipment shipment = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(shipment.getSize()).thenReturn(capacity);
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

    private List<AbstractActivity> getTourActivities(Shipment shipmentToInsert) {
        List<AbstractActivity> acts = new ArrayList<AbstractActivity>();
        acts.add(new PickupShipment(shipmentToInsert));
        acts.add(new DeliverShipment(shipmentToInsert));
        return acts;
    }

    @Test
    public void whenInsertingShipmentAndRouteIsOpen_itInsertsCorrectlyAndSwitchesEndLocation() {
        Shipment shipment = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(shipment.getSize()).thenReturn(capacity);
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
        Shipment shipment = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(shipment.getSize()).thenReturn(capacity);
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
        Shipment shipment = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(shipment.getSize()).thenReturn(capacity);
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
        Shipment shipment = mock(Shipment.class);
        Capacity capacity = Capacity.Builder.newInstance().build();
        when(shipment.getSize()).thenReturn(capacity);
        when(shipment.getDeliveryLocation()).thenReturn(Location.Builder.newInstance().setId("oldShipmentDelLoc").build());
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
