package com.graphhopper.jsprit.io.algorithm;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class TestAlgorithmSquashActivityTimes {

    @Test
    public void testSquash () {
        VehicleRoutingActivityCosts activityCost = new VehicleRoutingActivityCosts() {
            @Override
            public double getActivityCost(TourActivity prevAct, TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getActivityDuration(prevAct, tourAct, arrivalTime, driver, vehicle);
            }

            @Override
            public double getActivityDuration(TourActivity from, TourActivity to, double startTime, Driver driver, Vehicle vehicle) {
                if (from != null && !(to instanceof BreakActivity || from instanceof BreakActivity) && from.getLocation().getCoordinate().equals(to.getLocation().getCoordinate())) {
                    return 1;
                }

                return to.getOperationTime();
            }
        };
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(VehicleImpl.Builder.newInstance("vehicle")
                .setStartLocation(Location.newInstance(1.521801, 42.506285))
                .setEarliestStart(0)
                .setLatestArrival(10).setType(VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).addCapacityDimension(0, Integer.MAX_VALUE).build())
                .setReturnToDepot(false)
                .build())
            .addJob(getJobs(0, 10, 1, 7))
            .addJob(getJobs(0, 10, 1, 7))
            .addJob(getJobs(0, 10, 1, 7))
            .addJob(getJobs(0, 10, 1, 6))
            .setActivityCosts(activityCost)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        StateManager stateManager = new StateManager(vrp);

        ConstraintManager constraintManager = new ConstraintManager (vrp, stateManager) {
            @Override
            public HardActivityConstraint.ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime){
                if (newAct instanceof BreakActivity && newAct.getLocation().equals(nextAct.getLocation()))
                    return ConstraintsStatus.NOT_FULFILLED;

                return super.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
            }
        };

        final VehicleRoutingAlgorithm algorithm = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, Runtime.getRuntime().availableProcessors() + 1, getClass().getResource("algorithmConfig_withoutIterations.xml").getFile(), stateManager, constraintManager, null);

        final VehicleRoutingProblemSolution problemSolution = Solutions.bestOf(algorithm.searchSolutions());

        assertEquals(problemSolution.getUnassignedJobs().size(), 0);
        assertEquals(problemSolution.getRoutes().iterator().next().getTourActivities().getActivities().size(), 4);
    }

    private Job getJobs(int start, int end, int capacity, int duration) {
        return Delivery.Builder.newInstance("service_" + new Random().nextInt())
            .setLocation(Location.newInstance(1.52181, 42.5062))
            .setServiceTime(duration)
            .addTimeWindow(new TimeWindow(start, end))
            .addSizeDimension(0, capacity)
            .setName(UUID.randomUUID().toString())
            .build();
    }
}
