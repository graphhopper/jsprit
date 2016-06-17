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
