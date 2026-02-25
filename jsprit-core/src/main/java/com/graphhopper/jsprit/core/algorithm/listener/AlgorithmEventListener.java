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

import com.graphhopper.jsprit.core.algorithm.listener.events.AlgorithmEvent;

/**
 * Listener interface for receiving algorithm events.
 *
 * <p>Implement this interface to receive notifications about all events
 * that occur during algorithm execution. Use pattern matching on the
 * sealed {@link AlgorithmEvent} hierarchy to handle specific event types.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * algorithm.addEventListener(event -> {
 *     switch (event) {
 *         case IterationStarted e -> System.out.println("Starting iteration " + e.iteration());
 *         case JobInserted e -> System.out.println("Job " + e.job().getId() + " inserted into " + e.routeId());
 *         case AcceptanceDecision e -> {
 *             if (e.isNewBest()) {
 *                 System.out.println("New best solution: " + e.newCost());
 *             }
 *         }
 *         default -> {} // ignore other events
 *     }
 * });
 * }</pre>
 *
 * <p>For filtering specific event types, you can also use:</p>
 * <pre>{@code
 * algorithm.addEventListener(AlgorithmEventListener.forType(JobInserted.class, e -> {
 *     System.out.println("Job inserted: " + e.job().getId());
 * }));
 * }</pre>
 */
@FunctionalInterface
public interface AlgorithmEventListener {

    /**
     * Called when an algorithm event occurs.
     *
     * @param event The event that occurred
     */
    void onEvent(AlgorithmEvent event);

    /**
     * Creates a listener that only handles events of a specific type.
     *
     * @param eventType The type of events to handle
     * @param handler   The handler for events of this type
     * @param <T>       The event type
     * @return A listener that filters and handles only the specified event type
     */
    static <T extends AlgorithmEvent> AlgorithmEventListener forType(Class<T> eventType, java.util.function.Consumer<T> handler) {
        return event -> {
            if (eventType.isInstance(event)) {
                handler.accept(eventType.cast(event));
            }
        };
    }

    /**
     * Creates a composite listener that forwards events to multiple listeners.
     *
     * @param listeners The listeners to forward events to
     * @return A composite listener
     */
    static AlgorithmEventListener composite(AlgorithmEventListener... listeners) {
        return event -> {
            for (AlgorithmEventListener listener : listeners) {
                listener.onEvent(event);
            }
        };
    }
}
