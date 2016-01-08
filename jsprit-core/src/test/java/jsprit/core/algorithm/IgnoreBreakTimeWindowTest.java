package jsprit.core.algorithm;

import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Break;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.activity.BreakActivity;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 08/01/16.
 */
public class IgnoreBreakTimeWindowTest {

    @Test
    public void doNotIgnoreBreakTW(){
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType");
        VehicleType vehicleType = vehicleTypeBuilder.setCostPerWaitingTime(0.8).build();

		/*
         * get a vehicle-builder and build a vehicle located at (10,10) with type "vehicleType"
		 */

        VehicleImpl vehicle2;
        {
            VehicleImpl.Builder vehicleBuilder = VehicleImpl.Builder.newInstance("v2");
            vehicleBuilder.setStartLocation(Location.newInstance(0, 0));
            vehicleBuilder.setType(vehicleType);
            vehicleBuilder.setEarliestStart(10).setLatestArrival(50);
            vehicleBuilder.setBreak(Break.Builder.newInstance("lunch").setTimeWindow(TimeWindow.newInstance(14, 14)).setServiceTime(1.).build());
            vehicle2 = vehicleBuilder.build();
        }
		/*
         * build services at the required locations, each with a capacity-demand of 1.
		 */


        Service service4 = Service.Builder.newInstance("2").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(17,17)).build();

        Service service5 = Service.Builder.newInstance("3").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(18, 18)).build();

        Service service7 = Service.Builder.newInstance("4").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(10, 10)).build();

        Service service8 = Service.Builder.newInstance("5").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(12, 12)).build();

        Service service10 = Service.Builder.newInstance("6").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(16, 16)).build();

        Service service11 = Service.Builder.newInstance("7").setLocation(Location.newInstance(0, 0))
            .setServiceTime(1.).setTimeWindow(TimeWindow.newInstance(13, 13)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle2)
            .addJob(service4)
            .addJob(service5).addJob(service7)
            .addJob(service8).addJob(service10).addJob(service11)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(50);

        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());


        Assert.assertTrue(breakShouldBeTime(solution));
    }

    private boolean breakShouldBeTime(VehicleRoutingProblemSolution solution) {
        boolean inTime = true;
        for(TourActivity act : solution.getRoutes().iterator().next().getActivities()){
            if(act instanceof BreakActivity){
                if(act.getEndTime() < ((BreakActivity) act).getJob().getTimeWindow().getStart()){
                    inTime = false;
                }
                if(act.getArrTime() > ((BreakActivity) act).getJob().getTimeWindow().getEnd()){
                    inTime = false;
                }
            }
        }
        return inTime;
    }
}
