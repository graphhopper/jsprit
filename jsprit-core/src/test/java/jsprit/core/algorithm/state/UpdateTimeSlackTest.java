package jsprit.core.algorithm.state;

import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.CostFactory;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;

/**
 * Created by schroeder on 29/07/15.
 */
public class UpdateTimeSlackTest {


    @Test
    public void test_withoutTW(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(200).setStartLocation(Location.newInstance("0,0")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance("0,10")).build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("10,10")).build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("10,0")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
                .addJob(service2).addJob(service3).setRoutingCost(CostFactory.createManhattanCosts()).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager,CostFactory.createManhattanCosts()));
        stateManager.addStateUpdater(new UpdateTimeSlack(stateManager,CostFactory.createManhattanCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        double timeSlack_act1 = stateManager.getActivityState(route.getActivities().get(0),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act2 = stateManager.getActivityState(route.getActivities().get(1),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act3 = stateManager.getActivityState(route.getActivities().get(2),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        Assert.assertEquals(160.,timeSlack_act1);
        Assert.assertEquals(160.,timeSlack_act2);
        Assert.assertEquals(160.,timeSlack_act3);
    }

    @Test
    public void test_TW(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(200).setStartLocation(Location.newInstance("0,0")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(10,40)).setLocation(Location.newInstance("0,10")).build();
        Service service2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(0, 40)).setLocation(Location.newInstance("10,10")).build();
        Service service3 = Service.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(80, 120)).setLocation(Location.newInstance("10,0")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
                .addJob(service2).addJob(service3).setRoutingCost(CostFactory.createManhattanCosts()).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager,CostFactory.createManhattanCosts()));
        stateManager.addStateUpdater(new UpdateTimeSlack(stateManager,CostFactory.createManhattanCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        double timeSlack_act1 = stateManager.getActivityState(route.getActivities().get(0),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act2 = stateManager.getActivityState(route.getActivities().get(1),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act3 = stateManager.getActivityState(route.getActivities().get(2),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        Assert.assertEquals(20.,timeSlack_act1);
        Assert.assertEquals(20.,timeSlack_act2);
        Assert.assertEquals(0.,timeSlack_act3);
    }

    @Test
    public void test_TW2(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(200).setStartLocation(Location.newInstance("0,0")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(10,40)).setLocation(Location.newInstance("0,10")).build();
        Service service2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(0,40)).setLocation(Location.newInstance("10,10")).build();
        Service service3 = Service.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(40,120)).setLocation(Location.newInstance("10,0")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
                .addJob(service2).addJob(service3).setRoutingCost(CostFactory.createManhattanCosts()).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager,CostFactory.createManhattanCosts()));
        stateManager.addStateUpdater(new UpdateTimeSlack(stateManager,CostFactory.createManhattanCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        double timeSlack_act1 = stateManager.getActivityState(route.getActivities().get(0),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act2 = stateManager.getActivityState(route.getActivities().get(1),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act3 = stateManager.getActivityState(route.getActivities().get(2),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        Assert.assertEquals(20.,timeSlack_act1);
        Assert.assertEquals(20.,timeSlack_act2);
        Assert.assertEquals(10.,timeSlack_act3);
    }

    @Test
    public void test_TW3(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(200).setStartLocation(Location.newInstance("0,0")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setTimeWindow(TimeWindow.newInstance(10,40)).setLocation(Location.newInstance("0,10")).build();
        Service service2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(30,60)).setLocation(Location.newInstance("10,10")).build();
        Service service3 = Service.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(40,120)).setLocation(Location.newInstance("10,0")).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
                .addJob(service2).addJob(service3).setRoutingCost(CostFactory.createManhattanCosts()).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager,CostFactory.createManhattanCosts()));
        stateManager.addStateUpdater(new UpdateTimeSlack(stateManager,CostFactory.createManhattanCosts()));
        stateManager.informInsertionStarts(Arrays.asList(route), null);

        double timeSlack_act1 = stateManager.getActivityState(route.getActivities().get(0),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act2 = stateManager.getActivityState(route.getActivities().get(1),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        double timeSlack_act3 = stateManager.getActivityState(route.getActivities().get(2),route.getVehicle(),InternalStates.TIME_SLACK,Double.class);
        Assert.assertEquals(30.,timeSlack_act1);
        Assert.assertEquals(20.,timeSlack_act2);
        Assert.assertEquals(20.,timeSlack_act3);
    }

}
