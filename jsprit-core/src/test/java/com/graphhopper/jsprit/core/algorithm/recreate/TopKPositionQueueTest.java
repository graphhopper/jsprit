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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TopKPositionQueue Test")
class TopKPositionQueueTest {

    @Test
    @DisplayName("Should keep only top-k positions")
    void shouldKeepOnlyTopKPositions() {
        TopKPositionQueue queue = new TopKPositionQueue(3);

        VehicleRoute route = VehicleRoute.emptyRoute();

        // Add 5 positions with different costs
        queue.offer(new InsertionData(50, 0, 0, null, null), route);
        queue.offer(new InsertionData(10, 0, 0, null, null), route);
        queue.offer(new InsertionData(30, 0, 0, null, null), route);
        queue.offer(new InsertionData(20, 0, 0, null, null), route);
        queue.offer(new InsertionData(40, 0, 0, null, null), route);

        assertEquals(3, queue.size());

        List<TopKPositionQueue.Position> sorted = queue.getSortedPositions();
        assertEquals(10, sorted.get(0).getCost(), 0.001);
        assertEquals(20, sorted.get(1).getCost(), 0.001);
        assertEquals(30, sorted.get(2).getCost(), 0.001);
    }

    @Test
    @DisplayName("Should return correct worst cost")
    void shouldReturnCorrectWorstCost() {
        TopKPositionQueue queue = new TopKPositionQueue(3);
        VehicleRoute route = VehicleRoute.emptyRoute();

        // Not full yet
        assertEquals(Double.MAX_VALUE, queue.worstCost());

        queue.offer(new InsertionData(10, 0, 0, null, null), route);
        assertEquals(Double.MAX_VALUE, queue.worstCost()); // Still not full

        queue.offer(new InsertionData(20, 0, 0, null, null), route);
        assertEquals(Double.MAX_VALUE, queue.worstCost()); // Still not full

        queue.offer(new InsertionData(30, 0, 0, null, null), route);
        assertEquals(30, queue.worstCost(), 0.001); // Now full, worst is 30

        queue.offer(new InsertionData(15, 0, 0, null, null), route);
        assertEquals(20, queue.worstCost(), 0.001); // 30 was replaced, worst is now 20
    }

    @Test
    @DisplayName("Should correctly check if position could make top-k")
    void shouldCorrectlyCheckIfPositionCouldMakeTopK() {
        TopKPositionQueue queue = new TopKPositionQueue(2);
        VehicleRoute route = VehicleRoute.emptyRoute();

        assertTrue(queue.couldMakeTopK(100)); // Not full yet

        queue.offer(new InsertionData(10, 0, 0, null, null), route);
        assertTrue(queue.couldMakeTopK(100)); // Still not full

        queue.offer(new InsertionData(20, 0, 0, null, null), route);
        assertTrue(queue.couldMakeTopK(15)); // Could replace 20
        assertFalse(queue.couldMakeTopK(20)); // Equal to worst, won't make it
        assertFalse(queue.couldMakeTopK(25)); // Worse than worst
    }

    @Test
    @DisplayName("Should return correct best and second best")
    void shouldReturnCorrectBestAndSecondBest() {
        TopKPositionQueue queue = new TopKPositionQueue(3);
        VehicleRoute route = VehicleRoute.emptyRoute();

        assertNull(queue.getBest());
        assertNull(queue.getSecondBest());

        queue.offer(new InsertionData(30, 0, 0, null, null), route);
        assertEquals(30, queue.getBest().getCost(), 0.001);
        assertNull(queue.getSecondBest());

        queue.offer(new InsertionData(10, 0, 0, null, null), route);
        assertEquals(10, queue.getBest().getCost(), 0.001);
        assertEquals(30, queue.getSecondBest().getCost(), 0.001);

        queue.offer(new InsertionData(20, 0, 0, null, null), route);
        assertEquals(10, queue.getBest().getCost(), 0.001);
        assertEquals(20, queue.getSecondBest().getCost(), 0.001);
    }

    @Test
    @DisplayName("Should reject NoInsertionFound")
    void shouldRejectNoInsertionFound() {
        TopKPositionQueue queue = new TopKPositionQueue(3);
        VehicleRoute route = VehicleRoute.emptyRoute();

        assertFalse(queue.offer(new InsertionData.NoInsertionFound(), route));
        assertEquals(0, queue.size());
    }

    @Test
    @DisplayName("Should convert to RegretKAlternatives")
    void shouldConvertToRegretKAlternatives() {
        TopKPositionQueue queue = new TopKPositionQueue(3);
        VehicleRoute route = VehicleRoute.emptyRoute();

        queue.offer(new InsertionData(30, 0, 0, null, null), route);
        queue.offer(new InsertionData(10, 0, 0, null, null), route);
        queue.offer(new InsertionData(20, 0, 0, null, null), route);

        RegretKAlternatives alternatives = queue.toAlternatives();
        assertEquals(3, alternatives.size());
        assertEquals(10, alternatives.getBest().getCost(), 0.001);
        assertEquals(20, alternatives.getSecondBest().getCost(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception for invalid k")
    void shouldThrowExceptionForInvalidK() {
        assertThrows(IllegalArgumentException.class, () -> new TopKPositionQueue(0));
        assertThrows(IllegalArgumentException.class, () -> new TopKPositionQueue(-1));
    }
}
