/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.CostFactory;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The parallel position scan must give exactly the sequential result: same cost, same positions, same
 * selected time windows, same failed constraints, whatever the number of threads and however the route is
 * cut.
 */
@DisplayName("Parallel insertion scan")
class ParallelInsertionScanTest {

    private static final ForkJoinPool POOL = new ForkJoinPool(4);

    @AfterAll
    static void shutdown() {
        POOL.shutdown();
    }

    @Test
    @DisplayName("triangular bounds cover the positions once, in order, the first range being the longest")
    void triangularBounds() {
        for (int positions : new int[]{1, 2, 9, 64, 65, 100, 1001}) {
            for (int tasks : new int[]{1, 2, 3, 8, 16}) {
                int[] b = ParallelInsertionScan.triangularBounds(positions, tasks);
                assertEquals(0, b[0]);
                assertEquals(positions, b[b.length - 1]);
                assertTrue(b.length - 1 <= tasks, "at most " + tasks + " ranges: " + Arrays.toString(b));
                for (int k = 1; k < b.length; k++) assertTrue(b[k] > b[k - 1], "increasing: " + Arrays.toString(b));
            }
        }
        // 100 positions in 2: the first range carries half of the 5050 pair evaluations, which is 29 positions
        assertArrayEquals(new int[]{0, 30, 100}, ParallelInsertionScan.triangularBounds(100, 2));
    }

    @Test
    @DisplayName("linear bounds cut the positions into equal ranges")
    void linearBounds() {
        assertArrayEquals(new int[]{0, 25, 50, 75, 100}, ParallelInsertionScan.linearBounds(100, 4));
        assertArrayEquals(new int[]{0, 1, 2, 3}, ParallelInsertionScan.linearBounds(3, 8));
        for (int positions : new int[]{1, 7, 100, 1001}) {
            int[] b = ParallelInsertionScan.linearBounds(positions, 6);
            assertEquals(0, b[0]);
            assertEquals(positions, b[b.length - 1]);
            for (int k = 1; k < b.length; k++) assertTrue(b[k] > b[k - 1]);
        }
    }

