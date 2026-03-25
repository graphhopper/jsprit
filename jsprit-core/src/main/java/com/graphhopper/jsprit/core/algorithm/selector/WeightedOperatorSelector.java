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
package com.graphhopper.jsprit.core.algorithm.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Selects operators based on their weights using weighted random selection.
 *
 * <p>This class provides a simple way to manage multiple operators with different
 * probabilities of being selected. Operators with higher weights are selected
 * more frequently.</p>
 *
 * <p>Thread-safety: This class is NOT thread-safe. If used from multiple threads,
 * external synchronization is required.</p>
 *
 * @param <T> the type of operator
 */
public class WeightedOperatorSelector<T> {

    /**
     * Entry holding an operator with its weight.
     */
    public record Entry<T>(T operator, double weight, String name) {
        public Entry(T operator, double weight) {
            this(operator, weight, null);
        }
    }

    private final List<Entry<T>> operators = new ArrayList<>();
    private double totalWeight = 0;
    private Random random;
    private Entry<T> lastSelected = null;

    /**
     * Creates a new selector with default random.
     */
    public WeightedOperatorSelector() {
        this.random = new Random();
    }

    /**
     * Creates a new selector with the specified random.
     *
     * @param random the random number generator to use
     */
    public WeightedOperatorSelector(Random random) {
        this.random = random;
    }

    /**
     * Sets the random number generator.
     *
     * @param random the random number generator
     */
    public void setRandom(Random random) {
        this.random = random;
    }

    /**
     * Adds an operator with the specified weight.
     *
     * @param operator the operator to add
     * @param weight   the selection weight (must be positive)
     * @return this selector for chaining
     */
    public WeightedOperatorSelector<T> add(T operator, double weight) {
        return add(operator, weight, null);
    }

    /**
     * Adds an operator with the specified weight and name.
     *
     * @param operator the operator to add
     * @param weight   the selection weight (must be positive)
     * @param name     optional name for identification/debugging
     * @return this selector for chaining
     */
    public WeightedOperatorSelector<T> add(T operator, double weight, String name) {
        if (weight <= 0) {
            throw new IllegalArgumentException("Weight must be positive, got: " + weight);
        }
        operators.add(new Entry<>(operator, weight, name));
        totalWeight += weight;
        return this;
    }

    /**
     * Selects a random operator based on weights.
     *
     * @return the selected operator
     * @throws IllegalStateException if no operators have been added
     */
    public T select() {
        if (operators.isEmpty()) {
            throw new IllegalStateException("No operators registered");
        }

        if (operators.size() == 1) {
            lastSelected = operators.get(0);
            return lastSelected.operator();
        }

        double r = random.nextDouble() * totalWeight;
        double cumulative = 0;

        for (Entry<T> entry : operators) {
            cumulative += entry.weight();
            if (r < cumulative) {
                lastSelected = entry;
                return entry.operator();
            }
        }

        // Fallback (shouldn't happen due to floating point, but be safe)
        lastSelected = operators.get(operators.size() - 1);
        return lastSelected.operator();
    }

    /**
     * Returns the entry that was selected in the last call to {@link #select()}.
     *
     * @return the last selected entry, or null if select() hasn't been called
     */
    public Entry<T> getLastSelected() {
        return lastSelected;
    }

    /**
     * Returns the name of the operator selected in the last call to {@link #select()}.
     *
     * @return the name of the last selected operator, or "unknown" if no name was set
     */
    public String getLastSelectedName() {
        if (lastSelected == null) {
            return "unknown";
        }
        return lastSelected.name() != null ? lastSelected.name() : "op" + operators.indexOf(lastSelected);
    }

    /**
     * Returns the number of operators registered.
     */
    public int size() {
        return operators.size();
    }

    /**
     * Returns true if no operators have been registered.
     */
    public boolean isEmpty() {
        return operators.isEmpty();
    }

    /**
     * Returns the total weight of all operators.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    /**
     * Returns an unmodifiable list of all entries.
     */
    public List<Entry<T>> getEntries() {
        return Collections.unmodifiableList(operators);
    }

    /**
     * Returns the selection probability for an operator at the given index.
     *
     * @param index the index of the operator
     * @return the probability (0.0 to 1.0) of selecting this operator
     */
    public double getProbability(int index) {
        if (totalWeight == 0) return 0;
        return operators.get(index).weight() / totalWeight;
    }

    /**
     * Clears all registered operators.
     */
    public void clear() {
        operators.clear();
        totalWeight = 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("WeightedOperatorSelector{");
        for (int i = 0; i < operators.size(); i++) {
            Entry<T> entry = operators.get(i);
            if (i > 0) sb.append(", ");
            if (entry.name() != null) {
                sb.append(entry.name());
            } else {
                sb.append("op").append(i);
            }
            sb.append("=").append(String.format("%.1f%%", getProbability(i) * 100));
        }
        sb.append("}");
        return sb.toString();
    }
}
