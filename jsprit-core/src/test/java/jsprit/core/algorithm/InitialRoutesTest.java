package jsprit.core.algorithm;


import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InitialRoutesTest {

    @Test
    public void whenReading_jobMapShouldOnlyContainJob2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1,getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReadingProblem2_jobMapShouldContain_service2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1,getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReading_jobMapShouldContain_shipment4(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1,getNuShipments(vrp));
        assertTrue(vrp.getJobs().containsKey("4"));
    }

    private int getNuShipments(VehicleRoutingProblem vrp) {
        int nuShipments = 0;
        for(Job job : vrp.getJobs().values()){
            if(job instanceof Shipment) nuShipments++;
        }
        return nuShipments;
    }

    private int getNuServices(VehicleRoutingProblem vrp) {
        int nuServices = 0;
        for(Job job : vrp.getJobs().values()){
            if(job instanceof Service) nuServices++;
        }
        return nuServices;
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2_v2(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeTwoActsAssociatedToShipment4(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        Job job = vrp.getJobs().get("4");
        List<AbstractActivity> activities = vrp.getActivities(job);

        assertEquals(2, activities.size());
    }

    @Test
    public void whenSolving_nuJobsInSolutionShouldBe2(){

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
    public void whenSolvingProblem2_nuJobsInSolutionShouldBe4(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        int jobsInSolution = 0;
        for(VehicleRoute r : solution.getRoutes()){
            jobsInSolution += r.getTourActivities().jobSize();
        }
        assertEquals(4,jobsInSolution);
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
    public void whenSolvingProblem2_nuActsShouldBe6(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        int nuActs = 0;
        for(VehicleRoute r : solution.getRoutes()){
            nuActs += r.getActivities().size();
        }
        assertEquals(6, nuActs);
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

    @Test
    public void whenSolvingProblem2_deliverServices_and_allShipmentActs_shouldBeInRoute(){

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes(),"1"));
        assertTrue(hasActivityIn(solution.getRoutes(),"2"));
        assertTrue(hasActivityIn(solution.getRoutes(),"3"));
        assertTrue(hasActivityIn(solution.getRoutes(),"4"));
    }

    private boolean hasActivityIn(Collection<VehicleRoute> routes, String jobId) {
        boolean isInRoute = false;
        for(VehicleRoute route : routes) {
            for (TourActivity act : route.getActivities()) {
                if (act instanceof TourActivity.JobActivity) {
                    if (((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
                }
            }
        }
        return isInRoute;
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

        assertTrue(hasActivityIn(solution.getRoutes().iterator().next(), "2"));
    }
}
