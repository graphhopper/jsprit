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
package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.acceptor.SolutionAcceptor;
import com.graphhopper.jsprit.core.algorithm.selector.SolutionSelector;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Search Strategy Manager Test")
class SearchStrategyManagerTest {

    @Test
    @DisplayName("Strategy Manager In Action _ adding Strategy _ Is Successful")
    void StrategyManagerInAction_addingStrategy_IsSuccessful() {
        SearchStrategyManager manager = new SearchStrategyManager();
        SearchStrategy strat1 = mock(SearchStrategy.class);
        SearchStrategy strat2 = mock(SearchStrategy.class);
        when(strat1.getId()).thenReturn("strat1");
        when(strat2.getId()).thenReturn("strat2");
        manager.addStrategy(strat1, 0.5);
        manager.addStrategy(strat2, 0.5);
        assertTrue(true);
    }

    @Test
    @DisplayName("Strategy Manager In Action _ strategy Is Null _ throws Exception")
    void StrategyManagerInAction_strategyIsNull_throwsException() {
        assertThrows(IllegalStateException.class, () -> {
            SearchStrategyManager manager = new SearchStrategyManager();
            manager.addStrategy(null, 1.0);
            assertTrue(false);
        });
    }

    @Test
    @DisplayName("Strategy Manager In Action _ probability Is Lower Than Zero _ throws Exception")
    void StrategyManagerInAction_probabilityIsLowerThanZero_throwsException() {
        assertThrows(IllegalStateException.class, () -> {
            SearchStrategyManager manager = new SearchStrategyManager();
            SearchStrategy strat = mock(SearchStrategy.class);
            when(strat.getId()).thenReturn("strat1");
            manager.addStrategy(strat, -1.0);
            assertTrue(false);
        });
    }

