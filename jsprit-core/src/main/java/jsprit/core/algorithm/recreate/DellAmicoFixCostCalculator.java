/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.problem.constraint.SoftRouteConstraint;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

import java.util.Collection;

public class DellAmicoFixCostCalculator implements SoftRouteConstraint, InsertionStartsListener, JobInsertedListener{

	private int nuOfJobsToRecreate;
	
	private final JobInsertionConsideringFixCostsCalculator calculator;
	
	private final int nuOfJobs;
	
	public DellAmicoFixCostCalculator(final int nuOfJobs, final RouteAndActivityStateGetter stateGetter) {
		super();
		this.nuOfJobs=nuOfJobs;
		calculator = new JobInsertionConsideringFixCostsCalculator(null, stateGetter);
	}

	@Override
	public double getCosts(JobInsertionContext insertionContext) {
		return calculator.getCosts(insertionContext);
	}
	
	@Override
	public void informInsertionStarts(Collection<VehicleRoute> routes, Collection<Job> unassignedJobs) {
		this.nuOfJobsToRecreate = unassignedJobs.size();
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)nuOfJobs));
		calculator.setSolutionCompletenessRatio(completenessRatio);
	}

	@Override
	public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
		nuOfJobsToRecreate--;
		double completenessRatio = (1-((double)nuOfJobsToRecreate/(double)nuOfJobs));
		calculator.setSolutionCompletenessRatio(completenessRatio);
		System.out.println(completenessRatio);
	}

}
