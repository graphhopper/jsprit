/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.Collection;

import basics.Job;
import basics.algo.JobInsertedListener;
import basics.algo.RuinListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;


class StateUpdates {
	
//	static class UpdateCostsAtRouteLevel implements JobInsertedListener, InsertionStartsListener, InsertionEndsListener{
//		
//		private StateManagerImpl states;
//		
//		private VehicleRoutingTransportCosts tpCosts;
//		
//		private VehicleRoutingActivityCosts actCosts;
//		
//		public UpdateCostsAtRouteLevel(StateManagerImpl states, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts actCosts) {
//			super();
//			this.states = states;
//			this.tpCosts = tpCosts;
//			this.actCosts = actCosts;
//		}
//
//		@Override
//		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
////			inRoute.getVehicleRouteCostCalculator().addTransportCost(additionalCosts);
//			double oldCosts = states.getRouteState(inRoute, StateTypes.COSTS).toDouble();
//			oldCosts += additionalCosts;
//			states.putRouteState(inRoute, StateTypes.COSTS, new StateImpl(oldCosts));
//		}
//
//		@Override
//		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
//			RouteActivityVisitor forwardInTime = new RouteActivityVisitor();
//			forwardInTime.addActivityVisitor(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
//			for(VehicleRoute route : vehicleRoutes){
//				forwardInTime.visit(route);
//			}
//			
//		}
//
//		@Override
//		public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
//			
////			IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(tpCosts);
////			forwardInTime.addListener(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
////			for(VehicleRoute route : vehicleRoutes){
////				if(route.isEmpty()) continue;
////				route.getVehicleRouteCostCalculator().reset();
////				route.getVehicleRouteCostCalculator().addOtherCost(states.getRouteState(route, StateTypes.COSTS).toDouble());
////				route.getVehicleRouteCostCalculator().price(route.getVehicle());
////				forwardInTime.iterate(route);
////			}
//			
//		}
//
//	}
//
//	static class UpdateActivityTimes implements ActivityVisitor{
//
//		private Logger log = Logger.getLogger(UpdateActivityTimes.class);
//		
//		private ActivityTimeTracker timeTracker;
//		
//		private VehicleRoute route;
//		
//		public UpdateActivityTimes(ForwardTransportTime transportTime) {
//			super();
//			timeTracker = new ActivityTimeTracker(transportTime);
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			timeTracker.begin(route);
//			this.route = route;
//			route.getStart().setEndTime(timeTracker.getActEndTime());
//		}
//
//		@Override
//		public void visit(TourActivity activity) {
//			timeTracker.visit(activity);
//			activity.setArrTime(timeTracker.getActArrTime());
//			activity.setEndTime(timeTracker.getActEndTime());
//		}
//
//		@Override
//		public void finish() {
//			timeTracker.finish();
//			route.getEnd().setArrTime(timeTracker.getActArrTime());
//		}
//
//	}
//
//	static class UpdateCostsAtAllLevels implements ActivityVisitor{
//
//		private static Logger log = Logger.getLogger(UpdateCostsAtAllLevels.class);
//		
//		private VehicleRoutingActivityCosts activityCost;
//
//		private ForwardTransportCost transportCost;
//		
//		private StateManagerImpl states;
//		
//		private double totalOperationCost = 0.0;
//		
//		private VehicleRoute vehicleRoute = null;
//		
//		private TourActivity prevAct = null;
//		
//		private double startTimeAtPrevAct = 0.0;
//		
//		private ActivityTimeTracker timeTracker;
//		
//		public UpdateCostsAtAllLevels(VehicleRoutingActivityCosts activityCost, VehicleRoutingTransportCosts transportCost, StateManagerImpl states) {
//			super();
//			this.activityCost = activityCost;
//			this.transportCost = transportCost;
//			this.states = states;
//			timeTracker = new ActivityTimeTracker(transportCost);
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			vehicleRoute = route;
//			vehicleRoute.getVehicleRouteCostCalculator().reset();
//			timeTracker.begin(route);
//			prevAct = route.getStart();
//			startTimeAtPrevAct = timeTracker.getActEndTime();
//		}
//
//		@Override
//		public void visit(TourActivity act) {
//			timeTracker.visit(act);
//			
//			double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), act.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
//			double actCost = activityCost.getActivityCost(act, timeTracker.getActArrTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());
//
////			vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
////			vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
//			
//			totalOperationCost += transportCost;
//			totalOperationCost += actCost;
//
//			states.putActivityState(act, StateTypes.COSTS, new StateImpl(totalOperationCost));
//
//			prevAct = act;
//			startTimeAtPrevAct = timeTracker.getActEndTime();
//		}
//
//		@Override
//		public void finish() {
//			timeTracker.finish();
//			double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), vehicleRoute.getEnd().getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
//			double actCost = activityCost.getActivityCost(vehicleRoute.getEnd(), timeTracker.getActEndTime(), vehicleRoute.getDriver(), vehicleRoute.getVehicle());
//			
////			vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
////			vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
//			
//			totalOperationCost += transportCost;
//			totalOperationCost += actCost;
//			totalOperationCost += getFixCosts();
//			
//			states.putRouteState(vehicleRoute, StateTypes.COSTS, new StateImpl(totalOperationCost));
//			
//			//this is rather strange and likely to change
////			vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getDriver());
////			vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getVehicle());
////			vehicleRoute.getVehicleRouteCostCalculator().finish();
//			
//			startTimeAtPrevAct = 0.0;
//			prevAct = null;
//			vehicleRoute = null;
//			totalOperationCost = 0.0;
//		}
//
//		private double getFixCosts() {
//			Vehicle vehicle = vehicleRoute.getVehicle();
//			if(vehicle == null) return 0.0;
//			VehicleType type = vehicle.getType();
//			if(type == null) return 0.0;
//			return type.getVehicleCostParams().fix;
//		}
//
//	}
//
//	static class UpdateEarliestStartTimeWindowAtActLocations implements ActivityVisitor{
//
//		private StateManagerImpl states;
//		
//		private ActivityTimeTracker timeTracker;
//		
//		public UpdateEarliestStartTimeWindowAtActLocations(StateManagerImpl states, VehicleRoutingTransportCosts transportCosts) {
//			super();
//			this.states = states;
//			timeTracker = new ActivityTimeTracker(transportCosts);
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			timeTracker.begin(route);
//		}
//
//		@Override
//		public void visit(TourActivity activity) {
//			timeTracker.visit(activity);
//			states.putActivityState(activity, StateTypes.EARLIEST_OPERATION_START_TIME, new StateImpl(Math.max(timeTracker.getActArrTime(), activity.getTheoreticalEarliestOperationStartTime())));
//			
//		}
//
//		@Override
//		public void finish() {}
//
//	}
//
//	static class UpdateLatestOperationStartTimeAtActLocations implements ReverseActivityVisitor{
//
//		private static Logger log = Logger.getLogger(UpdateLatestOperationStartTimeAtActLocations.class);
//		
//		private StateManagerImpl states;
//		
//		private VehicleRoute route;
//		
//		private VehicleRoutingTransportCosts transportCosts;
//		
//		private double latestArrTimeAtPrevAct;
//		
//		private TourActivity prevAct;
//		
//		public UpdateLatestOperationStartTimeAtActLocations(StateManagerImpl states, VehicleRoutingTransportCosts tpCosts) {
//			super();
//			this.states = states;
//			this.transportCosts = tpCosts;
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			this.route = route;
//			latestArrTimeAtPrevAct = route.getEnd().getTheoreticalLatestOperationStartTime();
//			prevAct = route.getEnd();
//		}
//
//		@Override
//		public void visit(TourActivity activity) {
//			double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transportCosts.getBackwardTransportTime(activity.getLocationId(), prevAct.getLocationId(), latestArrTimeAtPrevAct, route.getDriver(),route.getVehicle()) - activity.getOperationTime();
//			double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
//			
//			states.putActivityState(activity, StateTypes.LATEST_OPERATION_START_TIME, new StateImpl(latestArrivalTime));
//			
//			latestArrTimeAtPrevAct = latestArrivalTime;
//			prevAct = activity;
//		}
//
//		@Override
//		public void finish() {}
//	}
//
//	static class UpdateLoadAtAllLevels implements ActivityVisitor{
//
//		private double load = 0.0;
//		
//		private StateManagerImpl states;
//		
//		private VehicleRoute vehicleRoute;
//		
//		public UpdateLoadAtAllLevels(StateManagerImpl states) {
//			super();
//			this.states = states;
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			vehicleRoute = route;
//		}
//
//		@Override
//		public void visit(TourActivity activity) {
//			load += (double)activity.getCapacityDemand();
//			states.putActivityState(activity, StateTypes.LOAD, new StateImpl(load));
//		}
//
//		@Override
//		public void finish() {
//			states.putRouteState(vehicleRoute, StateTypes.LOAD, new StateImpl(load));
//			load=0;
//			vehicleRoute = null;
//		}
//
//	}
//
//	static class UpdateLoadAtRouteLevel implements JobInsertedListener, InsertionStartsListener{
//
//		private StateManagerImpl states;
//		
//		public UpdateLoadAtRouteLevel(StateManagerImpl states) {
//			super();
//			this.states = states;
//		}
//
//		@Override
//		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
//			if(!(job2insert instanceof Service)){
//				return;
//			}
//			double oldLoad = states.getRouteState(inRoute, StateTypes.LOAD).toDouble();
//			states.putRouteState(inRoute, StateTypes.LOAD, new StateImpl(oldLoad + job2insert.getCapacityDemand()));
//		}
//
//		@Override
//		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
//			for(VehicleRoute route : vehicleRoutes){
//				int load = 0;
//				for(Job j : route.getTourActivities().getJobs()){
//					load += j.getCapacityDemand();
//				}
//				states.putRouteState(route, StateTypes.LOAD, new StateImpl(load));
//			}
//			
//		}
//
//	}
//
	static class UpdateStates implements JobInsertedListener, RuinListener{

