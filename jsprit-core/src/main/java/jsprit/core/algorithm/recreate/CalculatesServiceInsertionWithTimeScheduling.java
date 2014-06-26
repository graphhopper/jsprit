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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jsprit.core.algorithm.recreate.listener.InsertionStartsListener;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.RandomNumberGeneration;

import org.apache.log4j.Logger;



public class CalculatesServiceInsertionWithTimeScheduling implements JobInsertionCostsCalculator{


	public static class KnowledgeInjection implements InsertionStartsListener {

		private CalculatesServiceInsertionWithTimeScheduling c;
		
		public KnowledgeInjection(CalculatesServiceInsertionWithTimeScheduling c) {
			super();
			this.c = c;
		}

		@Override
		public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes,Collection<Job> unassignedJobs) {
			List<Double> knowledge = new ArrayList<Double>();
			if(vehicleRoutes.isEmpty()){
				System.out.println("hmm");
			}
			for(VehicleRoute route : vehicleRoutes){
				if(route.getDepartureTime() == 21600.){
					System.out.println("hu");
				}
				knowledge.add(route.getDepartureTime());
			}
			c.setDepartureTimeKnowledge(knowledge);	
		}
		
	}
	
private static Logger log = Logger.getLogger(CalculatesServiceInsertionWithTimeScheduling.class);
	
	private JobInsertionCostsCalculator jic;
	
	private List<Double> departureTimeKnowledge = new ArrayList<Double>();
	
	public CalculatesServiceInsertionWithTimeScheduling(JobInsertionCostsCalculator jic, double t, double f) {
		super();
		this.jic = jic;
		log.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=calculatesServiceInsertionWithTimeScheduling]";
	}
	
	

	@Override
	public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore) {
		double departureTime = newVehicleDepartureTime;
		if(currentRoute.isEmpty()){
			if(departureTimeKnowledge.isEmpty()){
				System.out.println("strange");
			}
			else departureTime = departureTimeKnowledge.get(RandomNumberGeneration.getRandom().nextInt(departureTimeKnowledge.size()));
		}
		if(departureTime == 21600){
			System.out.println("hu");
		}
		InsertionData insertionData = jic.getInsertionData(currentRoute, jobToInsert, newVehicle, departureTime, newDriver, bestKnownScore);
		return insertionData;
	}
	
	public void setDepartureTimeKnowledge(List<Double> departureTimes){
		departureTimeKnowledge=departureTimes;
	}

}
