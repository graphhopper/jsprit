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
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmEndsListener;
import basics.algo.IterationEndsListener;
import basics.algo.IterationStartsListener;
import basics.route.ServiceActivity;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class RouteStates implements IterationStartsListener{
	
	Logger log = Logger.getLogger(RouteStates.class);

	static class RouteState {
		private double costs;
		private int load;
		private VehicleRoute route;
		public RouteState(VehicleRoute route) {
			super();
			this.route = route;
		}
		/**
		 * @return the costs
		 */
		public double getCosts() {
			return costs;
		}
		/**
		 * @param costs the costs to set
		 */
		public void setCosts(double costs) {
			this.costs = costs;
		}
		/**
		 * @return the load
		 */
		public int getLoad() {
			return load;
		}
		/**
		 * @param load the load to set
		 */
		public void setLoad(int load) {
			this.load = load;
		}
		
	}
	
	static class ActivityState {
		private double earliestOperationStart;
		private double latestOperationStart;
		private double currentLoad;
		private double currentCost;
		private TourActivity act;
		
		public ActivityState(TourActivity activity){
			this.earliestOperationStart=activity.getTheoreticalEarliestOperationStartTime();
			this.latestOperationStart=activity.getTheoreticalLatestOperationStartTime();
			this.act = activity;
		}
		
		@Override
		public String toString() {
			return "[earliestStart="+earliestOperationStart+"][latestStart="+
				latestOperationStart+"][currLoad="+currentLoad+"][currCost="+currentCost+"]";
		}
		
		public double getEarliestOperationStart() {
			return earliestOperationStart;
		}
		
		void setEarliestOperationStart(double earliestOperationStart) {
			this.earliestOperationStart = earliestOperationStart;
		}
		
		public double getLatestOperationStart() {
			return latestOperationStart;
		}
		
		void setLatestOperationStart(double latestOperationStart) {
			this.latestOperationStart = latestOperationStart;
		}
		
		public double getCurrentLoad() {
			return currentLoad;
		}
		
		void setCurrentLoad(double currentLoad) {
			this.currentLoad = currentLoad;
		}
		
		public double getCurrentCost() {
			return currentCost;
		}
		
		void setCurrentCost(double currentCost) {
			this.currentCost = currentCost;
		}

		public void reset() {
			earliestOperationStart = act.getTheoreticalEarliestOperationStartTime();
			latestOperationStart = act.getTheoreticalLatestOperationStartTime() ;
			currentLoad = 0.0;
			currentCost = 0.0;
		}
	}
	
		
	private Map<TourActivity, ActivityState> activityStates;
	
	private Map<Service, TourActivity> tourActivities;
	
	private Map<VehicleRoute, RouteState> routeStates;
	
	public RouteStates() {
		activityStates = new HashMap<TourActivity, RouteStates.ActivityState>();
		tourActivities = new HashMap<Service,TourActivity>();
		routeStates = new HashMap<VehicleRoute, RouteStates.RouteState>();
	}

	ActivityState getState(TourActivity act){
		if(!activityStates.containsKey(act)) return null;
		return activityStates.get(act);
	}
	
	public void clearStates(){
		activityStates.clear();
	}
	
	public Map<TourActivity, ActivityState> getActivityStates() {
		return activityStates;
	}

	TourActivity getActivity(Service service, boolean resetState){
		TourActivity tourActivity = tourActivities.get(service);
		getState(tourActivity).reset(); 
		return tourActivity;
	}
	
	public void resetRouteStates(){
		routeStates.clear();
	}
	
	public RouteState getRouteState(VehicleRoute route){
		RouteState routeState = routeStates.get(route);
		if(routeState == null){
			routeState = new RouteState(route);
			putRouteState(route, routeState);
		}
		return routeState;
	}
	
	private void putRouteState(VehicleRoute route, RouteState routeState){
		routeStates.put(route, routeState);
	}
	
	void initialiseStateOfJobs(Collection<Job> jobs){
		for(Job job : jobs){
			if(job instanceof Service){
				ServiceActivity service = ServiceActivity.newInstance((Service)job);
				ActivityState state = new ActivityState(service);
				tourActivities.put((Service) job, service);
				activityStates.put(service, state);
			}
			else{
				throw new IllegalStateException();
			}
		}
	}

	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		resetRouteStates();
	}

	

}
