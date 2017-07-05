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
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Terminates algorithm prematurely based on variationCoefficient (http://en.wikipedia.org/wiki/Coefficient_of_variation).
 * <p>
 * <p>Note, that this must be registered as AlgorithmListener <br>
 * It will be activated by:<br>
 * <p>
 * <code>algorithm.setPrematureAlgorithmTermination(this);</code><br>
 * <code>algorithm.addListener(this);</code>
 *
 * @author stefan schroeder
 */
public class VariationCoefficientTermination implements PrematureAlgorithmTermination, IterationStartsListener, AlgorithmStartsListener, IterationEndsListener {

    private final static Logger logger = LoggerFactory.getLogger(VariationCoefficientTermination.class);

    private final int noIterations;

    private final double variationCoefficientThreshold;

    private int currentIteration;

    private double[] solutionValues;

    private VehicleRoutingProblemSolution lastAccepted = null;

    /**
     * Constructs termination.
     *
     * @param noIterations                  size of the sample, i.e. number previous solutions values to take into account. If for example
     *                                      noIterations = 10 then every 10th iteration the variationCoefficient will be calculated with the
     *                                      last 10 solution values.
     * @param variationCoefficientThreshold the threshold used to terminate the algorithm. If the calculated variationCoefficient
     *                                      is smaller than the specified threshold, the algorithm terminates.
     */
    public VariationCoefficientTermination(int noIterations, double variationCoefficientThreshold) {
        super();
        this.noIterations = noIterations;
        this.variationCoefficientThreshold = variationCoefficientThreshold;
        solutionValues = new double[noIterations];
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=VariationCoefficientBreaker][variationCoefficientThreshold=" + variationCoefficientThreshold + "][iterations=" + noIterations + "]";
    }

    @Override
    public boolean isPrematureBreak(SearchStrategy.DiscoveredSolution discoveredSolution) {
        if (discoveredSolution.isAccepted()) {
            lastAccepted = discoveredSolution.getSolution();
            solutionValues[currentIteration] = discoveredSolution.getSolution().getCost();
        } else {
            if (lastAccepted != null) {
                solutionValues[currentIteration] = lastAccepted.getCost();
            } else solutionValues[currentIteration] = Integer.MAX_VALUE;
        }
        if (currentIteration == (noIterations - 1)) {
            double mean = StatUtils.mean(solutionValues);
            double stdDev = new StandardDeviation(true).evaluate(solutionValues, mean);
            double variationCoefficient = stdDev / mean;
            if (variationCoefficient < variationCoefficientThreshold) {
                return true;
            }
        }
        return false;
    }

    private void reset() {
        currentIteration = 0;
    }

    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        reset();
    }

    @Override
    public void informIterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (currentIteration == (noIterations - 1)) {
            reset();
        } else {
            currentIteration++;
        }
    }

    public void informIterationEnds(int i, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        informIterationEnds(i, problem, toList(solution));
    }

    private List<VehicleRoutingProblemSolution> toList(VehicleRoutingProblemSolution solution) {
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        solutions.add(solution);
        return solutions;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        if (lastAccepted == null) lastAccepted = Solutions.bestOf(solutions);
    }

    public void informIterationStarts(int i, VehicleRoutingProblem problem, VehicleRoutingProblemSolution solution) {
        informIterationStarts(i, problem, toList(solution));
    }
}
