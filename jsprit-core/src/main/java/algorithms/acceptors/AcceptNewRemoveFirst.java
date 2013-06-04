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
package algorithms.acceptors;

import java.util.Collection;

import basics.VehicleRoutingProblemSolution;


public class AcceptNewRemoveFirst implements SolutionAcceptor{

	private final int solutionMemory;
	
	public AcceptNewRemoveFirst(int solutionMemory){
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
		if (solutions.size() >= solutionMemory) {
			solutions.remove(solutions.iterator().next());
		}
		solutions.add(newSolution);
		return true;
	}
	
	@Override
	public String toString() {
		return "[name=acceptNewRemoveFirst]";
	}
	



}
