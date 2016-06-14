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
