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
package com.graphhopper.jsprit.core.algorithm.acceptor;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


/**
 * @author schroeder
 */
public class RecordToRecordTravelAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener {

    private static Logger logger = LoggerFactory.getLogger(RecordToRecordTravelAcceptance.class.getName());

    private final int solutionMemory = 1;

    private int maxIterations = 1000;

    private int currentIteration = 0;

    private double initialThreshold = 0.0167;

    private double endThreshold = 0;

    private double bestEver = Double.MAX_VALUE;

    public RecordToRecordTravelAcceptance() {
        logger.debug("initialise {}", this);
    }

    @Override
    public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
        boolean solutionAccepted = false;
        if (solutions.size() == 0) {
            solutions.add(newSolution);
            solutionAccepted = true;
        } else {
            double threshold = getThreshold();
            VehicleRoutingProblemSolution currentSolution = solutions.iterator().next();
            if ((newSolution.getCost() - bestEver) / newSolution.getCost() < threshold) {
                solutions.remove(currentSolution);
                solutions.add(newSolution);
                solutionAccepted = true;
            }
        }
        if (newSolution.getCost() < bestEver) bestEver = newSolution.getCost();
        return solutionAccepted;
    }

    private double getThreshold() {
        return initialThreshold - (initialThreshold - endThreshold) * currentIteration / maxIterations;
    }

    @Override
    public String toString() {
        return "[name=record-to-record-travel]";
    }


    @SuppressWarnings("UnusedDeclaration")
    public double getInitialThreshold() {
        return initialThreshold;
    }

    public double getEndThreshold() {
        return endThreshold;
    }

    /**
     * Sets initial threshold.
     * <p>Note that if initial threshold has been set, automatic generation of initial threshold is disabled.
     *
     * @param initialThreshold the initialThreshold to set
     */
    public void setInitialThreshold(double initialThreshold) {
        this.initialThreshold = initialThreshold;
    }

    public void setEndThreshold(double endThreshold) {
        this.endThreshold = endThreshold;
    }

    public void setMaxIterations(int maxIteration) {
        this.maxIterations = maxIteration;
    }


    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        reset();
        this.maxIterations = algorithm.getMaxIterations();
    }

    private void reset() {
        currentIteration = 0;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        currentIteration = i;
    }

}
