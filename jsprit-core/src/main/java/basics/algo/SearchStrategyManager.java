/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.algo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import util.RandomNumberGeneration;


public class SearchStrategyManager {
	
	private List<SearchStrategyListener> searchStrategyListeners = new ArrayList<SearchStrategyListener>();
	
	private List<SearchStrategy> strategies = new ArrayList<SearchStrategy>();
	
	private List<Double> probabilities = new ArrayList<Double>();
	
	private Random random = RandomNumberGeneration.getRandom();
	
	private double sumOfProbabilities = 0;
	
	public void setRandom(Random random) {
		this.random = random;
	}
	
	
	public List<SearchStrategy> getStrategies() {
		return Collections.unmodifiableList(strategies);
	}


	public List<Double> getProbabilities() {
		return Collections.unmodifiableList(probabilities);
	}
	
	/**
	 * adds a new search strategy. the probability must be within [0,1].
	 * @param strategy
	 * @param probability
	 */
	public void addStrategy(SearchStrategy strategy, double probability){
		if(strategy == null){
			throw new IllegalStateException("strategy is null. make sure adding a valid strategy.");
		}
		if(probability > 1.0){
			throw new IllegalStateException("probability is higher than one, but it must be within [0,1].");
		}
		if(probability < 0.0){
			throw new IllegalStateException("probability is lower than zero, but it must be within [0,1].");
		}
		strategies.add(strategy);
		probabilities.add(probability);
		sumOfProbabilities += probability;
		if(sumOfProbabilities > 1.0){
			throw new IllegalStateException("total probability of all strategies is higher than one, but it must be within [0,1].");
		}
	}

	public SearchStrategy getRandomStrategy() {
		if(random == null) throw new IllegalStateException("randomizer is null. make sure you set random object correctly");
		double randomFig = random.nextDouble();
		double sumWeight = 0.0;
		for (int i = 0; i < probabilities.size(); i++) {
			sumWeight += probabilities.get(i);
			if (randomFig < sumWeight) {
				return strategies.get(i);
			}
		}
		throw new IllegalStateException("no seaarch-strategy found");
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
