/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package basics.algo;

import org.apache.log4j.Logger;

import basics.algo.SearchStrategy.DiscoveredSolution;

public class IterationWithoutImprovementBreaker implements PrematureAlgorithmBreaker{

	private static Logger log = Logger.getLogger(IterationWithoutImprovementBreaker.class);
	
	private int nuOfIterationWithoutImprovement;
	
	private int iterationsWithoutImprovement = 0;
	
	public IterationWithoutImprovementBreaker(int nuOfIterationsWithoutImprovement){
		this.nuOfIterationWithoutImprovement=nuOfIterationsWithoutImprovement;
		log.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=IterationWithoutImprovementBreaker][iterationsWithoutImprovement="+nuOfIterationWithoutImprovement+"]";
	}
	
	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if(discoveredSolution.isAccepted()) iterationsWithoutImprovement = 0;
		else iterationsWithoutImprovement++;
		if(iterationsWithoutImprovement > nuOfIterationWithoutImprovement){
			return true;
		}
		return false;
	}

	
}
