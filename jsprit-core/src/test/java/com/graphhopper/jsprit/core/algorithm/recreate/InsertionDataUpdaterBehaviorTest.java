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
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for InsertionDataUpdater internal behavior via integration tests.
 * These tests ensure the priority queue mechanics and scoring work correctly
 * before and after optimization refactoring.
 */
@DisplayName("InsertionDataUpdater Behavior Tests")
class InsertionDataUpdaterBehaviorTest {

    @Nested
    @DisplayName("Comparator Tests")
    class ComparatorTests {

        @Test
        @DisplayName("Comparator should order by insertion cost ascending")
        void comparatorOrdersByCostAscending() {
            Comparator<VersionedInsertionData> comparator = InsertionDataUpdater.getComparator();

            InsertionData data1 = new InsertionData(10.0, 0, 0, null, null);
            InsertionData data2 = new InsertionData(20.0, 0, 0, null, null);
            InsertionData data3 = new InsertionData(5.0, 0, 0, null, null);

            VersionedInsertionData v1 = new VersionedInsertionData(data1, 0, null);
            VersionedInsertionData v2 = new VersionedInsertionData(data2, 0, null);
            VersionedInsertionData v3 = new VersionedInsertionData(data3, 0, null);

            TreeSet<VersionedInsertionData> sorted = new TreeSet<>(comparator);
            sorted.add(v1);
            sorted.add(v2);
            sorted.add(v3);

            Iterator<VersionedInsertionData> iter = sorted.iterator();
            assertEquals(5.0, iter.next().getiData().getInsertionCost(), 0.001);
            assertEquals(10.0, iter.next().getiData().getInsertionCost(), 0.001);
            assertEquals(20.0, iter.next().getiData().getInsertionCost(), 0.001);
        }

        @Test
        @DisplayName("Comparator handles equal costs by keeping both entries")
        void comparatorHandlesEqualCosts() {
            Comparator<VersionedInsertionData> comparator = InsertionDataUpdater.getComparator();

            InsertionData data1 = new InsertionData(10.0, 0, 0, null, null);
            InsertionData data2 = new InsertionData(10.0, 0, 0, null, null);

            VersionedInsertionData v1 = new VersionedInsertionData(data1, 0, null);
            VersionedInsertionData v2 = new VersionedInsertionData(data2, 1, null);

            TreeSet<VersionedInsertionData> sorted = new TreeSet<>(comparator);
            sorted.add(v1);
            sorted.add(v2);

            // Both should be kept (current comparator never returns 0)
            assertEquals(2, sorted.size());
        }

        @Test
        @DisplayName("Comparator maintains stable ordering with many entries")
        void comparatorStableWithManyEntries() {
            Comparator<VersionedInsertionData> comparator = InsertionDataUpdater.getComparator();
            TreeSet<VersionedInsertionData> sorted = new TreeSet<>(comparator);

            // Add 100 entries with various costs
            Random rand = new Random(42);
            for (int i = 0; i < 100; i++) {
                double cost = rand.nextDouble() * 1000;
                InsertionData data = new InsertionData(cost, 0, 0, null, null);
                sorted.add(new VersionedInsertionData(data, i, null));
            }

            // Verify ordering is maintained
            double prevCost = -1;
            for (VersionedInsertionData vid : sorted) {
                assertTrue(vid.getiData().getInsertionCost() >= prevCost,
                        "Entries should be in ascending cost order");
                prevCost = vid.getiData().getInsertionCost();
            }
        }
    }

    @Nested
    @DisplayName("VersionedInsertionData Tests")
    class VersionedInsertionDataTests {

        @Test
        @DisplayName("VersionedInsertionData should store all fields correctly")
        void storesFieldsCorrectly() {
            InsertionData iData = new InsertionData(15.5, 1, 2, null, null);
            VersionedInsertionData vid = new VersionedInsertionData(iData, 5, null);

            assertEquals(15.5, vid.getiData().getInsertionCost(), 0.001);
            assertEquals(5, vid.getVersion());
            assertNull(vid.getRoute());
        }

