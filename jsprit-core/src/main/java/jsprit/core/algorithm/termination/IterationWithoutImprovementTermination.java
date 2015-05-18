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
package jsprit.core.algorithm.termination;

import jsprit.core.algorithm.SearchStrategy.DiscoveredSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Terminates algorithm prematurely based on iterations without any improvement (i.e. new solution acceptance).
 *
 * <p>Termination will be activated by:<br>
 *
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 *
 * @author stefan schroeder
 *
 */
public class IterationWithoutImprovementTermination implements PrematureAlgorithmTermination{

	private static Logger log = LogManager.getLogger(IterationWithoutImprovementTermination.class);
	
	private int noIterationWithoutImprovement;
	
	private int iterationsWithoutImprovement = 0;

    /**
     * Constructs termination.
     *
     * @param noIterationsWithoutImprovement previous iterations without any improvement
     */
	public IterationWithoutImprovementTermination(int noIterationsWithoutImprovement){
		this.noIterationWithoutImprovement =noIterationsWithoutImprovement;
		log.debug("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=IterationWithoutImprovementBreaker][iterationsWithoutImprovement="+ noIterationWithoutImprovement +"]";
	}
	
	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if(discoveredSolution.isAccepted()) iterationsWithoutImprovement = 0;
		else iterationsWithoutImprovement++;
		return (iterationsWithoutImprovement > noIterationWithoutImprovement);
	}

	
}
