package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.algo.SearchStrategyManager;
import basics.algo.VehicleRoutingAlgorithmListener;
import basics.route.VehicleFleetManager;

public class VehicleRoutingAlgorithmBuilder {

	private VehicleRoutingProblem vrp;
	
	private SearchStrategyManager searchStrategyManager;
	
	private StateManager stateManager;
	
	private Collection<VehicleRoutingAlgorithmListener> listeners = new ArrayList<VehicleRoutingAlgorithmListener>();

	private VehicleFleetManager fleetManager;
	
	public VehicleRoutingAlgorithmBuilder(VehicleRoutingProblem vrp, SearchStrategyManager searchStrategyManager, StateManager stateManager, VehicleFleetManager vehicleFleetManager) {
		super();
		this.vrp = vrp;
		this.searchStrategyManager = searchStrategyManager;
		this.stateManager = stateManager;
		this.fleetManager = vehicleFleetManager;
	}
	
	public void addListener(VehicleRoutingAlgorithmListener listener){
		listeners.add(listener);
	}
	
	public VehicleRoutingAlgorithm build(){
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(vrp, searchStrategyManager);
		algorithm.getAlgorithmListeners().addListener(stateManager);
		algorithm.getSearchStrategyManager().addSearchStrategyModuleListener(stateManager);
		algorithm.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
		return algorithm;
	}
	
}