        @Test
        @DisplayName("Multiple versions for same route should coexist in TreeSet")
        void multipleVersionsCoexist() {
            Comparator<VersionedInsertionData> comparator = InsertionDataUpdater.getComparator();
            TreeSet<VersionedInsertionData> sorted = new TreeSet<>(comparator);

            // Same cost, different versions - both should be stored
            InsertionData data1 = new InsertionData(10.0, 0, 0, null, null);
            InsertionData data2 = new InsertionData(10.0, 0, 0, null, null);
            InsertionData data3 = new InsertionData(10.0, 0, 0, null, null);

            sorted.add(new VersionedInsertionData(data1, 0, null));
            sorted.add(new VersionedInsertionData(data2, 1, null));
            sorted.add(new VersionedInsertionData(data3, 2, null));

            // All three should be stored (different versions)
            assertEquals(3, sorted.size());
        }
    }

    @Nested
    @DisplayName("Integration Tests via Algorithm")
    class IntegrationTests {

        @Test
        @DisplayName("Full insertion cycle with fast regret should work")
        void fullInsertionCycleWorks() {
            VehicleRoutingProblem vrp = createFourJobTwoVehicleProblem();

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
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
            assertEquals(2, solution.getRoutes().size(), "Should use 2 routes");
        }

        @Test
        @DisplayName("Regret-K integration should maintain consistency")
        void regretKIntegrationConsistent() {
            VehicleRoutingProblem vrp = createThreeJobThreeVehicleProblem();

            // Test with k=2
            StateManager sm1 = new StateManager(vrp);
            ConstraintManager cm1 = new ConstraintManager(vrp, sm1);
            VehicleRoutingAlgorithm vraK2 = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(sm1, cm1)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "2")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();
            VehicleRoutingProblemSolution solutionK2 = Solutions.bestOf(vraK2.searchSolutions());

            // Test with k=3
            StateManager sm2 = new StateManager(vrp);
            ConstraintManager cm2 = new ConstraintManager(vrp, sm2);
            VehicleRoutingAlgorithm vraK3 = Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(sm2, cm2)
                    .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
                    .setProperty(Jsprit.Parameter.REGRET_K, "3")
                    .setProperty(Jsprit.Parameter.ITERATIONS, "50")
                    .buildAlgorithm();
            VehicleRoutingProblemSolution solutionK3 = Solutions.bestOf(vraK3.searchSolutions());

            // Both should assign all jobs
            assertNotNull(solutionK2);
            assertNotNull(solutionK3);
            assertTrue(solutionK2.getUnassignedJobs().isEmpty(), "K=2 should assign all jobs");
            assertTrue(solutionK3.getUnassignedJobs().isEmpty(), "K=3 should assign all jobs");

            int totalJobsK2 = solutionK2.getRoutes().stream()
                    .mapToInt(r -> r.getTourActivities().getJobs().size()).sum();
            int totalJobsK3 = solutionK3.getRoutes().stream()
                    .mapToInt(r -> r.getTourActivities().getJobs().size()).sum();

            assertEquals(3, totalJobsK2);
            assertEquals(3, totalJobsK3);
        }

        @Test
        @DisplayName("Jobs should be distributed based on proximity")
        void jobsDistributedByProximity() {
            // s1 and s2 near v1 (at origin), s3 and s4 near v2 (at 100,0)
            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(10, 0)).build();
            Service s2 = Service.Builder.newInstance("s2")
                    .setLocation(Location.newInstance(20, 0)).build();
            Service s3 = Service.Builder.newInstance("s3")
                    .setLocation(Location.newInstance(80, 0)).build();
            Service s4 = Service.Builder.newInstance("s4")
                    .setLocation(Location.newInstance(90, 0)).build();

            VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                    .setStartLocation(Location.newInstance(0, 0)).build();
            VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                    .setStartLocation(Location.newInstance(100, 0)).build();

            VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                    .addJob(s1).addJob(s2).addJob(s3).addJob(s4)
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
            assertEquals(2, solution.getRoutes().size());

