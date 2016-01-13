package jsprit.core.algorithm;

import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 26/05/15.
 */
public class MultipleTimeWindowsTest {

    @Test
    public void service2ShouldNotBeInserted(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10,0)).build();

        Service s2 = Service.Builder.newInstance("s2")
                .addTimeWindow(50.,60.)
                .setLocation(Location.newInstance(20, 0)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0,0))
                .setEarliestStart(0.).setLatestArrival(40).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        Assert.assertEquals(1,solution.getUnassignedJobs().size());
    }

    @Test
    public void service2ShouldBeInsertedIntoNewVehicle(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10,0))
                .addTimeWindow(5.,15.).build();

        Service s2 = Service.Builder.newInstance("s2")
                .addTimeWindow(50.,60.)
                .setLocation(Location.newInstance(20, 0)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0,0))
                .setEarliestStart(0.).setLatestArrival(40).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setStartLocation(Location.newInstance(0,0))
                .setEarliestStart(40.).setLatestArrival(80).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).addVehicle(v2).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        Assert.assertEquals(0,solution.getUnassignedJobs().size());
        Assert.assertEquals(2, solution.getRoutes().size());
    }

    @Test
    public void service2ShouldBeInserted(){
        Service s = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10,0)).build();

        Service s2 = Service.Builder.newInstance("s2")
                .addTimeWindow(50., 60.).addTimeWindow(15., 25)
                .setLocation(Location.newInstance(20, 0)).build();

        VehicleImpl v = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0,0))
                .setEarliestStart(0.).setLatestArrival(40).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s).addJob(s2).addVehicle(v).build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        Assert.assertEquals(0,solution.getUnassignedJobs().size());
    }
}
