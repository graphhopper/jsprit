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
package com.graphhopper.jsprit.analysis.toolbox;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


public class StopWatch implements AlgorithmStartsListener, AlgorithmEndsListener {

    private static Logger log = LoggerFactory.getLogger(StopWatch.class);

    private double ran;

    private double startTime;


    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        reset();
        start();
    }

    public double getCompTimeInSeconds() {
        return (ran) / 1000.0;
    }

    @Override
    public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        stop();
        log.info("computation time [in sec]: {}", getCompTimeInSeconds());
    }

    public void stop() {
        ran += System.currentTimeMillis() - startTime;
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void reset() {
        startTime = 0;
        ran = 0;
    }

    @Override
    public String toString() {
        return "stopWatch: " + getCompTimeInSeconds() + " sec";
    }

    public double getCurrTimeInSeconds() {
        return (System.currentTimeMillis() - startTime) / 1000.0;
    }

}
