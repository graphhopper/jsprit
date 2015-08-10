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
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.NoiseMaker;
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

public final class RuinWorst extends AbstractRuinStrategy {

	private Logger logger = LogManager.getLogger(RuinWorst.class);

	private VehicleRoutingProblem vrp;

	private NoiseMaker noiseMaker = new NoiseMaker(){

		@Override
		public double makeNoise() {
			return 0;
		}
	};

	public void setNoiseMaker(NoiseMaker noiseMaker) {
		this.noiseMaker = noiseMaker;
	}

	public RuinWorst(VehicleRoutingProblem vrp, final int initialNumberJobsToRemove) {
		super(vrp);
		this.vrp = vrp;
		setRuinShareFactory(new RuinShareFactory() {
			@Override
			public int createNumberToBeRemoved() {
				return initialNumberJobsToRemove;
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
        throw new UnsupportedOperationException("ruinRoutes not supported");
	}

	private void ruin(Collection<VehicleRoute> vehicleRoutes, int nOfJobs2BeRemoved, List<Job> unassignedJobs) {
		LinkedList<Job> availableJobs = new LinkedList<Job>(vrp.getJobs().values());
		int toRemove = nOfJobs2BeRemoved;
		while(toRemove > 0){
			Job worst = getWorst(vehicleRoutes);
			if(worst == null) break;
			if(removeJob(worst,vehicleRoutes)) {
				availableJobs.remove(worst);
				unassignedJobs.add(worst);
			}
			toRemove--;
		}
	}

	private Job getWorst(Collection<VehicleRoute> copied) {
		Job worst = null;
		double bestSavings = Double.MIN_VALUE;

		for(VehicleRoute route : copied) {
			if(route.isEmpty()) continue;
			Map<Job,Double> savingsMap = new HashMap<Job,Double>();
			TourActivity actBefore = route.getStart();
			TourActivity actToEval = null;
			for (TourActivity act : route.getActivities()) {
				if (actToEval == null) {
					actToEval = act;
					continue;
				}
				double savings = savings(route, actBefore, actToEval, act);
				Job job = ((TourActivity.JobActivity) actToEval).getJob();
				if(!savingsMap.containsKey(job)){
					savingsMap.put(job,savings);
				}
				else {
					double s = savingsMap.get(job);
					savingsMap.put(job,s+savings);
				}
				actBefore = actToEval;
				actToEval = act;
			}
			double savings = savings(route, actBefore, actToEval, route.getEnd());
			Job job = ((TourActivity.JobActivity) actToEval).getJob();
			if(!savingsMap.containsKey(job)){
				savingsMap.put(job,savings);
			}
			else {
				double s = savingsMap.get(job);
				savingsMap.put(job,s+savings);
			}
			//getCounts best
			for(Job j : savingsMap.keySet()){
				if(savingsMap.get(j) > bestSavings){
					bestSavings = savingsMap.get(j);
					worst = j;
				}
			}
		}
		return worst;
	}

	private double savings(VehicleRoute route, TourActivity actBefore, TourActivity actToEval, TourActivity act) {
		double savings = c(actBefore, actToEval, route.getVehicle()) + c(actToEval, act, route.getVehicle()) - c(actBefore, act, route.getVehicle());
		return Math.max(0,savings + noiseMaker.makeNoise());
	}

	private double c(TourActivity from, TourActivity to, Vehicle vehicle) {
		return vrp.getTransportCosts().getTransportCost(from.getLocation(),to.getLocation(),from.getEndTime(), DriverImpl.noDriver(), vehicle);
	}

	@Override
	public String toString() {
		return "[name=worstRuin]";
	}

}
