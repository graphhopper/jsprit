package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.DependencyType;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.*;

import static com.graphhopper.jsprit.core.algorithm.recreate.GreedyByNeighborsInsertionTest.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GreedyInsertionByDistanceTest {

    @Test
    public void noRoutesShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        GreedyInsertionByDistance insertionByDistanceFromDepot = new GreedyInsertionByDistance(calculator, vrp);
        Collection<VehicleRoute> routes = new ArrayList<>();

        insertionByDistanceFromDepot.insertJobs(routes, vrp.getJobs().values());
        assertEquals(1, routes.size());
    }

    @Test
    public void noRoutesShouldBeCorrect_withOpenRoute() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v1).addVehicle(v2).build();

        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        GreedyInsertionByDistance insertionByDistanceFromDepot = new GreedyInsertionByDistance(calculator, vrp);
        Collection<VehicleRoute> routes = new ArrayList<>();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v1)
            .addService(s1)
            .build();
        routes.add(route);
        ArrayList<Job> unassigned = new ArrayList<>();
        unassigned.add(s2);

        insertionByDistanceFromDepot.insertJobs(routes, unassigned);
        assertEquals(1, routes.size());
    }

    @Test
    public void noJobsInRouteShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        GreedyInsertionByDistance insertionByDistanceFromDepot = new GreedyInsertionByDistance(calculator, vrp);
        Collection<VehicleRoute> routes = new ArrayList<>();

        insertionByDistanceFromDepot.insertJobs(routes, vrp.getJobs().values());
        assertEquals(2, routes.iterator().next().getActivities().size());
    }

    @Test
    public void solutionWithGreedyMustBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, -10)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 5)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, -5)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET, "1")
            .setStateAndConstraintManager(stateManager, constraintManager).buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        assertEquals(2, solution.getRoutes().size());
    }

    @Test
    public void solutionHaveToBeWithBreak() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).addTimeWindow(0, 50).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, -10)).addTimeWindow(0, 50).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 5))
            .setBreak(Break.Builder.newInstance(UUID.randomUUID().toString()).addTimeWindow(0, 10).build()).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0, -5))
            .setBreak(Break.Builder.newInstance(UUID.randomUUID().toString()).addTimeWindow(0, 10).build()).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        optimizeAndValidate(vrp, Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET);
    }

    @Test
    public void solutionWithConstraintAndWithFastRegretConcurrentMustBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).setLocation(Location.newInstance(0, -10)).build();
        Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).setLocation(Location.newInstance(0, -11)).build();
        Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,1).setLocation(Location.newInstance(0, 11)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        final StateManager stateManager = new StateManager(vrp);
        StateId job1Assigned = stateManager.createStateId("job1-assigned");
        StateId job2Assigned = stateManager.createStateId("job2-assigned");
        stateManager.addStateUpdater(new GreedyByNeighborsInsertionTest.JobInRouteUpdater(stateManager, job1Assigned, job2Assigned));
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new GreedyByNeighborsInsertionTest.RouteConstraint(job1Assigned, job2Assigned, stateManager));
        constraintManager.setDependencyType("s1", DependencyType.INTRA_ROUTE);
        constraintManager.setDependencyType("s2", DependencyType.INTRA_ROUTE);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET, "1")
            .setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        for(VehicleRoute route : solution.getRoutes()){
            if(route.getTourActivities().servesJob(s1)) {
                assertTrue(route.getTourActivities().servesJob(s2));
            }
        }
    }

    @Test
    public void solutionWithUnassignedJob() {
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).setLocation(Location.newInstance(0, -10)).build();
        Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).setLocation(Location.newInstance(0, -11)).build();
        Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,10).setLocation(Location.newInstance(0, 11)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type).setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4)
            .addVehicle(v1).addVehicle(v2).setFleetSize(VehicleRoutingProblem.FleetSize.INFINITE).build();

        final StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET, "1")
            .setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        assertTrue(solution.getUnassignedJobs().contains(s4));
    }

    @Test
    public void finiteWithVehicleWithoutRoute() {
        HashSet<Object> skills1 = new HashSet<>();
        skills1.add("a");
        HashSet<Object> skills2 = new HashSet<>();
        skills2.add("b");
        Service s1 = Service.Builder.newInstance("s1").addSizeDimension(0,1).addAllRequiredSkills(skills1).setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").addSizeDimension(0,1).addAllRequiredSkills(skills1).setLocation(Location.newInstance(0, -10)).build();
        Service s3 = Service.Builder.newInstance("s3").addSizeDimension(0,1).addAllRequiredSkills(skills1).setLocation(Location.newInstance(0, -10)).build();
        Service s4 = Service.Builder.newInstance("s4").addSizeDimension(0,1).addAllRequiredSkills(skills2).setLocation(Location.newInstance(0, -11)).build();
        Service s5 = Service.Builder.newInstance("s5").addSizeDimension(0,1).addAllRequiredSkills(skills2).setLocation(Location.newInstance(0, 11)).build();
        Service s6 = Service.Builder.newInstance("s6").addSizeDimension(0,1).addAllRequiredSkills(skills2).setLocation(Location.newInstance(0, 11)).build();

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,2).build();
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1").setType(type)
            .addSkill("a").setStartLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").addSkill("b").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setType(type).setStartLocation(Location.newInstance(0, -10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addJob(s6)
            .addVehicle(v1).addVehicle(v2).addVehicle(v3).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();

        final StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Strategy.GREEDY_BY_DISTANCE_REGRET, "1")
            .setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        assertEquals(2, solution.getRoutes().size());
        assertEquals(2, solution.getUnassignedJobs().size());
    }

    @Test
    public void shipment1ShouldBeAddedFirst() {
        Shipment s1 = Shipment.Builder.newInstance("s1")
            .setPickupLocation(Location.Builder.newInstance().setId("pick1").setCoordinate(Coordinate.newInstance(-1, 10)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del1").setCoordinate(Coordinate.newInstance(1, 10)).build())
            .build();

        Shipment s2 = Shipment.Builder.newInstance("s2")
            .setPickupLocation(Location.Builder.newInstance().setId("pick2").setCoordinate(Coordinate.newInstance(-1, 20)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del2").setCoordinate(Coordinate.newInstance(1, 20)).build())
            .build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build()).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();

        JobInsertionCostsCalculator calculator = getShipmentCalculator(vrp);
        GreedyInsertionByDistance insertionByDistanceFromDepot = new GreedyInsertionByDistance(calculator, vrp);
        Collection<VehicleRoute> routes = new ArrayList<VehicleRoute>();

        GreedyByNeighborsInsertionTest.CkeckJobSequence position = new GreedyByNeighborsInsertionTest.CkeckJobSequence(2, s2);
        insertionByDistanceFromDepot.addListener(position);
        insertionByDistanceFromDepot.insertJobs(routes, vrp.getJobs().values());
        assertTrue(position.isCorrect());
    }
}
