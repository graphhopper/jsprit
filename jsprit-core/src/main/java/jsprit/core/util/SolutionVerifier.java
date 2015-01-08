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
package jsprit.core.util;

import jsprit.core.algorithm.listener.AlgorithmEndsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


public class SolutionVerifier implements AlgorithmEndsListener{

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
