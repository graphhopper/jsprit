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
package jsprit.core.algorithm.termination;

import jsprit.core.algorithm.SearchStrategy.DiscoveredSolution;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;


/**
 * Terminates algorithm prematurely based on specified time.
 * 
 * <p>Note, TimeTermination must be registered as AlgorithmListener <br>
 * TimeTermination will be activated by:<br>
 *
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 * <code>algorithm.addListener(this);</code>
 * 
 * @author stefan schroeder
 *
 */
public class TimeTermination implements PrematureAlgorithmTermination, AlgorithmStartsListener{

	public static interface TimeGetter {

        public long getCurrentTime();

    }

    private static Logger logger = LogManager.getLogger(TimeTermination.class);
	
	private final long timeThreshold;

    private TimeGetter timeGetter = new TimeGetter() {

        @Override
        public long getCurrentTime() {
            return System.currentTimeMillis();
        }

    };
	
	private long startTime;
	
	/**
	 * Constructs TimeTermination that terminates algorithm prematurely based on specified time.
	 *
     * @param timeThreshold_in_milliseconds the computation time [in ms] after which the algorithm terminates
     */
	public TimeTermination(long timeThreshold_in_milliseconds) {
		super();
		this.timeThreshold = timeThreshold_in_milliseconds;
		logger.debug("initialise {}", this);
	}

    public void setTimeGetter(TimeGetter timeGetter) {
        this.timeGetter = timeGetter;
    }

    @Override
	public String toString() {
		return "[name=TimeTermination][timeThreshold="+timeThreshold+" ms]";
	}

	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		return ( now() - startTime )  > timeThreshold ;
	}

    void start(long startTime){
        this.startTime = startTime;
    }

    private long now(){
        return timeGetter.getCurrentTime();
    }
	
	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		start(timeGetter.getCurrentTime());
	}

}
