package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.RouteActivityVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;

import org.junit.Test;

public class UpdateLoadsTest {
	
	@Test
	public void whenVehicleRouteIsEmpty_loadsAtBeginningAndEndShouldBeZero(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));

		UpdateLoads updateLoads = new UpdateLoads(stateManager);
		VehicleRoute route = VehicleRoute.emptyRoute();

		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		assertEquals(0.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(0.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenVehcicleRouteIsNotEmpty_loadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		Service service = mock(Service.class);
		
		when(service.getCapacityDemand()).thenReturn(1);
		Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 1).build();
		when(service.getSize()).thenReturn(capacity);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(service).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		assertEquals(0.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(1.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@Test
	public void whenVehcicleRouteIsNotEmpty_multipleLoadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		Service service = mock(Service.class);
		Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1,2).build();
		when(service.getSize()).thenReturn(capacity);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(service).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		assertEquals(0.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(1.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenVehcicleRouteHasTwoActivities_loadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		Service service = mock(Service.class);
		when(service.getCapacityDemand()).thenReturn(1);
		
		Service service2 = mock(Service.class);
		when(service2.getCapacityDemand()).thenReturn(10);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(service).addService(service2).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		assertEquals(0.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(11.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenVehcicleRouteHasTwoActivities_loadsAtActivitiesShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);
		
		RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
		routeActivityVisitor.addActivityVisitor(updateLoads);
		
		Service service = mock(Service.class);
		when(service.getCapacityDemand()).thenReturn(1);
		
		Service service2 = mock(Service.class);
		when(service2.getCapacityDemand()).thenReturn(10);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(service).addService(service2).build();
		
		routeActivityVisitor.visit(route);
		
		assertEquals(1.,stateManager.getActivityState(route.getActivities().get(0), StateFactory.LOAD).toDouble(),0.1);
		assertEquals(11.,stateManager.getActivityState(route.getActivities().get(1), StateFactory.LOAD).toDouble(),0.1);
	}

	@SuppressWarnings("deprecation")
	@Test
	public void whenVehcicleRouteHasPickupAndDelivery_loadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);
		
		Pickup pickup = mock(Pickup.class);
		when(pickup.getCapacityDemand()).thenReturn(1);
		
		Delivery delivery = mock(Delivery.class);
		when(delivery.getCapacityDemand()).thenReturn(10);
		Capacity capacity2 = Capacity.Builder.newInstance().addDimension(0, 10).build();
		when(delivery.getSize()).thenReturn(capacity2);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(pickup).addService(delivery).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		assertEquals(10.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(1.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenVehcicleRouteHasPickupAndDelivery_loadsAtActivitiesShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
		routeActivityVisitor.addActivityVisitor(updateLoads);
		
		Pickup pickup = mock(Pickup.class);
		when(pickup.getCapacityDemand()).thenReturn(1);
		
		Delivery delivery = mock(Delivery.class);
		when(delivery.getCapacityDemand()).thenReturn(10);
		Capacity capacity2 = Capacity.Builder.newInstance().addDimension(0, 10).build();
		when(delivery.getSize()).thenReturn(capacity2);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(pickup).addService(delivery).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		routeActivityVisitor.visit(route);
		
		assertEquals(11.,stateManager.getActivityState(route.getActivities().get(0), StateFactory.LOAD).toDouble(),0.1);
		assertEquals(1.,stateManager.getActivityState(route.getActivities().get(1), StateFactory.LOAD).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenPickupIsInsertedIntoVehcicleRouteWithPickupAndDelivery_loadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
		routeActivityVisitor.addActivityVisitor(updateLoads);
		
		Pickup pickup = mock(Pickup.class);
		when(pickup.getCapacityDemand()).thenReturn(1);
		
		Delivery delivery = mock(Delivery.class);
		when(delivery.getCapacityDemand()).thenReturn(10);
		Capacity capacity2 = Capacity.Builder.newInstance().addDimension(0, 10).build();
		when(delivery.getSize()).thenReturn(capacity2);
		
		Pickup pickup2insert = mock(Pickup.class);
		when(pickup2insert.getCapacityDemand()).thenReturn(2);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(pickup).addService(delivery).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		updateLoads.informJobInserted(pickup2insert, route, 0., 0.);
		
		assertEquals(10.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(3.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void whenDeliveryIsInsertedIntoVehcicleRouteWithPickupAndDelivery_loadsAtBeginningAndEndShouldBeCalculatedCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
		UpdateLoads updateLoads = new UpdateLoads(stateManager);

		RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
		routeActivityVisitor.addActivityVisitor(updateLoads);
		
		Pickup pickup = mock(Pickup.class);
		when(pickup.getCapacityDemand()).thenReturn(1);
		
		Delivery delivery = mock(Delivery.class);
		when(delivery.getCapacityDemand()).thenReturn(10);
		Capacity size = Capacity.Builder.newInstance().addDimension(0, 10).build();
		when(delivery.getSize()).thenReturn(size);
		
		Delivery delivery2insert = mock(Delivery.class);
		when(delivery2insert.getCapacityDemand()).thenReturn(20);
		Capacity size2 = Capacity.Builder.newInstance().addDimension(0, 20).build();
		when(delivery2insert.getSize()).thenReturn(size2);
		
		VehicleRoute route = VehicleRoute.Builder.newInstance(mock(Vehicle.class), mock(Driver.class))
				.addService(pickup).addService(delivery).build();
		
		updateLoads.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
		updateLoads.informJobInserted(delivery2insert, route, 0., 0.);
		
		assertEquals(30.,stateManager.getRouteState(route, StateFactory.LOAD_AT_BEGINNING).toDouble(),0.1);
		assertEquals(1.,stateManager.getRouteState(route, StateFactory.LOAD_AT_END).toDouble(),0.1);
	}
	
}
