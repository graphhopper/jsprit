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
package com.graphhopper.jsprit.core.algorithm.listener;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.analyzers.ConvergenceTracker;
import com.graphhopper.jsprit.core.algorithm.listener.analyzers.InsertionStatistics;
import com.graphhopper.jsprit.core.algorithm.listener.analyzers.RuinStatistics;
import com.graphhopper.jsprit.core.algorithm.listener.events.AcceptanceDecision;
import com.graphhopper.jsprit.core.algorithm.listener.events.IterationCompleted;
import com.graphhopper.jsprit.core.algorithm.listener.events.IterationStarted;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

/**
 * Tests for the unified event system.
 */
public class EventSystemTest {

    private VehicleRoutingProblem vrp;

    @Before
    public void setup() {
        // Create a simple VRP problem
        VehicleTypeImpl vehicleType = VehicleTypeImpl.Builder.newInstance("type")
                .addCapacityDimension(0, 10)
                .build();

        VehicleImpl vehicle1 = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(0, 0)).build())
                .setType(vehicleType)
                .build();

        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2")
                .setStartLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 10)).build())
                .setType(vehicleType)
                .build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance()
                .addVehicle(vehicle1)
                .addVehicle(vehicle2);

        // Add services in a pattern
        for (int i = 1; i <= 8; i++) {
            Service service = Service.Builder.newInstance("s" + i)
                    .setLocation(Location.Builder.newInstance()
                            .setCoordinate(Coordinate.newInstance(i * 2, i % 2 == 0 ? 5 : -5))
                            .build())
                    .addSizeDimension(0, 1)
                    .build();
            vrpBuilder.addJob(service);
        }

        vrp = vrpBuilder.build();
    }

    @Test
    public void testIterationEventsAreEmitted() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(10);

        AtomicInteger startedCount = new AtomicInteger(0);
        AtomicInteger completedCount = new AtomicInteger(0);

        algorithm.addEventListener(event -> {
            switch (event) {
                case IterationStarted e -> startedCount.incrementAndGet();
                case IterationCompleted e -> completedCount.incrementAndGet();
                default -> {
                }
            }
        });

        algorithm.searchSolutions();

        assertEquals("Should have 10 iteration started events", 10, startedCount.get());
        assertEquals("Should have 10 iteration completed events", 10, completedCount.get());
    }

    @Test
    public void testAcceptanceEventsAreEmitted() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(10);

        AtomicInteger acceptanceCount = new AtomicInteger(0);
        AtomicInteger newBestCount = new AtomicInteger(0);

        algorithm.addEventListener(event -> {
            if (event instanceof AcceptanceDecision e) {
                acceptanceCount.incrementAndGet();
                if (e.isNewBest()) {
                    newBestCount.incrementAndGet();
                }
            }
        });

        algorithm.searchSolutions();

        assertEquals("Should have 10 acceptance events", 10, acceptanceCount.get());
        // Note: May have no improvements if initial solution is already optimal
        System.out.printf("New best count: %d%n", newBestCount.get());
    }

    @Test
    public void testConvergenceTracker() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(50);

        ConvergenceTracker tracker = new ConvergenceTracker();
        algorithm.addEventListener(tracker);

        algorithm.searchSolutions();

        assertEquals("Should track all iterations", 50, tracker.getTotalIterations());
        assertEquals("Should have 50 best cost entries", 50, tracker.getBestCosts().size());
        // Note: May have no improvements if initial solution is already optimal
        assertTrue("Final cost should be positive", tracker.getFinalCost() > 0);

        System.out.println("\n=== Convergence Tracker Results ===");
        tracker.printSummary();
    }

    @Test
    public void testRuinAndRecreateEventsViaAdapter() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(20);

        // Add adapter to bridge existing listeners to event system
        AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
        algorithm.addListener(adapter);

        RuinStatistics ruinStats = new RuinStatistics();
        InsertionStatistics insertionStats = new InsertionStatistics();

        algorithm.addEventListener(ruinStats);
        algorithm.addEventListener(insertionStats);

        algorithm.searchSolutions();

        System.out.println("\n=== Ruin Statistics ===");
        ruinStats.printSummary();

        System.out.println("\n=== Insertion Statistics ===");
        insertionStats.printSummary();

        // Verify some events were captured
        assertTrue("Should have some ruin phases", ruinStats.getTotalRuinPhases() > 0);
        assertTrue("Should have some removals", ruinStats.getTotalRemovals() > 0);
    }

    @Test
    public void testCustomEventListener() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(15);

        StringBuilder log = new StringBuilder();

        algorithm.addEventListener(event -> {
            switch (event) {
                case IterationStarted e -> {
                    if (e.iteration() == 1) {
                        log.append("Starting with cost: ").append(String.format("%.2f", e.currentBestCost())).append("\n");
                    }
                }
                case AcceptanceDecision e -> {
                    if (e.isNewBest()) {
                        log.append("Iteration ").append(e.iteration())
                                .append(": New best! ").append(String.format("%.2f", e.newCost())).append("\n");
                    }
                }
                case IterationCompleted e -> {
                    if (e.iteration() == 15) {
                        log.append("Final cost: ").append(String.format("%.2f", e.bestSolutionCost())).append("\n");
                    }
                }
                default -> {
                }
            }
        });

        algorithm.searchSolutions();

        System.out.println("\n=== Custom Event Log ===");
        System.out.println(log);

        assertTrue("Log should contain starting info", log.toString().contains("Starting with cost"));
        assertTrue("Log should contain final cost", log.toString().contains("Final cost"));
    }

    @Test
    public void testNoOverheadWithoutListeners() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(100);

        // No listeners added - hasEventListeners() should return false
        assertFalse("Should have no event listeners", algorithm.hasEventListeners());

        // Run algorithm - should complete without errors
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        assertNotNull("Should have a solution", best);
        assertTrue("Solution cost should be positive", best.getCost() > 0);
    }

    @Test
    public void testFilteredEventListener() {
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(10);

        // Use the helper method to filter for specific event type
        AtomicInteger acceptanceCount = new AtomicInteger(0);

        algorithm.addEventListener(
                AlgorithmEventListener.forType(AcceptanceDecision.class, e -> {
                    acceptanceCount.incrementAndGet();
                    if (e.isNewBest()) {
                        System.out.printf("New best at iteration %d: %.2f%n", e.iteration(), e.newCost());
                    }
                })
        );

        algorithm.searchSolutions();

        assertEquals("Should receive all acceptance events", 10, acceptanceCount.get());
    }

    @Test
    public void demonstrateFullEventSystem() {
        System.out.println("\n========================================");
        System.out.println("=== Full Event System Demonstration ===");
        System.out.println("========================================\n");

        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(vrp);
        algorithm.setMaxIterations(30);

        // Add adapter for ruin/recreate events
        AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
        algorithm.addListener(adapter);

        // Add all analyzers
        ConvergenceTracker convergence = new ConvergenceTracker();
        RuinStatistics ruin = new RuinStatistics();
        InsertionStatistics insertions = new InsertionStatistics();

        algorithm.addEventListener(convergence);
        algorithm.addEventListener(ruin);
        algorithm.addEventListener(insertions);

        // Run
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution best = Solutions.bestOf(solutions);

        // Print results
        convergence.printSummary();
        System.out.println();
        ruin.printSummary();
        System.out.println();
        insertions.printSummary();

        System.out.println("\n=== Final Solution ===");
        System.out.printf("Best solution cost: %.2f%n", best.getCost());
        System.out.printf("Number of routes: %d%n", best.getRoutes().size());
        System.out.printf("Unassigned jobs: %d%n", best.getUnassignedJobs().size());
    }
}
