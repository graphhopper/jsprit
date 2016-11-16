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
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.util.Solutions;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class SchrimpfInitialThresholdGenerator implements AlgorithmStartsListener {

    private static Logger logger = LoggerFactory.getLogger(SchrimpfInitialThresholdGenerator.class.getName());

    private SchrimpfAcceptance schrimpfAcceptance;

    private int nOfRandomWalks;

    public SchrimpfInitialThresholdGenerator(SchrimpfAcceptance schrimpfAcceptance, int nOfRandomWalks) {
        super();
        this.schrimpfAcceptance = schrimpfAcceptance;
        this.nOfRandomWalks = nOfRandomWalks;
    }

    @Override
    public void informAlgorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
        logger.info("prepare schrimpfAcceptanceFunction, i.e. determine initial threshold");
        double now = System.currentTimeMillis();

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
        double initialThreshold = standardDeviation / 2;

        schrimpfAcceptance.setInitialThreshold(initialThreshold);

        logger.info("took {} seconds", ((System.currentTimeMillis() - now) / 1000.0));
        logger.debug("initial threshold: {}", initialThreshold);
        logger.info("---------------------------------------------------------------------");
    }

}
