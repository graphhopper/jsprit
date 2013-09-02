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

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

import basics.Delivery;
import basics.Pickup;
import basics.Service;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.DeliveryActivity;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;

/**
 * collection of hard constrainters bot at activity and at route level.
 * 
 * <p>HardPickupAndDeliveryLoadConstraint requires LOAD_AT_DEPOT and LOAD (i.e. load at end) at route-level
 * 
 * <p>HardTimeWindowConstraint requires LATEST_OPERATION_START_TIME
 * 
 * <p>HardPickupAndDeliveryConstraint requires LOAD_AT_DEPOT and LOAD at route-level and FUTURE_PICKS and PAST_DELIVIERS on activity-level
 * 
 * <p>HardPickupAndDeliveryBackhaulConstraint requires LOAD_AT_DEPOT and LOAD at route-level and FUTURE_PICKS and PAST_DELIVIERS on activity-level
 * 
 * @author stefan
 *
 */
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
	
	static class ConstraintManager implements HardActivityLevelConstraint, HardRouteLevelConstraint{

		private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();
		
		private HardRouteLevelConstraintManager routeLevelConstraintManager = new HardRouteLevelConstraintManager();
		
		public void addConstraint(HardActivityLevelConstraint actLevelConstraint){
			actLevelConstraintManager.addConstraint(actLevelConstraint);
		}
		
		public void addConstraint(HardRouteLevelConstraint routeLevelConstraint){
			routeLevelConstraintManager.addConstraint(routeLevelConstraint);
		}
		
		@Override
		public boolean fulfilled(InsertionContext insertionContext) {
			return routeLevelConstraintManager.fulfilled(insertionContext);
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
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
	
	/**
	 * lsjdfjsdlfjsa
	 * 
	 * @author stefan
	 *
	 */
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
			else if(insertionContext.getJob() instanceof Pickup || insertionContext.getJob() instanceof Service){
				int loadAtEnd = (int) stateManager.getRouteState(insertionContext.getRoute(), StateTypes.LOAD).toDouble();
				if(loadAtEnd + insertionContext.getJob().getCapacityDemand() > insertionContext.getNewVehicle().getCapacity()){
					return false;
				}
			}
			return true;
		}
		
	}
	
	/**
	 * ljsljslfjs
	 * @author stefan
	 *
	 */
	public static class HardTimeWindowActivityLevelConstraint implements HardActivityLevelConstraint {

		private static Logger log = Logger.getLogger(HardTimeWindowActivityLevelConstraint.class);
		
		private StateManager states;
		
		private VehicleRoutingTransportCosts routingCosts;
		
		public HardTimeWindowActivityLevelConstraint(StateManager states, VehicleRoutingTransportCosts routingCosts) {
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
	
	static class HardPickupAndDeliveryActivityLevelConstraint implements HardActivityLevelConstraint {
		
		private StateManager stateManager;
		
		public HardPickupAndDeliveryActivityLevelConstraint(StateManager stateManager) {
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
			if(newAct instanceof PickupActivity || newAct instanceof ServiceActivity){
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
	
	static class HardPickupAndDeliveryBackhaulActivityLevelConstraint implements HardActivityLevelConstraint {
		
		private StateManager stateManager;
		
		public HardPickupAndDeliveryBackhaulActivityLevelConstraint(StateManager stateManager) {
			super();
			this.stateManager = stateManager;
		}

		@Override
		public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
			if(newAct instanceof PickupActivity && nextAct instanceof DeliveryActivity){ return false; }
			if(newAct instanceof ServiceActivity && nextAct instanceof DeliveryActivity){ return false; }
			if(newAct instanceof DeliveryActivity && prevAct instanceof PickupActivity){ return false; }
			if(newAct instanceof DeliveryActivity && prevAct instanceof ServiceActivity){ return false; }
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
			if(newAct instanceof PickupActivity || newAct instanceof ServiceActivity){
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
