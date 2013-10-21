package algorithms;

import java.util.Collection;

import algorithms.StateManager.StateImpl;
import basics.Job;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

class UpdateCostsAtRouteLevel implements StateUpdater,JobInsertedListener, InsertionStartsListener, InsertionEndsListener{
		
		private StateManager states;
		
		private VehicleRoutingTransportCosts tpCosts;
		
		private VehicleRoutingActivityCosts actCosts;
		
		public UpdateCostsAtRouteLevel(StateManager states, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts actCosts) {
			super();
			this.states = states;
			this.tpCosts = tpCosts;
			this.actCosts = actCosts;
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//			inRoute.getVehicleRouteCostCalculator().addTransportCost(additionalCosts);
			double oldCosts = states.getRouteState(inRoute, StateIdFactory.COSTS).toDouble();
			oldCosts += additionalCosts;
			states.putRouteState(inRoute, StateIdFactory.COSTS, new StateImpl(oldCosts));
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
			RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
			forwardInTime.addActivityVisitor(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
			for(VehicleRoute route : vehicleRoutes){
				forwardInTime.visit(route);
			}
			
		}

		@Override
		public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
			
//			IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(tpCosts);
//			forwardInTime.addListener(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
			for(VehicleRoute route : vehicleRoutes){
				if(route.isEmpty()) continue;
				route.getVehicleRouteCostCalculator().reset();
				route.getVehicleRouteCostCalculator().addOtherCost(states.getRouteState(route, StateIdFactory.COSTS).toDouble());
				route.getVehicleRouteCostCalculator().price(route.getVehicle());
//				forwardInTime.iterate(route);
			}
			
		}

	}