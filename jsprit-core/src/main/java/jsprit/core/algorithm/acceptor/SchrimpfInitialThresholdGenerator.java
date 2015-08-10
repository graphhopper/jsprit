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
package jsprit.core.algorithm.acceptor;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.AlgorithmConfigXmlReader;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.listener.AlgorithmStartsListener;
import jsprit.core.algorithm.listener.IterationEndsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Resource;
import jsprit.core.util.Solutions;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.Collection;

public class SchrimpfInitialThresholdGenerator implements AlgorithmStartsListener {
	
	private static Logger logger = LogManager.getLogger(SchrimpfInitialThresholdGenerator.class.getName());
	
	private SchrimpfAcceptance schrimpfAcceptance;
	
	private int nOfRandomWalks;
	
	public SchrimpfInitialThresholdGenerator(SchrimpfAcceptance schrimpfAcceptance, int nOfRandomWalks) {
		super();
		this.schrimpfAcceptance = schrimpfAcceptance;
		this.nOfRandomWalks = nOfRandomWalks;
	}

	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		logger.info("prepare schrimpfAcceptanceFunction, i.e. determine initial threshold");
		double now = System.currentTimeMillis();
		
		/*
		 * randomWalk to determine standardDev
		 */
		final double[] results = new double[nOfRandomWalks];
		
		URL resource = Resource.getAsURL("randomWalk.xml");
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(problem, algorithmConfig);
		vra.setMaxIterations(nOfRandomWalks);
		vra.getAlgorithmListeners().addListener(new IterationEndsListener() {
			
			@Override
			public void informIterationEnds(int iteration, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
				double result = Solutions.bestOf(solutions).getCost();
//				logger.info("result={}", result);
				results[iteration-1] = result;
			}
			
		});
		vra.searchSolutions();
		
		StandardDeviation dev = new StandardDeviation();
		double standardDeviation = dev.evaluate(results);
		double initialThreshold = standardDeviation / 2;
		
		schrimpfAcceptance.setInitialThreshold(initialThreshold);

		logger.info("took {} seconds", ((System.currentTimeMillis()-now)/1000.0) );
		logger.debug("initial threshold: {}", initialThreshold);
		logger.info("---------------------------------------------------------------------");
	}

}
