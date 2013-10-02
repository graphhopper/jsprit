package algorithms;

import basics.VehicleRoutingProblem;

public class RandomRuinStrategyFactory implements RuinStrategyFactory{

	private double fraction;
	
	public RandomRuinStrategyFactory(double fraction) {
		super();
		this.fraction = fraction;
	}

	@Override
	public RuinStrategy createStrategy(VehicleRoutingProblem vrp) {
		return new RuinRandom(vrp, fraction);
	}

}
