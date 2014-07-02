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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.solution.route.state.StateFactory.State;
import jsprit.core.problem.solution.route.state.StateFactory.StateId;

import org.junit.Test;

public class StateManagerTest {
	
	@Test
	public void whenRouteStateIsSetWithGenericMethod_itMustBeSetCorrectly(){
		VehicleRoute route = mock(VehicleRoute.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		State state = StateFactory.createState(1.);
		stateManager.putTypedRouteState(route, id, State.class, state);
		assertEquals(1.,stateManager.getRouteState(route, id, State.class).toDouble(),0.01);
	}
	
	@Test
	public void whenRouteStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly(){
		VehicleRoute route = mock(VehicleRoute.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		boolean routeIsRed = true;
		stateManager.putTypedRouteState(route, id, Boolean.class, routeIsRed);
		assertTrue(stateManager.getRouteState(route, id, Boolean.class));
	}
	
	@Test
	public void whenRouteStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly(){
		VehicleRoute route = mock(VehicleRoute.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		int load = 3;
		stateManager.putTypedRouteState(route, id, Integer.class, load);
		int getLoad = stateManager.getRouteState(route, id, Integer.class);
		assertEquals(3, getLoad);
	}
	
	@Test
	public void whenRouteStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly(){
		VehicleRoute route = mock(VehicleRoute.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
		stateManager.putTypedRouteState(route, id, Capacity.class, capacity);
		Capacity getCap = stateManager.getRouteState(route, id, Capacity.class);
		assertEquals(500, getCap.get(0));
	}
	
	@Test
	public void whenActivityStateIsSetWithGenericMethod_itMustBeSetCorrectly(){
		TourActivity activity = mock(TourActivity.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		State state = StateFactory.createState(1.);
		stateManager.putTypedActivityState(activity, id, State.class, state);
		assertEquals(1.,stateManager.getActivityState(activity, id, State.class).toDouble(),0.01);
	}
	
	@Test
	public void whenActivityStateIsSetWithGenericMethodAndBoolean_itMustBeSetCorrectly(){
		TourActivity activity = mock(TourActivity.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		boolean routeIsRed = true;
		stateManager.putTypedActivityState(activity, id, Boolean.class, routeIsRed);
		assertTrue(stateManager.getActivityState(activity, id, Boolean.class));
	}
	
	@Test
	public void whenActivityStateIsSetWithGenericMethodAndInteger_itMustBeSetCorrectly(){
		TourActivity activity = mock(TourActivity.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		int load = 3;
		stateManager.putTypedActivityState(activity, id, Integer.class, load);
		int getLoad = stateManager.getActivityState(activity, id, Integer.class);
		assertEquals(3, getLoad);
	}
	
	@Test
	public void whenActivityStateIsSetWithGenericMethodAndCapacity_itMustBeSetCorrectly(){
		TourActivity activity = mock(TourActivity.class);
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("myState");
		Capacity capacity = Capacity.Builder.newInstance().addDimension(0, 500).build();
		stateManager.putTypedActivityState(activity, id, Capacity.class, capacity);
		Capacity getCap = stateManager.getActivityState(activity, id, Capacity.class);
		assertEquals(500, getCap.get(0));
	}
	
	@Test
	public void whenProblemStateIsSet_itMustBeSetCorrectly(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("problemState");
		stateManager.putProblemState(id, Boolean.class, true);
		boolean problemState = stateManager.getProblemState(id, Boolean.class);
		assertTrue(problemState);
	}
	
	@Test(expected=NullPointerException.class)
	public void whenProblemStateIsSetAndStateManagerClearedAfterwards_itThrowsException(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("problemState");
		stateManager.putProblemState(id, Boolean.class, true);
		stateManager.clear();
		@SuppressWarnings("unused")
		boolean problemState = stateManager.getProblemState(id, Boolean.class);
	}
	
	@Test
	public void whenProblemStateIsSetAndStateManagerClearedAfterwards_itReturnsDefault(){
		StateManager stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
		StateId id = StateFactory.createId("problemState");
		stateManager.addDefaultProblemState(id, Boolean.class, false);
		stateManager.putProblemState(id, Boolean.class, true);
		stateManager.clear();
		boolean problemState = stateManager.getProblemState(id, Boolean.class);
		assertFalse(problemState);
	}
}
