package jsprit.examples;

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;

/**
 * Created by schroeder on 23/07/15.
 */
public class WaitingTimeExample2 {

    static interface AlgorithmFactory {
        VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp);
    }

    public static void main(String[] args) {

        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(1.5).setCostPerWaitingTime(1.).build();
//        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type1").setCostPerDistance(1.5).setCostPerWaitingTime(.0).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setReturnToDepot(true)
            .setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();


        Service s1 = Service.Builder.newInstance("s12").setLocation(Location.newInstance(-1, 5)).setTimeWindow(TimeWindow.newInstance(100, 110)).build();
        Service s4 = Service.Builder.newInstance("s13").setLocation(Location.newInstance(0, 10)).build();
        Service s2 = Service.Builder.newInstance("s10").setLocation(Location.newInstance(1, 12)).build();
        Service s3 = Service.Builder.newInstance("s11").setLocation(Location.newInstance(4, 10)).build();
        Service s5 = Service.Builder.newInstance("s14").setLocation(Location.newInstance(6, 5)).setTimeWindow(TimeWindow.newInstance(110, 220)).build();
        vrpBuilder.addJob(s1).addJob(s2).addJob(s3).addJob(s4).addJob(s5).addVehicle(v2);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        final VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(1000);
        vra.addListener(new AlgorithmSearchProgressChartListener("output/search"));
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        new Plotter(vrp, solution).setLabel(Plotter.Label.ID).plot("output/plot", "plot");
    }
}
