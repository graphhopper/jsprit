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

class ForwardInTimeListeners {
	
	interface ForwardInTimeListener{
		
		public void start(VehicleRoute route, Start start, double departureTime);

		public void nextActivity(TourActivity act, double arrTime,double endTime);

		public void end(End end, double arrivalTime);
		
	}
	
	private Collection<ForwardInTimeListener> listeners = new ArrayList<ForwardInTimeListeners.ForwardInTimeListener>();
	
	public void addListener(ForwardInTimeListener l){
		listeners.add(l);
	}
	
	public void start(VehicleRoute route, Start start, double departureTime){
		for(ForwardInTimeListener l : listeners){ l.start(route, start, departureTime); }
	}
	
	public void nextActivity(TourActivity act, double arrTime, double endTime){
		for(ForwardInTimeListener l : listeners){ l.nextActivity(act,arrTime,endTime); }
	}
	
	public void end(End end, double arrivalTime){
		for(ForwardInTimeListener l : listeners){ l.end(end, arrivalTime); }
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

}
