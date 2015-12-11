package jsprit.core.algorithm;

import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class UnassignedJobListTest {

    @Test
    public void job2ShouldBeInBadJobList_dueToTimeWindow() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setEarliestStart(0).setLatestArrival(12).setStartLocation(Location.newInstance(1, 1)).build());
        Service job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job1);
        Service job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(2, 2)).setTimeWindow(TimeWindow.newInstance(12, 24)).setServiceTime(1).build();
        builder.addJob(job2);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertTrue(!solution.getUnassignedJobs().contains(job1));
        assertTrue(solution.getUnassignedJobs().contains(job2));
    }

    @Test
    public void job2ShouldBeInBadJobList_dueToSize() {
        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
        builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setEarliestStart(0).setLatestArrival(12).setStartLocation(Location.newInstance(1, 1)).build());
        Service job1 = Service.Builder.newInstance("job1").setLocation(Location.newInstance(0, 0)).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job1);
        Service job2 = Service.Builder.newInstance("job2").setLocation(Location.newInstance(2, 2)).addSizeDimension(0, 10).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build();
        builder.addJob(job2);

        VehicleRoutingProblem vrp = builder.build();
        VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(10);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        assertTrue(!solution.getUnassignedJobs().contains(job1));
        assertTrue(solution.getUnassignedJobs().contains(job2));
    }

}