		private RouteActivityVisitor routeActivityVisitor;
		
		private ReverseRouteActivityVisitor revRouteActivityVisitor;
		
		public UpdateStates(StateManagerImpl states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
			routeActivityVisitor = new RouteActivityVisitor();
			routeActivityVisitor.addActivityVisitor(new UpdateActivityTimes(routingCosts));
			routeActivityVisitor.addActivityVisitor(new UpdateCostsAtAllLevels(activityCosts, routingCosts, states));
			routeActivityVisitor.addActivityVisitor(new UpdateLoadAtAllLevels(states));
			
			revRouteActivityVisitor = new ReverseRouteActivityVisitor();
			revRouteActivityVisitor.addActivityVisitor(new UpdateLatestOperationStartTimeAtActLocations(states, routingCosts));
		
		}
		
		public void update(VehicleRoute route){
			routeActivityVisitor.visit(route);
			revRouteActivityVisitor.visit(route);
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			routeActivityVisitor.visit(inRoute);
			revRouteActivityVisitor.visit(inRoute);
		}

		@Override
		public void ruinStarts(Collection<VehicleRoute> routes) {}

		@Override
		public void ruinEnds(Collection<VehicleRoute> routes,Collection<Job> unassignedJobs) {
			for(VehicleRoute route : routes) {
				routeActivityVisitor.visit(route);
				revRouteActivityVisitor.visit(route);
			}
		}

