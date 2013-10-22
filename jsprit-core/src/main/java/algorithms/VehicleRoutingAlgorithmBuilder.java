package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.algo.SearchStrategyManager;
import basics.algo.VehicleRoutingAlgorithmListener;

public class VehicleRoutingAlgorithmBuilder {

	private VehicleRoutingProblem vrp;
	
	private SearchStrategyManager searchStrategyManager;
	
	private StateManager stateManager;
	
	private Collection<VehicleRoutingAlgorithmListener> listeners = new ArrayList<VehicleRoutingAlgorithmListener>();
	
	public VehicleRoutingAlgorithmBuilder(VehicleRoutingProblem vrp, SearchStrategyManager searchStrategyManager, StateManager stateManager) {
		super();
		this.vrp = vrp;
		this.searchStrategyManager = searchStrategyManager;
		this.stateManager = stateManager;
	}
	
	public void addListener(VehicleRoutingAlgorithmListener listener){
		listeners.add(listener);
	}
	
	public VehicleRoutingAlgorithm build(){
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(vrp, searchStrategyManager);
		algorithm.getAlgorithmListeners().addListener(stateManager);
		algorithm.getSearchStrategyManager().addSearchStrategyModuleListener(stateManager);
		return algorithm;
	}
	
}
