package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import algorithms.BackwardInTimeListeners.BackwardInTimeListener;
import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import algorithms.RuinStrategy.RuinListener;
import algorithms.StateManager.StateImpl;
import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionEndsListener;
import basics.algo.InsertionStartsListener;
import basics.algo.IterationEndsListener;
import basics.algo.IterationStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.VehicleRoutingAlgorithmListener;
import basics.costs.ForwardTransportCost;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.DeliveryActivity;
import basics.route.End;
import basics.route.PickupActivity;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class StateUpdates {
	
	static class VRAListenersManager implements IterationStartsListener, IterationEndsListener, InsertionStartsListener, InsertionEndsListener, JobInsertedListener, RuinListener{

		private Map<Class<? extends VehicleRoutingAlgorithmListener>,Collection<VehicleRoutingAlgorithmListener>> listeners = new HashMap<Class<? extends VehicleRoutingAlgorithmListener>,Collection<VehicleRoutingAlgorithmListener>>();
		
		public void addListener(VehicleRoutingAlgorithmListener vraListener){
			if(!listeners.containsKey(vraListener.getClass())){
				listeners.put(vraListener.getClass(), new ArrayList<VehicleRoutingAlgorithmListener>());
			}
			listeners.get(vraListener.getClass()).add(vraListener);
		}
		
		@Override
		public void ruinStarts(Collection<VehicleRoute> routes) {
			if(listeners.containsKey(RuinListener.class)){
				for(VehicleRoutingAlgorithmListener l : listeners.get(RuinListener.class)){
					((RuinListener)l).ruinStarts(routes);
				}
			}
		}

		@Override
		public void ruinEnds(Collection<VehicleRoute> routes,Collection<Job> unassignedJobs) {
			if(listeners.containsKey(RuinListener.class)){
				for(VehicleRoutingAlgorithmListener l : listeners.get(RuinListener.class)){
					((RuinListener)l).ruinEnds(routes,unassignedJobs);
				}
			}
		}

		@Override
		public void removed(Job job, VehicleRoute fromRoute) {
			if(listeners.containsKey(RuinListener.class)){
				for(VehicleRoutingAlgorithmListener l : listeners.get(RuinListener.class)){
					((RuinListener)l).removed(job, fromRoute);
				}
			}
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			if(listeners.containsKey(JobInsertedListener.class)){
				for(VehicleRoutingAlgorithmListener l : listeners.get(RuinListener.class)){
					((JobInsertedListener)l).informJobInserted(job2insert, inRoute, additionalCosts, additionalTime);
				}
			}
			
		}

		@Override
		public void informInsertionEnds(Collection<VehicleRoute> vehicleRoutes) {
//			if(listeners.containsKey(JobInsertedListener.class)){
//				for(VehicleRoutingAlgorithmListener l : listeners.get(RuinListener.class)){
//					((JobInsertedListener)l).informJobInserted(job2insert, inRoute, additionalCosts, additionalTime);
//				}
//			}
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void informIterationEnds(int i, VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void informIterationStarts(int i, VehicleRoutingProblem problem,Collection<VehicleRoutingProblemSolution> solutions) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	static class UpdateCostsAtRouteLevel implements JobInsertedListener, InsertionStartsListener, InsertionEndsListener{
		
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
			IterateRouteForwardInTime forwardInTime = new IterateRouteForwardInTime(tpCosts);
			forwardInTime.addListener(new UpdateCostsAtAllLevels(actCosts, tpCosts, states));
			for(VehicleRoute route : vehicleRoutes){
				forwardInTime.iterate(route);
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

	static class UpdateActivityTimes implements ForwardInTimeListener{

		private Logger log = Logger.getLogger(UpdateActivityTimes.class);
		
		@Override
		public void start(VehicleRoute route, Start start, double departureTime) {
			start.setEndTime(departureTime);
		}

		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
			act.setArrTime(arrTime);
			act.setEndTime(endTime);
		}

		@Override
		public void end(End end, double arrivalTime) {
			end.setArrTime(arrivalTime);
		}

	}

	static class UpdateCostsAtAllLevels implements ForwardInTimeListener{

		private static Logger log = Logger.getLogger(UpdateCostsAtAllLevels.class);
		
		private VehicleRoutingActivityCosts activityCost;

		private ForwardTransportCost transportCost;
		
		private StateManagerImpl states;
		
		private double totalOperationCost = 0.0;
		
		private VehicleRoute vehicleRoute = null;
		
		private TourActivity prevAct = null;
		
		private double startTimeAtPrevAct = 0.0;
		
		public UpdateCostsAtAllLevels(VehicleRoutingActivityCosts activityCost, ForwardTransportCost transportCost, StateManagerImpl states) {
			super();
			this.activityCost = activityCost;
			this.transportCost = transportCost;
			this.states = states;
		}

		@Override
		public void start(VehicleRoute route, Start start, double departureTime) {
			vehicleRoute = route;
			vehicleRoute.getVehicleRouteCostCalculator().reset();
			prevAct = start;
			startTimeAtPrevAct = departureTime;
//			log.info(start + " depTime=" + departureTime);
		}

		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
//			log.info(act + " job " + ((JobActivity)act).getJob().getId() + " arrTime=" + arrTime + " endTime=" +  endTime);
			double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), act.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
			double actCost = activityCost.getActivityCost(act, arrTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());

			vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
			vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);

			if(transportCost > 10000 || actCost > 100000){
				throw new IllegalStateException("aaaääähh");
			}
			
			totalOperationCost += transportCost;
			totalOperationCost += actCost;

			states.putActivityState(act, StateTypes.COSTS, new StateImpl(totalOperationCost));

			prevAct = act;
			startTimeAtPrevAct = endTime;	
		}

		@Override
		public void end(End end, double arrivalTime) {
//			log.info(end + " arrTime=" + arrivalTime);
			double transportCost = this.transportCost.getTransportCost(prevAct.getLocationId(), end.getLocationId(), startTimeAtPrevAct, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
			double actCost = activityCost.getActivityCost(end, arrivalTime, vehicleRoute.getDriver(), vehicleRoute.getVehicle());
			
			vehicleRoute.getVehicleRouteCostCalculator().addTransportCost(transportCost);
			vehicleRoute.getVehicleRouteCostCalculator().addActivityCost(actCost);
			
			if(transportCost > 10000 || actCost > 100000){
				throw new IllegalStateException("aaaääähh");
			}
			
			totalOperationCost += transportCost;
			totalOperationCost += actCost;
			
			states.putRouteState(vehicleRoute, StateTypes.COSTS, new StateImpl(totalOperationCost));
			
			//this is rather strange and likely to change
			vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getDriver());
			vehicleRoute.getVehicleRouteCostCalculator().price(vehicleRoute.getVehicle());
			vehicleRoute.getVehicleRouteCostCalculator().finish();
			
			startTimeAtPrevAct = 0.0;
			prevAct = null;
			vehicleRoute = null;
			totalOperationCost = 0.0;
		}

	}

	static class UpdateEarliestStartTimeWindowAtActLocations implements ForwardInTimeListener{

		private StateManagerImpl states;
		
		public UpdateEarliestStartTimeWindowAtActLocations(StateManagerImpl states) {
			super();
			this.states = states;
		}

		@Override
		public void start(VehicleRoute route, Start start, double departureTime) {}

		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
			states.putActivityState(act, StateTypes.EARLIEST_OPERATION_START_TIME, new StateImpl(Math.max(arrTime, act.getTheoreticalEarliestOperationStartTime())));
		}

		@Override
		public void end(End end, double arrivalTime) {}

	}

	static class UpdateLatestOperationStartTimeAtActLocations implements BackwardInTimeListener{

		private static Logger log = Logger.getLogger(UpdateLatestOperationStartTimeAtActLocations.class);
		
		private StateManagerImpl states;
		
		public UpdateLatestOperationStartTimeAtActLocations(StateManagerImpl states) {
			super();
			this.states = states;
		}

		@Override
		public void start(VehicleRoute route, End end, double latestArrivalTime) {}

		@Override
		public void prevActivity(TourActivity act,double latestDepartureTime, double latestOperationStartTime) {
//			log.info(act + " jobId=" + ((JobActivity)act).getJob().getId() + " " + latestOperationStartTime);
			states.putActivityState(act, StateTypes.LATEST_OPERATION_START_TIME, new StateImpl(latestOperationStartTime));
		}

		@Override
		public void end(Start start, double latestDepartureTime) {}

		

	}

	static class UpdateLoadAtAllLevels implements ForwardInTimeListener{

		private double load = 0.0;
		
		private StateManagerImpl states;
		
		private VehicleRoute vehicleRoute;
		
		public UpdateLoadAtAllLevels(StateManagerImpl states) {
			super();
			this.states = states;
		}

		@Override
		public void start(VehicleRoute route, Start start, double departureTime) { vehicleRoute = route; }

		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
			load += (double)act.getCapacityDemand();
			states.putActivityState(act, StateTypes.LOAD, new StateImpl(load));
		}

		@Override
		public void end(End end, double arrivalTime) {
			states.putRouteState(vehicleRoute, StateTypes.LOAD, new StateImpl(load));
			load=0;
			vehicleRoute = null;
		}

	}

	static class UpdateLoadAtRouteLevel implements JobInsertedListener, InsertionStartsListener{

		private StateManagerImpl states;
		
		public UpdateLoadAtRouteLevel(StateManagerImpl states) {
			super();
			this.states = states;
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			if(!(job2insert instanceof Service)){
				return;
			}
			double oldLoad = states.getRouteState(inRoute, StateTypes.LOAD).toDouble();
			states.putRouteState(inRoute, StateTypes.LOAD, new StateImpl(oldLoad + job2insert.getCapacityDemand()));
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
			for(VehicleRoute route : vehicleRoutes){
				int load = 0;
				for(Job j : route.getTourActivities().getJobs()){
					load += j.getCapacityDemand();
				}
				states.putRouteState(route, StateTypes.LOAD, new StateImpl(load));
			}
			
		}

	}

	static class UpdateStates implements JobInsertedListener, RuinListener{

		private IterateRouteForwardInTime iterateForward;
		
		private IterateRouteBackwardInTime iterateBackward;
		
		public UpdateStates(StateManagerImpl states, VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts) {
			
			iterateForward = new IterateRouteForwardInTime(routingCosts);
			iterateForward.addListener(new UpdateActivityTimes());
			iterateForward.addListener(new UpdateCostsAtAllLevels(activityCosts, routingCosts, states));
			iterateForward.addListener(new UpdateLoadAtAllLevels(states));
//			iterateForward.addListener(new UpdateEarliestStartTimeWindowAtActLocations(states));
			
			iterateBackward = new IterateRouteBackwardInTime(routingCosts);
			iterateBackward.addListener(new UpdateLatestOperationStartTimeAtActLocations(states));
		}
		
		public void update(VehicleRoute route){
			iterateForward.iterate(route);
			iterateBackward.iterate(route);
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			iterateForward.iterate(inRoute);
			iterateBackward.iterate(inRoute);
		}

		@Override
		public void ruinStarts(Collection<VehicleRoute> routes) {}

		@Override
		public void ruinEnds(Collection<VehicleRoute> routes,Collection<Job> unassignedJobs) {
			for(VehicleRoute route : routes) {
				iterateForward.iterate(route);
				iterateBackward.iterate(route);
			}
		}

		@Override
		public void removed(Job job, VehicleRoute fromRoute) {}

	}

	static class UpdateFuturePickupsAtActivityLevel implements BackwardInTimeListener {
		private StateManagerImpl stateManager;
		private int futurePicks = 0;
		private VehicleRoute route;
		
		public UpdateFuturePickupsAtActivityLevel(StateManagerImpl stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public void start(VehicleRoute route, End end, double latestArrivalTime) {
			this.route = route;
		}
		
		@Override
		public void prevActivity(TourActivity act, double latestDepartureTime, double latestOperationStartTime) {
			stateManager.putActivityState(act, StateTypes.FUTURE_PICKS, new StateImpl(futurePicks));
			if(act instanceof PickupActivity){
				futurePicks += act.getCapacityDemand();
			}
			assert futurePicks <= route.getVehicle().getCapacity() : "sum of pickups must not be > vehicleCap";
			assert futurePicks >= 0 : "sum of pickups must not < 0";
		}
		
		@Override
		public void end(Start start, double latestDepartureTime) {
			futurePicks = 0;
			route = null;
		}
	}

	static class UpdateOccuredDeliveriesAtActivityLevel implements ForwardInTimeListener {
		private StateManagerImpl stateManager;
		private int deliveries = 0; 
		private VehicleRoute route;
		
		public UpdateOccuredDeliveriesAtActivityLevel(StateManagerImpl stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public void start(VehicleRoute route, Start start, double departureTime) {
			this.route = route; 
		}
		
		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
			if(act instanceof DeliveryActivity){
				deliveries += Math.abs(act.getCapacityDemand());
			}
			stateManager.putActivityState(act, StateTypes.PAST_DELIVERIES, new StateImpl(deliveries));
			assert deliveries >= 0 : "deliveries < 0";
			assert deliveries <= route.getVehicle().getCapacity() : "deliveries > vehicleCap";
		}
		
		@Override
		public void end(End end, double arrivalTime) {
			deliveries = 0;
			route = null;
		}
	}
	
	/**
	 * Updates load at activity level. Note that this assumed that StateTypes.LOAD_AT_DEPOT is already updated, i.e. it starts by setting loadAtDepot to StateTypes.LOAD_AT_DEPOT.
	 * If StateTypes.LOAD_AT_DEPOT is not set, it starts with 0 load at depot.
	 *  
	 * @author stefan
	 *
	 */
	static class UpdateLoadAtActivityLevel implements ForwardInTimeListener {
		private StateManagerImpl stateManager;
		private int currentLoad = 0;
		private VehicleRoute route;
		
		public UpdateLoadAtActivityLevel(StateManagerImpl stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public void start(VehicleRoute route, Start start, double departureTime) {
			currentLoad = (int) stateManager.getRouteState(route, StateTypes.LOAD_AT_DEPOT).toDouble();
			this.route = route;
		}
		
		@Override
		public void nextActivity(TourActivity act, double arrTime, double endTime) {
			currentLoad += act.getCapacityDemand();
			stateManager.putActivityState(act, StateTypes.LOAD, new StateImpl(currentLoad));
			assert currentLoad <= route.getVehicle().getCapacity() : "currentLoad at activity must not be > vehicleCapacity";
			assert currentLoad >= 0 : "currentLoad at act must not be < 0";
		}
		
		@Override
		public void end(End end, double arrivalTime) {
			currentLoad = 0;
		}
	}
	
	static class ResetStateManager implements IterationStartsListener {

		private StateManagerImpl stateManager;
		
		public ResetStateManager(StateManagerImpl stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
			stateManager.clear();
		}
	}
	
	static class WalkThroughAndUpdateRoutesOnceTheyChanged implements InsertionStartsListener, JobInsertedListener {

		private IterateRouteForwardInTime forwardInTimeIterator;
		
		private IterateRouteBackwardInTime backwardInTimeIterator;
		
		public WalkThroughAndUpdateRoutesOnceTheyChanged(VehicleRoutingTransportCosts routingCosts) {
			forwardInTimeIterator = new IterateRouteForwardInTime(routingCosts);
			backwardInTimeIterator = new IterateRouteBackwardInTime(routingCosts);
		}
		
		void addListener(ForwardInTimeListener l){
			forwardInTimeIterator.addListener(l);
		}
		
		void addListener(BackwardInTimeListener l){
			backwardInTimeIterator.addListener(l);
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
			// TODO Auto-generated method stub
			
		}
		
	}
}
