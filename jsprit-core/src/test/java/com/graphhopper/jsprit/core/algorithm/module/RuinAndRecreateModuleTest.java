package com.graphhopper.jsprit.core.algorithm.module;

import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivities;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.junit.Test;

import java.util.*;

import static com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule.getUnassignedJobs;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RuinAndRecreateModuleTest {
    @Test
    public void testUnassignedJobs() {
        VehicleRoute vehicleRoute = mock(VehicleRoute.class);
        Vehicle vehicle = mock(Vehicle.class);
        Break aBreak = Break.Builder.newInstance(UUID.randomUUID().toString()).build();
        when(vehicle.getBreak()).thenReturn(aBreak);
        when(vehicleRoute.getVehicle()).thenReturn(vehicle);
        TourActivities tourActivities = mock(TourActivities.class);
        when(vehicleRoute.getTourActivities()).thenReturn(tourActivities);

        Collection<VehicleRoute> routes = new ArrayList<>();
        routes.add(vehicleRoute);

        Break unRelatedBreak = Break.Builder.newInstance(UUID.randomUUID().toString()).build();
        Set<Job> unassigned = new HashSet<>();
        unassigned.add(unRelatedBreak);

        when(tourActivities.servesJob(aBreak)).thenReturn(true);
        assertEquals(0, getUnassignedJobs(new VehicleRoutingProblemSolution(routes, 212), unassigned).size());

        when(tourActivities.servesJob(aBreak)).thenReturn(false);
        assertEquals(1, getUnassignedJobs(new VehicleRoutingProblemSolution(routes, 212), unassigned).size());
    }
}
