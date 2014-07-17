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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateVariableCosts;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 02.07.14.
 */
public class TestRouteLevelServiceInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    @Before
    public void doBefore(){
        routingCosts = CostFactory.createEuclideanCosts();

        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return Math.max(0.,arrivalTime - tourAct.getTheoreticalLatestOperationStartTime());
            }

        };
        Service s1 = Service.Builder.newInstance("s1").setLocationId("10,0").setTimeWindow(TimeWindow.newInstance(10.,10.)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocationId("20,0").setTimeWindow(TimeWindow.newInstance(20.,20.)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocationId("30,0").setTimeWindow(TimeWindow.newInstance(30.,30.)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocationId("0,0").setType(type).build();

        route = VehicleRoute.Builder.newInstance(vehicle).addService(s1).addService(s2).addService(s3).build();

        stateManager = new StateManager(mock(VehicleRoutingProblem.class));
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts,routingCosts,stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());

        VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
        when(vrp.getTransportCosts()).thenReturn(routingCosts);

        constraintManager = new ConstraintManager(vrp,stateManager);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
                activityCosts,estimator,constraintManager,constraintManager);
        routeInserter.setStates(stateManager);
        InsertionData iData = routeInserter.getInsertionData(route,s4,route.getVehicle(),route.getDepartureTime(),route.getDriver(),Double.MAX_VALUE);
        assertEquals(0.,iData.getInsertionCost(),0.01);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionIndex(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
                activityCosts,estimator,constraintManager,constraintManager);
        routeInserter.setStates(stateManager);
        InsertionData iData = routeInserter.getInsertionData(route,s4,route.getVehicle(),route.getDepartureTime(),route.getDriver(),Double.MAX_VALUE);
        assertEquals(0,iData.getDeliveryInsertionIndex(),0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInserted_itShouldReturnCorrectInsertionData(){
        Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
                activityCosts,estimator,constraintManager,constraintManager);
        routeInserter.setStates(stateManager);
        InsertionData iData = routeInserter.getInsertionData(route,s4,route.getVehicle(),route.getDepartureTime(),route.getDriver(),Double.MAX_VALUE);
        assertEquals(0,iData.getDeliveryInsertionIndex(),0.01);
        assertEquals(30.,iData.getInsertionCost(),0.01);
    }


    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData(){
        Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
                activityCosts,estimator,constraintManager,constraintManager);
        routeInserter.setStates(stateManager);
        InsertionData iData = routeInserter.getInsertionData(emptyroute,s4,route.getVehicle(),route.getDepartureTime(),route.getDriver(),Double.MAX_VALUE);
        assertEquals(0,iData.getDeliveryInsertionIndex(),0.01);
        assertEquals(10.,iData.getInsertionCost(),0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeAndTWNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData(){
        Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(3.,3.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts,
                activityCosts,estimator,constraintManager,constraintManager);
        routeInserter.setStates(stateManager);
        InsertionData iData = routeInserter.getInsertionData(emptyroute,s4,route.getVehicle(),route.getDepartureTime(),route.getDriver(),Double.MAX_VALUE);
        assertEquals(0,iData.getDeliveryInsertionIndex(),0.01);
        assertEquals(10.+2.,iData.getInsertionCost(),0.01);
    }

}
