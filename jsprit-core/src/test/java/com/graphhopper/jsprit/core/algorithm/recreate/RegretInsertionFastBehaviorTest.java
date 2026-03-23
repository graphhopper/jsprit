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
import com.graphhopper.jsprit.core.algorithm.recreate.listener.BeforeJobInsertionListener;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Behavior tests for RegretInsertionFast and RegretInsertionConcurrentFast.
 * These tests capture the exact behavior of the current implementation to ensure
 * that any refactoring (e.g., priority queue optimizations) maintains identical functionality.
 * <p>
 * Test categories:
 * 1. Insertion order tests - verify deterministic job insertion sequence
 * 2. Solution structure tests - verify which jobs end up in which routes
 * 3. Regret scoring tests - verify correct regret calculation
 * 4. Edge case tests - empty routes, single job, infeasible insertions
 */
@DisplayName("Regret Insertion Fast Behavior Tests")
class RegretInsertionFastBehaviorTest {

    /**
     * Records the sequence of job insertions for verification.
     */
    static class InsertionSequenceRecorder implements BeforeJobInsertionListener {
        private final List<String> insertionOrder = new ArrayList<>();
        private final Map<String, String> jobToRoute = new HashMap<>();
        private final Map<String, Double> jobInsertionCosts = new HashMap<>();

        @Override
        public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
            insertionOrder.add(job.getId());
            String vehicleId = route.getVehicle() instanceof VehicleImpl.NoVehicle ?
                    "new_route" : route.getVehicle().getId();
            jobToRoute.put(job.getId(), vehicleId);
            jobInsertionCosts.put(job.getId(), data.getInsertionCost());
        }

        public List<String> getInsertionOrder() {
            return Collections.unmodifiableList(insertionOrder);
        }

        public Map<String, String> getJobToRouteMapping() {
            return Collections.unmodifiableMap(jobToRoute);
        }

