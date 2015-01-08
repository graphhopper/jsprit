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
import java.util.LinkedList;
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
		super();
		this.vrp = vrp;
		this.fractionOfAllNodes2beRuined = fraction;
		setRuinShareFactory(new RuinShareFactory() {
			@Override
			public int createNumberToBeRemoved() {
				return selectNuOfJobs2BeRemoved();
			}
		});
        logger.info("initialise " + this);
		logger.info("done");
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

	@Deprecated
	public void setRuinFraction(double fractionOfAllNodes2beRuined) {
		this.fractionOfAllNodes2beRuined = fractionOfAllNodes2beRuined;
		logger.info("fraction set " + this);
	}

	private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
		LinkedList<Job> availableJobs = new LinkedList<Job>(vrp.getJobs().values());
		for (int i = 0; i < nOfJobs2BeRemoved; i++) {
			Job job = pickRandomJob(availableJobs);
			unassignedJobs.add(job);
			availableJobs.remove(job);
			removeJob(job,vehicleRoutes);
		}
	}

		
	@Override
	public String toString() {
		return "[name=randomRuin][noJobsToBeRemoved="+selectNuOfJobs2BeRemoved()+"]";
	}
	
	private Job pickRandomJob(LinkedList<Job> availableJobs) {
		int randomIndex = random.nextInt(availableJobs.size());
		return availableJobs.get(randomIndex);
	}

	private int selectNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined);
	}


}
