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
package algorithms.selectors;

import java.util.Collection;

import basics.VehicleRoutingProblemSolution;




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
