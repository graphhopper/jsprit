package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class RandomInsertionTest {

    @Test
    public void initJobsCanBeServedByNumDrivers1() {
        final HashSet<String> first = new HashSet<>(); first.add("C");
        final HashSet<String> second = new HashSet<>(); second.add("A");second.add("B");
        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, first, false, 1, 1, false, null))
            .addVehicle(getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, second, false, 1, 1, false, null))
            .addJob(getService(Location.newInstance(0, 5), 0, 20, new HashSet<String>(), 1))
            .addJob(getService(Location.newInstance(0, 6), 0, 20, new HashSet<String>(), 1));

        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());
        final Map<String, Integer> jobCanBeServedByDriversCount = randomInsertion.jobCanBeServedByDriversCount;

        for (int numCanServe : jobCanBeServedByDriversCount.values())
            assertEquals(4, numCanServe);
    }

    @Test
    public void initBreaksCanBeServedByOneDriver() {
        final HashSet<String> first = new HashSet<>(); first.add("C");
        final HashSet<String> second = new HashSet<>(); second.add("A");second.add("B");
        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, first, false, 1, 1, true, null))
            .addVehicle(getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, second, false, 1, 1, true, null))
            .addJob(getService(Location.newInstance(0, 5), 0, 20, new HashSet<String>(), 1))
            .addJob(getService(Location.newInstance(0, 6), 0, 20, new HashSet<String>(), 1));

        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());
        final Map<String, Integer> jobCanBeServedByDriversCount = randomInsertion.jobCanBeServedByDriversCount;

        int numBreaks = 0;
        for (Map.Entry<String, Integer> entry : jobCanBeServedByDriversCount.entrySet()) {
            if (entry.getKey().contains("break_")) {
                ++numBreaks;
                assertEquals((int) entry.getValue(), 1);
            }
        }

        assertEquals(2, numBreaks);
    }

    @Test
    public void initJobsCanBeServedByNumDrivers2() {
        final HashSet<String> first = new HashSet<>(); first.add("C");
        final HashSet<String> second = new HashSet<>(); second.add("A");second.add("B");
        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, first, false, 1, 1, false, null))
            .addVehicle(getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, second, false, 1, 1, false, null))
            .addJob(getService(Location.newInstance(0, 5), 0, 20, first, 1))
            .addJob(getService(Location.newInstance(0, 6), 0, 20, second, 1));

        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());
        final Map<String, Integer> jobCanBeServedByDriversCount = randomInsertion.jobCanBeServedByDriversCount;

        for (int numCanServe : jobCanBeServedByDriversCount.values())
            assertEquals(3, numCanServe);
    }

    @Test
    public void initJobsCanBeServedByNumDrivers3() {
        final Service service1 = getService(Location.newInstance(0, 5), 0, 20, new HashSet<String>(), 1);
        final Service service2 = getService(Location.newInstance(0, 6), 0, 20, new HashSet<String>(), 1);
        final Vehicle v1 = getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, new HashSet<String>(), false, 1, 1, false, service1.getId());
        final Vehicle v2 = getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, new HashSet<String>(), false, 1, 1, false, service2.getId());


        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(v1)
            .addVehicle(v2)
            .addJob(service1)
            .addJob(service2);

        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());
        final Map<String, Integer> jobCanBeServedByDriversCount = randomInsertion.jobCanBeServedByDriversCount;

        for (int numCanServe : jobCanBeServedByDriversCount.values())
            assertEquals(3, numCanServe);
    }

    private static Service getService(Location location, int start, int end, Set<String> requiredSkills, int priority) {
        return Delivery.Builder.newInstance("service_" + UUID.randomUUID().toString().substring(0,5))
            .setLocation(location)
            .setServiceTime(1)
            .addTimeWindow(new TimeWindow(start, end))
            .addSizeDimension(0, 1)
            .addAllRequiredSkills(requiredSkills)
            .setPriority(priority)
            .setName(UUID.randomUUID().toString()).build();

    }

    private static Vehicle getVehicle(String id, Location location, int start, int end, int capacity, Set<String> skills, boolean returnToDepot, int fixedCost, int costPerDistance, boolean aBreak, String excludeTask) {
        final VehicleImpl.Builder builder = VehicleImpl.Builder.newInstance(id)
            .setStartLocation(location).setLatestArrival(end).setEarliestStart(start).setType(
                VehicleTypeImpl.Builder.newInstance(UUID.randomUUID().toString()).setFixedCost(fixedCost).setCostPerDistance(costPerDistance).addCapacityDimension(0, capacity).build()
            )
            .addAllSkills(skills).setReturnToDepot(returnToDepot);

        if (excludeTask != null)
            builder.addExcludedTask(excludeTask);
        if (aBreak)
            builder.setBreak(Break.Builder.newInstance("break_" + id).build());
        return builder.build();

    }

}
