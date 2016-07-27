/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVariableCosts;
import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
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

/**
 * unit tests to test route level insertion
 */
public class TestRouteLevelActivityInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

    @Before
    public void doBefore() {
        routingCosts = CostFactory.createEuclideanCosts();

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
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).setTimeWindow(TimeWindow.newInstance(10., 10.)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).setTimeWindow(TimeWindow.newInstance(20., 20.)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).setTimeWindow(TimeWindow.newInstance(30., 30.)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0,0")).setType(type).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(s1).addJob(s2).addJob(s3).build();

        vrp.getActivities(s1).get(0).setTheoreticalEarliestOperationStartTime(10);
        vrp.getActivities(s1).get(0).setTheoreticalLatestOperationStartTime(10);

        vrp.getActivities(s2).get(0).setTheoreticalEarliestOperationStartTime(20);
        vrp.getActivities(s2).get(0).setTheoreticalLatestOperationStartTime(20);

        vrp.getActivities(s3).get(0).setTheoreticalEarliestOperationStartTime(30);
        vrp.getActivities(s3).get(0).setTheoreticalLatestOperationStartTime(30);

        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(new JobActivityFactory() {
            @Override
            public List<AbstractActivity> createActivities(Job job) {
                return vrp.copyAndGetActivities(job);
            }

        }).addService(s1).addService(s2).addService(s3).build();

        stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts, routingCosts, stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    public void whenNewActInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        assertEquals(0., iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        assertEquals(0., iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setServiceTime(10.).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        pickupService.setTheoreticalEarliestOperationStartTime(5);
        pickupService.setTheoreticalLatestOperationStartTime(5);

        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedActivityCosts + expectedTransportCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setServiceTime(10.).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 30.;
        assertEquals(expectedActivityCosts + expectedTransportCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs0_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(0);
        double iCosts =
            estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts =
            estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10. + 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts() {
        Service s4 = Service.Builder.newInstance("s4").setLocation(Location.newInstance("5,0")).setTimeWindow(TimeWindow.newInstance(5., 5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        pickupService.setTheoreticalEarliestOperationStartTime(5);
        pickupService.setTheoreticalLatestOperationStartTime(5);
        JobInsertionContext context = new JobInsertionContext(route, s4, route.getVehicle(), route.getDriver(), 0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts, activityCosts, stateManager);
        estimator.setForwardLooking(3);
        double iCosts =
            estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10. + 10. + 10.;
        assertEquals(expectedTransportCosts + expectedActivityCosts, iCosts, 0.01);
    }

}
