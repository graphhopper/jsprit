package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateVariableCosts;
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

import static org.junit.Assert.assertEquals;

/**
 * Created by schroeder on 02.07.14.
 */
public class TestRouteLevelActivityInsertionCostEstimator {

    private VehicleRoute route;

    private VehicleRoutingTransportCosts routingCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private StateManager stateManager;

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

        stateManager = new StateManager(routingCosts);
        stateManager.addStateUpdater(new UpdateVariableCosts(activityCosts,routingCosts,stateManager));
        stateManager.informInsertionStarts(Arrays.asList(route), Collections.<Job>emptyList());
    }

    @Test
    public void whenNewActInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        assertEquals(0.,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        assertEquals(0.,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs0_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setServiceTime(10.).setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedActivityCosts+expectedTransportCosts,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActWithTWAndServiceTimeInBetweenFirstAndSecond_and_forwardLookingIs3_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setServiceTime(10.).setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(3);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts = estimator.getCosts(context, route.getStart(), route.getActivities().get(0), pickupService, 0.);
        double expectedTransportCosts = 0.;
        double expectedActivityCosts = 30.;
        assertEquals(expectedActivityCosts+expectedTransportCosts,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs0_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(0);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts =
                estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10.;
        assertEquals(expectedTransportCosts+expectedActivityCosts,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(3);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts =
                estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10.+10.;
        assertEquals(expectedTransportCosts+expectedActivityCosts,iCosts.getAdditionalCosts(),0.01);
    }

    @Test
    public void whenNewActWithTWInBetweenSecondAndThird_and_forwardLookingIs3_itShouldReturnCorrectCosts(){
        Service s4 = Service.Builder.newInstance("s4").setLocationId("5,0").setTimeWindow(TimeWindow.newInstance(5.,5.)).build();
        PickupActivity pickupService = new PickupService(s4);
        JobInsertionContext context = new JobInsertionContext(route,s4,route.getVehicle(),route.getDriver(),0.);
        RouteLevelActivityInsertionCostsEstimator estimator = new RouteLevelActivityInsertionCostsEstimator(routingCosts,activityCosts,stateManager);
        estimator.setForwardLooking(3);
        ActivityInsertionCostsCalculator.ActivityInsertionCosts iCosts =
                estimator.getCosts(context, route.getActivities().get(0), route.getActivities().get(1), pickupService, 10.);
        double expectedTransportCosts = 10.;
        double expectedActivityCosts = 10.+10.+10.;
        assertEquals(expectedTransportCosts+expectedActivityCosts,iCosts.getAdditionalCosts(),0.01);
    }

}
