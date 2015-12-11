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
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.PickupActivity;
import jsprit.core.problem.solution.route.activity.PickupService;
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

        };
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance("10,0")).setTimeWindow(TimeWindow.newInstance(10., 10.)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("20,0")).setTimeWindow(TimeWindow.newInstance(20., 20.)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("30,0")).setTimeWindow(TimeWindow.newInstance(30., 30.)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("vehicle").setStartLocation(Location.newInstance("0,0")).setType(type).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        final VehicleRoutingProblem vrp = vrpBuilder.addJob(s1).addJob(s2).addJob(s3).build();

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
