/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.algorithm.acceptor;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.Collection;


@Deprecated
public class GreedyAcceptance_minVehFirst implements SolutionAcceptor{

	private final int solutionMemory;
	
	public GreedyAcceptance_minVehFirst(int solutionMemory){
		this.solutionMemory = solutionMemory;
	}
	
	/**
	 * Accepts every solution if solution memory allows. If memory occupied, than accepts new solution only if better than the worst in memory.
	 * Consequently, the worst solution is removed from solutions, and the new solution added. 
	 * 
	 * <p>Note that this modifies Collection<VehicleRoutingProblemSolution> solutions.
	 */
	@Override
	public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution) {
		boolean solutionAccepted = false;
		if (solutions.size() < solutionMemory) {
			solutions.add(newSolution);
			solutionAccepted = true;
		} else {
			VehicleRoutingProblemSolution worstSolution = null;
			for (VehicleRoutingProblemSolution s : solutions) {
				if (worstSolution == null) worstSolution = s;
				else if (s.getRoutes().size() > worstSolution.getRoutes().size()) worstSolution = s;
			}
			if(newSolution.getRoutes().size() < worstSolution.getRoutes().size()){
				solutions.remove(worstSolution);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
			else if(newSolution.getRoutes().size() == worstSolution.getRoutes().size() && newSolution.getCost() < worstSolution.getCost()){
				solutions.remove(worstSolution);
				solutions.add(newSolution);
				solutionAccepted = true;
			}
		}
		return solutionAccepted;
	}
	
	@Override
	public String toString() {
		return "[name=greedyAcceptance_minVehFirst]";
	}
	



}
