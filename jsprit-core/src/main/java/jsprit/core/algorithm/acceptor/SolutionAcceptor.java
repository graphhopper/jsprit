
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


/**
 * Acceptor that decides whether the newSolution is accepted or not.
 * 
 * 
 * @author stefan
 *
 */
public interface SolutionAcceptor {
	
	/**
	 * Accepts solution or not, and returns true if a new solution has been accepted.
	 * 
	 * <p>If the solution is accepted, it is added to solutions, i.e. the solutions-collections is modified.
	 * 
	 * @param solutions collection of existing solutions
	 * @param newSolution new solution to be evaluated
	 * @return true if solution accepted
	 */
	public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution);

}
