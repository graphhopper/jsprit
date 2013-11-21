package algorithms;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.algo.SearchStrategyManager;
import basics.algo.VehicleRoutingAlgorithmFactory;
import basics.route.VehicleFleetManager;

public class VehicleRoutingAlgorithmFactoryImpl implements VehicleRoutingAlgorithmFactory{

	private SearchStrategyManager searchStrategyManager;
	
	private StateManager stateManager;

	private VehicleFleetManager fleetManager;
	
	public VehicleRoutingAlgorithmFactoryImpl(SearchStrategyManager searchStrategyManager,
			StateManager stateManager, VehicleFleetManager fleetManager) {
		super();
		this.searchStrategyManager = searchStrategyManager;
		this.stateManager = stateManager;
		this.fleetManager = fleetManager;
	}

	@Override
	public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp) {
		this.stateManager.addActivityVisitor(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), this.stateManager));
//<<<<<<< HEAD
//		this.stateManager.addActivityVisitor(new UpdateMaxLoad(this.stateManager));
		this.stateManager.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
//=======
////		this.stateManager.addActivityVisitor(new UpdateMaxLoad_(this.stateManager));
//>>>>>>> refs/heads/pickupAndDelivery
		VehicleRoutingAlgorithm algorithm = new VehicleRoutingAlgorithm(vrp, searchStrategyManager);
		algorithm.getAlgorithmListeners().addListener(stateManager);
		algorithm.getSearchStrategyManager().addSearchStrategyModuleListener(stateManager);
		algorithm.getSearchStrategyManager().addSearchStrategyModuleListener(new RemoveEmptyVehicles(fleetManager));
		return algorithm;
	}

}
