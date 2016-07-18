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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVariableCosts;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.SoftTimeWindowCost;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupService;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 02.07.14.
 */
public class TestRouteLevelServiceInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private SoftTimeWindowCost softCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private VehicleRoutingProblem vrp;

    private JobActivityFactory activityFactory;

    @Before
    public void doBefore() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();

        routingCosts = CostFactory.createEuclideanCosts();
        softCosts = new SoftTimeWindowCost(routingCosts, false);
        vrpBuilder.setRoutingCost(routingCosts);

        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return Math.max(0., arrivalTime - tourAct.getTheoreticalLatestOperationStartTime());
            }

            @Override
            public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return tourAct.getOperationTime();
            }

        };
        vrpBuilder.setActivityCosts(activityCosts);

        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).setTimeWindow(TimeWindow.newInstance(10., 10.)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).setTimeWindow(TimeWindow.newInstance(20., 20.)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).setTimeWindow(TimeWindow.newInstance(30., 30.)).build();
        vrpBuilder.addJob(s1).addJob(s2).addJob(s3);

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0,0")).setType(type).build();
        vrpBuilder.addVehicle(vehicle);
        vrp = vrpBuilder.build();

        vrp.getActivities(s1).get(0).setTheoreticalEarliestOperationStartTime(10);
        vrp.getActivities(s1).get(0).setTheoreticalLatestOperationStartTime(10);

        vrp.getActivities(s2).get(0).setTheoreticalEarliestOperationStartTime(20);
        vrp.getActivities(s2).get(0).setTheoreticalLatestOperationStartTime(20);

        vrp.getActivities(s3).get(0).setTheoreticalEarliestOperationStartTime(30);
        vrp.getActivities(s3).get(0).setTheoreticalLatestOperationStartTime(30);

        activityFactory = new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }
        };
        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(activityFactory).addService(s1).addService(s2).addService(s3).build();

        VehicleRoutingProblem vrpMock = mock(VehicleRoutingProblem.class);
        when(vrpMock.getFleetSize()).thenReturn(VehicleRoutingProblem.FleetSize.INFINITE);
        stateManager = new StateManager(vrpMock);
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts, routingCosts, softCosts, stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
        constraintManager = new ConstraintManager(vrp, stateManager);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionCosts() {
        final Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, vrp.getSoftTimeWindowCost(), activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts, vrp.getSoftTimeWindowCost(),
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = activityFactory.createActivities(job);
                if (acts.isEmpty()) {
                    acts.add(new PickupService(s4));
                }
                return acts;
            }
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
        assertEquals(0., iData.getInsertionCost(), 0.01);
    }

    @Test
    public void whenNewServiceNeedToBeInserted_itShouldReturnCorrectInsertionIndex() {
        final Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, vrp.getSoftTimeWindowCost(), activityCosts, stateManager);
        estimator.setForwardLooking(0);
        final ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts, vrp.getSoftTimeWindowCost(), 
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = activityFactory.createActivities(job);
                if (acts.isEmpty()) {
                    acts.add(new PickupService(s4));
                }
                return acts;
            }
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInserted_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, vrp.getSoftTimeWindowCost(), activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts, vrp.getSoftTimeWindowCost(),
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = activityFactory.createActivities(job);
                if (acts.isEmpty()) {
                    PickupService pickupService = new PickupService(s4);
                    pickupService.setTheoreticalEarliestOperationStartTime(5);
                    pickupService.setTheoreticalLatestOperationStartTime(5);
                    acts.add(pickupService);
                }
                return acts;
            }
        });
        InsertionData iData = routeInserter.getInsertionData(route, s4, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(30., iData.getInsertionCost(), 0.01);
    }


    @Test
    public void whenNewServiceWithServiceTimeNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, vrp.getSoftTimeWindowCost(), activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts, vrp.getSoftTimeWindowCost(), 
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = activityFactory.createActivities(job);
                if (acts.isEmpty()) {
                    PickupService pickupService = new PickupService(s4);
                    pickupService.setTheoreticalEarliestOperationStartTime(5);
                    pickupService.setTheoreticalLatestOperationStartTime(5);
                    acts.add(pickupService);
                }
                return acts;
            }
        });
        InsertionData iData = routeInserter.getInsertionData(emptyroute, s4, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(10., iData.getInsertionCost(), 0.01);
    }

    @Test
    public void whenNewServiceWithServiceTimeAndTWNeedToBeInsertedAndRouteIsEmpty_itShouldReturnCorrectInsertionData() {
        final Service s4 = Service.Builder.newInstance("s4").setServiceTime(10.).setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(3., 3.)).build();
//        PickupActivity pickupService = new PickupService(s4);
        VehicleRoute emptyroute = VehicleRoute.emptyRoute();
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, vrp.getSoftTimeWindowCost(), activityCosts, stateManager);
        estimator.setForwardLooking(0);
        ServiceInsertionOnRouteLevelCalculator routeInserter = new ServiceInsertionOnRouteLevelCalculator(routingCosts, vrp.getSoftTimeWindowCost(), 
            activityCosts, estimator, constraintManager, constraintManager);
        routeInserter.setStates(stateManager);
        routeInserter.setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                List<AbstractActivity> acts = activityFactory.createActivities(job);
                if (acts.isEmpty()) {
                    PickupService pickupService = new PickupService(s4);
                    pickupService.setTheoreticalEarliestOperationStartTime(3);
                    pickupService.setTheoreticalLatestOperationStartTime(3);
                    acts.add(pickupService);
                }
                return acts;
            }
        });
        InsertionData iData = routeInserter.getInsertionData(emptyroute, s4, route.getVehicle(), route.getDepartureTime(), route.getDriver(), Double.MAX_VALUE);
        assertEquals(0, iData.getDeliveryInsertionIndex(), 0.01);
        assertEquals(10. + 2., iData.getInsertionCost(), 0.01);
    }

}
