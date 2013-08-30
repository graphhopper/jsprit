package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import basics.Delivery;
import basics.Pickup;
import basics.Service;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.DeliveryActivity;
import basics.route.PickupActivity;
import basics.route.Start;
import basics.route.TourActivity;

class HardConstraints {
	
	interface HardRouteLevelConstraint {

		public boolean fulfilled(InsertionContext insertionContext);
		
	}
	
	interface HardActivityLevelConstraint {
		
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

	}
	
	static class HardRouteLevelConstraintManager implements HardRouteLevelConstraint {

		private Collection<HardRouteLevelConstraint> hardConstraints = new ArrayList<HardRouteLevelConstraint>();
		
		public void addConstraint(HardRouteLevelConstraint constraint){
			hardConstraints.add(constraint);
		}

		@Override
		public boolean fulfilled(InsertionContext insertionContext) {
			for(HardRouteLevelConstraint constraint : hardConstraints){
				if(!constraint.fulfilled(insertionContext)){
					return false;
				}
			}
			return true;
		}
		
	}
	
	
	
	static class HardActivityLevelConstraintManager implements HardActivityLevelConstraint {

		private Collection<HardActivityLevelConstraint> hardConstraints = new ArrayList<HardActivityLevelConstraint>();
		
		public void addConstraint(HardActivityLevelConstraint constraint){
			hardConstraints.add(constraint);
		}
		
		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			for(HardActivityLevelConstraint constraint : hardConstraints){
				if(!constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime)){
					return false;
				}
			}
			return true;
		}
		
	}
	
	static class HardLoadConstraint implements HardRouteLevelConstraint{

		private StateManager states;
		
		public HardLoadConstraint(StateManager states) {
			super();
			this.states = states;
		}

		@Override
		public boolean fulfilled(InsertionContext insertionContext) {
			int currentLoad = (int) states.getRouteState(insertionContext.getRoute(), StateTypes.LOAD).toDouble();
			Service service = (Service) insertionContext.getJob();
			if(currentLoad + service.getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
				return false;
			}
			return true;
		}
	}
	
	static class HardPickupAndDeliveryLoadConstraint implements HardRouteLevelConstraint {

		private StateManager stateManager;
		
		public HardPickupAndDeliveryLoadConstraint(StateManager stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public boolean fulfilled(InsertionContext insertionContext) {
			if(insertionContext.getJob() instanceof Delivery){
				int loadAtDepot = (int) stateManager.getRouteState(insertionContext.getRoute(), StateTypes.LOAD_AT_DEPOT).toDouble();
				if(loadAtDepot + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
					return false;
				}
			}
			else if(insertionContext.getJob() instanceof Pickup){
				int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateTypes.LOAD).toDouble();
				if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
					return false;
				}
			}
			return true;
		}
		
	}
	
	static class HardTimeWindowConstraint implements HardActivityLevelConstraint {

		private static Logger log = Logger.getLogger(HardTimeWindowConstraint.class);
		
		private StateManager states;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public HardTimeWindowConstraint(StateManager states, VehicleRoutingTransportCosts routingCosts) {
			super();
			this.states = states;
			this.routingCosts = routingCosts;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
//			log.info("check insertion of " + newAct + " between " + prevAct + " and " + nextAct + ". prevActDepTime=" + prevActDepTime);
			double arrTimeAtNewAct = prevActDepTime + routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevActDepTime, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNewAct = states.getActivityState(newAct, StateTypes.LATEST_OPERATION_START_TIME).toDouble();
			if(arrTimeAtNewAct > latestArrTimeAtNewAct){
				return false;
			}
//			log.info(newAct + " arrTime=" + arrTimeAtNewAct);
			double endTimeAtNewAct = CalcUtils.getActivityEndTime(arrTimeAtNewAct, newAct);
			double arrTimeAtNextAct = endTimeAtNewAct + routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), endTimeAtNewAct, iFacts.getNewDriver(), iFacts.getNewVehicle());
			double latestArrTimeAtNextAct = states.getActivityState(nextAct, StateTypes.LATEST_OPERATION_START_TIME).toDouble();
			if(arrTimeAtNextAct > latestArrTimeAtNextAct){
				return false;
			}
//			log.info(nextAct + " arrTime=" + arrTimeAtNextAct);
			return true;
		}
	}
	
	static class HardPickupAndDeliveryConstraint implements HardActivityLevelConstraint {
		
		private StateManager stateManager;
		
		public HardPickupAndDeliveryConstraint(StateManager stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			int loadAtPrevAct;
			int futurePicks;
			int pastDeliveries;
			if(prevAct instanceof Start){
				loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD_AT_DEPOT).toDouble();
				futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD).toDouble();
				pastDeliveries = 0;
			}
			else{
				loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateTypes.LOAD).toDouble();
				futurePicks = (int) stateManager.getActivityState(prevAct, StateTypes.FUTURE_PICKS).toDouble();
				pastDeliveries = (int) stateManager.getActivityState(prevAct, StateTypes.PAST_DELIVERIES).toDouble();
			}
			if(newAct instanceof PickupActivity){
				if(loadAtPrevAct + newAct.getCapacityDemand() + futurePicks > iFacts.getNewVehicle().getCapacity()){
					return false;
				}
			}
			if(newAct instanceof DeliveryActivity){
				if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) + pastDeliveries > iFacts.getNewVehicle().getCapacity()){
					return false;
				}
				
			}
			return true;
		}
			
	}
	
	static class HardPickupAndDeliveryBackhaulConstraint implements HardActivityLevelConstraint {
		
		private StateManager stateManager;
		
		public HardPickupAndDeliveryBackhaulConstraint(StateManager stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			if(newAct instanceof PickupActivity && nextAct instanceof DeliveryActivity){ return false; }
			if(newAct instanceof DeliveryActivity && prevAct instanceof PickupActivity){ return false; }
			int loadAtPrevAct;
			int futurePicks;
			int pastDeliveries;
			if(prevAct instanceof Start){
				loadAtPrevAct = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD_AT_DEPOT).toDouble();
				futurePicks = (int)stateManager.getRouteState(iFacts.getRoute(), StateTypes.LOAD).toDouble();
				pastDeliveries = 0;
			}
			else{
				loadAtPrevAct = (int) stateManager.getActivityState(prevAct, StateTypes.LOAD).toDouble();
				futurePicks = (int) stateManager.getActivityState(prevAct, StateTypes.FUTURE_PICKS).toDouble();
				pastDeliveries = (int) stateManager.getActivityState(prevAct, StateTypes.PAST_DELIVERIES).toDouble();
			}
			if(newAct instanceof PickupActivity){
				if(loadAtPrevAct + newAct.getCapacityDemand() + futurePicks > iFacts.getNewVehicle().getCapacity()){
					return false;
				}
			}
			if(newAct instanceof DeliveryActivity){
				if(loadAtPrevAct + Math.abs(newAct.getCapacityDemand()) + pastDeliveries > iFacts.getNewVehicle().getCapacity()){
					return false;
				}
				
			}
			return true;
		}
			
	}
	
	

}
