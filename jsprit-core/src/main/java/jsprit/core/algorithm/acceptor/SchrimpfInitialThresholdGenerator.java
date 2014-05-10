package jsprit.core.algorithm.acceptor;

import java.net.URL;
import java.util.Collection;

import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

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

public class SchrimpfInitialThresholdGenerator implements AlgorithmStartsListener {
	
	private static Logger logger = Logger.getLogger(SchrimpfInitialThresholdGenerator.class);
	
	private SchrimpfAcceptance schrimpfAcceptance;
	
	private int nOfRandomWalks;
	
	public SchrimpfInitialThresholdGenerator(SchrimpfAcceptance schrimpfAcceptance, int nOfRandomWalks) {
		super();
		this.schrimpfAcceptance = schrimpfAcceptance;
		this.nOfRandomWalks = nOfRandomWalks;
	}

	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		logger.info("---------------------------------------------------------------------");
		logger.info("prepare schrimpfAcceptanceFunction, i.e. determine initial threshold");
		logger.info("start random-walk (see randomWalk.xml)");
		double now = System.currentTimeMillis();
		
		/*
		 * randomWalk to determine standardDev
		 */
		final double[] results = new double[nOfRandomWalks];
		
		URL resource = Resource.getAsURL("randomWalk.xml");
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.createAlgorithm(problem, algorithmConfig);
		vra.setNuOfIterations(nOfRandomWalks);
		vra.getAlgorithmListeners().addListener(new IterationEndsListener() {
			
			@Override
			public void informIterationEnds(int iteration, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
				double result = Solutions.bestOf(solutions).getCost();
//				logger.info("result="+result);
				results[iteration-1] = result;
			}
			
		});
		vra.searchSolutions();
		
		StandardDeviation dev = new StandardDeviation();
		double standardDeviation = dev.evaluate(results);
		double initialThreshold = standardDeviation / 2;
		
		schrimpfAcceptance.setInitialThreshold(initialThreshold);
		
		logger.info("warmup done");
		logger.info("total time: " + ((System.currentTimeMillis()-now)/1000.0) + "s");
		logger.info("initial threshold: " + initialThreshold);
		logger.info("---------------------------------------------------------------------");
	}

}
