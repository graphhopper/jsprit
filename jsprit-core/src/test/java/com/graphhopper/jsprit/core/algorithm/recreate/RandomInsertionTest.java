package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.*;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

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
            assertEquals(2, numCanServe);
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
        assertEquals(1, jobCanBeServedByDriversCount.get("break_v1"), .001);
        assertEquals(1, jobCanBeServedByDriversCount.get("break_v2"), .001);
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
            assertEquals(1, numCanServe);
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
            assertEquals(1, numCanServe);
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

    @Test
    public void inTimeWindowShipment() {
        assertTrue(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 75, 170));
        assertTrue(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 0, 170));
        assertTrue(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 75, 150));
        assertTrue(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 90, 250));

        assertFalse(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 90, 110));
        assertFalse(RandomInsertion.inTimeWindow(getShipment(50, 100, 120, 200), 110, 250));
    }

    @Test
    public void inTimeWindowService() {
        assertTrue(RandomInsertion.inTimeWindow(getService(50, 100), 75, 170));
        assertTrue(RandomInsertion.inTimeWindow(getService(50, 100), 0, 75));
        assertTrue(RandomInsertion.inTimeWindow(getService(50, 100), 50, 100));

        assertFalse(RandomInsertion.inTimeWindow(getService(50, 100), 110, 250));
        assertFalse(RandomInsertion.inTimeWindow(getService(50, 100), 0, 25));
    }

    @Test
    public void sortTest() {
        final HashSet<String> skillsService1 = new HashSet<>();
        skillsService1.add("a"); skillsService1.add("b");
        final HashSet<String> skillsService2 = new HashSet<>();
        skillsService2.add("c"); skillsService2.add("b");
        final HashSet<String> skillsDriver1 = new HashSet<>();
        skillsDriver1.add("a"); skillsDriver1.add("b");
        final HashSet<String> skillsDriver2 = new HashSet<>();
        skillsDriver2.add("c"); skillsDriver2.add("b");
        final HashSet<String> skillsDriver3 = new HashSet<>();
        skillsDriver3.add("c"); skillsDriver3.add("b"); skillsDriver3.add("a");
        final Service service1 = getService(Location.newInstance(0, 5), 0, 20, skillsService1, 1);
        final Service service2 = getService(Location.newInstance(0, 6), 0, 20, skillsService2, 1);
        final Vehicle v1 = getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, skillsDriver1, false, 1, 1, false, service2.getId());
        final Vehicle v2 = getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, skillsDriver2, false, 1, 1, false, service1.getId());
        final Vehicle v3 = getVehicle("v3", Location.newInstance(0, 14), 0, 100, 20, skillsDriver3, false, 1, 1, false, service2.getId());


        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(v1)
            .addVehicle(v2)
            .addVehicle(v3)
            .addJob(service1)
            .addJob(service2);
        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());

        randomInsertion.random = new Random() {
            @Override
            public double nextDouble() {
                return 0.5;
            }
        };

        final List<Job> unassigned = new ArrayList<>();
        unassigned.add(service1);
        unassigned.add(service2);

        Collections.shuffle(unassigned);
        randomInsertion.sortJobs(unassigned);
        assertEquals(1, (int) randomInsertion.jobCanBeServedByDriversCount.get(service2.getId()));
        assertEquals(2, (int) randomInsertion.jobCanBeServedByDriversCount.get(service1.getId()));
        assertEquals(service2, unassigned.get(0));
        assertEquals(service1, unassigned.get(1));
    }


    @Test
    public void sortTestWithTaskThatNotExist() {
        final HashSet<String> skillsService1 = new HashSet<>();
        skillsService1.add("a"); skillsService1.add("b");
        final HashSet<String> skillsDriver1 = new HashSet<>();
        skillsDriver1.add("a"); skillsDriver1.add("b");
        final HashSet<String> skillsDriver2 = new HashSet<>();
        skillsDriver2.add("c"); skillsDriver2.add("b");
        final HashSet<String> skillsDriver3 = new HashSet<>();
        skillsDriver3.add("c"); skillsDriver3.add("b"); skillsDriver3.add("a");
        final Service service1 = getService(Location.newInstance(0, 5), 0, 20, skillsService1, 1);
        final Vehicle v1 = getVehicle("v1", Location.newInstance(0, 0), 0, 100, 20, skillsDriver1, false, 1, 1, false, null);
        final Vehicle v2 = getVehicle("v2", Location.newInstance(0, 14), 0, 100, 20, skillsDriver2, false, 1, 1, false, service1.getId());
        final Vehicle v3 = getVehicle("v3", Location.newInstance(0, 14), 0, 100, 20, skillsDriver3, false, 1, 1, false, null);


        final VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addVehicle(v1)
            .addVehicle(v2)
            .addVehicle(v3)
            .addJob(service1);
        final RandomInsertion randomInsertion = new RandomInsertion(null, builder.build());

        randomInsertion.random = new Random() {
            @Override
            public double nextDouble() {
                return 0.5;
            }
        };

        final Break aBreak = Break.Builder.newInstance(UUID.randomUUID().toString()).setServiceTime(60).addTimeWindow(0, 160).build();
        final List<Job> unassigned = new ArrayList<>();
        unassigned.add(service1);
        unassigned.add(aBreak);

        Collections.shuffle(unassigned);
        randomInsertion.sortJobs(unassigned);
        assertEquals(2, (int) randomInsertion.jobCanBeServedByDriversCount.get(service1.getId()));
        assertEquals(1, (int) randomInsertion.jobCanBeServedByDriversCount.get(aBreak.getId()));
        assertEquals(aBreak, unassigned.get(0));
        assertEquals(service1, unassigned.get(1));
    }

    private Shipment getShipment(int pStart, int pEnd, int dStart, int dEnd) {
        return Shipment.Builder.newInstance(UUID.randomUUID().toString())
            .setPickupLocation(Location.newInstance(UUID.randomUUID().toString()))
            .setDeliveryLocation(Location.newInstance(UUID.randomUUID().toString()))
            .setPickupTimeWindow(new TimeWindow(pStart, pEnd))
            .setDeliveryTimeWindow(new TimeWindow(dStart, dEnd))
            .build();
    }

    private Service getService(int start, int end) {
        return Service.Builder.newInstance(UUID.randomUUID().toString())
            .setLocation(Location.newInstance(UUID.randomUUID().toString()))
            .setTimeWindow(new TimeWindow(start, end))
            .build();
    }

}
