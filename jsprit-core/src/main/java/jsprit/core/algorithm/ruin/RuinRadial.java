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

import jsprit.core.algorithm.ruin.distance.JobDistance;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.util.RandomUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * 
 * RuinStrategy that ruins the neighborhood of a randomly selected job. The size and the structure of the neighborhood is defined by 
 * the share of jobs to be removed and the distance between jobs (where distance not necessarily mean Euclidean distance but an arbitrary 
 * measure).
 * 
 * @author stefan
 *
 */
public final class RuinRadial extends AbstractRuinStrategy {
	
	private Logger logger = LogManager.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private JobNeighborhoods jobNeighborhoods;

	private final int noJobsToMemorize;

	/**
	 * Constructs RuinRadial.
	 * 
	 * @param vrp
	 * @param fraction2beRemoved i.e. the share of jobs to be removed (relative to the total number of jobs in vrp)
	 * @param jobDistance i.e. a measure to define the distance between two jobs and whether they are located close or distant to eachother
	 */
	public RuinRadial(VehicleRoutingProblem vrp, double fraction2beRemoved, JobDistance jobDistance) {
		super(vrp);
		this.vrp = vrp;
		noJobsToMemorize = (int) Math.ceil(vrp.getJobs().values().size()*fraction2beRemoved);
		ruinShareFactory = new RuinShareFactory() {

			@Override
			public int createNumberToBeRemoved() {
				return noJobsToMemorize;
			}

		};
		JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, noJobsToMemorize);
		jobNeighborhoodsImpl.initialise();
		jobNeighborhoods = jobNeighborhoodsImpl;
		logger.debug("initialise {}", this);
	}

	public RuinRadial(VehicleRoutingProblem vrp, int noJobs2beRemoved, JobDistance jobDistance) {
		super(vrp);
		this.vrp = vrp;
//		this.fractionOfAllNodes2beRuined = fraction2beRemoved;
		noJobsToMemorize = noJobs2beRemoved;
		ruinShareFactory = new RuinShareFactory() {

			@Override
			public int createNumberToBeRemoved() {
				return noJobsToMemorize;
			}

		};
		JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, noJobsToMemorize);
		jobNeighborhoodsImpl.initialise();
		jobNeighborhoods = jobNeighborhoodsImpl;
		logger.debug("initialise {}", this);
	}

	public RuinRadial(VehicleRoutingProblem vrp, int noJobs2beRemoved, JobNeighborhoods neighborhoods) {
		super(vrp);
		this.vrp = vrp;
		noJobsToMemorize = noJobs2beRemoved;
		ruinShareFactory = new RuinShareFactory() {

			@Override
			public int createNumberToBeRemoved() {
				return noJobsToMemorize;
			}

		};
		jobNeighborhoods = neighborhoods;
		logger.debug("initialise {}", this);
	}
	
	@Override
	public String toString() {
		return "[name=radialRuin][noJobsToBeRemoved="+noJobsToMemorize+"]";
	}

	/**
	 * Ruins the collection of vehicleRoutes, i.e. removes a share of jobs. First, it selects a job randomly. Second, it identifies its neighborhood. And finally, it removes 
	 * the neighborhood plus the randomly selected job from the number of vehicleRoutes. All removed jobs are then returned as a collection.
	 */
	@Override
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes) {
		if(vehicleRoutes.isEmpty()){
			return Collections.emptyList();
		}
		int nOfJobs2BeRemoved = Math.min(ruinShareFactory.createNumberToBeRemoved(), noJobsToMemorize);
		if (nOfJobs2BeRemoved == 0) {
			return Collections.emptyList();
		}
		Job randomJob = RandomUtils.nextJob(vrp.getJobs().values(),random);
		return ruinRoutes(vehicleRoutes, randomJob, nOfJobs2BeRemoved);
	}
	
	/**
	 * Removes targetJob and its neighborhood and returns the removed jobs.
     * @deprecated will be private
	 */
    @Deprecated
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved){
        List<Job> unassignedJobs = new ArrayList<Job>();
		int nNeighbors = nOfJobs2BeRemoved - 1;
		removeJob(targetJob,vehicleRoutes);
		unassignedJobs.add(targetJob);
		Iterator<Job> neighborhoodIterator =  jobNeighborhoods.getNearestNeighborsIterator(nNeighbors, targetJob);
		while(neighborhoodIterator.hasNext()){
			Job job = neighborhoodIterator.next();
			if(removeJob(job,vehicleRoutes)){
				unassignedJobs.add(job);
			}
		}
        return unassignedJobs;
	}

}
