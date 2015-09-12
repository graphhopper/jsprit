package jsprit.examples;

import jsprit.analysis.toolbox.AlgorithmSearchProgressChartListener;
import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.RandomNumberGeneration;
import jsprit.core.util.Solutions;

import java.util.Random;

/**
 * Created by schroeder on 23/07/15.
 */
public class WaitingTimeExample {

    public static void main(String[] args) {

        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(4.).setCostPerWaitingTime(4.0).build();
        VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type1").setCostPerDistance(4.).setCostPerWaitingTime(4.0).build();

        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setReturnToDepot(true)
            .setStartLocation(Location.newInstance(0, 0))
            .setEarliestStart(0).setLatestArrival(220)
            .build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3").setType(type1).setReturnToDepot(true)
            .setStartLocation(Location.newInstance(0, 10))
            .setEarliestStart(200).setLatestArrival(450)
            .build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        Random r = RandomNumberGeneration.newInstance();
        for (int i = 0; i < 40; i++) {
            Service s = Service.Builder.newInstance("s_" + i).setServiceTime(5)
                .setLocation(Location.newInstance(1 - r.nextInt(5), 10 + r.nextInt(10))).build();
            vrpBuilder.addJob(s);
        }
        Service s1 = Service.Builder.newInstance("s12").setLocation(Location.newInstance(-3, 15)).setTimeWindow(TimeWindow.newInstance(290, 600)).build();
        Service s4 = Service.Builder.newInstance("s13").setLocation(Location.newInstance(0, 20)).setTimeWindow(TimeWindow.newInstance(290, 340)).build();
        Service s2 = Service.Builder.newInstance("s10").setLocation(Location.newInstance(-1, 15)).setTimeWindow(TimeWindow.newInstance(300, 350)).build();
        Service s3 = Service.Builder.newInstance("s11").setLocation(Location.newInstance(10, 10)).setTimeWindow(TimeWindow.newInstance(400, 600)).build();
        vrpBuilder.addJob(s1).addJob(s2).addJob(s3).addJob(s4).addVehicle(v2).addVehicle(v3);
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        final VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(2000);
        vra.addListener(new AlgorithmSearchProgressChartListener("output/search"));
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, new TransportDistance() {
            @Override
            public double getDistance(Location from, Location to) {
                return vrp.getTransportCosts().getTransportTime(from, to, 0., null, null);
            }
        });

        System.out.println("totalWaiting: " + sa.getWaitingTime());
        System.out.println("brokenTWs: " + sa.getTimeWindowViolation());

        new Plotter(vrp, solution).setLabel(Plotter.Label.ID).plot("output/plot", "plot");
    }
}
