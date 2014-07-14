package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.*;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.*;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 13.07.14.
 */
public class LoadConstraintTest {

    private VehicleRoute serviceRoute;

    private VehicleRoute pickup_delivery_route;

    private VehicleRoute shipment_route;

    private StateManager stateManager;

    @Before
    public void doBefore(){
        Vehicle vehicle = mock(Vehicle.class);
        VehicleType type = mock(VehicleType.class);
        when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0,20).build());
        when(vehicle.getType()).thenReturn(type);

        Service s1 = mock(Service.class);
        when(s1.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,10).build());
        Service s2 = mock(Service.class);
        when(s2.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,5).build());

        Pickup pickup = mock(Pickup.class);
        when(pickup.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        Delivery delivery = mock(Delivery.class);
        when(delivery.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,5).build());

        Shipment shipment1 = mock(Shipment.class);
        when(shipment1.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 10).build());
        Shipment shipment2 = mock(Shipment.class);
        when(shipment2.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 5).build());

        serviceRoute = VehicleRoute.Builder.newInstance(vehicle).addService(s1).addService(s2).build();
        pickup_delivery_route = VehicleRoute.Builder.newInstance(vehicle).addService(pickup).addService(delivery).build();
        shipment_route = VehicleRoute.Builder.newInstance(vehicle).addPickup(shipment1).addPickup(shipment2).addDelivery(shipment2).addDelivery(shipment1).build();

        stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
        stateManager.updateLoadStates();
        stateManager.informInsertionStarts(Arrays.asList(serviceRoute, pickup_delivery_route, shipment_route), Collections.<Job>emptyList());

    }


    /*
    serviceroute
     */
    @Test
    public void whenServiceRouteAndNewServiceFitsIn_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,5).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenStartAndAct1_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenServiceRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenServiceRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,6).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
    pickup_delivery_route
    pickup 10
    delivery 5
     */
    @Test
    public void whenPDRouteRouteAndNewPickupFitsIn_itShouldReturnFulfilled(){
        Pickup s = mock(Pickup.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,10).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,serviceRoute.getVehicle(),null,0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryFitsIn_itShouldReturnFulfilled(){
        Delivery s = mock(Delivery.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,15).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,serviceRoute.getVehicle(),null,0.);
        assertTrue(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewPickupDoesNotFitIn_itShouldReturnNotFulfilled(){
        Pickup s = mock(Pickup.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,11).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,serviceRoute.getVehicle(),null,0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    @Test
    public void whenPDRouteRouteAndNewDeliveryDoesNotFitIn_itShouldReturnNotFulfilled(){
        Delivery s = mock(Delivery.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,16).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,serviceRoute.getVehicle(),null,0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

    /*
    pick fits in between activities
     */
    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenStartAndAct1(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct1AndAct2(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newPickupShouldFitInBetweenAct2AndEnd(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    /*
    pickup does not fit in between activities
     */
    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenStartAndAct1(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct1AndAct2(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newPickupShouldNotFitInBetweenAct2AndEnd(){
        Pickup s = mock(Pickup.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        PickupService newAct = new PickupService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);
    }


    /*
    pick fits in between activities
     */
    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenStartAndAct1(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 15).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenStartAndAct1(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 16).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getStart(), newAct, pickup_delivery_route.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct1AndAct2(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newDeliveryNotShouldFitInBetweenAct1AndAct2(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(0), newAct, pickup_delivery_route.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldFitInBetweenAct2AndEnd(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);
    }

    @Test
    public void whenPDRoute_newDeliveryShouldNotFitInBetweenAct2AndEnd(){
        Delivery s = mock(Delivery.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(pickup_delivery_route,s,pickup_delivery_route.getVehicle(),null,0.);
        DeliverService newAct = new DeliverService(s);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, pickup_delivery_route.getActivities().get(1), newAct, pickup_delivery_route.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc1AndAct2_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceFitsInBetweenAc2AndEnd_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED, status);
    }

    /*
    service does not fit in at act level
     */
    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenStartAndAct1_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getStart(), newAct, serviceRoute.getActivities().get(0), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc1AndAct2_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(0), newAct, serviceRoute.getActivities().get(1), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }

    @Test
    public void whenPDRouteAndNewServiceDoesNotFitInBetweenAc2AndEnd_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);
        ServiceLoadActivityLevelConstraint loadConstraint = new ServiceLoadActivityLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        ServiceActivity newAct = mock(ServiceActivity.class);
        when(newAct.getSize()).thenReturn(newSize);

        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context, serviceRoute.getActivities().get(1), newAct, serviceRoute.getEnd(), 0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED, status);
    }


    @Test
    public void whenPDRouteAndNewServiceDoesNotFitIn_itShouldReturnFulfilled(){
        Service s = mock(Service.class);
        when(s.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0,6).build());
        ServiceLoadRouteLevelConstraint loadconstraint = new ServiceLoadRouteLevelConstraint(stateManager);

        JobInsertionContext context = new JobInsertionContext(serviceRoute,s,serviceRoute.getVehicle(),null,0.);
        assertFalse(loadconstraint.fulfilled(context));
    }

/*
shipment route
shipment1 10
shipment2 5

pickup(s1) pickup(s2) delivery(s2) deliver(s1)
 */

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenStartAndAct1(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getStart(),newAct,shipment_route.getActivities().get(0),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenStartAndAct1(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getStart(),newAct,shipment_route.getActivities().get(0),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct1AndAct2(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(0),newAct,shipment_route.getActivities().get(1),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct1AndAct2(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(0),newAct,shipment_route.getActivities().get(1),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct2AndAct3(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(1),newAct,shipment_route.getActivities().get(2),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct2AndAct3(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(1),newAct,shipment_route.getActivities().get(2),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct3AndAct4(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(2),newAct,shipment_route.getActivities().get(3),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct3AndAct4(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0,11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(2),newAct,shipment_route.getActivities().get(3),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldFitInBetweenAct4AndEnd(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(3),newAct,shipment_route.getEnd(),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndPickupOfNewShipmentShouldNotFitInBetweenAct4AndEnd(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0,21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        PickupShipment newAct = new PickupShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(3),newAct,shipment_route.getEnd(),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED,status);

    }

    /*
    deliverShipment
     */

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenStartAndAct1(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getStart(),newAct,shipment_route.getActivities().get(0),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenStartAndAct1(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getStart(),newAct,shipment_route.getActivities().get(0),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct1AndAct2(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(0),newAct,shipment_route.getActivities().get(1),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct1AndAct2(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(0),newAct,shipment_route.getActivities().get(1),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct2AndAct3(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 5).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(1),newAct,shipment_route.getActivities().get(2),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct2AndAct3(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 6).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(1),newAct,shipment_route.getActivities().get(2),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct3AndAct4(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 10).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(2),newAct,shipment_route.getActivities().get(3),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct3AndAct4(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0,11).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(2),newAct,shipment_route.getActivities().get(3),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldFitInBetweenAct4AndEnd(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0, 20).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(3),newAct,shipment_route.getEnd(),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.FULFILLED,status);

    }

    @Test
    public void whenShipmentRouteAndDeliveryOfNewShipmentShouldNotFitInBetweenAct4AndEnd(){
        Shipment s = mock(Shipment.class);
        Capacity newSize = Capacity.Builder.newInstance().addDimension(0,21).build();
        when(s.getSize()).thenReturn(newSize);

        JobInsertionContext context = new JobInsertionContext(shipment_route,s,shipment_route.getVehicle(),null,0.);

        DeliverShipment newAct = new DeliverShipment(s);
        PickupAndDeliverShipmentLoadActivityLevelConstraint loadConstraint = new PickupAndDeliverShipmentLoadActivityLevelConstraint(stateManager);
        HardActivityStateLevelConstraint.ConstraintsStatus status = loadConstraint.fulfilled(context,shipment_route.getActivities().get(3),newAct,shipment_route.getEnd(),0.);

        assertEquals(HardActivityStateLevelConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK,status);

    }

}
