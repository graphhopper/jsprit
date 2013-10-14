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

import java.util.Collection;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategy.DiscoveredSolution;

public class TimeBreaker implements PrematureAlgorithmBreaker, AlgorithmStartsListener{

	private static Logger logger = Logger.getLogger(TimeBreaker.class);
	
	private double timeThreshold;
	
	private double startTime;
	
	public TimeBreaker(double time) {
		super();
		this.timeThreshold = time;
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=TimeBreaker][timeThreshold="+timeThreshold+"]";
	}

	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if((System.currentTimeMillis() - startTime)/1000.0 > timeThreshold) return true;
		return false;
	}
	
	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		startTime = System.currentTimeMillis();
	}

}
