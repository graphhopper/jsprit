package jsprit.core.algorithm;

import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;


public class ExternalInitialSolutionIsInValidTest {

    @Test
    public void itShouldSolveProblemWithIniSolutionExternallyCreated() {

        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0, 10)).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithm_without_construction.xml");

        /*
        create ini sol
         */
        VehicleRoute route1 = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(s1).build();

        vra.addInitialSolution(new VehicleRoutingProblemSolution(Arrays.asList(route1), 20.));

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);


    }

}
