package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;

public class RadialRuinStrategyFactory implements RuinStrategyFactory{

	private double fraction;
	
	private JobDistance jobDistance;
	
	public RadialRuinStrategyFactory(double fraction, JobDistance jobDistance) {
		super();
		this.fraction = fraction;
		this.jobDistance = jobDistance;
	}

	@Override
	public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
		return new RuinRadial(vrp,fraction,jobDistance);
	}

}
