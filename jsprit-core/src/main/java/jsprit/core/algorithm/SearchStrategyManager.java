/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.algorithm;

import jsprit.core.algorithm.listener.SearchStrategyListener;
import jsprit.core.algorithm.listener.SearchStrategyModuleListener;
import jsprit.core.util.RandomNumberGeneration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


public class SearchStrategyManager {

	private final static Logger logger = LogManager.getLogger();
	
	private List<SearchStrategyListener> searchStrategyListeners = new ArrayList<SearchStrategyListener>();
	
	private List<SearchStrategy> strategies = new ArrayList<SearchStrategy>();
	
	private List<Double> weights = new ArrayList<Double>();

    private Map<String, Integer> id2index = new HashMap<String,Integer>();
	
	private Random random = RandomNumberGeneration.getRandom();
	
	private double sumWeights = 0;

    private int strategyIndex = 0;
	
	public void setRandom(Random random) {
		this.random = random;
	}
	
	public List<SearchStrategy> getStrategies() {
		return Collections.unmodifiableList(strategies);
	}

    /**
     * Returns the probabilities.
     * [schroeder (2014.11.21): Now they are actually no propabilities anymore but weights. The resulting probabilities
     * are calculated here with the sum of weights]
     * @return list of probabilities
     */
	@Deprecated
	public List<Double> getProbabilities() {
		return Collections.unmodifiableList(weights);
	}

	public List<Double> getWeights(){ return Collections.unmodifiableList(weights); }

	public double getWeight(String strategyId){
		return weights.get(id2index.get(strategyId));
	}

	/**
	 * adds a new search strategy with a certain weight.
	 * @param strategy strategy to be added
	 * @param weight of corresponding strategy to be added
     * @throws java.lang.IllegalStateException if strategy is null OR weight < 0
	 */
	public void addStrategy(SearchStrategy strategy, double weight){
        if(strategy == null){
            throw new IllegalStateException("strategy is null. make sure adding a valid strategy.");
        }
        if(id2index.keySet().contains(strategy.getId())){
            throw new IllegalStateException("strategyId " + strategy.getId() + " already in use. replace strateId in your config file or code with a unique strategy id");
        }
        if(weight < 0.0){
			throw new IllegalStateException("weight is lower than zero.");
		}
        id2index.put(strategy.getId(),strategyIndex);
        strategyIndex++;
		strategies.add(strategy);
		weights.add(weight);
		sumWeights += weight;
	}

    public void informStrategyWeightChanged(String strategyId, double weight){
        int strategyIndex = id2index.get(strategyId);
        weights.set(strategyIndex, weight);
        updateSumWeights();
    }

    private void updateSumWeights() {
        double sum = 0.;
        for(double w : weights){
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
		if(random == null) throw new IllegalStateException("randomizer is null. make sure you set random object correctly");
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
	
	public void addSearchStrategyListener(SearchStrategyListener strategyListener){
		searchStrategyListeners.add(strategyListener);
	}
	
	public void addSearchStrategyModuleListener(SearchStrategyModuleListener moduleListener){
		for(SearchStrategy s : strategies){
			s.addModuleListener(moduleListener);
		}
	}
}
