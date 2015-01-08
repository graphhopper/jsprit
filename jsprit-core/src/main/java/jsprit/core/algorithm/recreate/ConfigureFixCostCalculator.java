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
package jsprit.core.algorithm.recreate;


import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;


final class ConfigureFixCostCalculator implements InsertionStartsListener, JobInsertedListener{

	VehicleRoutingProblem vrp;
	
	JobInsertionConsideringFixCostsCalculator calcConsideringFix;
	
	private int nuOfJobsToRecreate;

	public ConfigureFixCostCalculator(VehicleRoutingProblem vrp, JobInsertionConsideringFixCostsCalculator calcConsideringFix) {
		super();
		this.vrp = vrp;
		this.calcConsideringFix = calcConsideringFix;
	}
	
	@Override
	public String toString() {
		return "[name=configureFixCostCalculator]";
	}

	@Override
	public void informInsertionStarts(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
		this.nuOfJobsToRecreate = unassignedJobs.size();
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)vrp.getJobs().values().size()));
		calcConsideringFix.setSolutionCompletenessRatio(completenessRatio);
//		log.debug("initialise completenessRatio to " + completenessRatio);
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		nuOfJobsToRecreate--;
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)vrp.getJobs().values().size()));
		calcConsideringFix.setSolutionCompletenessRatio(completenessRatio);
//		log.debug("set completenessRatio to " + completenessRatio);
	}
}
