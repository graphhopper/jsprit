/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.solution.route.ReverseRouteActivityVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.CostFactory;

import org.junit.Before;
import org.junit.Test;

public class UpdatePracticalTimeWindowTest {
	
	private VehicleRoutingTransportCosts routingCosts;
	
	private ReverseRouteActivityVisitor reverseActivityVisitor;
	
	private StateManager stateManager;
	
	private VehicleRoute route;
	
	@Before
	public void doBefore(){
		
		routingCosts = CostFactory.createManhattanCosts();

		stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		
		reverseActivityVisitor = new ReverseRouteActivityVisitor();
		reverseActivityVisitor.addActivityVisitor(new UpdatePracticalTimeWindows(stateManager, routingCosts));
		
		Pickup pickup = mock(Pickup.class);
		when(pickup.getTimeWindow()).thenReturn(TimeWindow.newInstance(0, 30));
		when(pickup.getLocationId()).thenReturn("0,20");
		
		Delivery delivery = mock(Delivery.class);
		when(delivery.getTimeWindow()).thenReturn(TimeWindow.newInstance(10, 40));
		when(delivery.getLocationId()).thenReturn("20,20");
		when(delivery.getSize()).thenReturn(Capacity.Builder.newInstance().build());
		
		Pickup pickup2 = mock(Pickup.class);
		when(pickup2.getTimeWindow()).thenReturn(TimeWindow.newInstance(20, 50));
		when(pickup2.getLocationId()).thenReturn("20,0");
		
		Vehicle vehicle = mock(Vehicle.class);
		when(vehicle.getStartLocationId()).thenReturn("0,0");
		when(vehicle.getLatestArrival()).thenReturn(Double.MAX_VALUE);
		
		route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class))
				.addService(pickup).addService(delivery).addService(pickup2).build();
		
		reverseActivityVisitor.visit(route);
		
	}
	
	@Test
	public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct3MustBeCorrect(){
		assertEquals(50.,route.getActivities().get(2).getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals(50.,stateManager.getActivityState(route.getActivities().get(2), StateFactory.LATEST_OPERATION_START_TIME, Double.class),0.01);
	}
	
	@Test
	public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct2MustBeCorrect(){
		assertEquals(40.,route.getActivities().get(1).getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals(30.,stateManager.getActivityState(route.getActivities().get(1), StateFactory.LATEST_OPERATION_START_TIME, Double.class),0.01);
	}
	
	@Test
	public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct1MustBeCorrect(){
		assertEquals(30.,route.getActivities().get(0).getTheoreticalLatestOperationStartTime(),0.01);
		assertEquals(10.,stateManager.getActivityState(route.getActivities().get(0), StateFactory.LATEST_OPERATION_START_TIME, Double.class),0.01);
	}

}
