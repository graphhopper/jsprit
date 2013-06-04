/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
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
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategyModule;
import basics.route.VehicleRoute;



final class RuinRadial implements RuinStrategy {

	private final static String NAME = "radialRuin";
	
	/**
	 * returns a new creation of instance of ruinRadial
	 * @param vrp
	 * @param fraction TODO
	 * @param jobDistance
	 * @param jobRemover TODO
	 * @param routeUpdater TODO
	 * @return
	 */
	static RuinRadial newInstance(VehicleRoutingProblem vrp, double fraction, JobDistance jobDistance, JobRemover jobRemover, VehicleRouteUpdater routeUpdater){
		return new RuinRadial(vrp, fraction, jobDistance, jobRemover, routeUpdater);
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

	private Logger logger = Logger.getLogger(RuinRadial.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	private Map<String, TreeSet<ReferencedJob>> distanceNodeTree = new HashMap<String, TreeSet<ReferencedJob>>();

	private Random random = RandomNumberGeneration.getRandom();

	private JobDistance jobDistance;
	
	private JobRemover jobRemover;
	
	private VehicleRouteUpdater routeUpdater;

	public void setRandom(Random random) {
		this.random = random;
	}

	public RuinRadial(VehicleRoutingProblem vrp, double fraction, JobDistance jobDistance, JobRemover jobRemover, VehicleRouteUpdater routeUpdater) {
		super();
		this.vrp = vrp;
		this.jobDistance = jobDistance;
		this.jobRemover = jobRemover;
		this.routeUpdater = routeUpdater;
		this.fractionOfAllNodes2beRuined = fraction;
		calculateDistancesFromJob2Job();
		logger.info("intialise " + this);
	}

	public void setRuinFraction(double fractionOfAllNodes) {
		this.fractionOfAllNodes2beRuined = fractionOfAllNodes;
		logger.info("fraction set " + this);
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
				double distance = jobDistance.calculateDistance(i, j);
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
	
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved){
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
				removed = jobRemover.removeJobWithoutTourUpdate(job, route); 
				if (removed) {
					break;
				}
			}
		}
		for(VehicleRoute route : vehicleRoutes){
			routeUpdater.updateRoute(route);
		}
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

//	@Override
//	public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
//		ruin(vrpSolution.getRoutes());
//		return vrpSolution;
//	}
//
//	@Override
//	public String getName() {
//		return NAME;
//	}

}
