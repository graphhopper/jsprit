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
package com.graphhopper.jsprit.core.algorithm.termination;

import com.graphhopper.jsprit.core.algorithm.SearchStrategy;
import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * Terminates algorithm prematurely based on specified time.
 * <p>
 * <p>Note, TimeTermination must be registered as AlgorithmListener <br>
 * TimeTermination will be activated by:<br>
 * <p>
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 * <code>algorithm.addListener(this);</code>
 *
 * @author stefan schroeder
 */
public class TimeTermination implements PrematureAlgorithmTermination, AlgorithmStartsListener {

    public static interface TimeGetter {

        public long getCurrentTime();

    }

    private static Logger logger = LoggerFactory.getLogger(TimeTermination.class);

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
        return "[name=TimeTermination][timeThreshold=" + timeThreshold + " ms]";
    }

    @Override
    public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
        return (now() - startTime) > timeThreshold;
    }

    void start(long startTime) {
        this.startTime = startTime;
    }

    private long now() {
        return timeGetter.getCurrentTime();
    }

    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        start(timeGetter.getCurrentTime());
    }

}
