package jsprit.core.algorithm;


import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InitialRoutesTest {

    @Test
    public void whenReading_jobMapShouldOnlyContainJob2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1,vrp.getJobs().size());
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1,vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenSolving_nuJobsShouldBe2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(2,solution.getRoutes().iterator().next().getTourActivities().getJobs().size());
    }

    @Test
    public void whenSolving_nuActsShouldBe2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(2, solution.getRoutes().iterator().next().getActivities().size());
    }

    @Test
    public void whenSolving_deliverService1_shouldBeInRoute(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes().iterator().next(),"1"));
    }

    private boolean hasActivityIn(VehicleRoute route, String jobId){
        boolean isInRoute = false;
        for(TourActivity act : route.getActivities()){
            if(act instanceof TourActivity.JobActivity){
                if(((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
            }
        }
        return isInRoute;
    }

    @Test
    public void whenSolving_deliverService2_shouldBeInRoute(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes().iterator().next(),"2"));
    }
}
