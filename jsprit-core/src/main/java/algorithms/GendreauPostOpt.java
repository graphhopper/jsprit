/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;

import basics.Job;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategyModule;
import basics.algo.SearchStrategyModuleListener;
import basics.route.TourActivity;
import basics.route.VehicleRoute;
import basics.route.TourActivity.JobActivity;

import util.RandomNumberGeneration;

final class GendreauPostOpt implements SearchStrategyModule{

	private final static Logger log = Logger.getLogger(GendreauPostOpt.class);
	
	private final static String NAME = "gendreauPostOpt"; 
	
	private final RuinStrategy ruin;
	
	private final VehicleRoutingProblem vrp;
	
	private final AbstractInsertionStrategy insertionStrategy;
	
	private final RouteAlgorithm routeAlgorithm;
	
	private VehicleFleetManager fleetManager;

	private Random random = RandomNumberGeneration.getRandom();
	
	private int nOfIterations = 10;

	private double shareOfJobsToRuin = 0.15;

	public void setShareOfJobsToRuin(double shareOfJobsToRuin) {
		this.shareOfJobsToRuin = shareOfJobsToRuin;
	}

	public GendreauPostOpt(VehicleRoutingProblem vrp, RuinStrategy ruin, AbstractInsertionStrategy insertionStrategy) {
		super();
		this.routeAlgorithm = insertionStrategy.getRouteAlgorithm();
		this.ruin = ruin;
		this.vrp = vrp;
		this.insertionStrategy = insertionStrategy;
	}

	@Override
	public String toString() {
		return "[name=gendreauPostOpt][iterations="+nOfIterations+"][share2ruin="+shareOfJobsToRuin+"]";
	}
	
	public void setRandom(Random random) {
		this.random = random;
	}


	public void setNuOfIterations(int nOfIterations) {
		this.nOfIterations = nOfIterations;
	}

	public void setFleetManager(VehicleFleetManager vehicleFleetManager) {
		this.fleetManager = vehicleFleetManager;
		
	}

	@Override
	public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
//		log.info("run gendreau postopt");
		VehicleRoutingProblemSolution bestSolution = vrpSolution;
		int itersWithoutImprovement = 0;
		
		for(int i=0;i<nOfIterations;i++){
			List<VehicleRoute> copiedRoutes = copyRoutes(bestSolution.getRoutes());
			iniFleet(copiedRoutes);
				
			VehicleRoute route2split = pickRouteThatHasAtLeastTwoJobs(copiedRoutes);
			if(route2split == null) continue;
			List<Job> jobsInRoute = getJobs(route2split);
			Set<Job> unassignedJobs = new HashSet<Job>();
			unassignedJobs.addAll(jobsInRoute);
			copiedRoutes.remove(route2split);
			
			Collections.shuffle(jobsInRoute,random);
			Job targetJob = jobsInRoute.get(0);
			int nOfJobs2BeRemovedAdditionally = (int) (shareOfJobsToRuin*(double)vrp.getJobs().size());
			Collection<Job> unassignedJobsList = ruin.ruin(copiedRoutes, targetJob, nOfJobs2BeRemovedAdditionally);
			unassignedJobs.addAll(unassignedJobsList);
			
			VehicleRoute emptyRoute1 = VehicleRoute.emptyRoute();
			copiedRoutes.add(emptyRoute1);
			routeAlgorithm.insertJob(targetJob, routeAlgorithm.calculateBestInsertion(emptyRoute1, targetJob, Double.MAX_VALUE), emptyRoute1);
			unassignedJobs.remove(targetJob);
			
			VehicleRoute emptyRoute2 = VehicleRoute.emptyRoute();
			copiedRoutes.add(emptyRoute2);
			Job job2 = jobsInRoute.get(1);
			routeAlgorithm.insertJob(job2, routeAlgorithm.calculateBestInsertion(emptyRoute2, job2, Double.MAX_VALUE), emptyRoute2);
			unassignedJobs.remove(job2);
			
			insertionStrategy.run(copiedRoutes, unassignedJobs, Double.MAX_VALUE);
			double cost = getCost(copiedRoutes);
			
			if(cost < bestSolution.getCost()){
//				log.info("BING - new: " + cost + " old: " + bestSolution.getCost());
				bestSolution = new VehicleRoutingProblemSolution(copiedRoutes, cost);
				itersWithoutImprovement=0;
			}
			else{
				itersWithoutImprovement++;
				if(itersWithoutImprovement > 200){
//					log.info("BREAK i="+i);
					break;
				}
			}
		}
		return bestSolution;
	}

	private List<VehicleRoute> copyRoutes(Collection<VehicleRoute> routes) {
		List<VehicleRoute> routeList = new ArrayList<VehicleRoute>();
		for(VehicleRoute r : routes){
			routeList.add(VehicleRoute.copyOf(r));
		}
		return routeList;
	}

	private void iniFleet(Collection<VehicleRoute> routes) {
		fleetManager.unlockAll();
		for(VehicleRoute route : routes){
			if(!route.isEmpty()){
				fleetManager.lock(route.getVehicle());
			}
		}
	}

	private double getCost(Collection<VehicleRoute> routes) {
		double c = 0.0;
		for(VehicleRoute r : routes){
			c+=r.getCost();
		}
		return c;
	}

	private List<Job> getJobs(VehicleRoute route2split) {
		Set<Job> jobs = new HashSet<Job>();
		for(TourActivity act : route2split.getTourActivities().getActivities()){
			if(act instanceof JobActivity){
				jobs.add(((JobActivity) act).getJob());
			}
		}
		return new ArrayList<Job>(jobs);
	}

	private VehicleRoute pickRouteThatHasAtLeastTwoJobs(Collection<VehicleRoute> routeList) {
		List<VehicleRoute> routes = new ArrayList<VehicleRoute>();
		for(VehicleRoute r : routeList){
			if(getJobs(r).size() > 1){
				routes.add(r);
			}
		}
		if(routes.isEmpty()) return null;
		Collections.shuffle(routes,random);
		return routes.get(0);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void addModuleListener(SearchStrategyModuleListener moduleListener) {
		// TODO Auto-generated method stub
		
	}
}
