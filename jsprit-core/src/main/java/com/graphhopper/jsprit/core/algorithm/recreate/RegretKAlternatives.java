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

import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Holds k-best insertion alternatives for a job, sorted by insertion cost.
 * Used for regret-k insertion heuristics where k can be 2, 3, 4, or more.
 */
public class RegretKAlternatives {

    /**
     * Represents a single insertion alternative with its route and cost.
     */
    public static class Alternative {
        private final InsertionData insertionData;
        private final VehicleRoute route;

        public Alternative(InsertionData insertionData, VehicleRoute route) {
            this.insertionData = insertionData;
            this.route = route;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }

        public double getCost() {
            return insertionData.getInsertionCost();
        }
    }

    private final List<Alternative> alternatives;

    public RegretKAlternatives() {
        this.alternatives = new ArrayList<>();
    }

    public RegretKAlternatives(List<Alternative> alternatives) {
        this.alternatives = new ArrayList<>(alternatives);
        this.alternatives.sort(Comparator.comparingDouble(Alternative::getCost));
    }

    /**
     * Adds an alternative and maintains sorted order by cost.
     */
    public void add(InsertionData insertionData, VehicleRoute route) {
        Alternative alt = new Alternative(insertionData, route);
        int insertPos = Collections.binarySearch(alternatives, alt, Comparator.comparingDouble(Alternative::getCost));
        if (insertPos < 0) {
            insertPos = -(insertPos + 1);
        }
        alternatives.add(insertPos, alt);
    }

    /**
     * Returns all alternatives sorted by cost (ascending).
     */
    public List<Alternative> getAlternatives() {
        return Collections.unmodifiableList(alternatives);
    }

    /**
     * Returns the best (lowest cost) alternative, or null if empty.
     */
    public Alternative getBest() {
        return alternatives.isEmpty() ? null : alternatives.get(0);
    }

    /**
     * Returns the second-best alternative for backward compatibility.
     * Returns null if fewer than 2 alternatives exist.
     */
    public Alternative getSecondBest() {
        return alternatives.size() < 2 ? null : alternatives.get(1);
    }

    /**
     * Returns the alternative at the given index (0-based).
     * Returns null if index is out of bounds.
     */
    public Alternative get(int index) {
        return index >= 0 && index < alternatives.size() ? alternatives.get(index) : null;
    }

    /**
     * Returns the number of alternatives.
     */
    public int size() {
        return alternatives.size();
    }

    /**
     * Returns true if there are no alternatives.
     */
    public boolean isEmpty() {
        return alternatives.isEmpty();
    }

    /**
     * Returns the top k alternatives (or all if fewer than k exist).
     */
    public List<Alternative> getTopK(int k) {
        int limit = Math.min(k, alternatives.size());
        return Collections.unmodifiableList(alternatives.subList(0, limit));
    }

    /**
     * Creates RegretKAlternatives from best and secondBest InsertionData for backward compatibility.
     */
    public static RegretKAlternatives fromBestAndSecondBest(InsertionData best, InsertionData secondBest,
                                                            VehicleRoute bestRoute, VehicleRoute secondBestRoute) {
        RegretKAlternatives alts = new RegretKAlternatives();
        if (best != null) {
            alts.add(best, bestRoute);
        }
        if (secondBest != null) {
            alts.add(secondBest, secondBestRoute);
        }
        return alts;
    }
}
