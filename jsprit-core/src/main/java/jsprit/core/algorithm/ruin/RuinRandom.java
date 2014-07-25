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
package jsprit.core.algorithm.ruin;

import jsprit.core.algorithm.ruin.listener.RuinListener;
import jsprit.core.algorithm.ruin.listener.RuinListeners;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.util.RandomNumberGeneration;
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

final class RuinRandom implements RuinStrategy {
	
	private Logger logger = LogManager.getLogger(RuinRandom.class);

	private VehicleRoutingProblem vrp;

	private double fractionOfAllNodes2beRuined;

	private Random random = RandomNumberGeneration.getRandom();
	
	private RuinListeners ruinListeners;

	public void setRandom(Random random) {
		this.random = random;
	}

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
		ruinListeners = new RuinListeners();
		logger.info("initialise " + this);
		logger.info("done");
	}

	/**
	 * Removes a fraction of jobs from vehicleRoutes. 
	 * 
	 * <p>The number of jobs is calculated as follows: Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined).
	 */
	@Override
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
		ruinListeners.ruinStarts(vehicleRoutes);
		List<Job> unassignedJobs = new ArrayList<Job>();
		int nOfJobs2BeRemoved = selectNuOfJobs2BeRemoved();
		ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
		ruinListeners.ruinEnds(vehicleRoutes, unassignedJobs);
		return unassignedJobs;
	}

	/**
	 * Removes nOfJobs2BeRemoved from vehicleRoutes, including targetJob.
	 */
	@Override
	public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes, Job targetJob, int nOfJobs2BeRemoved) {
		ruinListeners.ruinStarts(vehicleRoutes);
		List<Job> unassignedJobs = new ArrayList<Job>();
		if(targetJob != null){
			boolean removed = false;
			for (VehicleRoute route : vehicleRoutes) {
				removed = route.getTourActivities().removeJob(targetJob);
				if (removed) {
					nOfJobs2BeRemoved--;
					unassignedJobs.add(targetJob);
					ruinListeners.removed(targetJob,route);
					break;
				}
			}
		}
		ruin(vehicleRoutes, nOfJobs2BeRemoved, unassignedJobs);
		ruinListeners.ruinEnds(vehicleRoutes, unassignedJobs);
		return unassignedJobs;
	}

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
			for (VehicleRoute route : vehicleRoutes) {
				boolean removed = route.getTourActivities().removeJob(job);
				if (removed) {
					ruinListeners.removed(job,route);
					break;
				}
			}
		}
	}

		
	@Override
	public String toString() {
		return "[name=randomRuin][fraction="+fractionOfAllNodes2beRuined+"]";
	}
	
	private Job pickRandomJob(LinkedList<Job> availableJobs) {
		int randomIndex = random.nextInt(availableJobs.size());
		return availableJobs.get(randomIndex);
	}

	private int selectNuOfJobs2BeRemoved() {
		return (int) Math.ceil(vrp.getJobs().values().size() * fractionOfAllNodes2beRuined);
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
