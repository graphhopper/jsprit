package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityNextLocations;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityPrevLocations;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.algorithm.state.UpdateVehicleDependentPracticalTimeWindows;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.cost.WaitingTimeCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ActWithoutStaticLocation;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A service without a location (an {@link ActWithoutStaticLocation}, a phone appointment or a break booked
 * as a service) is performed wherever the vehicle is, i.e. at the location of its predecessor. Inserting a
 * located activity in front of it therefore moves the floating activity and changes the leg that leads from
 * the floating activity to the next located stop. The time window constraint has to charge that leg, plus the
 * waiting and the duration of the floating activity, against the next located stop. Before the fix the
 * constraint checked the new activity's departure plus the travel to the next located stop against the
 * floating activity's latest start, so the floating activity's waiting and duration never reached the stop
 * after it and the route ended up with hard time window violations (seen in production: a 429 s leg after a
 * floating stop arriving 369 s after a 60 s window).
 */
class FloatingActivityTimeWindowTest {

    private VehicleRoutingTransportCosts transportCosts;
    private VehicleRoutingActivityCosts activityCosts;

    private static Location at(double x) {
        return Location.Builder.newInstance().setId("x" + x).setCoordinate(Coordinate.newInstance(x, 0)).build();
    }

    @BeforeEach
    void setUp() {
        transportCosts = new VehicleRoutingTransportCosts() {
            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                if (from == null || to == null) return 0;
                return Math.abs(to.getCoordinate().getX() - from.getCoordinate().getX());
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(from, to, arrivalTime, driver, vehicle);
            }

            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return getTransportCost(from, to, departureTime, null, vehicle);
            }
        };
        activityCosts = new WaitingTimeCosts();
    }

    private static VehicleImpl vehicle(double latestArrival) {
        return VehicleImpl.Builder.newInstance("v")
            .setType(VehicleTypeImpl.Builder.newInstance("type").build())
            .setStartLocation(at(0)).setEndLocation(at(0))
            .setEarliestStart(0).setLatestArrival(latestArrival)
            .build();
    }

    private static Service located(String id, double x, double duration, double earliest, double latest) {
        return Service.Builder.newInstance(id).setLocation(at(x)).setServiceTime(duration)
            .setTimeWindow(TimeWindow.newInstance(earliest, latest)).build();
    }

    private static Service floating(String id, double duration, double earliest, double latest) {
        return Service.Builder.newInstance(id).setServiceTime(duration)
            .setTimeWindow(TimeWindow.newInstance(earliest, latest)).build();
    }

    /** the route a, floating..., n with all states computed */
    private StateManager statesOf(VehicleRoutingProblem vrp, VehicleRoute route) {
        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateActivityPrevLocations());
        stateManager.addStateUpdater(new UpdateActivityNextLocations());
        stateManager.addStateUpdater(new UpdateActivityTimes(transportCosts, activityCosts));
        stateManager.addStateUpdater(new UpdateVehicleDependentPracticalTimeWindows(stateManager, transportCosts, activityCosts));
        stateManager.informInsertionStarts(List.of(route), Collections.emptyList());
        return stateManager;
    }

    private HardActivityConstraint.ConstraintsStatus insertBeforeFloating(VehicleRoutingProblem vrp, VehicleRoute route, StateManager stateManager, Service newService, int index) {
        VehicleDependentTimeWindowConstraints constraint = new VehicleDependentTimeWindowConstraints(stateManager, transportCosts, activityCosts);
        TourActivity newAct = vrp.getActivities(newService).getFirst();
        TimeWindow tw = newService.getTimeWindows().iterator().next();
        newAct.setTheoreticalEarliestOperationStartTime(tw.getStart());
        newAct.setTheoreticalLatestOperationStartTime(tw.getEnd());
        JobInsertionContext context = new JobInsertionContext(route, newService, route.getVehicle(), null, 0);
        ActivityContext activityContext = new ActivityContext();
        activityContext.setInsertionIndex(index);
        context.setActivityContext(activityContext);
        TourActivity prevAct = index == 0 ? route.getStart() : route.getActivities().get(index - 1);
        TourActivity nextAct = index == route.getActivities().size() ? route.getEnd() : route.getActivities().get(index);
        assertInstanceOf(ActWithoutStaticLocation.class, nextAct, "test setup: the activity after the insertion point floats");
        return constraint.fulfilled(context, prevAct, newAct, nextAct, prevAct.getEndTime());
    }

    /**
     * a(0..100) at x=0, then the floating l(500..510, 100 s), then n(600..610) at x=0: n is reached at 600.
     * Inserting y at x=100 before l moves l to x=100, so n is reached at 600 + 100 = 700 and is late.
     */
    @Test
    void insertionBeforeFloatingActivityIsChargedWithTheLegToTheNextStop() {
        Service a = located("a", 0, 10, 0, 100);
        Service l = floating("l", 100, 500, 510);
        Service n = located("n", 0, 10, 600, 610);
        Service y = located("y", 100, 10, 0, Double.MAX_VALUE);
        VehicleImpl v = vehicle(10000);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(a).addJob(l).addJob(n).addJob(y).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(a).addService(l).addService(n).build();
        StateManager states = statesOf(vrp, route);
        assertEquals(600, route.getActivities().get(2).getArrTime(), 1e-9, "test setup: n reached in time without y");

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, insertBeforeFloating(vrp, route, states, y, 1),
            "y before the floating l makes n late by 90 s");
    }

    @Test
    void insertionBeforeFloatingActivityThatKeepsTheNextStopInTimeIsAccepted() {
        Service a = located("a", 0, 10, 0, 100);
        Service l = floating("l", 100, 500, 510);
        Service n = located("n", 0, 10, 600, 610);
        Service y = located("y", 5, 10, 0, Double.MAX_VALUE);
        VehicleImpl v = vehicle(10000);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(a).addJob(l).addJob(n).addJob(y).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(a).addService(l).addService(n).build();
        StateManager states = statesOf(vrp, route);

        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, insertBeforeFloating(vrp, route, states, y, 1),
            "n is reached at 605, inside its window");
    }

    /** two floating activities in a row: both are performed at the new location, the leg to n leaves from there */
    @Test
    void consecutiveFloatingActivitiesAreChainedToTheNextStop() {
        Service a = located("a", 0, 10, 0, 100);
        Service l = floating("l", 100, 500, 510);
        Service l2 = floating("l2", 50, 700, 710);
        Service n = located("n", 0, 10, 800, 810);
        Service yLate = located("y", 100, 10, 0, Double.MAX_VALUE);
        Service yOk = located("y2", 5, 10, 0, Double.MAX_VALUE);
        VehicleImpl v = vehicle(10000);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(v).addJob(a).addJob(l).addJob(l2).addJob(n).addJob(yLate).addJob(yOk).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(v).setJobActivityFactory(vrp.getJobActivityFactory()).addService(a).addService(l).addService(l2).addService(n).build();
        StateManager states = statesOf(vrp, route);
        assertEquals(750, route.getActivities().get(3).getArrTime(), 1e-9, "test setup: n reached at 750 without y");

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, insertBeforeFloating(vrp, route, states, yLate, 1), "n would be reached at 850");
        assertEquals(HardActivityConstraint.ConstraintsStatus.FULFILLED, insertBeforeFloating(vrp, route, states, yOk, 1), "n is reached at 755");
        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, insertBeforeFloating(vrp, route, states, yLate, 2), "y between l and l2: n reached at 850 as well");
    }

    /** the floating activity is the last one: the leg to the vehicle's end leaves from the new location */
    @Test
    void floatingActivityBeforeTheRouteEndIsChargedWithTheReturnLeg() {
        Service a = located("a", 0, 10, 0, 100);
        Service l = floating("l", 100, 500, 510);
        Service y = located("y", 100, 10, 0, Double.MAX_VALUE);
        VehicleImpl tight = vehicle(650);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(tight).addJob(a).addJob(l).addJob(y).build();
        VehicleRoute route = VehicleRoute.Builder.newInstance(tight).setJobActivityFactory(vrp.getJobActivityFactory()).addService(a).addService(l).build();
        StateManager states = statesOf(vrp, route);
        assertEquals(600, route.getEnd().getArrTime(), 1e-9, "test setup: back at 600 without y");

        assertEquals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED, insertBeforeFloating(vrp, route, states, y, 1), "back at 700, after the vehicle's latest arrival 650");
    }

    /**
     * The full algorithm on the same shape: y can only be placed in front of the floating l (every other position
     * breaks a window or the vehicle's latest arrival), and that position is infeasible as well, so y has to stay
     * unassigned. Before the fix the algorithm assigned all jobs and n and z started after their windows.
     */
    @Test
    void algorithmLeavesTheJobUnassignedInsteadOfViolatingWindowsAfterAFloatingActivity() {
        Service a = located("a", 0, 10, 0, 100);
        Service l = floating("l", 100, 500, 510);
        Service n = located("n", 0, 10, 600, 610);
        Service z = located("z", 0, 10, 700, 800);
        Service y = located("y", 100, 10, 0, Double.MAX_VALUE);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle(900)).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(a).addJob(l).addJob(n).addJob(z).addJob(y).setRoutingCost(transportCosts).setActivityCosts(activityCosts).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "1").buildAlgorithm();
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution best = Solutions.bestOf(algorithm.searchSolutions());

        for (VehicleRoute route : best.getRoutes()) {
            for (TourActivity act : route.getActivities()) {
                double start = Math.max(act.getArrTime(), act.getTheoreticalEarliestOperationStartTime());
                assertTrue(start <= act.getTheoreticalLatestOperationStartTime() + 1e-9,
                    act.getName() + " of " + ((TourActivity.JobActivity) act).getJob().getId() + " starts at " + start + " after its latest " + act.getTheoreticalLatestOperationStartTime());
            }
            assertTrue(route.getEnd().getArrTime() <= route.getVehicle().getLatestArrival() + 1e-9, "vehicle back at " + route.getEnd().getArrTime());
        }
        StringBuilder tour = new StringBuilder();
        for (VehicleRoute route : best.getRoutes()) {
            for (TourActivity act : route.getActivities()) {
                tour.append(((TourActivity.JobActivity) act).getJob().getId()).append('@').append(act.getArrTime()).append("->").append(act.getEndTime()).append(' ');
            }
            tour.append("end@").append(route.getEnd().getArrTime());
        }
        assertEquals(1, best.getUnassignedJobs().size(), "y has no feasible position, but the tour is " + tour);
        assertEquals("y", best.getUnassignedJobs().iterator().next().getId());
    }
}
