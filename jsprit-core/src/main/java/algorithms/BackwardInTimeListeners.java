/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class BackwardInTimeListeners {
	
	interface BackwardInTimeListener{
		
		public void start(VehicleRoute route, End end, double latestArrivalTime);

		public void prevActivity(TourActivity act, double latestDepartureTime, double latestOperationStartTime);

		public void end(Start start, double latestDepartureTime);
		
	}
	
	private Collection<BackwardInTimeListener> listeners = new ArrayList<BackwardInTimeListener>();
	
	public void addListener(BackwardInTimeListener l){
		listeners.add(l);
	}
	
	public void start(VehicleRoute route, End end, double latestArrivalTime){
		for(BackwardInTimeListener l : listeners){ l.start(route, end, latestArrivalTime); }
	}
	
	/**
	 * Informs listener about nextActivity.
	 * 
	 * <p>LatestDepartureTime is the theoretical latest departureTime to meet the latestOperationStartTimeWindow at the nextActivity (forward in time), i.e.
	 * assume act_i and act_j are two successive activities and the latestDepTime of act_j is 10pm. With a travelTime from act_i to act_j of 1h the latestDepartureTime at act_i is 9pm.
	 * However, the latestOperationStartTime of act_i is 8pm, then (with a serviceTime of 0) the latestOperationStartTime at act_i amounts to 8pm.
	 * 
	 * @param act
	 * @param latestDepartureTime
	 * @param latestArrivalTime
	 */
	public void prevActivity(TourActivity act, double latestDepartureTime, double latestArrivalTime){
		for(BackwardInTimeListener l : listeners){ l.prevActivity(act,latestDepartureTime,latestArrivalTime); }
	}
	
	public void end(Start start, double latestDepartureTime){
		for(BackwardInTimeListener l : listeners){ l.end(start, latestDepartureTime); }
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

}
