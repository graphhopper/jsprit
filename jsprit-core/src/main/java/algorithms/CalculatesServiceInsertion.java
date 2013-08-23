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
import basics.costs.VehicleRoutingTransportCosts;
import basics.route.Driver;
import basics.route.End;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleImpl.NoVehicle;
import basics.route.VehicleRoute;



final class CalculatesServiceInsertion implements JobInsertionCalculator{

	private static final Logger logger = Logger.getLogger(CalculatesServiceInsertion.class);

	private HardRouteLevelConstraint hardRouteLevelConstraint;
	
	private Neighborhood neighborhood = new Neighborhood() {
		
		@Override
		public boolean areNeighbors(String location1, String location2) {
			return true;
		}
	};
	
	private MarginalsCalculus marginalCalculus;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	public void setNeighborhood(Neighborhood neighborhood) {
		this.neighborhood = neighborhood;
		logger.info("initialise neighborhood " + neighborhood);
	}
	
	public CalculatesServiceInsertion(VehicleRoutingTransportCosts routingCosts, MarginalsCalculus marginalsCalculus, HardRouteLevelConstraint hardRouteLevelConstraint) {
		super();
		this.marginalCalculus = marginalsCalculus;
		this.hardRouteLevelConstraint = hardRouteLevelConstraint;
		this.transportCosts = routingCosts;
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
		
		double bestCost = bestKnownCosts;
		Marginals bestMarginals = null;
		Service service = (Service)jobToInsert;
		int insertionIndex = InsertionData.NO_INDEX;
		TourActivity deliveryAct2Insert = ServiceActivity.newInstance(service);
		
		Start start = Start.newInstance(newVehicle.getLocationId(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
		start.setEndTime(newVehicleDepartureTime);
		
		End end = End.newInstance(newVehicle.getLocationId(), 0.0, newVehicle.getLatestArrival());
		
		TourActivity prevAct = start;
		double prevActStartTime = newVehicleDepartureTime;
		int actIndex = 0;

		for(TourActivity nextAct : currentRoute.getTourActivities().getActivities()){
			if(deliveryAct2Insert.getTheoreticalLatestOperationStartTime() < prevAct.getTheoreticalEarliestOperationStartTime()){
				break;
			}
			if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
				Marginals mc = calculate(iFacts, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
				if(mc != null){ 
					if(mc.getAdditionalCosts() < bestCost){
						bestCost = mc.getAdditionalCosts();
						bestMarginals = mc;
						insertionIndex = actIndex;
					}
				}
			}
			prevAct = nextAct;
			prevActStartTime = CalcUtils.getStartTimeAtAct(prevActStartTime, transportCosts.getTransportTime(prevAct.getLocationId(), nextAct.getLocationId(), prevActStartTime, newDriver, newVehicle), nextAct);
			actIndex++;
		}
		End nextAct = end;
		if(neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), prevAct.getLocationId()) && neighborhood.areNeighbors(deliveryAct2Insert.getLocationId(), nextAct.getLocationId())){
			Marginals mc = calculate(iFacts, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
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

	public Marginals calculate(InsertionFacts iFacts, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double departureTimeAtPrevAct) {	
		return marginalCalculus.calculate(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);
		
	}
}
