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
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import util.RandomNumberGeneration;
import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.algo.InsertionListener;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleRoute;



/**
 * 
 * @author stefan schroeder
 * 
 */

final class BestInsertion implements InsertionStrategy{
	
	class Insertion {
		
		private final VehicleRoute route;
		
		private final InsertionData insertionData;

		public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
			super();
			this.route = vehicleRoute;
			this.insertionData = insertionData;
		}

		public VehicleRoute getRoute() {
			return route;
		}
		
		public InsertionData getInsertionData() {
			return insertionData;
		}
		
	}
	
	private static Logger logger = Logger.getLogger(BestInsertion.class);

	private Random random = RandomNumberGeneration.getRandom();
	
	private final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;
	
	private final static Vehicle NO_NEW_VEHICLE_YET = null;
	
	private final static Driver NO_NEW_DRIVER_YET = null;
	
	private InsertionListeners insertionsListeners;
	
	private Inserter inserter;
	
	private JobInsertionCostsCalculator bestInsertionCostCalculator;

	private boolean minVehiclesFirst = false;

	public void setRandom(Random random) {
		this.random = random;
	}
	
	public BestInsertion(JobInsertionCostsCalculator jobInsertionCalculator) {
		super();
		this.insertionsListeners = new InsertionListeners();
		inserter = new Inserter(insertionsListeners);
		bestInsertionCostCalculator = jobInsertionCalculator;
		logger.info("initialise " + this);
	}

	@Override
	public String toString() {
		return "[name=bestInsertion]";
	}

	@Override
	public void insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		insertionsListeners.informInsertionStarts(vehicleRoutes,unassignedJobs);
		List<Job> unassignedJobList = new ArrayList<Job>(unassignedJobs);
		Collections.shuffle(unassignedJobList, random);
		for(Job unassignedJob : unassignedJobList){			
			Insertion bestInsertion = null;
			double bestInsertionCost = Double.MAX_VALUE;
			for(VehicleRoute vehicleRoute : vehicleRoutes){
				InsertionData iData = bestInsertionCostCalculator.getInsertionData(vehicleRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost); 
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
				InsertionData newIData = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
				if(newIData.getInsertionCost() < bestInsertionCost){
					bestInsertion = new Insertion(newRoute,newIData);
					bestInsertionCost = newIData.getInsertionCost();
					vehicleRoutes.add(newRoute);
				}
			}			
			if(bestInsertion == null){
				VehicleRoute newRoute = VehicleRoute.emptyRoute();
				InsertionData bestI = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, Double.MAX_VALUE);
				if(bestI instanceof InsertionData.NoInsertionFound){
					throw new IllegalStateException(getErrorMsg(unassignedJob));
				}
				else{
					bestInsertion = new Insertion(newRoute,bestI);
					vehicleRoutes.add(newRoute);
				}
			}
			
			inserter.insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
			
		}
		insertionsListeners.informInsertionEndsListeners(vehicleRoutes);
	}

	private String getErrorMsg(Job unassignedJob) {
		return "given the vehicles, could not insert job\n" +
				"\t" + unassignedJob + 
				"\n\tthis might have the following reasons:\n" + 
				"\t- no vehicle has the capacity to transport the job [check whether there is at least one vehicle that is capable to transport the job]\n" +
				"\t- the time-window cannot be met, even in a commuter tour the time-window is missed [check whether it is possible to reach the time-window on the shortest path or make hard time-windows soft]\n" +
				"\t- if you deal with finite vehicles, and the available vehicles are already fully employed, no vehicle can be found anymore to transport the job [add penalty-vehicles]";
	}

	@Override
	public void removeListener(InsertionListener insertionListener) {
		insertionsListeners.removeListener(insertionListener);
	}

	@Override
	public Collection<InsertionListener> getListeners() {
		return Collections.unmodifiableCollection(insertionsListeners.getListeners());
	}

	@Override
	public void addListener(InsertionListener insertionListener) {
		insertionsListeners.addListener(insertionListener);
		
	}

}
