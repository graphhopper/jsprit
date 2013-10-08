package algorithms.states;

import java.util.Collection;

import algorithms.RouteActivityVisitor;
import algorithms.StateManagerImpl;
import algorithms.StateManagerImpl.StateImpl;
import algorithms.StateTypes;
import basics.Job;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

public class UpdateCostsAtRouteLevel implements JobInsertedListener, InsertionStartsListener, InsertionEndsListener{
		
		private StateManagerImpl states;
		
		private VehicleRoutingTransportCosts tpCosts;
		
		private VehicleRoutingActivityCosts actCosts;
		
		public UpdateCostsAtRouteLevel(StateManagerImpl states, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts actCosts) {
			super();
			this.states = states;
			this.tpCosts = tpCosts;
			this.actCosts = actCosts;
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//			inRoute.getVehicleRouteCostCalculator().addTransportCost(additionalCosts);
			double oldCosts = states.getRouteState(inRoute, StateTypes.COSTS).toDouble();
			oldCosts += additionalCosts;
			states.putRouteState(inRoute, StateTypes.COSTS, new StateImpl(oldCosts));
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
				route.getVehicleRouteCostCalculator().addOtherCost(states.getRouteState(route, StateTypes.COSTS).toDouble());
				route.getVehicleRouteCostCalculator().price(route.getVehicle());
//				forwardInTime.iterate(route);
			}
			
		}

	}