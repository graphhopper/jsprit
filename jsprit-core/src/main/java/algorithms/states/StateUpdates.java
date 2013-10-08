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
package algorithms.states;

import java.util.ArrayList;
import java.util.Collection;


import algorithms.ActivityVisitor;
import algorithms.ReverseActivityVisitor;
import algorithms.ReverseRouteActivityVisitor;
import algorithms.RouteActivityVisitor;
import algorithms.StateManagerImpl;
import basics.Job;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.InsertionStartsListener;
import basics.algo.IterationStartsListener;
import basics.algo.JobInsertedListener;
import basics.algo.RuinListener;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.VehicleRoute;

public class StateUpdates {
	
	public static class UpdateStates implements JobInsertedListener, RuinListener{

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
	
	static interface InsertionStarts {
		
		void insertionStarts(VehicleRoute route);
		
	}
	
	static class UpdateRouteStatesOnceTheRouteHasBeenChanged implements InsertionStartsListener, JobInsertedListener {

		private RouteActivityVisitor forwardInTimeIterator;
		
		private ReverseRouteActivityVisitor backwardInTimeIterator;
		
		private Collection<InsertionStarts> insertionStartsListeners;
	
		private Collection<JobInsertedListener> jobInsertionListeners;
		
		public UpdateRouteStatesOnceTheRouteHasBeenChanged(VehicleRoutingTransportCosts routingCosts) {
			forwardInTimeIterator = new RouteActivityVisitor();
			backwardInTimeIterator = new ReverseRouteActivityVisitor();
			insertionStartsListeners = new ArrayList<InsertionStarts>();
			jobInsertionListeners = new ArrayList<JobInsertedListener>();
		}
		
		void addVisitor(ActivityVisitor vis){
			forwardInTimeIterator.addActivityVisitor(vis);
		}
		
		void addVisitor(ReverseActivityVisitor revVis){
			backwardInTimeIterator.addActivityVisitor(revVis);
		}
		
		void addInsertionStartsListener(InsertionStarts insertionStartListener){
			insertionStartsListeners.add(insertionStartListener);
		}
		
		void addJobInsertedListener(JobInsertedListener jobInsertedListener){
			jobInsertionListeners.add(jobInsertedListener);
		}

		@Override
		public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
			for(JobInsertedListener l : jobInsertionListeners){ l.informJobInserted(job2insert, inRoute, additionalCosts, additionalTime); }
			forwardInTimeIterator.visit(inRoute);
			backwardInTimeIterator.visit(inRoute);
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
			for(VehicleRoute route : vehicleRoutes){
				for(InsertionStarts insertionsStartsHandler : insertionStartsListeners){
					insertionsStartsHandler.insertionStarts(route);
				}
				forwardInTimeIterator.visit(route);
				backwardInTimeIterator.visit(route);
			}
		}
		
	}
}
