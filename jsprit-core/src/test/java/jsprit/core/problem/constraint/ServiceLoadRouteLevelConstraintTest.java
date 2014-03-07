package jsprit.core.problem.constraint;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleType;

import org.junit.Before;
import org.junit.Test;

public class ServiceLoadRouteLevelConstraintTest {
	
	private Vehicle vehicle;
	
	private VehicleRoute route;
	
	RouteAndActivityStateGetter stateGetter;
	
	JobInsertionContext iContext;
	
	ServiceLoadRouteLevelConstraint constraint;
	
	@Before
	public void doBefore(){
		VehicleType type = mock(VehicleType.class);
		when(type.getCapacityDimensions()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());
		vehicle = mock(Vehicle.class);
		when(vehicle.getType()).thenReturn(type);
		
		route = mock(VehicleRoute.class);
		
		Capacity currentLoad = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build();
		stateGetter = mock(RouteAndActivityStateGetter.class);
		when(stateGetter.getRouteState(route, StateFactory.LOAD_AT_BEGINNING, Capacity.class)).thenReturn(currentLoad);
		when(stateGetter.getRouteState(route, StateFactory.LOAD_AT_END, Capacity.class)).thenReturn(currentLoad);
		
		
		
		constraint = new ServiceLoadRouteLevelConstraint(stateGetter);
	}
	
	@Test
	public void whenLoadPlusDeliverySizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue(){
		Service service = mock(Delivery.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse(){
		Service service = mock(Delivery.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusDeliverySizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse(){
		Service service = mock(Delivery.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusDeliverySizeJustFitIntoVehicle_itShouldReturnTrue(){
		Service service = mock(Delivery.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}

	@Test
	public void whenLoadPlusPickupSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue(){
		Service service = mock(Pickup.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusPickupSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse(){
		Service service = mock(Pickup.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusPickupSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse(){
		Service service = mock(Pickup.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusPickupSizeJustFitIntoVehicle_itShouldReturnTrue(){
		Service service = mock(Pickup.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusServiceSizeDoesNotExceedsVehicleCapacity_itShouldReturnTrue(){
		Service service = mock(Service.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 1).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusServiceSizeExceedsVehicleCapacityInAllDimension_itShouldReturnFalse(){
		Service service = mock(Service.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 3).addDimension(1, 3).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusServiceSizeExceedsVehicleCapacityInOneDimension_itShouldReturnFalse(){
		Service service = mock(Service.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 1).addDimension(2, 3).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertFalse(constraint.fulfilled(iContext));
	}
	
	@Test
	public void whenLoadPlusServiceSizeJustFitIntoVehicle_itShouldReturnTrue(){
		Service service = mock(Service.class);
		when(service.getSize()).thenReturn(Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 2).build());
		
		JobInsertionContext iContext = mock(JobInsertionContext.class);
		when(iContext.getJob()).thenReturn(service);
		when(iContext.getRoute()).thenReturn(route);
		when(iContext.getNewVehicle()).thenReturn(vehicle);
		
		assertTrue(constraint.fulfilled(iContext));
	}
}
