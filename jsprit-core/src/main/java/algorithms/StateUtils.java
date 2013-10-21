package algorithms;

import basics.VehicleRoutingProblem;

public class StateUtils {
	
	public static void addCoreStateUpdaters(VehicleRoutingProblem vrp, StateManager stateManager){
		stateManager.addListener(new InitializeLoadsAtStartAndEndOfRouteWhenInsertionStarts(stateManager));
		stateManager.addListener(new UpdateLoadsAtStartAndEndOfRouteWhenJobHasBeenInserted(stateManager));
	
		stateManager.addActivityVisitor(new UpdateActivityTimes(vrp.getTransportCosts()));
		stateManager.addActivityVisitor(new UpdateLoadAtActivityLevel(stateManager));
		
		stateManager.addActivityVisitor(new UpdateCostsAtAllLevels(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));
		
		stateManager.addActivityVisitor(new UpdateOccuredDeliveriesAtActivityLevel(stateManager));
		stateManager.addActivityVisitor(new UpdateLatestOperationStartTimeAtActLocations(stateManager, vrp.getTransportCosts()));
		stateManager.addActivityVisitor(new UpdateFuturePickupsAtActivityLevel(stateManager));
		
	}

}
