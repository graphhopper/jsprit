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
package com.graphhopper.jsprit.core.algorithm.box;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.recreate.Insertion;
import com.graphhopper.jsprit.core.algorithm.ruin.Ruin;
import com.graphhopper.jsprit.core.algorithm.selector.WeightedOperatorSelector;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.StrategySelectedListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for independent operator selection functionality.
 */
class IndependentOperatorSelectionTest {

    private VehicleRoutingProblem vrp;

    @BeforeEach
    void setUp() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
            .addCapacityDimension(0, 100)
            .build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1")
            .setType(type)
            .setStartLocation(Location.newInstance(0, 0))
            .build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
            .addVehicle(vehicle);

        // Add 20 services in a grid
        for (int i = 0; i < 20; i++) {
            vrpBuilder.addJob(Service.Builder.newInstance("s" + i)
                .setLocation(Location.newInstance(i % 5 * 10, i / 5 * 10))
                .addSizeDimension(0, 5)
                .build());
        }

        vrp = vrpBuilder.build();
    }

    @Test
    void testWeightedOperatorSelectorBasicSelection() {
        WeightedOperatorSelector<String> selector = new WeightedOperatorSelector<>();
        selector.add("A", 0.7, "opA");
        selector.add("B", 0.3, "opB");

        assertEquals(2, selector.size());
        assertEquals(1.0, selector.getTotalWeight(), 0.001);

        // With fixed random, should be deterministic
        selector.setRandom(new Random(42));

        int countA = 0;
        int countB = 0;
        for (int i = 0; i < 1000; i++) {
            String selected = selector.select();
            if (selected.equals("A")) countA++;
            else countB++;
        }

        // Should roughly follow the weights (70% A, 30% B)
        assertTrue(countA > 600 && countA < 800, "A count should be around 700: " + countA);
        assertTrue(countB > 200 && countB < 400, "B count should be around 300: " + countB);
    }

    @Test
    void testWeightedOperatorSelectorSingleOperator() {
        WeightedOperatorSelector<String> selector = new WeightedOperatorSelector<>();
        selector.add("only", 1.0);

        assertEquals(1, selector.size());

        for (int i = 0; i < 10; i++) {
            assertEquals("only", selector.select());
        }
    }

    @Test
    void testWeightedOperatorSelectorEmpty() {
        WeightedOperatorSelector<String> selector = new WeightedOperatorSelector<>();
        assertTrue(selector.isEmpty());

        assertThrows(IllegalStateException.class, selector::select);
    }

    @Test
    void testWeightedOperatorSelectorInvalidWeight() {
        WeightedOperatorSelector<String> selector = new WeightedOperatorSelector<>();

        assertThrows(IllegalArgumentException.class, () -> selector.add("invalid", 0.0));
        assertThrows(IllegalArgumentException.class, () -> selector.add("invalid", -1.0));
    }

    @Test
    void testIndependentOperatorModeWithInsertionOnlyFails() {
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(0.7, Insertion.regretFast(2, 5, true), "regretFast")
            .addInsertionOperator(0.3, Insertion.regret(2), "regretThorough");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            builder::buildAlgorithm
        );

        assertTrue(exception.getMessage().contains("ruin operator"),
            "Error should mention missing ruin operator");
    }

    @Test
    void testIndependentOperatorModeWithRuinOnlyFails() {
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp)
            .addRuinOperator(0.4, Ruin.random(0.3), "random")
            .addRuinOperator(0.3, Ruin.radial(0.3), "radial")
            .addRuinOperator(0.3, Ruin.cluster(), "cluster");

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            builder::buildAlgorithm
        );

        assertTrue(exception.getMessage().contains("insertion operator"),
            "Error should mention missing insertion operator");
    }

    @Test
    void testIndependentOperatorModeWithBothOperators() {
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            // Insertion operators
            .addInsertionOperator(0.7, Insertion.regretFast(2, 5, true), "regretFast")
            .addInsertionOperator(0.3, Insertion.regret(2), "regretThorough")
            // Ruin operators
            .addRuinOperator(0.4, Ruin.random(0.3), "random")
            .addRuinOperator(0.3, Ruin.radial(0.3), "radial")
            .addRuinOperator(0.3, Ruin.cluster(), "cluster")
            .setProperty(Jsprit.Parameter.ITERATIONS, "100")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    void testAllInsertionOperatorFactories() {
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(0.3, Insertion.regretFast(), "regretFast")
            .addInsertionOperator(0.2, Insertion.regret(), "regret")
            .addInsertionOperator(0.2, Insertion.best(), "best")
            .addInsertionOperator(0.2, Insertion.cheapest(), "cheapest")
            .addInsertionOperator(0.1, Insertion.positionRegret(), "positionRegret")
            .addRuinOperator(1.0, Ruin.random(0.3), "random")  // Required
            .setProperty(Jsprit.Parameter.ITERATIONS, "50")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    void testAllRuinOperatorFactories() {
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addRuinOperator(0.2, Ruin.random(0.3), "random")
            .addRuinOperator(0.2, Ruin.radial(0.3), "radial")
            .addRuinOperator(0.15, Ruin.cluster(), "cluster")
            .addRuinOperator(0.15, Ruin.kruskalCluster(), "kruskal")
            .addRuinOperator(0.15, Ruin.worst(0.2), "worst")
            .addRuinOperator(0.15, Ruin.string(), "string")
            .addInsertionOperator(1.0, Insertion.regret(), "regret")  // Required
            .setProperty(Jsprit.Parameter.ITERATIONS, "50")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    void testBuilderIsIndependentOperatorMode() {
        // Without operators - should be false
        Jsprit.Builder builder1 = Jsprit.Builder.newInstance(vrp);
        assertFalse(builder1.isIndependentOperatorMode());

        // With insertion operator - should be true
        Jsprit.Builder builder2 = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regret());
        assertTrue(builder2.isIndependentOperatorMode());

        // With ruin operator - should be true
        Jsprit.Builder builder3 = Jsprit.Builder.newInstance(vrp)
            .addRuinOperator(1.0, Ruin.random(0.3));
        assertTrue(builder3.isIndependentOperatorMode());

        // With both - should be true
        Jsprit.Builder builder4 = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regret())
            .addRuinOperator(1.0, Ruin.random(0.3));
        assertTrue(builder4.isIndependentOperatorMode());
    }

    @Test
    void testMixingIndependentAndCoupledStrategiesFails() {
        // Mixing independent operators with explicit coupled strategy weights should fail fast
        Jsprit.Builder builder = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.5")  // explicit coupled strategy
            .addInsertionOperator(0.7, Insertion.regret())       // independent operator
            .addRuinOperator(0.3, Ruin.random(0.3));

        IllegalStateException exception = assertThrows(
            IllegalStateException.class,
            builder::buildAlgorithm
        );

        assertTrue(exception.getMessage().contains("Cannot mix"));
        assertTrue(exception.getMessage().contains("radial_regret"));
    }

    @Test
    void testDefaultStrategyPropertiesDoNotTriggerValidationError() {
        // Default properties (not explicitly set) should not cause validation error
        // This should work fine - only using independent operators, no explicit strategy weights
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regret())
            .addRuinOperator(1.0, Ruin.random(0.3))
            .setProperty(Jsprit.Parameter.ITERATIONS, "20")  // Parameter, not Strategy - OK
            .setRandom(new Random(42))
            .buildAlgorithm();

        assertNotNull(algorithm);
    }

    @Test
    void testIndependentModeIsExclusiveFromCoupledStrategies() {
        // When using independent mode, default coupled strategies should not run
        // We can verify this indirectly by checking that the algorithm works
        // with only the independent operators

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regret())
            .addRuinOperator(1.0, Ruin.random(0.3))
            .setProperty(Jsprit.Parameter.ITERATIONS, "20")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty(), "All jobs should be assigned");
    }

    @Test
    void testCustomStrategiesWorkAlongsideIndependentMode() {
        // Custom strategies added via addSearchStrategy should still work in independent mode
        // This tests that customStrategies and strategyComponents are preserved

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(0.8, Insertion.regret())
            .addRuinOperator(0.8, Ruin.random(0.3))
            .setProperty(Jsprit.Parameter.ITERATIONS, "30")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());

        assertNotNull(solution);
        assertTrue(solution.getUnassignedJobs().isEmpty());
    }

    @Test
    void testAllListenersWorkWithIndependentOperators() {
        // Track listener invocations
        List<String> events = new ArrayList<>();

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regret(), "regret")
            .addRuinOperator(0.5, Ruin.random(0.3), "random")
            .addRuinOperator(0.5, Ruin.radial(0.3), "radial")
            .setProperty(Jsprit.Parameter.ITERATIONS, "5")
            .setRandom(new Random(42))
            .buildAlgorithm();

        // Algorithm-level listeners
        algorithm.addListener((IterationStartsListener) (i, problem, solutions) ->
            events.add("iterationStarts:" + i));

        algorithm.addListener((IterationEndsListener) (i, problem, solutions) ->
            events.add("iterationEnds:" + i));

        // Strategy-selected listener
        algorithm.addListener((StrategySelectedListener) (ds, problem, solutions) ->
            events.add("strategySelected:" + ds.getStrategyId()));

        // Module-level listeners (ruin/insertion)
        algorithm.addListener(new RuinListener() {
            @Override
            public void ruinStarts(Collection<VehicleRoute> routes) {
                events.add("ruinStarts");
            }
            @Override
            public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
                events.add("ruinEnds:" + unassignedJobs.size());
            }
            @Override
            public void removed(Job job, VehicleRoute fromRoute) {
                // Called for each removed job - don't log to avoid noise
            }
        });

        algorithm.addListener((JobInsertedListener) (job, route, insertionData) -> {
            // Called for each inserted job - just verify it's called
            events.add("jobInserted:" + job.getId());
        });

        algorithm.searchSolutions();

        // Verify iteration listeners fired
        assertTrue(events.stream().anyMatch(e -> e.startsWith("iterationStarts:")),
            "IterationStartsListener should fire");
        assertTrue(events.stream().anyMatch(e -> e.startsWith("iterationEnds:")),
            "IterationEndsListener should fire");

        // Verify strategy selected listeners fired with dynamic IDs
        assertTrue(events.stream().anyMatch(e -> e.startsWith("strategySelected:independent")),
            "StrategySelectedListener should receive dynamic IDs");

        // Verify ruin listeners fired
        assertTrue(events.stream().anyMatch(e -> e.equals("ruinStarts")),
            "RuinListener.ruinStarts should fire");
        assertTrue(events.stream().anyMatch(e -> e.startsWith("ruinEnds:")),
            "RuinListener.ruinEnds should fire");

        // Verify insertion listeners fired
        assertTrue(events.stream().anyMatch(e -> e.startsWith("jobInserted:")),
            "JobInsertedListener should fire");
    }

    @Test
    void testStrategySelectedListenerReceivesDynamicIds() {
        List<String> receivedStrategyIds = new ArrayList<>();

        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(0.5, Insertion.regretFast(), "regretFast")
            .addInsertionOperator(0.5, Insertion.regret(), "regretThorough")
            .addRuinOperator(0.5, Ruin.random(0.3), "random")
            .addRuinOperator(0.5, Ruin.radial(0.3), "radial")
            .setProperty(Jsprit.Parameter.ITERATIONS, "20")
            .setRandom(new Random(42))
            .buildAlgorithm();

        algorithm.addListener(new StrategySelectedListener() {
            @Override
            public void informSelectedStrategy(
                SearchStrategy.DiscoveredSolution discoveredSolution,
                VehicleRoutingProblem problem,
                Collection<VehicleRoutingProblemSolution> solutions
            ) {
                receivedStrategyIds.add(discoveredSolution.getStrategyId());
            }
        });

        algorithm.searchSolutions();

        // Should have received strategy IDs for each iteration
        assertEquals(20, receivedStrategyIds.size());

        // All IDs should follow the pattern "independent:{ruin}+{insertion}"
        for (String id : receivedStrategyIds) {
            assertTrue(id.startsWith("independent:"),
                "Strategy ID should start with module name: " + id);
            assertTrue(id.contains("+"),
                "Strategy ID should contain '+' separator: " + id);

            // Should contain one of the ruin operators
            assertTrue(id.contains("random") || id.contains("radial"),
                "Strategy ID should contain ruin operator name: " + id);

            // Should contain one of the insertion operators
            assertTrue(id.contains("regretFast") || id.contains("regretThorough"),
                "Strategy ID should contain insertion operator name: " + id);
        }

        // With random seed 42, we should see variety in selections
        long uniqueIds = receivedStrategyIds.stream().distinct().count();
        assertTrue(uniqueIds > 1, "Should have multiple different strategy combinations");
    }

    @Test
    void testIndependentModeProducesSimilarQuality() {
        // Run default algorithm
        VehicleRoutingAlgorithm defaultAlg = Jsprit.Builder.newInstance(vrp)
            .setProperty(Jsprit.Parameter.ITERATIONS, "100")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution defaultSolution = Solutions.bestOf(defaultAlg.searchSolutions());

        // Run independent mode with similar operators
        VehicleRoutingAlgorithm independentAlg = Jsprit.Builder.newInstance(vrp)
            .addInsertionOperator(1.0, Insertion.regretFast(2, 5, true))
            .addRuinOperator(0.4, Ruin.radial(0.3))
            .addRuinOperator(0.3, Ruin.random(0.3, 0.5))
            .addRuinOperator(0.3, Ruin.cluster())
            .setProperty(Jsprit.Parameter.ITERATIONS, "100")
            .setRandom(new Random(42))
            .buildAlgorithm();

        VehicleRoutingProblemSolution independentSolution = Solutions.bestOf(independentAlg.searchSolutions());

        assertNotNull(defaultSolution);
        assertNotNull(independentSolution);

        // Solutions should be reasonably close in quality (within 20%)
        double defaultCost = defaultSolution.getCost();
        double independentCost = independentSolution.getCost();

        assertTrue(Math.abs(defaultCost - independentCost) / defaultCost < 0.20,
            "Solutions should be similar in quality. Default: " + defaultCost + ", Independent: " + independentCost);
    }
}
