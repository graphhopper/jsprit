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

import org.apache.log4j.Logger;

import util.Neighborhood;

import algorithms.RouteStates.ActivityState;
import basics.Job;
import basics.Service;
import basics.costs.VehicleRoutingActivityCosts;
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.End;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;
import basics.route.VehicleImpl.NoVehicle;



final class CalculatesServiceInsertion implements JobInsertionCalculator{
	
	private static final Logger logger = Logger.getLogger(CalculatesServiceInsertion.class);
	
	private RouteStates routeStates;
	
	private VehicleRoutingTransportCosts routingCosts;
	
	private VehicleRoutingActivityCosts activityCosts;
	
	private Start start;
	
	private End end;
	
	private Neighborhood neighborhood = new Neighborhood() {
		
		@Override
		public boolean areNeighbors(String location1, String location2) {
			return true;
		}
	};
	
	
	
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
		logger.info("initialise neighborhood " + neighborhood);
	}

	public void setActivityStates(RouteStates actStates){
		this.routeStates = actStates;
	}

	public ActivityState state(TourActivity act){
		return routeStates.getState(act);
	}
	
	public CalculatesServiceInsertion(VehicleRoutingTransportCosts vehicleRoutingTransportCosts, VehicleRoutingActivityCosts vehicleRoutingActivityCosts) {
		super();
		this.routingCosts = vehicleRoutingTransportCosts;
		this.activityCosts = vehicleRoutingActivityCosts;
		logger.info("initialise " + this);
	}
	
	@Override
	public String toString() {
		return "[name=calculatesServiceInsertion]";
	}
	
	/**
	 * Calculates the marginal cost of inserting job i locally. This is based on the
	 * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
	 *  
	 */
	@Override
	public InsertionData calculate(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
		if(jobToInsert == null) throw new IllegalStateException("jobToInsert is missing.");
		if(newVehicle == null || newVehicle instanceof NoVehicle) throw new IllegalStateException("newVehicle is missing.");
		
		TourActivities tour = currentRoute.getTourActivities();
		double bestCost = bestKnownCosts;
		Service service = (Service)jobToInsert;
		
		if(routeStates.getRouteState(currentRoute).getLoad() + service.getCapacityDemand() > newVehicle.getCapacity()){
			return InsertionData.noInsertionFound();
		}
		int insertionIndex = InsertionData.NO_INDEX;
		
		TourActivity deliveryAct2Insert = ServiceActivity.newInstance(service);
//		TourActivity deliveryAct2Insert = actStates.getActivity(service, true);
		
		initialiseStartAndEnd(newVehicle, newVehicleDepartureTime);
		
		TourActivity prevAct = start;
		double prevCostInOriginalTour = 0.0;
		int actIndex = 0;
		for(TourActivity nextAct : tour.getActivities()){
			double nextCostInOriginalTour = state(nextAct).getCurrentCost();
			if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
				double mc = calculate(tour, prevAct, nextAct, deliveryAct2Insert, newDriver, newVehicle, bestCost, nextCostInOriginalTour - prevCostInOriginalTour);
				if(mc < bestCost){
					bestCost = mc;
					insertionIndex = actIndex;
				}
			}
			prevCostInOriginalTour = nextCostInOriginalTour;
			prevAct = nextAct;
			actIndex++;
		}
		End nextAct = end;
		if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
			double mc = calculate(tour, prevAct, nextAct, deliveryAct2Insert, newDriver, newVehicle, bestCost, routeStates.getRouteState(currentRoute).getCosts() - prevCostInOriginalTour);
			if(mc < bestCost){
				bestCost = mc;
				insertionIndex = actIndex;
			}
		}			

		if(insertionIndex == InsertionData.NO_INDEX) {
			return InsertionData.noInsertionFound();
		}
		InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		return insertionData;
	}

	private void initialiseStartAndEnd(final Vehicle newVehicle,
			double newVehicleDepartureTime) {
		if(start == null){
			start = Start.newInstance(newVehicle.getLocationId(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
			start.setEndTime(newVehicleDepartureTime);
		}
		else{
			start.setLocationId(newVehicle.getLocationId());
			start.setTheoreticalEarliestOperationStartTime(newVehicle.getEarliestDeparture());
			start.setTheoreticalLatestOperationStartTime(newVehicle.getLatestArrival());
			start.setEndTime(newVehicleDepartureTime);
		}
		
		if(end == null){
			end = End.newInstance(newVehicle.getLocationId(), 0.0, newVehicle.getLatestArrival());
		}
		else{
			end.setLocationId(newVehicle.getLocationId());
			end.setTheoreticalEarliestOperationStartTime(newVehicleDepartureTime);
			end.setTheoreticalLatestOperationStartTime(newVehicle.getLatestArrival());
		}
	}

	public double calculate(TourActivities tour, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, Driver driver, Vehicle vehicle, double bestKnownCosts, double costWithoutNewJob) {	
		
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		
		double newAct_arrTime = prevAct.getEndTime() + tp_time_prevAct_newAct;
		double newAct_operationStartTime = Math.max(newAct_arrTime, newAct.getTheoreticalEarliestOperationStartTime());
		
		double newAct_endTime = newAct_operationStartTime + newAct.getOperationTime();
		
		double act_costs_newAct = activityCosts.getActivityCost(newAct, newAct_arrTime, driver, vehicle);
		
		if((tp_costs_prevAct_newAct + act_costs_newAct - costWithoutNewJob) > bestKnownCosts){
			return Double.MAX_VALUE;
		}
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, driver, vehicle);
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), newAct_endTime, driver, vehicle);
		
		double nextAct_arrTime = newAct_endTime + tp_time_newAct_nextAct;
		double act_costs_nextAct = activityCosts.getActivityCost(nextAct, nextAct_arrTime, driver, vehicle);
		
		double activityInsertionCosts;
		
		double totalCosts = tp_costs_prevAct_newAct + tp_costs_newAct_nextAct + act_costs_newAct + act_costs_nextAct; 
		
		if(totalCosts - costWithoutNewJob > bestKnownCosts){
			activityInsertionCosts = Double.MAX_VALUE;
		}
		if(nextAct_arrTime > getLatestOperationStart(nextAct)){
			activityInsertionCosts = Double.MAX_VALUE;
		}
		else{
			activityInsertionCosts = totalCosts - costWithoutNewJob;
		} 
		return activityInsertionCosts;
	}
	
	private double getLatestOperationStart(TourActivity act) {
		if(state(act) != null){
			return state(act).getLatestOperationStart();
		}
		return act.getTheoreticalLatestOperationStartTime();
	}
}
