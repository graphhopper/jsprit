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

import jsprit.core.algorithm.listener.IterationStartsListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.util.RandomUtils;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;


/**
 * Ruin strategy that ruins current solution randomly. I.e.
 * customer are removed randomly from current solution.
 * 
 * @author stefan schroeder
 * 
 */

public final class RuinClusters extends AbstractRuinStrategy implements IterationStartsListener {

	@Override
	public void informIterationStarts(int i, VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		minPts = 1 + random.nextInt(2);
		epsFactor = 0.5 + random.nextDouble();
	}

	public static class JobActivityWrapper implements Clusterable {

		private TourActivity.JobActivity jobActivity;

		public JobActivityWrapper(TourActivity.JobActivity jobActivity) {
			this.jobActivity = jobActivity;
		}

		@Override
		public double[] getPoint() {
			return new double[]{ jobActivity.getLocation().getCoordinate().getX(), jobActivity.getLocation().getCoordinate().getY() };
		}

		public TourActivity.JobActivity getActivity(){
			return jobActivity;
		}
	}

	private Logger logger = LogManager.getLogger(RuinClusters.class);

	private VehicleRoutingProblem vrp;


	private JobNeighborhoods jobNeighborhoods;

	private int noClusters = 2;

	private int minPts = 1;

	private double epsFactor = 0.8;

	public RuinClusters(VehicleRoutingProblem vrp, final int initialNumberJobsToRemove, JobNeighborhoods jobNeighborhoods) {
		super(vrp);
		this.vrp = vrp;
		setRuinShareFactory(new RuinShareFactory() {
			@Override
			public int createNumberToBeRemoved() {
				return initialNumberJobsToRemove;
			}
		});
		this.jobNeighborhoods = jobNeighborhoods;
        logger.debug("initialise {}", this);
	}

	public void setNoClusters(int noClusters) {
		this.noClusters = noClusters;
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
		if(vrp.getJobs().values().size() == 0) return;
		Map<Job,VehicleRoute> mappedRoutes = map(vehicleRoutes);
		int toRemove = nOfJobs2BeRemoved;

		Collection<Job> lastRemoved = new ArrayList<Job>();
		Set<VehicleRoute> ruined = new HashSet<VehicleRoute>();
		Set<Job> removed = new HashSet<Job>();
		Set<VehicleRoute> cycleCandidates = new HashSet<VehicleRoute>();
		while(toRemove > 0) {
			Job target;
			VehicleRoute targetRoute = null;
			if(lastRemoved.isEmpty()){
				target = RandomUtils.nextJob(vrp.getJobs().values(), random);
				targetRoute = mappedRoutes.get(target);
			}
			else{
				target = RandomUtils.nextJob(lastRemoved, random);
				Iterator<Job> neighborIterator = jobNeighborhoods.getNearestNeighborsIterator(nOfJobs2BeRemoved,target);
				while(neighborIterator.hasNext()){
					Job j = neighborIterator.next();
					if(!removed.contains(j) && !ruined.contains(mappedRoutes.get(j))){
						targetRoute = mappedRoutes.get(j);
						break;
					}
				}
				lastRemoved.clear();
			}
			if(targetRoute == null) break;
			if(cycleCandidates.contains(targetRoute)) break;
			if(ruined.contains(targetRoute)) {
				cycleCandidates.add(targetRoute);
				break;
			}
			DBSCANClusterer dbscan = new DBSCANClusterer(vrp.getTransportCosts());
			dbscan.setRandom(random);
			dbscan.setMinPts(minPts);
			dbscan.setEpsFactor(epsFactor);
			List<Job> cluster = dbscan.getRandomCluster(targetRoute);
			for(Job j : cluster){
				if(toRemove == 0) break;
				if(removeJob(j, vehicleRoutes)) {
					lastRemoved.add(j);
					unassignedJobs.add(j);
				}
				toRemove--;
			}
			ruined.add(targetRoute);
		}
	}

	private List<JobActivityWrapper> wrap(List<TourActivity> activities) {
		List<JobActivityWrapper> wl = new ArrayList<JobActivityWrapper>();
		for(TourActivity act : activities){
			wl.add(new JobActivityWrapper((TourActivity.JobActivity) act));
		}
		return wl;
	}

	private Map<Job, VehicleRoute> map(Collection<VehicleRoute> vehicleRoutes) {
		Map<Job,VehicleRoute> map = new HashMap<Job, VehicleRoute>(vrp.getJobs().size());
		for(VehicleRoute r : vehicleRoutes){
			for(Job j : r.getTourActivities().getJobs()){
				map.put(j,r);
			}
		}
		return map;
	}

	@Override
	public String toString() {
		return "[name=clusterRuin]";
	}

}
