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

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.events.*;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.*;
import com.graphhopper.jsprit.core.algorithm.ruin.listener.RuinListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.*;

/**
 * Adapter that bridges the existing fragmented listener interfaces to the unified event system.
 *
 * <p>This adapter implements all the existing listener interfaces (RuinListener, JobInsertedListener, etc.)
 * and translates their callbacks into unified AlgorithmEvent instances that are emitted via the
 * VehicleRoutingAlgorithm's event system.</p>
 *
 * <p>Usage:</p>
 * <pre>{@code
 * VehicleRoutingAlgorithm algorithm = ...;
 * AlgorithmEventAdapter adapter = new AlgorithmEventAdapter(algorithm);
 * algorithm.addListener(adapter);
 *
 * // Now add event listeners for the unified events
 * algorithm.addEventListener(event -> {
 *     switch (event) {
 *         case JobRemoved e -> System.out.println("Job removed: " + e.job().getId());
 *         case JobInserted e -> System.out.println("Job inserted: " + e.job().getId());
 *         default -> {}
 *     }
 * });
 * }</pre>
 */
public class AlgorithmEventAdapter implements
        VehicleRoutingAlgorithmListener,
        IterationStartsListener,
        StrategySelectedListener,
        RuinListener,
        InsertionStartsListener,
        JobInsertedListener,
        JobUnassignedListener,
        InsertionEndsListener {

    private final VehicleRoutingAlgorithm algorithm;
    private int currentIteration = 0;
    private String currentRuinStrategy = "unknown";
    private String currentInsertionStrategy = "unknown";
    private int insertionOrder = 0;
    private Map<String, List<String>> failedConstraintsByRoute = new HashMap<>();

    public AlgorithmEventAdapter(VehicleRoutingAlgorithm algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * Sets the name of the current ruin strategy (for event metadata).
     */
    public void setRuinStrategyName(String name) {
        this.currentRuinStrategy = name;
    }

    /**
     * Sets the name of the current insertion strategy (for event metadata).
     */
    public void setInsertionStrategyName(String name) {
        this.currentInsertionStrategy = name;
    }

    // --- IterationStartsListener ---

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        this.currentIteration = i;
        this.insertionOrder = 0;
        this.failedConstraintsByRoute.clear();
    }

    // --- StrategySelectedListener ---

    @Override
    public void informSelectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution,
                                       VehicleRoutingProblem problem,
                                       Collection<VehicleRoutingProblemSolution> solutions) {
        // Capture the strategy name for use in ruin/recreate events
        String strategyId = discoveredSolution.getStrategyId();
        this.currentRuinStrategy = strategyId;
        this.currentInsertionStrategy = strategyId;
    }

    // --- RuinListener ---

    @Override
    public void ruinStarts(Collection<VehicleRoute> routes) {
        if (!algorithm.hasEventListeners()) return;
        algorithm.emit(new RuinStarted(
                currentIteration,
                System.currentTimeMillis(),
                currentRuinStrategy,
                routes.size()
        ));
    }

    @Override
    public void removed(Job job, VehicleRoute fromRoute) {
        if (!algorithm.hasEventListeners()) return;
        String routeId = fromRoute.getVehicle() != null ? fromRoute.getVehicle().getId() : "unknown";
        algorithm.emit(new JobRemoved(
                currentIteration,
                System.currentTimeMillis(),
                job,
                routeId
        ));
    }

    @Override
    public void ruinEnds(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
        if (!algorithm.hasEventListeners()) return;
        algorithm.emit(new RuinCompleted(
                currentIteration,
                System.currentTimeMillis(),
                new ArrayList<>(unassignedJobs),
                countAffectedRoutes(routes)
        ));
    }

    // --- InsertionStartsListener ---

    @Override
    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        this.insertionOrder = 0;
        this.failedConstraintsByRoute.clear();
        if (!algorithm.hasEventListeners()) return;
        algorithm.emit(new RecreateStarted(
                currentIteration,
                System.currentTimeMillis(),
                currentInsertionStrategy,
                unassignedJobs.size(),
                vehicleRoutes.size()
        ));
    }

    // --- JobInsertedListener ---

    @Override
    public void informJobInserted(Job job, VehicleRoute inRoute, InsertionData insertionData) {
        insertionOrder++;
        if (!algorithm.hasEventListeners()) return;
        String routeId = inRoute.getVehicle() != null ? inRoute.getVehicle().getId() : "unknown";
        algorithm.emit(new JobInserted(
                currentIteration,
                System.currentTimeMillis(),
                job,
                routeId,
                insertionData.getDeliveryInsertionIndex() != InsertionData.NO_INDEX
                        ? insertionData.getDeliveryInsertionIndex()
                        : insertionData.getPickupInsertionIndex(),
                insertionData.getInsertionCost(),
                insertionData.getCostBreakdown(),
                insertionOrder
        ));
    }

    // --- JobUnassignedListener ---

    @Override
    public void informJobUnassigned(Job unassigned, Collection<String> failedConstraintNames) {
        if (!algorithm.hasEventListeners()) return;
        // Aggregate reasons - in the current implementation, reasons is a collection of constraint names
        Map<String, List<String>> failedByRoute = new HashMap<>();
        List<String> reasonsList = new ArrayList<>(failedConstraintNames);
        failedByRoute.put("all_routes", reasonsList);
        algorithm.emit(new JobUnassigned(
                currentIteration,
                System.currentTimeMillis(),
                unassigned,
                failedByRoute,
                failedConstraintNames.isEmpty() ? "No feasible insertion found" : String.join(", ", failedConstraintNames)
        ));
    }

    // --- InsertionEndsListener ---

    @Override
    public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes, Collection<Job> badJobs) {
        if (!algorithm.hasEventListeners()) return;
        double totalCost = 0; // Cost would need to be calculated separately
        algorithm.emit(new RecreateCompleted(
                currentIteration,
                System.currentTimeMillis(),
                insertionOrder,
                new ArrayList<>(badJobs),
                totalCost
        ));
    }

    // --- Helper methods ---

    private int countAffectedRoutes(Collection<VehicleRoute> routes) {
        // Approximate: count non-empty routes
        int count = 0;
        for (VehicleRoute route : routes) {
            if (!route.getTourActivities().getActivities().isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
