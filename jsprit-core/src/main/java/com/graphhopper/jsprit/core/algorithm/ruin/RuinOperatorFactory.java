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
package com.graphhopper.jsprit.core.algorithm.ruin;

import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;

import java.util.Random;

/**
 * Factory interface for creating ruin strategies.
 *
 * <p>This allows registering ruin operators with the algorithm builder
 * without needing to pre-create all dependencies. The factory will be called
 * during algorithm construction when all dependencies are available.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * Jsprit.Builder.newInstance(vrp)
 *     .addRuinOperator(0.3, Ruin.random(0.3))
 *     .addRuinOperator(0.3, Ruin.radial(0.3))
 *     .addRuinOperator(0.2, Ruin.cluster())
 *     .addRuinOperator(0.2, Ruin.kruskalCluster())
 *     .buildAlgorithm();
 * </pre>
 *
 * @see Ruin
 */
public interface RuinOperatorFactory {

    /**
     * Creates a ruin strategy with the given dependencies.
     *
     * @param context the context containing all required dependencies
     * @return the configured ruin strategy
     */
    RuinStrategy create(Context context);

    /**
     * Returns the default name for this operator factory.
     *
     * <p>This name is used in strategy IDs when no explicit name is provided
     * to {@code addRuinOperator()}.</p>
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
    static RuinOperatorFactory named(String name, RuinOperatorFactory factory) {
        return new RuinOperatorFactory() {
            @Override
            public RuinStrategy create(Context context) {
                return factory.create(context);
            }

            @Override
            public String getName() {
                return name;
            }
        };
    }

    /**
     * Context object containing all dependencies needed to create a ruin strategy.
     */
    record Context(
        VehicleRoutingProblem vrp,
        StateManager stateManager,
        JobNeighborhoods jobNeighborhoods,
        double maxTransportCosts,
        Random random
    ) {}
}
