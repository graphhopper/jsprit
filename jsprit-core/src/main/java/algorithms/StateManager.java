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

import basics.route.TourActivity;
import basics.route.VehicleRoute;

public interface StateManager {
	
	interface State {
		double toDouble();
	}
	
	class StateImpl implements State{
		double state;

		public StateImpl(double state) {
			super();
			this.state = state;
		}

		@Override
		public double toDouble() {
			return state;
		}
		
//		public void setState(double val){
//			state=val;
//		}
	}
	
	interface States {
		
//		void putState(String key, State state);
		
		State getState(String key);
		
	}
	
	
	
//	Map<VehicleRoute, States> getRouteStates();
	
//	void put(VehicleRoute route, States states);
	
//	Map<TourActivity, States> getActivityStates();
	
//	void put(TourActivity act, States states);
	
	State getActivityState(TourActivity act, String stateType);
	
	State getRouteState(VehicleRoute route, String stateType);

}
