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
import com.graphhopper.jsprit.core.algorithm.box.GreedySchrimpfFactory;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.listener.AlgorithmStartsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationEndsListener;
import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;


public class ExperimentalSchrimpfAcceptance implements SolutionAcceptor, IterationStartsListener, AlgorithmStartsListener {

    final static Logger logger = LoggerFactory.getLogger(ExperimentalSchrimpfAcceptance.class.getName());

    private final double alpha;

    private int nOfTotalIterations = 1000;

    private int currentIteration = 0;

    private double initialThreshold = 0.0;

    private final int nOfRandomWalks;

    private final int solutionMemory;


    public ExperimentalSchrimpfAcceptance(int solutionMemory, double alpha, int nOfWarmupIterations) {
        super();
        this.alpha = alpha;
        this.nOfRandomWalks = nOfWarmupIterations;
        this.solutionMemory = solutionMemory;
        logger.info("initialise {}", this);
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
            if (newSolution.getRoutes().size() < worst.getRoutes().size()) {
                solutions.remove(worst);
                solutions.add(newSolution);
                solutionAccepted = true;
            } else if (newSolution.getRoutes().size() == worst.getRoutes().size() && newSolution.getCost() < worst.getCost() + threshold) {
                solutions.remove(worst);
                solutions.add(newSolution);
                solutionAccepted = true;
            }
        }
        return solutionAccepted;
    }

    @Override
    public String toString() {
        return "[name=schrimpfAcceptanceFunction][alpha=" + alpha + "][warmup=" + nOfRandomWalks + "]";
    }

    private double getThreshold(int iteration) {
        double scheduleVariable = (double) iteration / (double) nOfTotalIterations;
//		logger.debug("iter={} totalIter={} scheduling={}", iteration, nOfTotalIterations, scheduleVariable);
        double currentThreshold = initialThreshold * Math.exp(-Math.log(2) * scheduleVariable / alpha);
        return currentThreshold;
    }


    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        reset();
        logger.info("---------------------------------------------------------------------");
        logger.info("prepare schrimpfAcceptanceFunction, i.e. determine initial threshold");
        logger.info("start random-walk (see randomWalk.xml)");
        double now = System.currentTimeMillis();
        this.nOfTotalIterations = algorithm.getMaxIterations();

		/*
         * randomWalk to determine standardDev
		 */
        final double[] results = new double[nOfRandomWalks];

        Jsprit.Builder builder = new GreedySchrimpfFactory().createGreedyAlgorithmBuilder(problem);
        builder.setCustomAcceptor(new AcceptNewRemoveFirst(1));
        VehicleRoutingAlgorithm vra = builder.buildAlgorithm();
        vra.setMaxIterations(nOfRandomWalks);
        vra.getAlgorithmListeners().addListener(new IterationEndsListener() {

            @Override
            public void informIterationEnds(int iteration, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
                double result = Solutions.bestOf(solutions).getCost();
//				logger.info("result={}", result);
                results[iteration - 1] = result;
            }

        });
        vra.searchSolutions();

        StandardDeviation dev = new StandardDeviation();
        double standardDeviation = dev.evaluate(results);
        initialThreshold = standardDeviation / 2;

        logger.info("warmup done");
        logger.info("total time: {}s", ((System.currentTimeMillis() - now) / 1000.0));
        logger.info("initial threshold: {}", initialThreshold);
        logger.info("---------------------------------------------------------------------");

    }

    private void reset() {
        currentIteration = 0;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
        currentIteration = i;
    }

}
