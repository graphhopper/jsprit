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

import org.apache.log4j.Logger;

import algorithms.RouteStates.ActivityState;
import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.Service;
import basics.route.ServiceActivity;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


/**
 * 
 * @author stefan schroeder
 *
 */

final class RouteAlgorithmImpl implements RouteAlgorithm {
	
	private static Logger logger = Logger.getLogger(RouteAlgorithmImpl.class);
	
	static double NO_DEPARTURE_TIME = -12345.12345;
	
	private String algoDescription = "algorithm to remove and insert jobs from vehicleRoutes. it also calculates marginal costs of the best insertion of a " +
			"job into the given vehicle route";
	
	public static RouteAlgorithmImpl newInstance(JobInsertionCalculator jobInsertionCalculator, VehicleRouteUpdater tourStateCalculator){
		return new RouteAlgorithmImpl(jobInsertionCalculator, tourStateCalculator);
	}
	
	private Collection<RouteAlgorithmListener> listeners = new ArrayList<RouteAlgorithmListener>();
	
	private VehicleRouteUpdater tourCalculator;

	private JobInsertionCalculator insertionCostCalculator;
	
	private RouteStates actStates;
	
	public void setActivityStates(RouteStates actStates){
		this.actStates = actStates;
	}

	public ActivityState state(TourActivity act){
		return actStates.getState(act);
	}
	
	private RouteAlgorithmImpl(JobInsertionCalculator insertionCostCalculator, VehicleRouteUpdater tourCalculator){
		this.tourCalculator = tourCalculator;
		this.insertionCostCalculator = insertionCostCalculator;
	}

	
	public InsertionData calculateBestInsertion(VehicleRoute vehicleRoute, Job job, double bestKnownCost) {
		return insertionCostCalculator.calculate(vehicleRoute, job, null, NO_DEPARTURE_TIME, null, bestKnownCost);
	}

	
	public boolean removeJobWithoutTourUpdate(Job job, VehicleRoute vehicleRoute) {
		boolean removed = vehicleRoute.getTourActivities().removeJob(job);
		if(removed){
			jobRemoved(vehicleRoute,job);
		}
		return removed;
	}
	
	private void jobRemoved(VehicleRoute vehicleRoute, Job job) {
		for(RouteAlgorithmListener l : listeners){
			if(l instanceof JobRemovedListener){
				((JobRemovedListener) l).removed(vehicleRoute, job);
			}
		}
	}

	
	public boolean removeJob(Job job, VehicleRoute vehicleRoute){
		boolean removed = removeJobWithoutTourUpdate(job, vehicleRoute);
		if(removed) updateTour(vehicleRoute);
		return removed;
	}
	
	
	public void updateTour(VehicleRoute vehicleRoute){
		boolean tourIsFeasible = tourCalculator.updateRoute(vehicleRoute);
		if(!tourIsFeasible){
			throw new IllegalStateException("At this point tour should be feasible. but it is not. \n currentTour=" + vehicleRoute.getTourActivities() + 
					"\n error sources: check jobInsertionCostCalculators and actInsertionCalculators. somehow an insertion is made, althought a hard constraint is broken. Here, hard constraints refer to \n" +
					"hard time-window constraints. If you want to deal with such constraints, make sure a violation is penalized properly (such that it can never be the best insertion position). \n" +
					"If you use CalculatesServiceInsertion and CalculatesActivityInsertion, the only hard constraint is the vehicle-capacity constraints. A violation of time-windows must be penalized in \n" +
					"the vehicleRouteCostFunction. For example: in handleActivity(....) one can check whether the act start-time is higher than the latestOperationStartTime. If so penalize it with a very high value. \n" +
					"For example: \n" +
					"public void handleActivity(TourActivity tourAct, double startTime, double endTime) {\n" + 
						"\tif(startTime > tourAct.getLatestOperationStartTime()){\n" +
							"\t\tcost += Double.MAX_VALUE;\n" +
						"\t}\n" + 
					"});");
		}
	}

	
	@Override
	public void insertJobWithoutTourUpdate(VehicleRoute vehicleRoute, Job job, InsertionData insertionData) {
		if(insertionData == null || (insertionData instanceof NoInsertionFound)) throw new IllegalStateException("insertionData null. cannot insert job.");
		if(job == null) throw new IllegalStateException("cannot insert null-job");
		if(!(vehicleRoute.getVehicle().getId().toString().equals(insertionData.getSelectedVehicle().getId().toString()))){
			vehicleSwitched(vehicleRoute.getVehicle(),insertionData.getSelectedVehicle());
			vehicleRoute.setVehicle(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
		}		
		if(job instanceof Service) {
			vehicleRoute.getTourActivities().addActivity(insertionData.getDeliveryInsertionIndex(), ServiceActivity.newInstance((Service)job));
			vehicleRoute.setDepartureTime(insertionData.getVehicleDepartureTime());
		}
		else throw new IllegalStateException("neither service nor shipment. this is not supported.");
		jobInserted(vehicleRoute,job);
	}
	
	

	private void vehicleSwitched(Vehicle oldVehicle, Vehicle newVehicle) {
		for(RouteAlgorithmListener l : listeners){
			if(l instanceof VehicleSwitchedListener){
				((VehicleSwitchedListener) l).vehicleSwitched(oldVehicle,newVehicle);
			}
		}
	}

	private void jobInserted(VehicleRoute vehicleRoute, Job job) {
		for(RouteAlgorithmListener l : listeners){
			if(l instanceof JobInsertedListener){
				((JobInsertedListener) l).inserted(vehicleRoute, job);
			}
		}
	}

	
	public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute){
		insertJobWithoutTourUpdate(vehicleRoute, job, insertionData);
		updateTour(vehicleRoute);
	}

	@Override
	public String toString() {
		return algoDescription;
	}

	public Collection<RouteAlgorithmListener> getListeners() {
		return listeners;
	}

	public void setAlgoDescription(String algoDescription) {
		this.algoDescription = algoDescription;
	}

}