		@Override
		public void removed(Job job, VehicleRoute fromRoute) {}

	}
//
//	static class UpdateFuturePickupsAtActivityLevel implements ReverseActivityVisitor {
//		private StateManagerImpl stateManager;
//		private int futurePicks = 0;
//		private VehicleRoute route;
//		
//		public UpdateFuturePickupsAtActivityLevel(StateManagerImpl stateManager) {
//			super();
//			this.stateManager = stateManager;
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			this.route = route;
//		}
//
//		@Override
//		public void visit(TourActivity act) {
//			stateManager.putActivityState(act, StateTypes.FUTURE_PICKS, new StateImpl(futurePicks));
//			if(act instanceof PickupActivity || act instanceof ServiceActivity){
//				futurePicks += act.getCapacityDemand();
//			}
//			assert futurePicks <= route.getVehicle().getCapacity() : "sum of pickups must not be > vehicleCap";
//			assert futurePicks >= 0 : "sum of pickups must not < 0";
//		}
//
//		@Override
//		public void finish() {
//			futurePicks = 0;
//			route = null;
//		}
//	}
//
//	static class UpdateOccuredDeliveriesAtActivityLevel implements ActivityVisitor {
//		private StateManagerImpl stateManager;
//		private int deliveries = 0; 
//		private VehicleRoute route;
//		
//		public UpdateOccuredDeliveriesAtActivityLevel(StateManagerImpl stateManager) {
//			super();
//			this.stateManager = stateManager;
//		}
//
//		@Override
//		public void begin(VehicleRoute route) {
//			this.route = route; 
//		}
//
//		@Override
//		public void visit(TourActivity act) {
//			if(act instanceof DeliveryActivity){
//				deliveries += Math.abs(act.getCapacityDemand());
//			}
//			stateManager.putActivityState(act, StateTypes.PAST_DELIVERIES, new StateImpl(deliveries));
//			assert deliveries >= 0 : "deliveries < 0";
//			assert deliveries <= route.getVehicle().getCapacity() : "deliveries > vehicleCap";
//		}
//
//		@Override
//		public void finish() {
//			deliveries = 0;
//			route = null;
//		}
//	}
//	
//	/**
//	 * Updates load at activity level. Note that this assumed that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
//	 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
//	 *  
//	 * @author stefan
//	 *
//	 */
//	static class UpdateLoadAtActivityLevel implements ActivityVisitor {
//		private StateManagerImpl stateManager;
//		private int currentLoad = 0;
//		private VehicleRoute route;
//		
//		public UpdateLoadAtActivityLevel(StateManagerImpl stateManager) {
//			super();
//			this.stateManager = stateManager;
//		}
//		
//		@Override
//		public void begin(VehicleRoute route) {
//			currentLoad = (int) stateManager.getRouteState(route, StateTypes.LOAD_AT_DEPOT).toDouble();
//			this.route = route;
//		}
//
//		@Override
//		public void visit(TourActivity act) {
//			currentLoad += act.getCapacityDemand();
//			stateManager.putActivityState(act, StateTypes.LOAD, new StateImpl(currentLoad));
//			assert currentLoad <= route.getVehicle().getCapacity() : "currentLoad at activity must not be > vehicleCapacity";
//			assert currentLoad >= 0 : "currentLoad at act must not be < 0";
//		}
//
//		@Override
//		public void finish() {
//			currentLoad = 0;
//		}
//	}
//	
//	static class ResetStateManager implements IterationStartsListener {
//
//		private StateManagerImpl stateManager;
//		
//		public ResetStateManager(StateManagerImpl stateManager) {
//			super();
//			this.stateManager = stateManager;
//		}
//
//		@Override
//		public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
//			stateManager.clear();
//		}
//	}
//	
//	static interface InsertionStarts {
//		
//		void insertionStarts(VehicleRoute route);
//		
//	}
//	
//	static class UpdateLoadsAtStartAndEndOfRouteWhenInsertionStarts implements InsertionStarts {
//
//		private StateManagerImpl stateManager;
//		
//		public UpdateLoadsAtStartAndEndOfRouteWhenInsertionStarts(StateManagerImpl stateManager) {
//			super();
//			this.stateManager = stateManager;
//		}
//
//		@Override
//		public void insertionStarts(VehicleRoute route) {
//			int loadAtDepot = 0;
//			int loadAtEnd = 0;
//			for(Job j : route.getTourActivities().getJobs()){
//				if(j instanceof Delivery){
//					loadAtDepot += j.getCapacityDemand();
//				}
//				else if(j instanceof Pickup || j instanceof Service){
//					loadAtEnd += j.getCapacityDemand();
//				}
//			}
//			stateManager.putRouteState(route, StateTypes.LOAD_AT_DEPOT, new StateImpl(loadAtDepot));
//			stateManager.putRouteState(route, StateTypes.LOAD, new StateImpl(loadAtEnd));
//		}
//		
//	}
//	

	
	
}
