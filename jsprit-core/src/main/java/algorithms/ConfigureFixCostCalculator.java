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
package algorithms;


import java.util.Collection;

import org.apache.log4j.Logger;

import basics.Job;
import basics.VehicleRoutingProblem;
import basics.algo.InsertionStartsListener;
import basics.algo.JobInsertedListener;
import basics.route.VehicleRoute;





final class ConfigureFixCostCalculator implements InsertionStartsListener, JobInsertedListener{

	private static Logger log = Logger.getLogger(ConfigureFixCostCalculator.class);
	
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
