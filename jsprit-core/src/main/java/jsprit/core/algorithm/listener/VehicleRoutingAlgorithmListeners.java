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
package jsprit.core.algorithm.listener;

import jsprit.core.algorithm.SearchStrategy;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.*;




public class VehicleRoutingAlgorithmListeners {

	public static class PrioritizedVRAListener {
		
		Priority priority;
		VehicleRoutingAlgorithmListener l;
		public PrioritizedVRAListener(Priority priority, VehicleRoutingAlgorithmListener l) {
			super();
			this.priority = priority;
			this.l = l;
		}
		public Priority getPriority() {
			return priority;
		}
		public VehicleRoutingAlgorithmListener getListener() {
			return l;
		}
		
//		@Override
//		public int hashCode() {
//			final int prime = 31;
//			int result = 1;
//			result = prime * result + ((l == null) ? 0 : l.hashCode());
//			result = prime * result
//					+ ((priority == null) ? 0 : priority.hashCode());
//			return result;
//		}
//		@Override
//		public boolean equals(Object obj) {
//			if (this == obj)
//				return true;
//			if (obj == null)
//				return false;
//			if (getClass() != obj.getClass())
//				return false;
//			PrioritizedVRAListener other = (PrioritizedVRAListener) obj;
//			if (l == null) {
//				if (other.l != null)
//					return false;
//			} else if (!l.equals(other.l))
//				return false;
//			if (priority == null) {
//				if (other.priority != null)
//					return false;
//			} else if (!priority.equals(other.priority))
//				return false;
//			return true;
//		}
		
	}
	
	public enum Priority {
		HIGH, MEDIUM, LOW
	}
	
	
	private TreeSet<PrioritizedVRAListener> algorithmListeners = new TreeSet<PrioritizedVRAListener>(new Comparator<PrioritizedVRAListener>() {

		@Override
		public int compare(PrioritizedVRAListener o1, PrioritizedVRAListener o2) {
			if(o1 == o2) return 0;
			if(o1.getPriority() == Priority.HIGH && o2.getPriority() != Priority.HIGH){
				return -1;
			}
			else if(o2.getPriority() == Priority.HIGH && o1.getPriority() != Priority.HIGH){
				return 1;
			}
			else if(o1.getPriority() == Priority.MEDIUM && o2.getPriority() != Priority.MEDIUM){
				return -1;
			}
			else if(o2.getPriority() == Priority.MEDIUM && o1.getPriority() != Priority.MEDIUM){
				return 1;
			}
			return 1;
		}
	});
	
	
	public Collection<VehicleRoutingAlgorithmListener> getAlgorithmListeners() {
		List<VehicleRoutingAlgorithmListener> list = new ArrayList<VehicleRoutingAlgorithmListener>();
		for(PrioritizedVRAListener l : algorithmListeners){
			list.add(l.getListener());
		}
		return Collections.unmodifiableCollection(list);
	}
	
	public void remove(PrioritizedVRAListener listener){
		boolean removed = algorithmListeners.remove(listener);
		if(!removed){ throw new IllegalStateException("cannot remove listener"); }
	}

	public void addListener(VehicleRoutingAlgorithmListener listener, Priority priority){
		algorithmListeners.add(new PrioritizedVRAListener(priority, listener));
	}

	public void addListener(VehicleRoutingAlgorithmListener listener){
		addListener(listener, Priority.LOW);
	}

	public void algorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		for(PrioritizedVRAListener l : algorithmListeners){
			if(l.getListener() instanceof AlgorithmEndsListener){
				((AlgorithmEndsListener)l.getListener()).informAlgorithmEnds(problem, solutions);
			}
		}
		
	}

	public void iterationEnds(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		for(PrioritizedVRAListener l : algorithmListeners){
			if(l.getListener() instanceof IterationEndsListener){
				((IterationEndsListener)l.getListener()).informIterationEnds(i,problem, solutions);
			}
		}
	}



	public void iterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		for(PrioritizedVRAListener l : algorithmListeners){
			if(l.getListener() instanceof IterationStartsListener){
				((IterationStartsListener)l.getListener()).informIterationStarts(i,problem, solutions);
			}
		}
	}

	

	public void algorithmStarts(VehicleRoutingProblem problem, VehicleRoutingAlgorithm algorithm, Collection<VehicleRoutingProblemSolution> solutions) {
		for(PrioritizedVRAListener l : algorithmListeners){
			if(l.getListener() instanceof AlgorithmStartsListener){
				((AlgorithmStartsListener)l.getListener()).informAlgorithmStarts(problem, algorithm, solutions);
			}
		}
	}

	public void add(PrioritizedVRAListener l){
		algorithmListeners.add(l);
	}
	
	public void addAll(Collection<PrioritizedVRAListener> algorithmListeners) {
		for(PrioritizedVRAListener l : algorithmListeners){
			this.algorithmListeners.add(l);
		}	
	}

	public void selectedStrategy(SearchStrategy.DiscoveredSolution discoveredSolution, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		for(PrioritizedVRAListener l : algorithmListeners){
			if(l.getListener() instanceof StrategySelectedListener){
				((StrategySelectedListener)l.getListener()).informSelectedStrategy(discoveredSolution, problem, solutions);
			}
		}
	}
}
