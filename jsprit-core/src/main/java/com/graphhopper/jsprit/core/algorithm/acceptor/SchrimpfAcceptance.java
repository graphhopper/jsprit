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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * ThresholdAcceptance-Function defined by Schrimpf et al. (2000).
 * <p>
 * <p>The <b>idea</b> can be described as follows: Most problems do not only have one unique minimum (maximum) but
 * a number of local minima (maxima). To avoid to get stuck in a local minimum at the beginning of a search
 * this threshold-acceptance function accepts also worse solution at the beginning (in contrary to a greedy
 * approach which only accepts better solutions), and converges to a greedy approach at the end. <br>
 * The difficulty is to define (i) an appropriate initial threshold and (ii) a corresponding function describing
 * how the threshold converges to zero, i.e. the greedy threshold.
 * <p>
 * <p>ad i) The initial threshold is determined by a random walk through the search space.
 * The random walk currently runs with the following algorithm: src/main/resources/randomWalk.xml. It runs
 * as long as it is specified in nuOfWarmupIterations. In the first iteration or walk respectively the algorithm generates a solution.
 * This solution in turn is the basis of the next walk yielding to another solution value ... and so on.
 * Each solution value is memorized since the initial threshold is essentially a function of the standard deviation of these solution values.
 * To be more precise: initial threshold = stddev(solution values) / 2.
 * <p>
 * <p>ad ii) The threshold of iteration i is determined as follows:
 * threshold(i) = initialThreshold * Math.exp(-Math.log(2) * (i / nuOfTotalIterations) / alpha)
 * To get a better understanding of the threshold-function go to Wolfram Alpha and plot the following line
 * (just copy and paste it into Wolfram's console: <a href="https://www.wolframalpha.com/">www.wolframalpha.com</a>):
 * <p>100. * exp(-log(2)* (x/1000) / 0.1) (x from 0 to 1000) (y from 0 to 100)
 * <p>with <br>
 * initialThreshold = 100<br>
 * nuOfTotalIter = 1000<br>
 * alpha = 0.1<br>
 * x corresponds to i iterations and<br>
 * y to the threshold(i)
 * <p>
 * <p>Gerhard Schrimpf, Johannes Schneider, Hermann Stamm- Wilbrandt, and Gunter Dueck (2000).
 * Record breaking optimization results using the ruin and recreate principle.
 * Journal of Computational Physics, 159(2):139 – 171, 2000. ISSN 0021-9991. doi: 10.1006/jcph.1999. 6413.
 * URL http://www.sciencedirect.com/science/article/ pii/S0021999199964136
 *
 * @author schroeder
 */
public class SchrimpfAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener {

    private static Logger logger = LoggerFactory.getLogger(SchrimpfAcceptance.class.getName());

    private final double alpha;

    private int maxIterations = 1000;

    private int currentIteration = 0;

    private double initialThreshold = 0.0;

    private final int solutionMemory;

    public SchrimpfAcceptance(int solutionMemory, double alpha) {
        this.alpha = alpha;
        this.solutionMemory = solutionMemory;
        logger.debug("initialise {}", this);
    }

    @Override
    public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
        boolean solutionAccepted = false;
        if (solutions.size() < solutionMemory) {
            solutions.add(newSolution);
            solutionAccepted = true;
        } else {
            VehicleRoutingProblemSolution worst = null;
            double threshold = getThreshold(currentIteration);
            for (VehicleRoutingProblemSolution solutionInMemory : solutions) {
                if (worst == null) worst = solutionInMemory;
                else if (solutionInMemory.getCost() > worst.getCost()) worst = solutionInMemory;
            }
            if (worst == null) {
                solutions.add(newSolution);
                solutionAccepted = true;
            } else if (newSolution.getCost() < worst.getCost() + threshold) {
                solutions.remove(worst);
                solutions.add(newSolution);
                solutionAccepted = true;
            }
        }
        return solutionAccepted;
    }

    public boolean acceptSolution(VehicleRoutingProblemSolution solution, VehicleRoutingProblemSolution newSolution) {
        List<VehicleRoutingProblemSolution> solutions = new ArrayList<>();
        solutions.add(solution);
        boolean solutionAccepted = false;
        if (solutions.size() < solutionMemory) {
            solutions.add(newSolution);
            solutionAccepted = true;
        } else {
            VehicleRoutingProblemSolution worst = null;
            double threshold = getThreshold(currentIteration);
            for (VehicleRoutingProblemSolution solutionInMemory : solutions) {
                if (worst == null) worst = solutionInMemory;
                else if (solutionInMemory.getCost() > worst.getCost()) worst = solutionInMemory;
            }
            if (worst == null) {
                solutions.add(newSolution);
                solutionAccepted = true;
            } else if (newSolution.getCost() < worst.getCost() + threshold) {
                solutions.remove(worst);
                solutions.add(newSolution);
                solutionAccepted = true;
            }
        }
        return solutionAccepted;
    }

    @Override
    public String toString() {
        return "[name=SchrimpfAcceptance][alpha=" + alpha + "]";
    }

    private double getThreshold(int iteration) {
        double scheduleVariable = (double) iteration / (double) maxIterations;
        return initialThreshold * Math.exp(-1. * Math.log(2) * scheduleVariable / alpha);
    }


    @SuppressWarnings("UnusedDeclaration")
    public double getInitialThreshold() {
        return initialThreshold;
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

    public void setMaxIterations(int maxIteration) {
        this.maxIterations = maxIteration;
    }

    public void incIteration() {
        currentIteration++;
    }

    ;

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
