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


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import algorithms.RouteStates.ActivityState;
import basics.algo.AlgorithmEndsListener;
import basics.algo.JobInsertedListener;
import basics.route.TourActivity;
import basics.route.VehicleRoute;
import basics.route.TourActivity.JobActivity;
import basics.Job;
import basics.Service;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;

class JobObserver implements JobInsertedListener, BeforeJobInsertionListener, AlgorithmEndsListener{

	private static class Info {
		double depTime;
		double tourSize;
		double insertionIndex;
		double error;
		public Info(double depTime, double tourSize, double insertionIndex,
				double error) {
			super();
			this.depTime = depTime;
			this.tourSize = tourSize;
			this.insertionIndex = insertionIndex;
			this.error = error;
		}
		
	}
	
	private String locationId = "70";
	
	private double routeCostBefore;
	private double estimatedMC;
	private boolean beforeFirst = false;
	
	private RouteStates actStates;
	
	public void setActivityStates(RouteStates actStates){
		this.actStates = actStates;
	}

	public ActivityState state(TourActivity act){
		return actStates.getState(act);
	}

	
	Collection<Info> infos = new ArrayList<Info>();
	
	@Override
	public void informJobInserted(int nOfJobsStill2Recreate, Job job2insert, VehicleRoute insertedIn) {
		if(job2insert instanceof Service){
			if(((Service) job2insert).getLocationId().equals(locationId)){
				double actualMC = insertedIn.getCost()-routeCostBefore;
				TourActivity act = getAct(job2insert,insertedIn);
				double error = (estimatedMC-actualMC);
				int tourSize = insertedIn.getTourActivities().getActivities().size();
				int insertionIndex = getIndexOf(job2insert, insertedIn);
//				infos.add(new Info())
				double depTime = state(act).getEarliestOperationStart()+act.getOperationTime();
				infos.add(new Info(depTime,tourSize,insertionIndex,error));
//				System.out.println("[id=1][tourSize="+tourSize+"][index="+insertionIndex+
//						"][earliestDeparture="+depTime+
//						"][tourCostBefore="+routeCostBefore+"][routeCostAfter="+insertedIn.getCost()+"]" +
//						"[estimated="+Math.round(estimatedMC)+"][actual="+Math.round(actualMC)+"][error(abs)="+error + 
//						"][errorPerNextCustomer="+ (error/(double)(tourSize-insertionIndex)) + "]");
				routeCostBefore = 0.0;
				estimatedMC = 0.0;
				if(!beforeFirst) throw new IllegalStateException("ähhh");
				beforeFirst = false;
			}
		}
	}

	private TourActivity getAct(Job job2insert, VehicleRoute insertedIn) {
		for(TourActivity act : insertedIn.getTourActivities().getActivities()){
			if(act instanceof JobActivity){
				if(((JobActivity) act).getJob().getId().equals(job2insert.getId())){
					return act; 
				}
			}
		}
		return null;
	}

	private int getIndexOf(Job job2insert, VehicleRoute insertedIn) {
		int index=0;
		for(TourActivity act : insertedIn.getTourActivities().getActivities()){
			if(act instanceof JobActivity){
				if(((JobActivity) act).getJob().getId().equals(job2insert.getId())){
					return index; 
				}
			}
			index++;
		}
		return -1;
	}

	@Override
	public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
		if(job instanceof Service){
			if(((Service) job).getLocationId().equals(locationId)){
//				System.out.println("[id=1][tourSize="+route.getTour().getActivities().size()+"][tourCost="+route.getCost()+"]" +
//						"[estimatedMarginalInsertionCost="+data.getInsertionCost()+"]");
				routeCostBefore = route.getCost();
				estimatedMC = data.getInsertionCost();
				beforeFirst = true;
			}
		}
	}

	@Override
	public void informAlgorithmEnds(VehicleRoutingProblem problem, Collection<VehicleRoutingProblemSolution> solutions) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter("output/errorAna.txt"));
			for(Info info : infos){
				writer.write(new StringBuilder().append(info.depTime).append(";").append(info.tourSize).append(";").append(info.insertionIndex).append(";")
						.append(info.error).append("\n").toString());
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
