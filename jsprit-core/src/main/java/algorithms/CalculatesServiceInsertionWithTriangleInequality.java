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
import algorithms.StateManager.State;
import algorithms.StateManager.States;
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



final class CalculatesServiceInsertionWithTriangleInequality implements JobInsertionCalculator{
	
	class Marginals {
		private double marginalCosts;
		private double marginalTime;
		public Marginals(double marginalCosts, double marginalTime) {
			super();
			this.marginalCosts = marginalCosts;
			this.marginalTime = marginalTime;
		}
		/**
		 * @return the marginalCosts
		 */
		public double getMarginalCosts() {
			return marginalCosts;
		}
		/**
		 * @return the marginalTime
		 */
		public double getMarginalTime() {
			return marginalTime;
		}
		
	}
	
	private static final Logger logger = Logger.getLogger(CalculatesServiceInsertionWithTriangleInequality.class);
	
	private StateManager routeStates;
	
	private VehicleRoutingTransportCosts routingCosts;
	
	private Start start;
	
	private End end;
	
	private HardConstraint hardConstraint = new HardConstraint() {
		
		@Override
		public boolean fulfilled(InsertionScenario iScenario) {
			return true;
		}
	};
	
	public void setHardConstraint(HardConstraint hardConstraint){
		this.hardConstraint = hardConstraint;
	}
	
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

	public void setActivityStates(StateManager activityStates2){
		this.routeStates = activityStates2;
	}
	
	public CalculatesServiceInsertionWithTriangleInequality(VehicleRoutingTransportCosts vehicleRoutingTransportCosts, VehicleRoutingActivityCosts vehicleRoutingActivityCosts) {
		super();
		this.routingCosts = vehicleRoutingTransportCosts;
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
		Marginals bestMarginals = new Marginals(bestKnownCosts,Double.MAX_VALUE);
		Service service = (Service)jobToInsert;
		
		if(getCurrentLoad(currentRoute) + service.getCapacityDemand() > newVehicle.getCapacity()){
			return InsertionData.noInsertionFound();
		}
		int insertionIndex = InsertionData.NO_INDEX;
		
		TourActivity deliveryAct2Insert = ServiceActivity.newInstance(service);
		
		initialiseStartAndEnd(newVehicle, newVehicleDepartureTime);
		
		TourActivity prevAct = start;
		int actIndex = 0;
		for(TourActivity nextAct : tour.getActivities()){
			if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
				Marginals mc = calculate(prevAct, nextAct, deliveryAct2Insert, newDriver, newVehicle);
				if(mc.getMarginalCosts() < bestMarginals.getMarginalCosts()){
					bestMarginals = mc;
					insertionIndex = actIndex;
				}
			}
			prevAct = nextAct;
			actIndex++;
		}
		End nextAct = end;
		if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
			Marginals mc = calculate(prevAct, nextAct, deliveryAct2Insert, newDriver, newVehicle);
			if(mc.getMarginalCosts() < bestMarginals.getMarginalCosts()){
				bestMarginals = mc;
				insertionIndex = actIndex;
			}
		}			

		if(insertionIndex == InsertionData.NO_INDEX) {
			return InsertionData.noInsertionFound();
		}
		InsertionData insertionData = new InsertionData(bestMarginals.getMarginalCosts(), InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		insertionData.setAdditionalTime(bestMarginals.getMarginalTime());
		return insertionData;
	}

	private int getCurrentLoad(VehicleRoute currentRoute) {
		return (int) routeStates.getRouteState(currentRoute, StateTypes.LOAD).toDouble();
	}

	private void initialiseStartAndEnd(final Vehicle newVehicle, double newVehicleDepartureTime) {
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

	public Marginals calculate(TourActivity prevAct, TourActivity nextAct, TourActivity newAct, Driver driver, Vehicle vehicle) {	
		
		double tp_costs_prevAct_newAct = routingCosts.getTransportCost(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		double tp_time_prevAct_newAct = routingCosts.getTransportTime(prevAct.getLocationId(), newAct.getLocationId(), prevAct.getEndTime(), driver, vehicle);
		
		double tp_costs_newAct_nextAct = routingCosts.getTransportCost(newAct.getLocationId(), nextAct.getLocationId(), 0.0, driver, vehicle);
		double tp_time_newAct_nextAct = routingCosts.getTransportTime(newAct.getLocationId(), nextAct.getLocationId(), 0.0, driver, vehicle);
		
		double tp_costs_prevAct_nextAct = routingCosts.getTransportCost(prevAct.getLocationId(), nextAct.getLocationId(), 0.0, driver, vehicle);
		double tp_time_prevAct_nextAct = routingCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), 0.0, driver, vehicle);
		
		return new Marginals(tp_costs_prevAct_newAct + tp_costs_newAct_nextAct - tp_costs_prevAct_nextAct, tp_time_prevAct_newAct + tp_time_newAct_nextAct - tp_time_prevAct_nextAct);
	}
}
