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
package jsprit.core.algorithm.selector;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import java.util.Collection;





public class SelectBest implements SolutionSelector{

	private static SelectBest selector = null;
	
	public static SelectBest getInstance(){
		if(selector == null){
			selector = new SelectBest();
			return selector;
		}
		return selector;
	}
	
	@Override
	public VehicleRoutingProblemSolution selectSolution(Collection<VehicleRoutingProblemSolution> solutions) {
		double minCost = Double.MAX_VALUE;
		VehicleRoutingProblemSolution bestSolution = null;
		for(VehicleRoutingProblemSolution sol : solutions){
			if(bestSolution == null){
				bestSolution = sol;
				minCost = sol.getCost();
			}
			else if(sol.getCost() < minCost){
				bestSolution = sol;
				minCost = sol.getCost();
			}
		}
		return bestSolution;
	}
	
	@Override
	public String toString() {
		return "[name=selectBest]";
	}

}
