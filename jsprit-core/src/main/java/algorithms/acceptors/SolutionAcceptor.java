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
	 * @param solutions
	 * @param newSolution
	 * @return TODO
	 */
	public boolean acceptSolution(Collection<VehicleRoutingProblemSolution> solutions, VehicleRoutingProblemSolution newSolution);

}
