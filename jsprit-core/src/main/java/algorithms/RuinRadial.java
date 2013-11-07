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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import util.RandomNumberGeneration;
import util.StopWatch;
import basics.Job;
import basics.VehicleRoutingProblem;
import basics.algo.RuinListener;
import basics.algo.RuinListeners;
import basics.route.VehicleRoute;


/**
 * 
 * RuinStrategy that ruins the neighborhood of a randomly selected job. The size and the structure of the neighborhood is defined by 
 * the share of jobs to be removed and the distance between jobs (where distance not necessarily mean Euclidean distance but an arbitrary 
 * measure).
 * 
 * @author stefan
 *
 */
final class RuinRadial implements RuinStrategy {
	
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

	private Logger logger = Logger.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

	private Random random = RandomNumberGeneration.getRandom();

	private JobDistance jobDistance;
	
	private RuinListeners ruinListeners;
	
	public void setRandom(Random random) {
		this.random = random;
	}

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
		this.jobDistance = jobDistance;
		this.fractionOfAllNodes2beRuined = fraction2beRemoved;
		ruinListeners = new RuinListeners();
		calculateDistancesFromJob2Job();
		logger.info("intialise " + this);
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
								return 1;
							} else {
								return -1;
							}
						}
					});
			distanceNodeTree.put(i.getId(), treeSet);
			for (Job j : vrp.getJobs().values()) {
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
	
	@Override
	public String toString() {
		return "[name=radialRuin][fraction="+fractionOfAllNodes2beRuined+"]";
	}

	/**
	 * Ruins the collection of vehicleRoutes, i.e. removes a share of jobs. First, it selects a job randomly. Second, it identifies its neighborhood. And finally, it removes 
	 * the neighborhood plus the randomly selected job from the number of vehicleRoutes. All removed jobs are then returned as a collection.
	 */
	@Override
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
		if(vehicleRoutes.isEmpty()){
			return Collections.EMPTY_LIST;
		}
		int nOfJobs2BeRemoved = getNuOfJobs2BeRemoved();
		if (nOfJobs2BeRemoved == 0) {
			return Collections.EMPTY_LIST;
		}
		Job randomJob = pickRandomJob();
		Collection<Job> unassignedJobs = ruin(vehicleRoutes,randomJob,nOfJobs2BeRemoved);
		return unassignedJobs;
	}
	
	/**
	 * Removes targetJob and its neighborhood and returns the removed jobs.
	 */
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved){
		ruinListeners.ruinStarts(vehicleRoutes);
		List<Job> unassignedJobs = new ArrayList<Job>();
		TreeSet<ReferencedJob> tree = distanceNodeTree.get(targetJob.getId());
		Iterator<ReferencedJob> descendingIterator = tree.descendingIterator();
		int counter = 0;
		while (descendingIterator.hasNext() && counter < nOfJobs2BeRemoved) {
			ReferencedJob refJob = descendingIterator.next();
			Job job = refJob.getJob();
			unassignedJobs.add(job);
			counter++;
			boolean removed = false;
			for (VehicleRoute route : vehicleRoutes) {
				removed = route.getTourActivities().removeJob(job);; 
				if (removed) {
					ruinListeners.removed(job,route);
					break;
				}
			}
		}
		ruinListeners.ruinEnds(vehicleRoutes, unassignedJobs);
		return unassignedJobs;
	}
	
	private Job pickRandomJob() {
		int totNuOfJobs = vrp.getJobs().values().size();
		int randomIndex = random.nextInt(totNuOfJobs);
		Job job = new ArrayList<Job>(vrp.getJobs().values()).get(randomIndex);
		return job;
	}

	private int getNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size()
				* fractionOfAllNodes2beRuined);
	}

	@Override
	public void addListener(RuinListener ruinListener) {
		ruinListeners.addListener(ruinListener);
	}

	@Override
	public void removeListener(RuinListener ruinListener) {
		ruinListeners.removeListener(ruinListener);
	}

	@Override
	public Collection<RuinListener> getListeners() {
		return ruinListeners.getListeners();
	}


}
