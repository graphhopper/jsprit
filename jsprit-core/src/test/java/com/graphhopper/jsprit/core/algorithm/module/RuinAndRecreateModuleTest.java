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
import static com.graphhopper.jsprit.core.algorithm.module.RuinAndRecreateModule.removeEmptyRoutes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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


    @Test
    public void testEmptyRoutesRemoved() {
        List<VehicleRoute> routes = new ArrayList<>();
        VehicleRoute vehicleRoute1 = mock(VehicleRoute.class);
        when(vehicleRoute1.isEmpty()).thenReturn(false);
        VehicleRoute vehicleRoute2 = mock(VehicleRoute.class);
        when(vehicleRoute2.isEmpty()).thenReturn(true);

        routes.add(vehicleRoute1);
        routes.add(vehicleRoute2);

        removeEmptyRoutes(routes);

        assertEquals(routes.size(), 1);
        assertTrue(routes.contains(vehicleRoute1));
    }
}
