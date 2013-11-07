package algorithms;

import basics.VehicleRoutingProblem;

public class BestInsertionStrategyFactory implements InsertionStrategyFactory{

	private JobInsertionCostsCalculator jobInsertionCalculator;
	
	public BestInsertionStrategyFactory(JobInsertionCostsCalculator jobInsertionCalculator) {
		super();
		this.jobInsertionCalculator = jobInsertionCalculator;
	}

	@Override
	public InsertionStrategy createStrategy(VehicleRoutingProblem vrp) {
		return new BestInsertion(jobInsertionCalculator);
	}

}
