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
import java.util.HashSet;
import java.util.Set;

import basics.Job;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.AlgorithmEndsListener;
import basics.route.VehicleRoute;

class SolutionVerifier implements AlgorithmEndsListener{

	@Override
	public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		
		for(VehicleRoutingProblemSolution solution : solutions){
			Set<Job> jobsInSolution = new HashSet<Job>();
			for(VehicleRoute route : solution.getRoutes()){
				jobsInSolution.addAll(route.getTourActivities().getJobs());
			}
			if(jobsInSolution.size() != problem.getJobs().size()){
				throw new IllegalStateException("we are at the end of the algorithm and still have not found a valid solution." +
						"This cannot be.");
			}
		}
		
	}

}
