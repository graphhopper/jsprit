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
public final class RuinRadialMultipleCenters extends AbstractRuinStrategy {

	private Logger logger = LogManager.getLogger(RuinRadialMultipleCenters.class);

	private VehicleRoutingProblem vrp;

	private JobNeighborhoods jobNeighborhoods;

	private final int noJobsToMemorize;

	private int noCenters = 1;

	public RuinRadialMultipleCenters(VehicleRoutingProblem vrp, int neighborhoodSize, JobDistance jobDistance) {
		super(vrp);
		this.vrp = vrp;
		noJobsToMemorize = neighborhoodSize;
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

	public void setNumberOfRuinCenters(int noCenters){
		this.noCenters = noCenters;
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
		Set<Job> available = new HashSet<Job>(vrp.getJobs().values());
		Collection<Job> ruined = new ArrayList<Job>();
		for(int center=0;center<noCenters;center++) {
			int nOfJobs2BeRemoved = ruinShareFactory.createNumberToBeRemoved();
			if (nOfJobs2BeRemoved == 0) {
				return Collections.emptyList();
			}
			Job randomJob = pickRandomJob(available);
			if(randomJob != null) {
				ruined.addAll(ruinRoutes_(vehicleRoutes, randomJob, nOfJobs2BeRemoved, available));
			}
		}
		return ruined;
	}
	
	/**
	 * Removes targetJob and its neighborhood and returns the removed jobs.
     * @deprecated will be private
	 */
    @Deprecated
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved){
		return ruinRoutes_(vehicleRoutes,targetJob,nOfJobs2BeRemoved,null);
	}

	private Collection<Job> ruinRoutes_(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved, Set<Job> available){
		List<Job> unassignedJobs = new ArrayList<Job>();
		int nNeighbors = nOfJobs2BeRemoved - 1;
		removeJob(targetJob,vehicleRoutes);
		unassignedJobs.add(targetJob);
		Iterator<Job> neighborhoodIterator =  jobNeighborhoods.getNearestNeighborsIterator(nNeighbors, targetJob);
		while(neighborhoodIterator.hasNext()){
			Job job = neighborhoodIterator.next();
			if(available!=null) available.remove(job);
			if(removeJob(job,vehicleRoutes)) {
				unassignedJobs.add(job);
			}
		}
		return unassignedJobs;
	}

	private Job pickRandomJob(Set<Job> available) {
		int randomIndex = random.nextInt(available.size());
		int i=0;
		for(Job j : available){
			if(i>=randomIndex) {
				return j;
			}
			else i++;
		}
		return null;
    }

}
