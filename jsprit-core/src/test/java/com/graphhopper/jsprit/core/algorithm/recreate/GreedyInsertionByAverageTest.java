package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.StateUpdater;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.DependencyType;
import com.graphhopper.jsprit.core.problem.constraint.HardRouteConstraint;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.*;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.*;

import static com.graphhopper.jsprit.core.algorithm.box.Jsprit.Parameter.BREAK_SCHEDULING;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class GreedyInsertionByAverageTest {

    @Test
    public void noRoutesShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();

        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        GreedyInsertionByAverage greedyInsertionByAverage = new GreedyInsertionByAverage(calculator, vrp, fleetManager);
        Collection<VehicleRoute> routes = new ArrayList<>();

        greedyInsertionByAverage.insertJobs(routes, vrp.getJobs().values());
        assertEquals(1, routes.size());
    }

    @Test
    public void noJobsInRouteShouldBeCorrect() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 5)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();
        VehicleFleetManager fleetManager = new FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager();
        JobInsertionCostsCalculator calculator = getCalculator(vrp);
        GreedyInsertionByAverage greedyInsertionByAverage = new GreedyInsertionByAverage(calculator, vrp, fleetManager);
        Collection<VehicleRoute> routes = new ArrayList<>();

        greedyInsertionByAverage.insertJobs(routes, vrp.getJobs().values());
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
            .setProperty(Jsprit.Strategy.GREEDY_BY_AVERAGE_REGRET, "1")
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

        optimizeAndValidate(vrp, Jsprit.Strategy.GREEDY_BY_AVERAGE_REGRET);
    }

    static void optimizeAndValidate(VehicleRoutingProblem vrp, Jsprit.Strategy insertionStrategy) {
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setProperty(BREAK_SCHEDULING, Boolean.FALSE.toString())
            .addCoreStateAndConstraintStuff(true)
            .setProperty(insertionStrategy, "1")
            .setStateAndConstraintManager(stateManager, constraintManager).buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

        assertEquals(2, solution.getRoutes().size());
        final Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        while (iterator.hasNext()) {
            final VehicleRoute route = iterator.next();
            boolean hasBreak = false;
            for (TourActivity activity : route.getActivities())
                if (activity instanceof BreakActivity)
                    hasBreak = true;
            assertTrue(hasBreak);
        }
    }

    static class JobInRouteUpdater implements StateUpdater, ActivityVisitor {

        private StateManager stateManager;

        private StateId job1AssignedId;

        private StateId job2AssignedId;

        private VehicleRoute route;

        public JobInRouteUpdater(StateManager stateManager, StateId job1AssignedId, StateId job2AssignedId) {
            this.stateManager = stateManager;
            this.job1AssignedId = job1AssignedId;
            this.job2AssignedId = job2AssignedId;
        }

        @Override
        public void begin(VehicleRoute route) {
            this.route = route;
        }

        @Override
        public void visit(TourActivity activity) {
            if(((TourActivity.JobActivity)activity).getJob().getId().equals("s1")){
                stateManager.putProblemState(job1AssignedId,Boolean.class,true);
            }
            if(((TourActivity.JobActivity)activity).getJob().getId().equals("s2")){
                stateManager.putProblemState(job2AssignedId,Boolean.class,true);
            }

        }

        @Override
        public void finish() {

        }
    }

    static class RouteConstraint implements HardRouteConstraint {

        private final StateId job1AssignedId;

        private final StateId job2AssignedId;

        private StateManager stateManager;

        public RouteConstraint(StateId job1Assigned, StateId job2Assigned, StateManager stateManager) {
            this.job1AssignedId = job1Assigned;
            this.job2AssignedId = job2Assigned;
            this.stateManager = stateManager;
        }

        @Override
        public boolean fulfilled(JobInsertionContext insertionContext) {
            if(insertionContext.getJob().getId().equals("s1")){
                Boolean job2Assigned = stateManager.getProblemState(job2AssignedId,Boolean.class);
                if(job2Assigned == null || job2Assigned == false) return true;
                else {
                    for(Job j : insertionContext.getRoute().getTourActivities().getJobs()){
                        if(j.getId().equals("s2")) return true;
                    }
                }
                return false;
            }
            if(insertionContext.getJob().getId().equals("s2")){
                Boolean job1Assigned = stateManager.getProblemState(job1AssignedId,Boolean.class);
                if(job1Assigned == null || job1Assigned == false) return true;
                else {
                    for(Job j : insertionContext.getRoute().getTourActivities().getJobs()){
                        if(j.getId().equals("s1")) return true;
                    }
                }
                return false;
            }
            return true;
        }
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
        stateManager.addStateUpdater(new JobInRouteUpdater(stateManager, job1Assigned, job2Assigned));
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new RouteConstraint(job1Assigned, job2Assigned, stateManager));
        constraintManager.setDependencyType("s1", DependencyType.INTRA_ROUTE);
        constraintManager.setDependencyType("s2", DependencyType.INTRA_ROUTE);

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .addCoreStateAndConstraintStuff(true)
            .setProperty(Jsprit.Strategy.GREEDY_BY_AVERAGE_REGRET, "1")
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
            .setProperty(Jsprit.Strategy.GREEDY_BY_AVERAGE_REGRET, "1")
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
            .setProperty(Jsprit.Strategy.GREEDY_BY_AVERAGE_REGRET, "1")
            .setStateAndConstraintManager(stateManager, constraintManager)
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        assertEquals(2, solution.getRoutes().size());
        assertEquals(2, solution.getUnassignedJobs().size());
    }

    static JobInsertionCostsCalculator getCalculator(final VehicleRoutingProblem vrp) {
        return new JobInsertionCostsCalculator() {

            @Override
            public InsertionData getInsertionData(VehicleRoute currentRoute, Job newJob, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
                Service service = (Service) newJob;
                Vehicle vehicle = vrp.getVehicles().iterator().next();
                InsertionData iData;
                if (currentRoute.isEmpty()) {
                    double mc = getCost(service.getLocation(), vehicle.getStartLocation());
                    iData = new InsertionData(2 * mc, -1, 0, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), 0));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                } else {
                    double best = Double.MAX_VALUE;
                    int bestIndex = 0;
                    int index = 0;
                    TourActivity prevAct = currentRoute.getStart();
                    for (TourActivity act : currentRoute.getActivities()) {
                        double mc = getMarginalCost(service, prevAct, act);
                        if (mc < best) {
                            best = mc;
                            bestIndex = index;
                        }
                        index++;
                        prevAct = act;
                    }
                    double mc = getMarginalCost(service, prevAct, currentRoute.getEnd());
                    if (mc < best) {
                        best = mc;
                        bestIndex = index;
                    }
                    iData = new InsertionData(best, -1, bestIndex, vehicle, newDriver);
                    iData.getEvents().add(new InsertActivity(currentRoute, vehicle, vrp.copyAndGetActivities(newJob).get(0), bestIndex));
                    iData.getEvents().add(new SwitchVehicle(currentRoute, vehicle, newVehicleDepartureTime));
                }
                return iData;
            }

            private double getMarginalCost(Service service, TourActivity prevAct, TourActivity act) {
                double prev_new = getCost(prevAct.getLocation(), service.getLocation());
                double new_act = getCost(service.getLocation(), act.getLocation());
                double prev_act = getCost(prevAct.getLocation(), act.getLocation());
                return prev_new + new_act - prev_act;
            }

            private double getCost(Location loc1, Location loc2) {
                return vrp.getTransportCosts().getTransportCost(loc1, loc2, 0., null, null);
            }
        };
    }
}
