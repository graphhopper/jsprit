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
package jsprit.core.algorithm.ruin;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 * 
 * @author stefan schroeder
 * 
 */

public final class RuinRandom extends AbstractRuinStrategy {
	
	private Logger logger = LogManager.getLogger(RuinRandom.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	/**
	 * Constructs ruinRandom.
	 * 
	 * @param vrp
	 * @param fraction which is the fraction of total c
	 */
	public RuinRandom(VehicleRoutingProblem vrp, double fraction) {
		super(vrp);
		this.vrp = vrp;
		this.fractionOfAllNodes2beRuined = fraction;
		setRuinShareFactory(new RuinShareFactory() {
			@Override
			public int createNumberToBeRemoved() {
				return selectNuOfJobs2BeRemoved();
			}
		});
        logger.debug("initialise {}", this);
	}

	/**
	 * Removes a fraction of jobs from vehicleRoutes. 
	 * 
	 * <p>The number of jobs is calculated as follows: Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined).
	 */
	@Override
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
        List<Job> unassignedJobs = new ArrayList<Job>();
		int nOfJobs2BeRemoved = getRuinShareFactory().createNumberToBeRemoved();
		ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
        return unassignedJobs;
	}

	/**
	 * Removes nOfJobs2BeRemoved from vehicleRoutes, including targetJob.
	 */
	@Override
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved) {
        throw new IllegalStateException("not supported");
	}

	private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
		ArrayList<Job> availableJobs = new ArrayList<Job>(vrp.getJobs().values());
		Collections.shuffle(availableJobs,random);
		int removed = 0;
		for (Job job : availableJobs) {
			if(removed == nOfJobs2BeRemoved) break;
			if(removeJob(job,vehicleRoutes)) {
				unassignedJobs.add(job);
			}
			removed++;
		}
	}

	@Override
	public String toString() {
		return "[name=randomRuin][noJobsToBeRemoved="+selectNuOfJobs2BeRemoved()+"]";
	}

	private int selectNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined);
	}


}