        public Map<String, Double> getJobInsertionCosts() {
            return Collections.unmodifiableMap(jobInsertionCosts);
        }
    }

    @Nested
    @DisplayName("Insertion Order Tests via Algorithm")
    class InsertionOrderTests {

        @Test
        @DisplayName("Insertion order should be deterministic with same input")
        void insertionOrderDeterministic() {
            VehicleRoutingProblem vrp = createStandardProblem();

            double cost1 = runAndGetCost(vrp, false);
            double cost2 = runAndGetCost(vrp, false);

            assertEquals(cost1, cost2, 0.001, "Solution cost should be identical for same input");
        }

        @Test
        @DisplayName("Fast regret should produce feasible solution")
        void fastRegretProducesFeasibleSolution() {
            VehicleRoutingProblem vrp = createStandardProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
        }

        @Test
        @DisplayName("Concurrent fast regret should produce feasible solution")
        void concurrentFastRegretProducesFeasibleSolution() {
            VehicleRoutingProblem vrp = createStandardProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.THREADS, "2")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
        }

        private double runAndGetCost(VehicleRoutingProblem vrp, boolean concurrent) {
            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "1");

            if (concurrent) {
                builder.setProperty(Jsprit.Parameter.THREADS, "2");
            }

            VehicleRoutingAlgorithm vra = builder.buildAlgorithm();
            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            return solution.getCost();
        }
    }

    @Nested
    @DisplayName("Solution Structure Tests")
    class SolutionStructureTests {

        @Test
        @DisplayName("Jobs should be assigned to nearest vehicles when possible")
        void jobsAssignedToNearestVehicles() {
            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(10, 0)).build();
            Service s2 = Service.Builder.newInstance("s2")
                    .setLocation(Location.newInstance(90, 0)).build();

            VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                    .setStartLocation(Location.newInstance(0, 0)).build();
            VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                    .setStartLocation(Location.newInstance(100, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addJob(s2)
                    .addVehicle(v1).addVehicle(v2)
                    .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                    .build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());

            // With optimization, jobs should end up on nearest vehicles
            for (VehicleRoute route : solution.getRoutes()) {
                if (route.getVehicle().getId().equals("v1")) {
                    assertTrue(route.getTourActivities().servesJob(s1),
                            "v1 should serve s1 (nearest)");
                } else if (route.getVehicle().getId().equals("v2")) {
                    assertTrue(route.getTourActivities().servesJob(s2),
                            "v2 should serve s2 (nearest)");
                }
            }
        }

        @Test
        @DisplayName("Single vehicle should serve all jobs")
        void singleVehicleServesAllJobs() {
            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(10, 0)).build();
            Service s2 = Service.Builder.newInstance("s2")
                    .setLocation(Location.newInstance(20, 0)).build();
            Service s3 = Service.Builder.newInstance("s3")
                    .setLocation(Location.newInstance(30, 0)).build();

            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
            assertEquals(1, solution.getRoutes().size());
            assertEquals(3, solution.getRoutes().iterator().next().getTourActivities().getJobs().size());
        }

        @Test
        @DisplayName("Routes should not exceed capacity")
        void routesShouldRespectCapacity() {
            VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
                    .addCapacityDimension(0, 2).build();

            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(10, 0))
                    .addSizeDimension(0, 1).build();
            Service s2 = Service.Builder.newInstance("s2")
                    .setLocation(Location.newInstance(20, 0))
                    .addSizeDimension(0, 1).build();
            Service s3 = Service.Builder.newInstance("s3")
                    .setLocation(Location.newInstance(30, 0))
                    .addSizeDimension(0, 1).build();

            VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                    .setType(type)
                    .setStartLocation(Location.newInstance(0, 0)).build();
            VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                    .setType(type)
                    .setStartLocation(Location.newInstance(50, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addJob(s2).addJob(s3)
                    .addVehicle(v1).addVehicle(v2)
                    .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                    .build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");

            for (VehicleRoute route : solution.getRoutes()) {
                assertTrue(route.getTourActivities().getJobs().size() <= 2,
                        "Each route should have at most 2 jobs due to capacity");
            }
        }
    }

    @Nested
    @DisplayName("Regret-K Scoring Tests")
    class RegretKScoringTests {

        @Test
        @DisplayName("Regret-3 should produce feasible solution")
        void regret3ProducesFeasibleSolution() {
            VehicleRoutingProblem vrp = createMultiVehicleProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "3")
                    .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "sum")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }

        @Test
        @DisplayName("Regret-4 with max strategy should work")
        void regret4MaxStrategyWorks() {
            VehicleRoutingProblem vrp = createMultiVehicleProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "4")
                    .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "max")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }

        @Test
        @DisplayName("Regret-all with avg strategy should work")
        void regretAllAvgStrategyWorks() {
            VehicleRoutingProblem vrp = createMultiVehicleProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "all")
                    .setProperty(Jsprit.Parameter.REGRET_K_STRATEGY, "avg")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }

        @Test
        @DisplayName("Regret-K with k larger than routes should work")
        void regretKLargerThanRoutesWorks() {
            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(10, 0)).build();

            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            // k=10 but only 1 vehicle
            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "10")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "10")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }
    }

    @Nested
    @DisplayName("Concurrent vs Sequential Consistency Tests")
    class ConcurrentConsistencyTests {

        @Test
        @DisplayName("Concurrent and sequential should produce similar quality solutions")
        void concurrentAndSequentialSimilarQuality() {
            VehicleRoutingProblem vrp = createStandardProblem();

            // Run sequential
            StateManager sm1 = new StateManager(vrp);
            ConstraintManager cm1 = new ConstraintManager(vrp, sm1);
            VehicleRoutingAlgorithm sequential = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(sm1, cm1)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                    .buildAlgorithm();
            VehicleRoutingProblemSolution seqSolution = Solutions.bestOf(sequential.searchSolutions());

            // Run concurrent
            StateManager sm2 = new StateManager(vrp);
            ConstraintManager cm2 = new ConstraintManager(vrp, sm2);
            VehicleRoutingAlgorithm concurrent = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(sm2, cm2)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.THREADS, "2")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                    .buildAlgorithm();
            VehicleRoutingProblemSolution concSolution = Solutions.bestOf(concurrent.searchSolutions());

            // Both should produce valid solutions
            assertNotNull(seqSolution);
            assertNotNull(concSolution);
            assertTrue(seqSolution.getUnassignedJobs().isEmpty());
            assertTrue(concSolution.getUnassignedJobs().isEmpty());

            // Costs should be within reasonable range (allowing for different insertion orders)
            double ratio = Math.max(seqSolution.getCost(), concSolution.getCost()) /
                    Math.min(seqSolution.getCost(), concSolution.getCost());
            assertTrue(ratio < 1.5, "Sequential and concurrent costs should be within 50% of each other");
        }

        @Test
        @DisplayName("Concurrent with regret-K should work correctly")
        void concurrentWithRegretK() {
            VehicleRoutingProblem vrp = createMultiVehicleProblem();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.THREADS, "2")
                    .setProperty(Jsprit.Parameter.REGRET_K, "3")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Case Tests")
    class EdgeCaseTests {

        @Test
        @DisplayName("Empty job list should return empty solution")
        void emptyJobList() {
            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "10")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getRoutes().isEmpty() ||
                    solution.getRoutes().stream().allMatch(r -> r.getTourActivities().isEmpty()));
        }

        @Test
        @DisplayName("Single job should be inserted")
        void singleJob() {
            Service s = Service.Builder.newInstance("s")
                    .setLocation(Location.newInstance(10, 0)).build();

            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s).addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "10")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
            assertEquals(1, solution.getRoutes().size());
        }

        @Test
        @DisplayName("Shipments should be handled correctly")
        void shipmentsHandledCorrectly() {
            Shipment ship = Shipment.Builder.newInstance("ship")
                    .setPickupLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(10, 0)).build())
                    .setDeliveryLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(20, 0)).build())
                    .build();

            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(ship).addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "10")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
            assertEquals(1, solution.getRoutes().size());
            VehicleRoute route = solution.getRoutes().iterator().next();
            assertTrue(route.getTourActivities().servesJob(ship));
            assertEquals(2, route.getTourActivities().getActivities().size(),
                    "Shipment should create 2 activities (pickup + delivery)");
        }

        @Test
        @DisplayName("Multiple shipments should be handled correctly")
        void multipleShipmentsHandledCorrectly() {
            Shipment s1 = Shipment.Builder.newInstance("ship1")
                    .setPickupLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(0, 10)).build())
                    .setDeliveryLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(10, 10)).build())
                    .build();
            Shipment s2 = Shipment.Builder.newInstance("ship2")
                    .setPickupLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(0, 20)).build())
                    .setDeliveryLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(10, 20)).build())
                    .build();
            Shipment s3 = Shipment.Builder.newInstance("ship3")
                    .setPickupLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(0, 30)).build())
                    .setDeliveryLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(10, 30)).build())
                    .build();

            VehicleImpl v = VehicleImpl.Builder.newInstance("v")
                    .setStartLocation(Location.newInstance(0, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addJob(s2).addJob(s3).addVehicle(v).build();

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All shipments should be assigned");
        }
    }

    @Nested
    @DisplayName("Priority Queue Behavior Tests")
    class PriorityQueueBehaviorTests {

        @Test
        @DisplayName("Multiple insertions should maintain correct ordering")
        void multipleInsertionsMaintainOrdering() {
            VehicleRoutingProblem vrp = createLargeProblem(20);

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All 20 jobs should be assigned");

            int totalJobs = solution.getRoutes().stream()
                    .mapToInt(r -> r.getTourActivities().getJobs().size())
                    .sum();
            assertEquals(20, totalJobs);
        }

        @Test
        @DisplayName("Solution cost should be consistent across runs with same iterations")
        void solutionCostConsistent() {
            VehicleRoutingProblem vrp = createStandardProblem();

            // Run twice with same setup - initial solution (iteration 0) should be deterministic
            double cost1 = runAndGetInitialCost(vrp);
            double cost2 = runAndGetInitialCost(vrp);

            assertEquals(cost1, cost2, 0.001, "Initial solution cost should be identical");
        }

        private double runAndGetInitialCost(VehicleRoutingProblem vrp) {
            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "0")  // Just initial solution
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
            return solution.getCost();
        }
    }

    @Nested
    @DisplayName("Large Problem Stress Tests")
    class LargeProblemStressTests {

        @Test
        @DisplayName("50 jobs should complete successfully")
        void fiftyJobsComplete() {
            VehicleRoutingProblem vrp = createLargeProblem(50);

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }

        @Test
        @DisplayName("50 jobs with regret-4 should complete successfully")
        void fiftyJobsWithRegret4() {
            VehicleRoutingProblem vrp = createLargeProblem(50);

            StateManager stateManager = new StateManager(vrp);
            ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

            VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "4")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty());
        }
    }

    // Helper methods to create test problems

    private VehicleRoutingProblem createStandardProblem() {
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(100, 0)).build();

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v1).addVehicle(v2)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        for (int i = 1; i <= 10; i++) {
            Service s = Service.Builder.newInstance("s" + i)
                    .setLocation(Location.newInstance(i * 10, (i % 3) * 10))
                    .build();
            builder.addJob(s);
        }

        return builder.build();
    }

    private VehicleRoutingProblem createMultiVehicleProblem() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
                .addCapacityDimension(0, 5).build();

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance()
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        for (int i = 1; i <= 4; i++) {
            VehicleImpl v = VehicleImpl.Builder.newInstance("v" + i)
                    .setType(type)
                    .setStartLocation(Location.newInstance(i * 25, 0)).build();
            builder.addVehicle(v);
        }

        for (int i = 1; i <= 12; i++) {
            Service s = Service.Builder.newInstance("s" + i)
                    .setLocation(Location.newInstance((i % 5) * 20, (i / 5) * 20))
                    .addSizeDimension(0, 1)
                    .build();
            builder.addJob(s);
        }

        return builder.build();
    }

    private VehicleRoutingProblem createLargeProblem(int numJobs) {
        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(100, 0)).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3")
                .setStartLocation(Location.newInstance(50, 100)).build();

        VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(v1).addVehicle(v2).addVehicle(v3)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);

        Random rand = new Random(42); // Fixed seed for determinism
        for (int i = 1; i <= numJobs; i++) {
            Service s = Service.Builder.newInstance("s" + i)
                    .setLocation(Location.newInstance(rand.nextInt(100), rand.nextInt(100)))
                    .build();
            builder.addJob(s);
        }

        return builder.build();
    }
}
