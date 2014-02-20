package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;

import org.junit.Test;

public class UpdateLoadsTest {
	
	@Test
	public void whenVehicleRouteIsEmpty_loadsAtBeginningAndEndShouldBeZero(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingProblem.class));
//		RouteActivityVisitor routeActivityVisitor = new RouteActivityVisitor();
		UpdateLoads updateLoads = new UpdateLoads(stateManager);
//		routeActivityVisitor.addActivityVisitor(updateLoads);
		
		VehicleRoute route = VehicleRoute.emptyRoute();
//		routeActivityVisitor.(route);
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

}