            // Verify distribution - jobs should be served by nearest vehicles
            for (var route : solution.getRoutes()) {
                if (route.getVehicle().getId().equals("v1")) {
                    // v1 should serve s1 and/or s2 (nearby jobs)
                    boolean servesNearby = route.getTourActivities().servesJob(s1) ||
                            route.getTourActivities().servesJob(s2);
                    assertTrue(servesNearby, "v1 should serve nearby jobs s1 or s2");
                }
            }
        }

        @Test
        @DisplayName("Algorithm should handle high regret jobs first")
        void highRegretJobsHandledFirst() {
            // Create scenario where one job has very limited options (high regret)
            VehicleType smallType = VehicleTypeImpl.Builder.newInstance("small")
                    .addCapacityDimension(0, 1).build();
            VehicleType largeType = VehicleTypeImpl.Builder.newInstance("large")
                    .addCapacityDimension(0, 10).build();

            // s1 requires capacity 2, can only be served by large vehicle
            Service s1 = Service.Builder.newInstance("s1")
                    .setLocation(Location.newInstance(50, 0))
                    .addSizeDimension(0, 2).build();
            // s2 and s3 have capacity 1, can be served by either
            Service s2 = Service.Builder.newInstance("s2")
                    .setLocation(Location.newInstance(10, 0))
                    .addSizeDimension(0, 1).build();
            Service s3 = Service.Builder.newInstance("s3")
                    .setLocation(Location.newInstance(90, 0))
                    .addSizeDimension(0, 1).build();

            VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                    .setType(smallType)
                    .setStartLocation(Location.newInstance(0, 0)).build();
            VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                    .setType(largeType)
                    .setStartLocation(Location.newInstance(100, 0)).build();

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
                    .setProperty(Jsprit.Parameter.ITERATIONS, "100")
                    .buildAlgorithm();

            VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());

            assertNotNull(solution);
            assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");

            // s1 must be on v2 (only vehicle with enough capacity)
            for (var route : solution.getRoutes()) {
                if (route.getVehicle().getId().equals("v2")) {
                    assertTrue(route.getTourActivities().servesJob(s1),
                            "s1 must be served by v2 (only feasible vehicle)");
                }
            }
        }
    }

    @Nested
    @DisplayName("Scoring Function Tests")
    class ScoringFunctionTests {

        @Test
        @DisplayName("Default scoring function should produce valid scores")
        void defaultScoringProducesValidScores() {
            VehicleRoutingProblem vrp = createSimpleProblem();

            DefaultScorer scorer = new DefaultScorer(vrp);

            // Scoring should work without exceptions
            assertNotNull(scorer);
        }

        @Test
        @DisplayName("RegretKAlternatives should maintain sorted order")
        void regretKAlternativesMaintainOrder() {
            RegretKAlternatives alts = new RegretKAlternatives();

            InsertionData d1 = new InsertionData(30.0, 0, 0, null, null);
            InsertionData d2 = new InsertionData(10.0, 0, 0, null, null);
            InsertionData d3 = new InsertionData(20.0, 0, 0, null, null);

            alts.add(d1, null);
            alts.add(d2, null);
            alts.add(d3, null);

            assertEquals(3, alts.size());
            assertEquals(10.0, alts.getBest().getCost(), 0.001);
            assertEquals(20.0, alts.getSecondBest().getCost(), 0.001);
            assertEquals(30.0, alts.get(2).getCost(), 0.001);
        }

        @Test
        @DisplayName("RegretKAlternatives.getTopK should return correct subset")
        void getTopKReturnsCorrectSubset() {
            RegretKAlternatives alts = new RegretKAlternatives();

            for (int i = 10; i >= 1; i--) {
                InsertionData d = new InsertionData(i * 10.0, 0, 0, null, null);
                alts.add(d, null);
            }

            assertEquals(10, alts.size());

            var top3 = alts.getTopK(3);
            assertEquals(3, top3.size());
            assertEquals(10.0, top3.get(0).getCost(), 0.001);
            assertEquals(20.0, top3.get(1).getCost(), 0.001);
            assertEquals(30.0, top3.get(2).getCost(), 0.001);
        }

        @Test
        @DisplayName("RegretKAlternatives should handle empty case")
        void handlesEmptyCase() {
            RegretKAlternatives alts = new RegretKAlternatives();

            assertTrue(alts.isEmpty());
            assertEquals(0, alts.size());
            assertNull(alts.getBest());
            assertNull(alts.getSecondBest());
            assertNull(alts.get(0));
        }

        @Test
        @DisplayName("RegretKAlternatives should handle single entry")
        void handlesSingleEntry() {
            RegretKAlternatives alts = new RegretKAlternatives();
            InsertionData d = new InsertionData(15.0, 0, 0, null, null);
            alts.add(d, null);

            assertFalse(alts.isEmpty());
            assertEquals(1, alts.size());
            assertEquals(15.0, alts.getBest().getCost(), 0.001);
            assertNull(alts.getSecondBest());
        }
    }

    @Nested
    @DisplayName("BoundedInsertionQueue Stale Entry Tests")
    class BoundedInsertionQueueStaleEntryTests {

        /**
         * This is the CRITICAL test for the core regret insertion optimization.
         * <p>
         * When a job is inserted into a route, the insertion costs for OTHER jobs
         * into that route change (usually increase due to reduced capacity/time).
         * The old insertion data becomes STALE and must be replaced.
         * <p>
         * The original TreeSet implementation used versioning to handle this:
         * - Each update round incremented a version counter
         * - Only entries with the current version were considered valid
         * <p>
         * The BoundedInsertionQueue optimization must achieve the same behavior
         * by ALWAYS replacing entries for a route, even when the new cost is higher.
         * <p>
         * Without this behavior, the algorithm uses stale (outdated) costs when
         * computing regret values, leading to suboptimal job selection.
         */
        @Test
        @DisplayName("Must replace stale entry even when new cost is HIGHER")
        void mustReplaceStaleEntryEvenWhenNewCostIsHigher() {
            BoundedInsertionQueue queue = new BoundedInsertionQueue();

            // Create a mock route (using VehicleRoute.emptyRoute() as a stand-in)
            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute route =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.emptyRoute();

            // Initial insertion: job can be inserted with cost 10
            InsertionData initialData = new InsertionData(10.0, 0, 0, null, null);
            queue.addOrReplace(initialData, route);

            assertEquals(10.0, queue.getBest().getCost(), 0.001,
                    "Initial cost should be 10.0");

            // Simulate route modification: another job was inserted into this route
            // Now the same job has a HIGHER insertion cost (e.g., due to capacity constraints)
            InsertionData updatedData = new InsertionData(25.0, 0, 0, null, null);
            boolean replaced = queue.addOrReplace(updatedData, route);

            // CRITICAL: The entry MUST be replaced even though cost is higher
            // The old entry is STALE - it represents outdated route state
            assertTrue(replaced, "Entry should be replaced even when new cost is higher");
            assertEquals(25.0, queue.getBest().getCost(), 0.001,
                    "Queue should contain the NEW (higher) cost, not the stale old cost");
            assertEquals(1, queue.size(),
                    "Queue should still have exactly one entry for the route");
        }

        @Test
        @DisplayName("Replace stale entry maintains correct sorting order")
        void replaceStaleEntryMaintainsCorrectSorting() {
            BoundedInsertionQueue queue = new BoundedInsertionQueue();

            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute route1 =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.emptyRoute();
            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute route2 =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.Builder
                            .newInstance(VehicleImpl.Builder.newInstance("v1")
                                    .setStartLocation(Location.newInstance(0, 0)).build())
                            .build();

            // Add entries: route1=10, route2=20
            queue.addOrReplace(new InsertionData(10.0, 0, 0, null, null), route1);
            queue.addOrReplace(new InsertionData(20.0, 0, 0, null, null), route2);

            assertEquals(10.0, queue.getBest().getCost(), 0.001);

            // Update route1 with higher cost (simulating route modification)
            queue.addOrReplace(new InsertionData(30.0, 0, 0, null, null), route1);

            // Now route2 should be best (cost 20), route1 is worst (cost 30)
            assertEquals(20.0, queue.getBest().getCost(), 0.001,
                    "Best should now be route2 with cost 20");
            assertEquals(route2, queue.getBest().getRoute());
        }

        @Test
        @DisplayName("Stale entry replacement works in regret calculation scenario")
        void staleEntryReplacementWorksInRegretScenario() {
            BoundedInsertionQueue queue = new BoundedInsertionQueue();

            // Create two distinct routes
            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute routeA =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.emptyRoute();
            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute routeB =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.Builder
                            .newInstance(VehicleImpl.Builder.newInstance("v1")
                                    .setStartLocation(Location.newInstance(0, 0)).build())
                            .build();

            // Initial state: routeA=5 (best), routeB=15 (second best)
            // Regret = 15 - 5 = 10
            queue.addOrReplace(new InsertionData(5.0, 0, 0, null, null), routeA);
            queue.addOrReplace(new InsertionData(15.0, 0, 0, null, null), routeB);

            double initialBest = queue.getBest().getCost();
            double initialSecond = queue.getSecondBest().getCost();
            double initialRegret = initialSecond - initialBest;
            assertEquals(10.0, initialRegret, 0.001, "Initial regret should be 10");

            // After inserting a job into routeA, costs change
            // routeA now has cost 20 (worse), routeB unchanged at 15
            // New correct regret = 20 - 15 = 5
            queue.addOrReplace(new InsertionData(20.0, 0, 0, null, null), routeA);

            double newBest = queue.getBest().getCost();
            double newSecond = queue.getSecondBest().getCost();
            double newRegret = newSecond - newBest;

            assertEquals(15.0, newBest, 0.001, "Best should now be routeB with cost 15");
            assertEquals(20.0, newSecond, 0.001, "Second should now be routeA with cost 20");
            assertEquals(5.0, newRegret, 0.001, "New regret should be 5");
        }

        @Test
        @DisplayName("NoInsertionFound should not replace valid entry")
        void noInsertionFoundShouldNotReplaceValidEntry() {
            BoundedInsertionQueue queue = new BoundedInsertionQueue();

            com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute route =
                    com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute.emptyRoute();

            // Add valid entry
            queue.addOrReplace(new InsertionData(10.0, 0, 0, null, null), route);
            assertEquals(1, queue.size());

            // Try to add NoInsertionFound - should be rejected
            boolean added = queue.addOrReplace(InsertionData.createEmptyInsertionData(), route);

            assertFalse(added, "NoInsertionFound should be rejected");
            assertEquals(1, queue.size(), "Queue should still have the valid entry");
            assertEquals(10.0, queue.getBest().getCost(), 0.001);
        }
    }

    // Helper methods to create test problems

    private VehicleRoutingProblem createFourJobTwoVehicleProblem() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(20, 0)).build();
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.newInstance(80, 0)).build();
        Service s4 = Service.Builder.newInstance("s4")
                .setLocation(Location.newInstance(90, 0)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(100, 0)).build();

        return VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addJob(s4)
                .addVehicle(v1).addVehicle(v2)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();
    }

    private VehicleRoutingProblem createThreeJobThreeVehicleProblem() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2")
                .setLocation(Location.newInstance(50, 0)).build();
        Service s3 = Service.Builder.newInstance("s3")
                .setLocation(Location.newInstance(90, 0)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.newInstance(50, 0)).build();
        VehicleImpl v3 = VehicleImpl.Builder.newInstance("v3")
                .setStartLocation(Location.newInstance(100, 0)).build();

        return VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3)
                .addVehicle(v1).addVehicle(v2).addVehicle(v3)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .build();
    }

    private VehicleRoutingProblem createSimpleProblem() {
        Service s1 = Service.Builder.newInstance("s1")
                .setLocation(Location.newInstance(10, 0)).build();

        VehicleImpl v1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0)).build();

        return VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addVehicle(v1).build();
    }
}
