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
package jsprit.core.algorithm.recreate;

import jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.RandomNumberGeneration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;




/**
 * Best insertion that insert the job where additional costs are minimal.
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
	
	private static Logger logger = LogManager.getLogger(BestInsertion.class);

	private Random random = RandomNumberGeneration.getRandom();
	
	private final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;
	
	private final static Vehicle NO_NEW_VEHICLE_YET = null;
	
	private final static Driver NO_NEW_DRIVER_YET = null;
	
	private InsertionListeners insertionsListeners;
	
	private Inserter inserter;
	
	private JobInsertionCostsCalculator bestInsertionCostCalculator;

	public void setRandom(Random random) {
		this.random = random;
	}
	
	public BestInsertion(JobInsertionCostsCalculator jobInsertionCalculator, VehicleRoutingProblem vehicleRoutingProblem) {
		super();
		this.insertionsListeners = new InsertionListeners();
		inserter = new Inserter(insertionsListeners, vehicleRoutingProblem);
		bestInsertionCostCalculator = jobInsertionCalculator;
		logger.info("initialise " + this);
	}

	@Override
	public String toString() {
		return "[name=bestInsertion]";
	}

	@Override
	public Collection<Job> insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
		insertionsListeners.informInsertionStarts(vehicleRoutes,unassignedJobs);
        List<Job> badJobs = new ArrayList<Job>(unassignedJobs.size());
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
            VehicleRoute newRoute = VehicleRoute.emptyRoute();
            InsertionData newIData = bestInsertionCostCalculator.getInsertionData(newRoute, unassignedJob, NO_NEW_VEHICLE_YET, NO_NEW_DEPARTURE_TIME_YET, NO_NEW_DRIVER_YET, bestInsertionCost);
            if(!(newIData instanceof NoInsertionFound)){
                if(newIData.getInsertionCost() < bestInsertionCost){
                    bestInsertion = new Insertion(newRoute,newIData);
                    vehicleRoutes.add(newRoute);
                }
            }
            if(bestInsertion == null) badJobs.add(unassignedJob);
            else inserter.insertJob(unassignedJob, bestInsertion.getInsertionData(), bestInsertion.getRoute());
        }
		insertionsListeners.informInsertionEndsListeners(vehicleRoutes);
        return badJobs;
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
