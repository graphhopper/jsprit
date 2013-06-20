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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import util.RandomNumberGeneration;
import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.route.VehicleRoute;



/**
 * 
 * @author stefan schroeder
 * 
 */

final class BestInsertion extends AbstractInsertionStrategy{
	
	public static BestInsertion newInstance(RouteAlgorithm routeAlgorithm){
			return new BestInsertion(routeAlgorithm);
	}
	
	private static Logger logger = Logger.getLogger(BestInsertion.class);

	private Random random = RandomNumberGeneration.getRandom();
	
	private RouteAlgorithm routeAlgorithm;
	
	public void setExperimentalPreferredRoute(Map<String, VehicleRoute> experimentalPreferredRoute) {
	}

	private boolean allowUnassignedJobs = false;
	
	private boolean fixRouteSet = false;
	
	private boolean minVehiclesFirst = false;
	
	public void setFixRouteSet(boolean fixRouteSet) {
		this.fixRouteSet = fixRouteSet;
	}

	public void setRandom(Random random) {
		this.random = random;
	}
	
	public BestInsertion(RouteAlgorithm routeAlgorithm) {
		super();
		this.routeAlgorithm = routeAlgorithm;
		logger.info("initialise " + this);
	}
	
	public RouteAlgorithm getRouteAlgorithm(){
		return routeAlgorithm;
	}

	@Override
	public String toString() {
		return "[name=bestInsertion]";
	}

	@Override
	public void run(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs, double result2beat) {
		List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
		Collections.shuffle(unassignedJobList, random);
		informInsertionStarts(vehicleRoutes,unassignedJobs.size());
		int inserted = 0;
		List<String> reasons = new ArrayList<String>();
		for(Job unassignedJob : unassignedJobList){
			
			VehicleRoute insertIn = null;
			Insertion bestInsertion = null;
			double bestInsertionCost = Double.MAX_VALUE;
			for(VehicleRoute vehicleRoute : vehicleRoutes){
				InsertionData iData = routeAlgorithm.calculateBestInsertion(vehicleRoute, unassignedJob, bestInsertionCost);
				if(iData instanceof NoInsertionFound) {
					continue;
				}
				if(iData.getInsertionCost() < bestInsertionCost){
					bestInsertion = new Insertion(vehicleRoute,iData);
					bestInsertionCost = iData.getInsertionCost();
				}
			}
			if(!minVehiclesFirst){
				VehicleRoute newRoute = VehicleRoute.emptyRoute();
				InsertionData newIData = routeAlgorithm.calculateBestInsertion(newRoute, unassignedJob, Double.MAX_VALUE);
				if(newIData.getInsertionCost() < bestInsertionCost){
					bestInsertion = new Insertion(newRoute,newIData);
					bestInsertionCost = newIData.getInsertionCost();
					vehicleRoutes.add(newRoute);
				}
			}
			if(bestInsertion != null){
				informBeforeJobInsertion(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
				insertIn = bestInsertion.getRoute();
//				logger.debug("insert job="+unassignedJob+" at index=" + bestInsertion.getInsertionData().getInsertionIndex() + " delta cost=" + bestInsertion.getInsertionData().getInsertionCost());
				routeAlgorithm.insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
			} 
			else {
				if(fixRouteSet){
					if(allowUnassignedJobs) logger.warn("cannot insert job yet " + unassignedJob);
					else throw new IllegalStateException("given the vehicles, could not insert job\n"); 
				}
				else{
					VehicleRoute newRoute = VehicleRoute.emptyRoute();
					InsertionData bestI = routeAlgorithm.calculateBestInsertion(newRoute, unassignedJob, Double.MAX_VALUE);
					if(bestI instanceof InsertionData.NoInsertionFound){
						if(allowUnassignedJobs){
							logger.warn("cannot insert job yet " + unassignedJob);
						}
						else {
							for(String s : reasons){
								System.out.println("reason="+s);
							}
							throw new IllegalStateException("given the vehicles, could not insert job\n" +
								"\t" + unassignedJob + 
								"\n\tthis might have the following reasons:\n" + 
								"\t- no vehicle has the capacity to transport the job [check whether there is at least one vehicle that is capable to transport the job]\n" +
								"\t- the time-window cannot be met, even in a commuter tour the time-window is missed [check whether it is possible to reach the time-window on the shortest path or make hard time-windows soft]\n" +
								"\t- if you deal with finite vehicles, and the available vehicles are already fully employed, no vehicle can be found anymore to transport the job [add penalty-vehicles]"
								);
						}
					}
					else{
						insertIn = newRoute;
						informBeforeJobInsertion(unassignedJob,bestI,newRoute);
						routeAlgorithm.insertJob(unassignedJob,bestI,newRoute);
						vehicleRoutes.add(newRoute);
					}
				}
			}
			inserted++;
			informJobInserted((unassignedJobList.size()-inserted), unassignedJob, insertIn);
		}
		informInsertionEndsListeners(vehicleRoutes);
	}

}
