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

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.solution.route.ReverseRouteActivityVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UpdatePracticalTimeWindowTest {

    private VehicleRoutingTransportCosts routingCosts;

    private ReverseRouteActivityVisitor reverseActivityVisitor;

    private StateManager stateManager;

    private VehicleRoute route;

    @Before
    public void doBefore() {

        routingCosts = CostFactory.createManhattanCosts();

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.FINITE);
        stateManager = new StateManager(vrpMock);

        reverseActivityVisitor = new ReverseRouteActivityVisitor();
        reverseActivityVisitor.addActivityVisitor(new UpdatePracticalTimeWindows(stateManager, routingCosts));

        Pickup pickup = (Pickup) Pickup.Builder.newInstance("pick").setLocation(Location.newInstance("0,20")).setTimeWindow(TimeWindow.newInstance(0, 30)).build();
        Delivery delivery = (Delivery) Delivery.Builder.newInstance("del").setLocation(Location.newInstance("20,20")).setTimeWindow(TimeWindow.newInstance(10, 40)).build();
        Pickup pickup2 = (Pickup) Pickup.Builder.newInstance("pick2").setLocation(Location.newInstance("20,0")).setTimeWindow(TimeWindow.newInstance(20, 50)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("0,0")).setType(mock(VehicleType.class)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(pickup).addJob(pickup2).addJob(delivery).build();

        route = VehicleRoute.Builder.newInstance(vehicle, mock(Driver.class)).setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        })
            .addService(pickup).addService(delivery).addService(pickup2).build();

        reverseActivityVisitor.visit(route);

    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct3MustBeCorrect() {
        assertEquals(50., route.getActivities().get(2).getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals(50., stateManager.getActivityState(route.getActivities().get(2), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct2MustBeCorrect() {
        assertEquals(40., route.getActivities().get(1).getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals(30., stateManager.getActivityState(route.getActivities().get(1), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

    @Test
    public void whenVehicleRouteHasPickupAndDeliveryAndPickup_latestStartTimeOfAct1MustBeCorrect() {
        assertEquals(30., route.getActivities().get(0).getTheoreticalLatestOperationStartTime(), 0.01);
        assertEquals(10., stateManager.getActivityState(route.getActivities().get(0), InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.01);
    }

}
