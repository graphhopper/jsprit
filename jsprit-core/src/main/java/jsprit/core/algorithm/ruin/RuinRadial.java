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
import jsprit.core.util.StopWatch;
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
final class RuinRadial extends AbstractRuinStrategy {
	
	static interface JobNeighborhoods {
		
		public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo);
		
	}
	
	static class NeighborhoodIterator implements Iterator<Job>{

		private static Logger log = LogManager.getLogger(NeighborhoodIterator.class);
		
		private Iterator<ReferencedJob> jobIter;
		
		private int nJobs;
		
		private int jobCount = 0;
		
		public NeighborhoodIterator(Iterator<ReferencedJob> jobIter, int nJobs) {
			super();
			this.jobIter = jobIter;
			this.nJobs = nJobs;
		}

		@Override
		public boolean hasNext() {
			if(jobCount < nJobs){
				boolean hasNext = jobIter.hasNext();
				if(!hasNext) log.warn("more jobs are requested then iterator can iterate over. probably the number of neighbors memorized in JobNeighborhoods is too small");
				return hasNext;
			}
			return false;
		}

		@Override
		public Job next() {
			ReferencedJob next = jobIter.next();
			jobCount++;
			return next.getJob();
		}

		@Override
		public void remove() {
			jobIter.remove();
		}
		
	}
	
	static class JobNeighborhoodsImpl implements JobNeighborhoods {

		private static Logger logger = LogManager.getLogger(JobNeighborhoodsImpl.class);
		
		private VehicleRoutingProblem vrp;
		
		private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();
		
		private JobDistance jobDistance;
		
		public JobNeighborhoodsImpl(VehicleRoutingProblem vrp, JobDistance jobDistance) {
			super();
			this.vrp = vrp;
			this.jobDistance = jobDistance;
			logger.info("intialise " + this);
		}
		
		public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo){
			TreeSet<ReferencedJob> tree = distanceNodeTree.get(neighborTo.getId());
			Iterator<ReferencedJob> descendingIterator = tree.iterator();
			return new NeighborhoodIterator(descendingIterator, nNeighbors);
		}
		
		public void initialise(){
			logger.info("calculates and memorizes distances from EACH job to EACH job --> n^2 calculations");
			calculateDistancesFromJob2Job();
		}
		
		private void calculateDistancesFromJob2Job() {
			logger.info("preprocess distances between locations ...");
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			int nuOfDistancesStored = 0;
			for (Job i : vrp.getJobs().values()) {
				TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
						new Comparator<ReferencedJob>() {
							@Override
							public int compare(ReferencedJob o1, ReferencedJob o2) {
								if (o1.getDistance() <= o2.getDistance()) {
									return -1;
								} else {
									return 1;
								}
							}
						});
				distanceNodeTree.put(i.getId(), treeSet);
				for (Job j : vrp.getJobs().values()) {
					if(i==j) continue;
					double distance = jobDistance.getDistance(i, j);
					ReferencedJob refNode = new ReferencedJob(j, distance);
					treeSet.add(refNode);
					nuOfDistancesStored++;
				}

			}
			stopWatch.stop();
			logger.info("preprocessing comp-time: " + stopWatch + "; nuOfDistances stored: " + nuOfDistancesStored + "; estimated memory: " + 
					(distanceNodeTree.keySet().size()*64+nuOfDistancesStored*92) + " bytes");
		}
		
	}

	static class JobNeighborhoodsImplWithCapRestriction implements JobNeighborhoods {

		private static Logger logger = LogManager.getLogger(JobNeighborhoodsImpl.class);
		
		private VehicleRoutingProblem vrp;
		
		private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();
		
		private JobDistance jobDistance;
		
		private int capacity;
		
		public JobNeighborhoodsImplWithCapRestriction(VehicleRoutingProblem vrp, JobDistance jobDistance, int capacity) {
			super();
			this.vrp = vrp;
			this.jobDistance = jobDistance;
			this.capacity = capacity;
			logger.info("intialise " + this);
		}
		
		public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo){
			TreeSet<ReferencedJob> tree = distanceNodeTree.get(neighborTo.getId());
			Iterator<ReferencedJob> descendingIterator = tree.iterator();
			return new NeighborhoodIterator(descendingIterator, nNeighbors);
		}
		
		public void initialise(){
			logger.info("calculates distances from EACH job to EACH job --> n^2="+Math.pow(vrp.getJobs().values().size(), 2) + " calculations, but 'only' "+(vrp.getJobs().values().size()*capacity)+ " are cached.");
			if(capacity==0) return;
			calculateDistancesFromJob2Job();
		}
		
		private void calculateDistancesFromJob2Job() {
			logger.info("preprocess distances between locations ...");
			StopWatch stopWatch = new StopWatch();
			stopWatch.start();
			int nuOfDistancesStored = 0;
			for (Job i : vrp.getJobs().values()) {
				TreeSet<ReferencedJob> treeSet = new TreeSet<ReferencedJob>(
						new Comparator<ReferencedJob>() {
							@Override
							public int compare(ReferencedJob o1, ReferencedJob o2) {
								if (o1.getDistance() <= o2.getDistance()) {
									return -1;
								} else {
									return 1;
								}
							}
						});
				distanceNodeTree.put(i.getId(), treeSet);
				for (Job j : vrp.getJobs().values()) {
					if(i==j) continue;
					double distance = jobDistance.getDistance(i, j);
					ReferencedJob refNode = new ReferencedJob(j, distance);
					if(treeSet.size() < capacity){
						treeSet.add(refNode);
						nuOfDistancesStored++;
					}
					else{
						if(treeSet.last().getDistance() > distance){
							treeSet.pollLast();
							treeSet.add(refNode);
						}
					}
				}
				assert treeSet.size() <= capacity : "treeSet.size() is bigger than specified capacity";

			}
			stopWatch.stop();
			logger.info("preprocessing comp-time: " + stopWatch + "; nuOfDistances stored: " + nuOfDistancesStored + "; estimated memory: " + 
					(distanceNodeTree.keySet().size()*64+nuOfDistancesStored*92) + " bytes");
		}
		
		@Override
		public String toString() {
			return "[name=neighborhoodWithCapRestriction][capacity="+capacity+"]";
		}
		
	}
	
	
	static class ReferencedJob {
		private Job job;
		private double distance;

		public ReferencedJob(Job job, double distance) {
			super();
			this.job = job;
			this.distance = distance;
		}

		public Job getJob() {
			return job;
		}

		public double getDistance() {
			return distance;
		}
	}

	private Logger logger = LogManager.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	private JobNeighborhoods jobNeighborhoods;


	/**
	 * Constructs RuinRadial.
	 * 
	 * @param vrp
	 * @param fraction2beRemoved i.e. the share of jobs to be removed (relative to the total number of jobs in vrp)
	 * @param jobDistance i.e. a measure to define the distance between two jobs and whether they are located close or distant to eachother
	 */
	public RuinRadial(VehicleRoutingProblem vrp, double fraction2beRemoved, JobDistance jobDistance) {
		super();
		this.vrp = vrp;
		this.fractionOfAllNodes2beRuined = fraction2beRemoved;
		int nJobsToMemorize = (int) Math.ceil(vrp.getJobs().values().size()*fraction2beRemoved);
		JobNeighborhoodsImplWithCapRestriction jobNeighborhoodsImpl = new JobNeighborhoodsImplWithCapRestriction(vrp, jobDistance, nJobsToMemorize);
		jobNeighborhoodsImpl.initialise();
		jobNeighborhoods = jobNeighborhoodsImpl;
		logger.info("intialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=radialRuin][fraction="+fractionOfAllNodes2beRuined+"]";
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
		int nOfJobs2BeRemoved = getNuOfJobs2BeRemoved();
		if (nOfJobs2BeRemoved == 0) {
			return Collections.emptyList();
		}
		Job randomJob = pickRandomJob();
		return ruinRoutes(vehicleRoutes, randomJob, nOfJobs2BeRemoved);
	}
	
	/**
	 * Removes targetJob and its neighborhood and returns the removed jobs.
	 */
	public Collection<Job> ruinRoutes(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved){
        List<Job> unassignedJobs = new ArrayList<Job>();
		int nNeighbors = nOfJobs2BeRemoved - 1;
		removeJob(targetJob,vehicleRoutes);
		unassignedJobs.add(targetJob);
		Iterator<Job> neighborhoodIterator =  jobNeighborhoods.getNearestNeighborsIterator(nNeighbors, targetJob);
		while(neighborhoodIterator.hasNext()){
			Job job = neighborhoodIterator.next();
			removeJob(job,vehicleRoutes);
			unassignedJobs.add(job);
		}
        return unassignedJobs;
	}

	private Job pickRandomJob() {
		int totNuOfJobs = vrp.getJobs().values().size();
		int randomIndex = random.nextInt(totNuOfJobs);
		return new ArrayList<Job>(vrp.getJobs().values()).get(randomIndex);
    }

	private int getNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size()
				* fractionOfAllNodes2beRuined);
	}


}
