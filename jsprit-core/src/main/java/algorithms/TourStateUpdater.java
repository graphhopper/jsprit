/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import org.apache.log4j.Logger;

import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;





/**
 * Updates tour state, i.e. the tour as well as each activity in that tour has a state such as currentLoad, currentCost, earliestOperationTime and 
 * latestOperationTime. Each time the tour is changed (for instance by removing or adding an activity), tour and activity states 
 * might change, thus this updater updates activity states.
 * This includes:
 * - update load and totalCost at tour-level
 * - update currentLoad and currentCost at activity-level
 * - update earliest- and latestOperationStart values at activity-level
 * 
 * If ensureFeasibility is true, then it additionally checks whether the earliestOperationStartTime is higher than the latestOperationStartTime.
 * If it is, it returns a false value to indicate that the tour is not feasible. This makes only sense for hard-timewindows.
 * 
 * If softTimeWindow is set to true, latestOperationStartTimes are not updated and the tour is always feasible.
 * 
 * @author stefan schroeder
 *
 */

class TourStateUpdater implements VehicleRouteUpdater{
	
//	public final static Counter counter = new Counter("#updateTWProcesses: ");
	
	private static Logger logger = Logger.getLogger(TourStateUpdater.class);
	
	private boolean ensureFeasibility = true;
	
	private UpdateTourStatesForwardInTime forwardUpdate;
	
	private UpdateTourStatesBackwardInTime backwardUpdate;

	private boolean updateTimeWindows = true;
	
	private RouteStates actStates;
	
	public TourStateUpdater(RouteStates activityStates, VehicleRoutingTransportCosts costs, VehicleRoutingActivityCosts costFunction) {
		super();
		forwardUpdate = new UpdateTourStatesForwardInTime(costs, costs, costFunction);
		backwardUpdate = new UpdateTourStatesBackwardInTime(costs);
		actStates=activityStates;
		forwardUpdate.setStates(actStates);
		backwardUpdate.setStates(actStates);
	}

	/*
	 * 
	 */
	public boolean updateRoute(VehicleRoute vehicleRoute) {
		if(updateTimeWindows){
			backwardUpdate.checkFeasibility = ensureFeasibility;
			backwardUpdate.updateRoute(vehicleRoute);
		}
		forwardUpdate.updateRoute(vehicleRoute);
		boolean tourIsFeasible = true; 
		
		return tourIsFeasible;
	}

	public void setTimeWindowUpdate(boolean updateTimeWindows) {
		this.updateTimeWindows = updateTimeWindows;
		logger.info("set timeWindowUpdate to " + updateTimeWindows);
	}

	public void setEnsureFeasibility(boolean ensureFeasibility) {
		this.ensureFeasibility = ensureFeasibility;
	}

}
