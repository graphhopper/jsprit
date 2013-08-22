package algorithms;

import java.util.Collection;

import util.RouteUtils;
import algorithms.RuinStrategy.RuinListener;
import basics.Job;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionListener;
import basics.algo.SearchStrategyModule;
import basics.algo.SearchStrategyModuleListener;

class RuinAndRecreateModule implements SearchStrategyModule{

	private InsertionStrategy insertion;
	
	private RuinStrategy ruin;
	
	private String moduleName;
	
	public RuinAndRecreateModule(String moduleName, InsertionStrategy insertion, RuinStrategy ruin) {
		super();
		this.insertion = insertion;
		this.ruin = ruin;
		this.moduleName = moduleName;
	}

	@Override
	public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
		Collection<Job> ruinedJobs = ruin.ruin(vrpSolution.getRoutes());
		insertion.insertJobs(vrpSolution.getRoutes(), ruinedJobs);
		double totalCost = RouteUtils.getTotalCost(vrpSolution.getRoutes());
		vrpSolution.setCost(totalCost);
		return vrpSolution;
	}

	@Override
	public String getName() {
		return moduleName;
	}

	@Override
	public void addModuleListener(SearchStrategyModuleListener moduleListener) {
		if(moduleListener instanceof InsertionListener){
			InsertionListener iListener = (InsertionListener) moduleListener; 
			if(!insertion.getListeners().contains(iListener)){
				insertion.addListener(iListener);
			}
		}
		if(moduleListener instanceof RuinListener){
			RuinListener rListener = (RuinListener) moduleListener;
			if(!ruin.getListeners().contains(rListener)){
				ruin.addListener(rListener);
			}
		}
		
	}

}
