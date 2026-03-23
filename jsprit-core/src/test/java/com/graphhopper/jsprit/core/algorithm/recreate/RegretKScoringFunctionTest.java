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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Regret K Scoring Function Test")
class RegretKScoringFunctionTest {

    private Service job;
    private VehicleRoute emptyRoute;

    @BeforeEach
    void setUp() {
        job = Service.Builder.newInstance("job1")
                .setLocation(Location.newInstance(0, 0))
                .build();
        emptyRoute = VehicleRoute.emptyRoute();
    }

    @Test
    @DisplayName("Sum scoring with 3 alternatives should compute sum of regrets")
    void sumScoringWithThreeAlternatives() {
        // Costs: 10, 20, 30
        // Sum of regrets = (20-10) + (30-10) = 10 + 20 = 30
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);
        alternatives.add(createInsertionData(30), emptyRoute);

        SumRegretKScoringFunction scorer = new SumRegretKScoringFunction(null, 3);
        double score = scorer.score(alternatives, job);

        // Default priority is 2, so multiplier is (11-2) = 9
        // Score = 9 * 30 = 270
        assertEquals(270.0, score, 0.001);
    }

    @Test
    @DisplayName("Max scoring with 3 alternatives should compute max regret")
    void maxScoringWithThreeAlternatives() {
        // Costs: 10, 20, 30
        // Max regret = c_k - c_1 = 30 - 10 = 20
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);
        alternatives.add(createInsertionData(30), emptyRoute);

        MaxRegretKScoringFunction scorer = new MaxRegretKScoringFunction(null, 3);
        double score = scorer.score(alternatives, job);

        // Default priority is 2, so multiplier is (11-2) = 9
        // Score = 9 * 20 = 180
        assertEquals(180.0, score, 0.001);
    }

    @Test
    @DisplayName("Average scoring with 3 alternatives should compute average regret")
    void averageScoringWithThreeAlternatives() {
        // Costs: 10, 20, 30
        // Average of c_2..c_3 = (20 + 30) / 2 = 25
        // Average regret = 25 - 10 = 15
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);
        alternatives.add(createInsertionData(30), emptyRoute);

        AverageRegretKScoringFunction scorer = new AverageRegretKScoringFunction(null, 3);
        double score = scorer.score(alternatives, job);

        // Default priority is 2, so multiplier is (11-2) = 9
        // Score = 9 * 15 = 135
        assertEquals(135.0, score, 0.001);
    }

    @Test
    @DisplayName("Sum scoring with k=2 should be equivalent to regret-2")
    void sumScoringWithK2EquivalentToRegret2() {
        // Costs: 10, 20
        // Sum of regrets = 20 - 10 = 10
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);
        alternatives.add(createInsertionData(30), emptyRoute); // Should be ignored for k=2

        SumRegretKScoringFunction scorer = new SumRegretKScoringFunction(null, 2);
        double score = scorer.score(alternatives, job);

        // Default priority is 2, so multiplier is (11-2) = 9
        // Score = 9 * (20-10) = 90
        assertEquals(90.0, score, 0.001);
    }

    @Test
    @DisplayName("Scoring with single alternative should give high priority")
    void scoringWithSingleAlternative() {
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);

        SumRegretKScoringFunction scorer = new SumRegretKScoringFunction(null, 3);
        double score = scorer.score(alternatives, job);

        // With only one alternative, should get very high score
        assertTrue(score > 1e9, "Score should be very high with single alternative");
    }

    @Test
    @DisplayName("K greater than available alternatives should use all")
    void kGreaterThanAvailableAlternatives() {
        // Only 2 alternatives, but k=5
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);

        SumRegretKScoringFunction scorer = new SumRegretKScoringFunction(null, 5);
        double score = scorer.score(alternatives, job);

        // Should gracefully use only the 2 available alternatives
        // Sum of regrets = 20 - 10 = 10
        // Default priority is 2, so multiplier is (11-2) = 9
        // Score = 9 * 10 = 90
        assertEquals(90.0, score, 0.001);
    }

    @Test
    @DisplayName("Priority weighting should affect score")
    void priorityWeightingShouldAffectScore() {
        Service highPriorityJob = Service.Builder.newInstance("highPriority")
                .setLocation(Location.newInstance(0, 0))
                .setPriority(1)
                .build();
        Service lowPriorityJob = Service.Builder.newInstance("lowPriority")
                .setLocation(Location.newInstance(0, 0))
                .setPriority(10)
                .build();

        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);

        SumRegretKScoringFunction scorer = new SumRegretKScoringFunction(null, 2);

        double highPriorityScore = scorer.score(alternatives, highPriorityJob);
        double lowPriorityScore = scorer.score(alternatives, lowPriorityJob);

        // High priority (1) -> multiplier (11-1) = 10
        // Low priority (10) -> multiplier (11-10) = 1
        // Regret = 20 - 10 = 10
        assertEquals(100.0, highPriorityScore, 0.001);
        assertEquals(10.0, lowPriorityScore, 0.001);
    }

    @Test
    @DisplayName("Factory should create correct strategy")
    void factoryShouldCreateCorrectStrategy() {
        RegretKScoringFunction sumFunction = RegretKScoringFunctionFactory.create(
                RegretKScoringFunctionFactory.Strategy.SUM, 3, null);
        RegretKScoringFunction maxFunction = RegretKScoringFunctionFactory.create(
                RegretKScoringFunctionFactory.Strategy.MAX, 3, null);
        RegretKScoringFunction avgFunction = RegretKScoringFunctionFactory.create(
                RegretKScoringFunctionFactory.Strategy.AVG, 3, null);

        assertTrue(sumFunction instanceof SumRegretKScoringFunction);
        assertTrue(maxFunction instanceof MaxRegretKScoringFunction);
        assertTrue(avgFunction instanceof AverageRegretKScoringFunction);
    }

    @Test
    @DisplayName("Factory should parse strategy names correctly")
    void factoryShouldParseStrategyNames() {
        assertEquals(RegretKScoringFunctionFactory.Strategy.SUM,
                RegretKScoringFunctionFactory.parseStrategy("sum"));
        assertEquals(RegretKScoringFunctionFactory.Strategy.SUM,
                RegretKScoringFunctionFactory.parseStrategy("SUM"));
        assertEquals(RegretKScoringFunctionFactory.Strategy.MAX,
                RegretKScoringFunctionFactory.parseStrategy("max"));
        assertEquals(RegretKScoringFunctionFactory.Strategy.AVG,
                RegretKScoringFunctionFactory.parseStrategy("avg"));
        assertEquals(RegretKScoringFunctionFactory.Strategy.AVG,
                RegretKScoringFunctionFactory.parseStrategy("average"));
    }

    @Test
    @DisplayName("Factory with k=-1 should treat as all alternatives")
    void factoryWithKMinusOneShouldTreatAsAll() {
        RegretKScoringFunction function = RegretKScoringFunctionFactory.create(
                RegretKScoringFunctionFactory.Strategy.SUM, -1, null);

        assertTrue(function instanceof SumRegretKScoringFunction);
        SumRegretKScoringFunction sumFunction = (SumRegretKScoringFunction) function;
        assertEquals(Integer.MAX_VALUE, sumFunction.getK());
    }

    @Test
    @DisplayName("Adapter should wrap legacy scoring function")
    void adapterShouldWrapLegacyScoringFunction() {
        RegretScoringFunction legacyFunction = new DefaultRegretScoringFunction(null);
        RegretKScoringFunctionAdapter adapter = new RegretKScoringFunctionAdapter(legacyFunction);

        assertSame(legacyFunction, adapter.getLegacyScoringFunction());
    }

    @Test
    @DisplayName("RegretKAlternatives should maintain sorted order")
    void alternativesShouldMaintainSortedOrder() {
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(30), emptyRoute);
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);

        assertEquals(10.0, alternatives.getBest().getCost(), 0.001);
        assertEquals(20.0, alternatives.getSecondBest().getCost(), 0.001);
        assertEquals(30.0, alternatives.get(2).getCost(), 0.001);
    }

    @Test
    @DisplayName("RegretKAlternatives getTopK should return correct subset")
    void alternativesGetTopKShouldReturnCorrectSubset() {
        RegretKAlternatives alternatives = new RegretKAlternatives();
        alternatives.add(createInsertionData(10), emptyRoute);
        alternatives.add(createInsertionData(20), emptyRoute);
        alternatives.add(createInsertionData(30), emptyRoute);
        alternatives.add(createInsertionData(40), emptyRoute);

        var topK = alternatives.getTopK(2);
        assertEquals(2, topK.size());
        assertEquals(10.0, topK.get(0).getCost(), 0.001);
        assertEquals(20.0, topK.get(1).getCost(), 0.001);
    }

    private InsertionData createInsertionData(double cost) {
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1")
                .setStartLocation(Location.newInstance(0, 0))
                .build();
        return new InsertionData(cost, 0, 0, vehicle, null);
    }
}