    @Test
    @DisplayName("shipments: the parallel scan of a long route gives the sequential insertion data")
    void shipmentsIdentical() {
        for (long seed = 1; seed <= 12; seed++) {
            Random random = new Random(seed);
            boolean tightWindows = seed % 3 != 0;
            // the first 75 shipments form the route in a random order (every pickup before its delivery), the
            // last 15 are evaluated; with tight windows the routed activities get windows around their arrival
            // (so the route is feasible) and the candidates random ones (so the time window constraint breaks)
            int n = 90, routed = 75;
            Coordinate depot = Coordinate.newInstance(50, 50);
            Coordinate[][] coords = new Coordinate[n][];
            int[] sizes = new int[n];
            for (int i = 0; i < n; i++) {
                coords[i] = new Coordinate[]{randomCoordinate(random), randomCoordinate(random)};
                sizes[i] = 1 + random.nextInt(3);
            }
            List<int[]> order = randomShipmentOrder(random, routed); // {shipment, 0 = pickup / 1 = delivery}
            double[][] arrival = new double[n][2];
            double time = 0, load = 0, maxLoad = 0;
            Coordinate prev = depot;
            for (int[] e : order) {
                Coordinate c = coords[e[0]][e[1]];
                time += distance(prev, c);
                arrival[e[0]][e[1]] = time;
                time += 3;
                prev = c;
                load += e[1] == 0 ? sizes[e[0]] : -sizes[e[0]];
                maxLoad = Math.max(maxLoad, load);
            }
            double horizon = time + distance(prev, depot);
            List<Shipment> shipments = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Shipment.Builder b = Shipment.Builder.newInstance("s" + i)
                    .setPickupLocation(Location.Builder.newInstance().setCoordinate(coords[i][0]).build())
                    .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(coords[i][1]).build())
                    .addSizeDimension(0, sizes[i]).setPickupServiceTime(3).setDeliveryServiceTime(3);
                if (tightWindows) {
                    for (int a = 0; a < 2; a++) {
                        if (!random.nextBoolean()) continue;
                        double start = i < routed ? arrival[i][a] - 5 : random.nextInt((int) horizon);
                        TimeWindow tw = TimeWindow.newInstance(Math.max(0, start), start + 40);
                        if (a == 0) {
                            b.addPickupTimeWindow(tw);
                            if (i >= routed && random.nextBoolean()) b.addPickupTimeWindow(TimeWindow.newInstance(start + 300, start + 340));
                        } else b.addDeliveryTimeWindow(tw);
                    }
                }
                shipments.add(b.build());
            }
            VehicleImpl vehicle = vehicle((int) maxLoad + 2, horizon * 2);
            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addAllJobs(new ArrayList<>(shipments))
                .setRoutingCost(CostFactory.createEuclideanCosts()).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
            VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory());
            for (int[] e : order) {
                if (e[1] == 0) builder.addPickup(shipments.get(e[0]));
                else builder.addDelivery(shipments.get(e[0]));
            }
            compareCalculators(vrp, builder.build(), new ArrayList<>(shipments.subList(routed, n)), seed);
        }
    }

    @Test
    @DisplayName("services: the parallel scan of a long route gives the sequential insertion data")
    void servicesIdentical() {
        for (long seed = 1; seed <= 12; seed++) {
            Random random = new Random(seed);
            boolean tightWindows = seed % 3 != 0;
            int n = 200, routed = 180;
            Coordinate depot = Coordinate.newInstance(50, 50);
            Coordinate[] coords = new Coordinate[n];
            for (int i = 0; i < n; i++) coords[i] = randomCoordinate(random);
            List<Integer> order = new ArrayList<>();
            for (int i = 0; i < routed; i++) order.add(i);
            Collections.shuffle(order, random);
            double[] arrival = new double[n];
            double time = 0;
            Coordinate prev = depot;
            for (int i : order) {
                time += distance(prev, coords[i]);
                arrival[i] = time;
                time += 2;
                prev = coords[i];
            }
            double horizon = time + distance(prev, depot);
            List<Service> services = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                Service.Builder b = Service.Builder.newInstance("j" + i).setLocation(Location.Builder.newInstance().setCoordinate(coords[i]).build()).addSizeDimension(0, 1).setServiceTime(2);
                if (tightWindows && random.nextBoolean()) {
                    double start = i < routed ? arrival[i] - 5 : random.nextInt((int) horizon);
                    b.addTimeWindow(TimeWindow.newInstance(Math.max(0, start), start + 30));
                    if (i >= routed && random.nextBoolean()) b.addTimeWindow(TimeWindow.newInstance(start + 500, start + 530));
                }
                services.add(b.build());
            }
            VehicleImpl vehicle = vehicle(1000, horizon * 2);
            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addAllJobs(new ArrayList<>(services))
                .setRoutingCost(CostFactory.createEuclideanCosts()).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
            VehicleRoute.Builder builder = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory());
            for (int i : order) builder.addService(services.get(i));
            compareCalculators(vrp, builder.build(), new ArrayList<>(services.subList(routed, n)), seed);
        }
    }

    @Test
    @DisplayName("the whole algorithm: with and without the parallel scan the same solution comes out")
    void algorithmIdentical() {
        Random random = new Random(7);
        List<Shipment> shipments = randomShipments(random, 70, true);
        VehicleImpl vehicle = vehicle(12, 100_000);
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addAllJobs(new ArrayList<>(shipments))
            .setRoutingCost(CostFactory.createEuclideanCosts()).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).build();
        VehicleRoutingProblemSolution parallel = solve(vrp, 1), sequential = solve(vrp, 0);
        assertEquals(sequential.getCost(), parallel.getCost(), 0.0);
        assertEquals(sequential.getUnassignedJobs().size(), parallel.getUnassignedJobs().size());
        assertEquals(activityIds(sequential), activityIds(parallel));
    }

    private static VehicleRoutingProblemSolution solve(VehicleRoutingProblem vrp, int minActivities) {
        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setExecutorService(POOL, 4)
            .setRandom(new Random(42))
            .setProperty(Jsprit.Parameter.INSERTION_NOISE_PROB, "0")
            .setProperty(Jsprit.Parameter.PARALLEL_SCAN_MIN_SHIPMENT_ACTIVITIES, String.valueOf(minActivities))
            .setProperty(Jsprit.Parameter.PARALLEL_SCAN_MIN_SERVICE_ACTIVITIES, String.valueOf(minActivities))
            .buildAlgorithm();
        vra.setMaxIterations(60);
        return Solutions.bestOf(vra.searchSolutions());
    }

    private static List<String> activityIds(VehicleRoutingProblemSolution solution) {
        List<String> ids = new ArrayList<>();
        for (VehicleRoute route : solution.getRoutes()) {
            for (TourActivity act : route.getActivities()) ids.add(act.getName() + ":" + ((TourActivity.JobActivity) act).getJob().getId());
        }
        return ids;
    }

    /** Builds the sequential and the parallel calculator over the same states and compares every candidate's insertion data. */
    private static void compareCalculators(VehicleRoutingProblem vrp, VehicleRoute route, List<Job> candidates, long seed) {
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addTimeWindowConstraint();
        constraintManager.addLoadConstraint();
        stateManager.updateLoadStates();
        stateManager.updateTimeWindowStates();
        // the activity end times the ranges start from; the algorithm registers this updater itself
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(), vrp.getActivityCosts()));
        List<VehicleRoute> routes = List.of(route);
        stateManager.informInsertionStarts(routes, candidates);

        JobInsertionCostsCalculator sequential = calculator(vrp, stateManager, constraintManager, null);
        ParallelInsertionScan scan = new ParallelInsertionScan(POOL, 4, 1, 1);
        JobInsertionCostsCalculator parallel = calculator(vrp, stateManager, constraintManager, scan);
        int found = 0;
        for (Job job : candidates) {
            for (double bestKnown : new double[]{Double.MAX_VALUE, 150.0}) {
                InsertionData s = sequential.getInsertionData(route, job, route.getVehicle(), route.getDepartureTime(), route.getDriver(), bestKnown);
                InsertionData p = parallel.getInsertionData(route, job, route.getVehicle(), route.getDepartureTime(), route.getDriver(), bestKnown);
                String what = "seed " + seed + " job " + job.getId() + " bestKnown " + bestKnown;
                assertEquals(s instanceof InsertionData.NoInsertionFound, p instanceof InsertionData.NoInsertionFound, what);
                assertEquals(s.getFailedConstraintNames(), p.getFailedConstraintNames(), what);
                if (s instanceof InsertionData.NoInsertionFound) continue;
                found++;
                assertEquals(s.getInsertionCost(), p.getInsertionCost(), 0.0, what);
                assertEquals(s.getPickupInsertionIndex(), p.getPickupInsertionIndex(), what);
                assertEquals(s.getDeliveryInsertionIndex(), p.getDeliveryInsertionIndex(), what);
                assertEquals(s.getSelectedTimeWindow(), p.getSelectedTimeWindow(), what);
                assertEquals(s.getSelectedPickupTimeWindow(), p.getSelectedPickupTimeWindow(), what);
                assertEquals(s.getCostBreakdown().getTotal(), p.getCostBreakdown().getTotal(), 0.0, what);
            }
        }
        assertTrue(found > 0, "seed " + seed + ": no candidate could be inserted, the comparison saw only rejections");
    }

    private static JobInsertionCostsCalculator calculator(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager, ParallelInsertionScan scan) {
        JobInsertionCostsCalculatorBuilder builder = new JobInsertionCostsCalculatorBuilder(new ArrayList<>(), new ArrayList<>())
            .setLocalLevel(true)
            .setConstraintManager(constraintManager)
            .setStateManager(stateManager)
            .setVehicleRoutingProblem(vrp)
            .setVehicleFleetManager(new com.graphhopper.jsprit.core.problem.vehicle.FiniteFleetManagerFactory(vrp.getVehicles()).createFleetManager());
        if (scan != null) builder.setParallelScan(scan);
        return builder.build();
    }

    private static VehicleImpl vehicle(int capacity, double latestArrival) {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0, capacity).setCostPerDistance(1).setCostPerTransportTime(0).build();
        return VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(50, 50)).build()).setType(type).setEarliestStart(0).setLatestArrival(latestArrival).build();
    }

    private static Coordinate randomCoordinate(Random random) {
        return Coordinate.newInstance(random.nextInt(100), random.nextInt(100));
    }

    private static double distance(Coordinate a, Coordinate b) {
        return Math.sqrt((a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY()));
    }

    /** Random shipments in a 100 x 100 square, half of them with a tight window on pickup or delivery. */
    private static List<Shipment> randomShipments(Random random, int n, boolean tightWindows) {
        List<Shipment> shipments = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            Shipment.Builder b = Shipment.Builder.newInstance("s" + i)
                .setPickupLocation(Location.Builder.newInstance().setCoordinate(randomCoordinate(random)).build())
                .setDeliveryLocation(Location.Builder.newInstance().setCoordinate(randomCoordinate(random)).build())
                .addSizeDimension(0, 1 + random.nextInt(3)).setPickupServiceTime(3).setDeliveryServiceTime(3);
            if (tightWindows && random.nextBoolean()) {
                double start = random.nextInt(2000);
                b.addPickupTimeWindow(TimeWindow.newInstance(start, start + 200));
            }
            if (tightWindows && random.nextBoolean()) {
                double start = random.nextInt(3000);
                b.addDeliveryTimeWindow(TimeWindow.newInstance(start, start + 200));
            }
            shipments.add(b.build());
        }
        return shipments;
    }

    /** The pickups and deliveries of the shipments 0..n-1 in a random order, every pickup before its delivery. */
    private static List<int[]> randomShipmentOrder(Random random, int n) {
        List<int[]> events = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            events.add(new int[]{i, 0});
            events.add(new int[]{i, 1});
        }
        Collections.shuffle(events, random);
        // a delivery that comes before its pickup swaps places with it
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i)[1] == 1) {
                for (int j = i + 1; j < events.size(); j++) {
                    if (events.get(j)[0] == events.get(i)[0]) {
                        int[] t = events.get(i); events.set(i, events.get(j)); events.set(j, t);
                        break;
                    }
                }
            }
        }
        return events;
    }
}
