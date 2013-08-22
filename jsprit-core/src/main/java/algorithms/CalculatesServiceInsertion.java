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
import algorithms.HardConstraints.HardRouteLevelConstraint;
import basics.Job;
import basics.Service;
import basics.route.Driver;
import basics.route.End;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivities;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleImpl.NoVehicle;
import basics.route.VehicleRoute;



final class CalculatesServiceInsertion implements JobInsertionCalculator{
	
	static class Break {
		private Marginals marginals;
		private boolean breakLoop;
		public Break(Marginals marginals, boolean breakLoop) {
			super();
			this.marginals = marginals;
			this.breakLoop = breakLoop;
		}
		/**
		 * @return the marginals
		 */
		public Marginals getMarginals() {
			return marginals;
		}
		/**
		 * @return the breakLoop
		 */
		public boolean isBreakLoop() {
			return breakLoop;
		}
		
	}
	
	private static final Logger logger = Logger.getLogger(CalculatesServiceInsertion.class);
	
	private Start start;
	
	private End end;
	
	private HardRouteLevelConstraint hardRouteLevelConstraint;
	
	private Neighborhood neighborhood = new Neighborhood() {
		
		@Override
		public boolean areNeighbors(String location1, String location2) {
			return true;
		}
	};
	
	private MarginalsCalculus marginalCalculus;
	
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
		logger.info("initialise neighborhood " + neighborhood);
	}
	
	public CalculatesServiceInsertion(MarginalsCalculus marginalsCalculus, HardRouteLevelConstraint hardRouteLevelConstraint) {
		super();
		this.marginalCalculus = marginalsCalculus;
		this.hardRouteLevelConstraint = hardRouteLevelConstraint;
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
		
		InsertionFacts iFacts = new InsertionFacts(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
		if(!hardRouteLevelConstraint.fulfilled(new InsertionScenario(iFacts, null))){
			return InsertionData.noInsertionFound();
		}
		
		TourActivities tour = currentRoute.getTourActivities();
		double bestCost = bestKnownCosts;
		Marginals bestMarginals = null;
		Service service = (Service)jobToInsert;
		int insertionIndex = InsertionData.NO_INDEX;
		TourActivity deliveryAct2Insert = ServiceActivity.newInstance(service);
		
		initialiseStartAndEnd(newVehicle, newVehicleDepartureTime);
		
		TourActivity prevAct = start;
		int actIndex = 0;

		for(TourActivity nextAct : tour.getActivities()){
			if(deliveryAct2Insert.getTheoreticalLatestOperationStartTime() < prevAct.getTheoreticalEarliestOperationStartTime()){
				break;
			}
			if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
				Marginals mc = calculate(iFacts, prevAct, nextAct, deliveryAct2Insert);
				if(mc != null){ 
					if(mc.getAdditionalCosts() < bestCost){
						bestCost = mc.getAdditionalCosts();
						bestMarginals = mc;
						insertionIndex = actIndex;
					}
				}
			}
			prevAct = nextAct;
			actIndex++;
		}
		End nextAct = end;
		if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
			Marginals mc = calculate(iFacts, prevAct, nextAct, deliveryAct2Insert);
			if(mc != null) {
				if(mc.getAdditionalCosts() < bestCost){
					bestCost = mc.getAdditionalCosts();
					bestMarginals = mc;
					insertionIndex = actIndex;
				}
			}
		}			

		if(insertionIndex == InsertionData.NO_INDEX) {
			return InsertionData.noInsertionFound();
		}
		InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		insertionData.setAdditionalTime(bestMarginals.getAdditionalTime());
		return insertionData;
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
			end.setEndTime(newVehicle.getLatestArrival());
		}
	}

	public Marginals calculate(InsertionFacts iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct) {	
		return marginalCalculus.calculate(iFacts, prevAct, nextAct, newAct);
		
	}
}