    @Test
    @DisplayName("It Should Return Strategy 2")
    void itShouldReturnStrategy2() {
        SearchStrategyManager manager = new SearchStrategyManager();
        SearchStrategy mockedStrat1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrat2 = mock(SearchStrategy.class);
        when(mockedStrat1.getId()).thenReturn("strat1");
        when(mockedStrat2.getId()).thenReturn("strat2");
        manager.addStrategy(mockedStrat1, 0.5);
        manager.addStrategy(mockedStrat2, 1.5);
        Random mockedRandom = mock(Random.class);
        manager.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.25);
        assertThat(manager.getRandomStrategy(), is(mockedStrat2));
    }

    @Test
    @DisplayName("When Strat Weight Changed _ it Should Return Strategy 1")
    void whenStratWeightChanged_itShouldReturnStrategy1() {
        SearchStrategyManager manager = new SearchStrategyManager();
        SearchStrategy mockedStrat1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrat2 = mock(SearchStrategy.class);
        when(mockedStrat1.getId()).thenReturn("strat1");
        when(mockedStrat2.getId()).thenReturn("strat2");
        manager.addStrategy(mockedStrat1, 0.5);
        manager.addStrategy(mockedStrat2, 1.5);
        Random mockedRandom = mock(Random.class);
        manager.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.25);
        assertThat(manager.getRandomStrategy(), is(mockedStrat2));
        manager.informStrategyWeightChanged("strat2", 1.4);
        assertThat(manager.getRandomStrategy(), is(mockedStrat1));
    }

    @Test
    @DisplayName("It Should Return Strategy 1")
    void itShouldReturnStrategy1() {
        SearchStrategyManager manager = new SearchStrategyManager();
        SearchStrategy mockedStrat1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrat2 = mock(SearchStrategy.class);
        when(mockedStrat1.getId()).thenReturn("strat1");
        when(mockedStrat2.getId()).thenReturn("strat2");
        manager.addStrategy(mockedStrat1, 0.5);
        manager.addStrategy(mockedStrat2, 1.5);
        Random mockedRandom = mock(Random.class);
        manager.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.24);
        assertThat(manager.getRandomStrategy(), is(mockedStrat1));
    }

    @Test
    @DisplayName("When Random Dices _ 0 point 1 _ returns Strategy 1")
    void whenRandomDices_0point1_returnsStrategy1() {
        SearchStrategyManager managerUnderTest = new SearchStrategyManager();
        SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
        when(mockedStrategy1.getId()).thenReturn("strat1");
        when(mockedStrategy2.getId()).thenReturn("strat2");
        managerUnderTest.addStrategy(mockedStrategy1, 0.2);
        managerUnderTest.addStrategy(mockedStrategy2, 0.8);
        Random mockedRandom = mock(Random.class);
        managerUnderTest.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.1);
        assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy1));
    }

    @Test
    @DisplayName("When Random Dices _ 0 point 5 _ returns Strategy 2")
    void whenRandomDices_0point5_returnsStrategy2() {
        SearchStrategyManager managerUnderTest = new SearchStrategyManager();
        SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
        when(mockedStrategy1.getId()).thenReturn("strat1");
        when(mockedStrategy2.getId()).thenReturn("strat2");
        managerUnderTest.addStrategy(mockedStrategy1, 0.2);
        managerUnderTest.addStrategy(mockedStrategy2, 0.8);
        Random mockedRandom = mock(Random.class);
        managerUnderTest.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.5);
        assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy2));
    }

    @Test
    @DisplayName("When Random Dices _ 0 point 0 _ returns Strategy 1")
    void whenRandomDices_0point0_returnsStrategy1() {
        SearchStrategyManager managerUnderTest = new SearchStrategyManager();
        SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
        SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
        when(mockedStrategy1.getId()).thenReturn("strat1");
        when(mockedStrategy2.getId()).thenReturn("strat2");
        managerUnderTest.addStrategy(mockedStrategy1, 0.2);
        managerUnderTest.addStrategy(mockedStrategy2, 0.8);
        Random mockedRandom = mock(Random.class);
        managerUnderTest.setRandom(mockedRandom);
        when(mockedRandom.nextDouble()).thenReturn(0.0);
        assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy1));
    }

    @Test
    @DisplayName("When Random Is Null _ throw Exception")
    void whenRandomIsNull_throwException() {
        assertThrows(IllegalStateException.class, () -> {
            SearchStrategyManager managerUnderTest = new SearchStrategyManager();
            SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
            SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
            managerUnderTest.addStrategy(mockedStrategy1, 0.2);
            managerUnderTest.addStrategy(mockedStrategy2, 0.8);
            when(mockedStrategy1.getId()).thenReturn("strat1");
            when(mockedStrategy2.getId()).thenReturn("strat2");
            Random mockedRandom = null;
            managerUnderTest.setRandom(mockedRandom);
            managerUnderTest.getRandomStrategy();
        });
    }

    @Test
    @DisplayName("Strategy Draw Should Be Reproducible")
    void strategyDrawShouldBeReproducible() {
        RandomNumberGeneration.reset();
        SearchStrategyManager managerUnderTest = new SearchStrategyManager();
        SearchStrategy mockedStrategy1 = new SearchStrategy("strat1", mock(SolutionSelector.class), mock(SolutionAcceptor.class), mock(SolutionCostCalculator.class));
        SearchStrategy mockedStrategy2 = new SearchStrategy("strat2", mock(SolutionSelector.class), mock(SolutionAcceptor.class), mock(SolutionCostCalculator.class));
        managerUnderTest.addStrategy(mockedStrategy1, 0.2);
        managerUnderTest.addStrategy(mockedStrategy2, 0.8);
        List<String> firstRecord = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            firstRecord.add(managerUnderTest.getRandomStrategy().getId());
        }
        RandomNumberGeneration.reset();
        List<String> secondRecord = new ArrayList<String>();
        for (int i = 0; i < 1000; i++) {
            secondRecord.add(managerUnderTest.getRandomStrategy().getId());
        }
        for (int i = 0; i < 1000; i++) {
            if (!firstRecord.get(i).equals(secondRecord.get(i))) {
                Assertions.assertFalse(true);
            }
        }
        Assertions.assertTrue(true);
    }
}
