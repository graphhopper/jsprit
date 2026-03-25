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

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleFleetManager;

import java.util.Random;
import java.util.concurrent.ExecutorService;

/**
 * Factory interface for creating insertion strategies.
 *
 * <p>This allows registering insertion operators with the algorithm builder
 * without needing to pre-create all dependencies. The factory will be called
 * during algorithm construction when all dependencies are available.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Jsprit.Builder.newInstance(vrp)
 *     .addInsertionOperator(0.7, Insertion.regretFast())
 *     .addInsertionOperator(0.3, Insertion.regret())
 *     .buildAlgorithm();
 * </pre>
 *
 * @see Insertion
 */
public interface InsertionOperatorFactory {

    /**
     * Creates an insertion strategy with the given dependencies.
     *
     * @param context the context containing all required dependencies
     * @return the configured insertion strategy
     */
    InsertionStrategy create(Context context);

    /**
     * Returns the default name for this operator factory.
     *
     * <p>This name is used in strategy IDs when no explicit name is provided
     * to {@code addInsertionOperator()}.</p>
     *
     * @return the default name, or null if not specified
     */
    default String getName() {
        return null;
    }

    /**
     * Creates a named factory wrapper.
     *
     * @param name the operator name
     * @param factory the factory to wrap
     * @return a factory that returns the given name from {@link #getName()}
     */
    static InsertionOperatorFactory named(String name, InsertionOperatorFactory factory) {
        return new InsertionOperatorFactory() {
            @Override
            public InsertionStrategy create(Context context) {
                return factory.create(context);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Context object containing all dependencies needed to create an insertion strategy.
     */
    record Context(
        VehicleRoutingProblem vrp,
        VehicleFleetManager fleetManager,
        StateManager stateManager,
        ConstraintManager constraintManager,
        ActivityInsertionCostsCalculator activityInsertionCalculator,
        ScoringFunction scoringFunction,
        Random random,
        ExecutorService executorService,
        int numThreads
    ) {
        public boolean isConcurrent() {
            return executorService != null && numThreads > 1;
        }
    }
}
