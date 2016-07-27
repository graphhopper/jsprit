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

import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyListener;
import com.graphhopper.jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SearchStrategyManager {

    private final static Logger logger = LoggerFactory.getLogger(SearchStrategyManager.class);

    private List<SearchStrategyListener> searchStrategyListeners = new ArrayList<SearchStrategyListener>();

    private List<SearchStrategy> strategies = new ArrayList<SearchStrategy>();

    private List<Double> weights = new ArrayList<Double>();

    private Map<String, Integer> id2index = new HashMap<String, Integer>();

    private Random random = RandomNumberGeneration.getRandom();

    private double sumWeights = 0;

    private int strategyIndex = 0;

    public void setRandom(Random random) {
        this.random = random;
    }

    public List<SearchStrategy> getStrategies() {
        return Collections.unmodifiableList(strategies);
    }

    public List<Double> getWeights() {
        return Collections.unmodifiableList(weights);
    }

    public double getWeight(String strategyId) {
        return weights.get(id2index.get(strategyId));
    }

    /**
     * adds a new search strategy with a certain weight.
     *
     * @param strategy strategy to be added
     * @param weight   of corresponding strategy to be added
     * @throws java.lang.IllegalStateException if strategy is null OR weight < 0
     */
    public void addStrategy(SearchStrategy strategy, double weight) {
        if (strategy == null) {
            throw new IllegalStateException("strategy is null. make sure adding a valid strategy.");
        }
        if (id2index.keySet().contains(strategy.getId())) {
            throw new IllegalStateException("strategyId " + strategy.getId() + " already in use. replace strateId in your config file or code with a unique strategy id");
        }
        if (weight < 0.0) {
            throw new IllegalStateException("weight is lower than zero.");
        }
        id2index.put(strategy.getId(), strategyIndex);
        strategyIndex++;
        strategies.add(strategy);
        weights.add(weight);
        sumWeights += weight;
    }

    public void informStrategyWeightChanged(String strategyId, double weight) {
        int strategyIndex = id2index.get(strategyId);
        weights.set(strategyIndex, weight);
        updateSumWeights();
    }

    private void updateSumWeights() {
        double sum = 0.;
        for (double w : weights) {
            sum += w;
        }
        sumWeights = sum;
    }

    /**
     * Returns search strategy that has been randomly selected.
     *
     * @return selected search strategy
     * @throws java.lang.IllegalStateException if randomNumberGenerator is null OR no search strategy can be found
     */
    public SearchStrategy getRandomStrategy() {
        if (random == null)
            throw new IllegalStateException("randomizer is null. make sure you set random object correctly");
        double randomFig = random.nextDouble();
        double sumProbabilities = 0.0;
        for (int i = 0; i < weights.size(); i++) {
            sumProbabilities += weights.get(i) / sumWeights;
            if (randomFig < sumProbabilities) {
                return strategies.get(i);
            }
        }
        throw new IllegalStateException("no search-strategy found");
    }

    public void addSearchStrategyListener(SearchStrategyListener strategyListener) {
        searchStrategyListeners.add(strategyListener);
    }

    public void addSearchStrategyModuleListener(SearchStrategyModuleListener moduleListener) {
        for (SearchStrategy s : strategies) {
            s.addModuleListener(moduleListener);
        }
    }
}
