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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captures a breakdown of insertion costs by component.
 * <p>
 * This allows full transparency into why an insertion costs what it costs,
 * showing contributions from each soft constraint and the base activity insertion cost.
 */
public class InsertionCostBreakdown {

    private final Map<String, Double> components = new LinkedHashMap<>();

    /**
     * Add a cost component.
     *
     * @param name The component name (e.g., "Route:SpatialCompactness", "ActivityInsertion")
     * @param cost The cost value (positive = penalty, negative = bonus)
     */
    public void add(String name, double cost) {
        components.merge(name, cost, Double::sum);
    }

    /**
     * Merge another breakdown into this one.
     * Costs for components with the same name are summed.
     *
     * @param other The breakdown to merge
     */
    public void merge(InsertionCostBreakdown other) {
        if (other != null) {
            for (var entry : other.components.entrySet()) {
                add(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Get all cost components.
     *
     * @return Unmodifiable map of component name to cost
     */
    public Map<String, Double> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    /**
     * Get the cost for a specific component.
     *
     * @param name The component name
     * @return The cost, or 0 if not present
     */
    public double get(String name) {
        return components.getOrDefault(name, 0.0);
    }

    /**
     * Get the total cost (sum of all components).
     *
     * @return Total cost
     */
    public double getTotal() {
        return components.values().stream().mapToDouble(d -> d).sum();
    }

    /**
     * Check if this breakdown has any components.
     *
     * @return true if at least one component was added
     */
    public boolean isEmpty() {
        return components.isEmpty();
    }

    /**
     * Print the breakdown to stdout.
     */
    public void print() {
        double total = getTotal();
        for (var entry : components.entrySet()) {
            double pct = total != 0 ? 100.0 * entry.getValue() / total : 0;
            System.out.printf("  %-40s: %10.2f (%5.1f%%)%n",
                    entry.getKey(), entry.getValue(), pct);
        }
        System.out.printf("  %-40s: %10.2f%n", "TOTAL", total);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("InsertionCostBreakdown{");
        boolean first = true;
        for (var entry : components.entrySet()) {
            if (!first) sb.append(", ");
            sb.append(entry.getKey()).append("=").append(String.format("%.2f", entry.getValue()));
            first = false;
        }
        sb.append(", total=").append(String.format("%.2f", getTotal()));
        sb.append("}");
        return sb.toString();
    }
}
