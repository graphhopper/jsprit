package com.graphhopper.jsprit.core.problem;

import static org.junit.Assert.assertEquals;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl.Builder;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Test;

public class BreakAssignmentTest {
    @Test
    public void whenBreakHasFixedLocation_breakActivityHasLocationSpecified() {
        VehicleImpl vehicle = Builder.newInstance("vehicle")
            .setType(VehicleTypeImpl.Builder.newInstance("vehicleType")
                .addCapacityDimension(0, 2)
                .build())
            .setStartLocation(Location.newInstance(0, 0))
            .setBreak(Break.Builder.newInstance("break")
                .setTimeWindow(TimeWindow.newInstance(5, 8))
                .setLocation(Location.newInstance(6, 0))
                .setServiceTime(1)
                .build())
            .build();
        Service service1 = Service.Builder.newInstance("1")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(5, 0))
            .setServiceTime(1)
            .build();
        Service service2 = Service.Builder.newInstance("2")
            .addSizeDimension(0, 1)
            .setLocation(Location.newInstance(10, 0))
            .setServiceTime(10)
            .build();
        VehicleRoutingProblem problem = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle)
            .addJob(service1)
            .addJob(service2)
            .setFleetSize(FleetSize.FINITE)
            .build();

        Location location = findBreak(Solutions.bestOf(Jsprit.Builder
            .newInstance(problem)
            .buildAlgorithm().searchSolutions()).getRoutes().iterator().next())
            .getLocation();

        assertEquals(Location.newInstance(6, 0), location);
    }

    private BreakActivity findBreak(VehicleRoute route) {
        for (TourActivity tourActivity : route.getTourActivities().getActivities()) {
            if (tourActivity instanceof BreakActivity)
                return (BreakActivity) tourActivity;
        }
        throw new IllegalStateException("Break activity is not found.");
    }
}
