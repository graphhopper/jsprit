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
package jsprit.core.util;

import jsprit.core.algorithm.state.UpdateActivityTimes;
import jsprit.core.problem.cost.TransportTime;
import jsprit.core.problem.driver.DriverImpl;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.RouteActivityVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.Collection;

@Deprecated
public class RouteUtils {
	
	/**
	 * Returns total service time, i.e. sum of service time of each job.
	 * 
	 * @param routes
	 * @return
	 */
	public static double calculateServiceTime(Collection<VehicleRoute> routes) {
		double serviceTime = 0.;
		for(VehicleRoute r : routes){
			for(Job j : r.getTourActivities().getJobs()){
				serviceTime += ((Service)j).getServiceDuration();
			}
		}
		return serviceTime;
	}
	
	/**
	 * Returns total transport time.
	 * 
	 * @param routes
	 * @param transportTimes
	 * @return
	 */
	public static double calculateTransportTime(Collection<VehicleRoute> routes, TransportTime transportTimes) {
		double tpTime = 0.;
		for(VehicleRoute r : routes){
			TourActivity lastact = r.getStart();
			double lastActDepTime = r.getDepartureTime();
			for(TourActivity act : r.getActivities()){
				tpTime += transportTimes.getTransportTime(lastact.getLocation(), act.getLocation(), lastActDepTime, DriverImpl.noDriver(), r.getVehicle());
				lastact=act;
				lastActDepTime=act.getEndTime();
			}
			tpTime+=transportTimes.getTransportTime(lastact.getLocation(), r.getEnd().getLocation(), lastActDepTime, DriverImpl.noDriver(), r.getVehicle());
		}
		return tpTime;
	}
	
	/**
	 * Returns total waiting time.
	 * 
	 * @param routes
	 * @return
	 */
	public static double calculateWaitingTime(Collection<VehicleRoute> routes) {
		double waitingTime = 0.;
		for(VehicleRoute r : routes){
			for(TourActivity act : r.getActivities()){
				waitingTime += Math.max(0., act.getTheoreticalEarliestOperationStartTime() - act.getArrTime());
			}
		}
		return waitingTime;
	}
	
	/**
	 * Returns total operation time.
	 * 
	 * @param routes
	 * @return
	 */
	public static double calulateOperationTime(Collection<VehicleRoute> routes) {
		double opTime = 0.;
		for(VehicleRoute r : routes){
			opTime += r.getEnd().getArrTime() - r.getDepartureTime();
		}
		return opTime;
	}
	
	/**
	 * Updates activity arrival/end-times of activities in specified route.
	 * 
	 * @param route
	 * @param transportTimes
	 */
	public static void updateActivityTimes(VehicleRoute route, TransportTime transportTimes){
		new RouteActivityVisitor().addActivityVisitor(new UpdateActivityTimes(transportTimes)).visit(route);
	}

}
