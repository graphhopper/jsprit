package basics.algo;

import java.util.Collection;

import org.apache.commons.math.stat.StatUtils;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.StandardDeviation;
import org.apache.log4j.Logger;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategy.DiscoveredSolution;

public class TimeBreaker implements PrematureAlgorithmBreaker, AlgorithmStartsListener{

	private static Logger logger = Logger.getLogger(TimeBreaker.class);
	
	private double timeThreshold;
	
	private double startTime;
	
	public TimeBreaker(double time) {
		super();
		this.timeThreshold = time;
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=TimeBreaker][timeThreshold="+timeThreshold+"]";
	}

	@Override
	public boolean isPrematureBreak(DiscoveredSolution discoveredSolution) {
		if((System.currentTimeMillis() - startTime)/1000.0 > timeThreshold) return true;
		return false;
	}
	
	@Override
	public void informAlgorithmStarts(VehicleRoutingProblem problem,VehicleRoutingAlgorithm algorithm,Collection<VehicleRoutingProblemSolution> solutions) {
		startTime = System.currentTimeMillis();
	}

}
