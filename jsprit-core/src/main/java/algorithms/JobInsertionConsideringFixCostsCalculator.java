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

import org.apache.log4j.Logger;

import algorithms.InsertionData.NoInsertionFound;
import basics.Job;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleImpl.NoVehicle;
import basics.route.VehicleRoute;



final class JobInsertionConsideringFixCostsCalculator implements JobInsertionCalculator{

	private static final Logger logger = Logger.getLogger(JobInsertionConsideringFixCostsCalculator.class);
	
	private final JobInsertionCalculator standardServiceInsertion;
	
	private double weight_deltaFixCost = 0.5;
	
	private double solution_completeness_ratio = 0.5;
	
	private StateGetter stateGetter;

	public JobInsertionConsideringFixCostsCalculator(final JobInsertionCalculator standardInsertionCalculator, StateGetter stateGetter) {
		super();
		this.standardServiceInsertion = standardInsertionCalculator;
		this.stateGetter = stateGetter;
		logger.info("inialise " + this);
	}

	@Override
	public InsertionData calculate(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownPrice) {
		double relFixCost = getDeltaRelativeFixCost(currentRoute, newVehicle, jobToInsert);
		double absFixCost = getDeltaAbsoluteFixCost(currentRoute, newVehicle, jobToInsert);
		double deltaFixCost = (1-solution_completeness_ratio)*relFixCost + solution_completeness_ratio*absFixCost;
		double fixcost_contribution = weight_deltaFixCost*solution_completeness_ratio*deltaFixCost;
		if(fixcost_contribution > bestKnownPrice){
			return InsertionData.noInsertionFound();
		}
		InsertionData iData = standardServiceInsertion.calculate(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownPrice);
		if(iData instanceof NoInsertionFound){
			return iData;
		} 
		double totalInsertionCost = iData.getInsertionCost() + fixcost_contribution;
		InsertionData insertionData = new InsertionData(totalInsertionCost, iData.getPickupInsertionIndex(), iData.getDeliveryInsertionIndex(), newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		return insertionData;
	}
	
	public void setWeightOfFixCost(double weight){
		weight_deltaFixCost = weight;
		logger.info("set weightOfFixCostSaving to " + weight);
	}
	
	@Override
	public String toString() {
		return "[name=calculatesServiceInsertionConsideringFixCost][weightOfFixedCostSavings="+weight_deltaFixCost+"]";
	}

	public void setSolutionCompletenessRatio(double ratio){
		solution_completeness_ratio = ratio;
	}

	private double getDeltaAbsoluteFixCost(VehicleRoute route, Vehicle newVehicle, Job job) {
		double load = getCurrentMaxLoadInRoute(route) + job.getCapacityDemand();
		double currentFix = 0.0;
		if(route.getVehicle() != null){
			if(!(route.getVehicle() instanceof NoVehicle)){
				currentFix += route.getVehicle().getType().getVehicleCostParams().fix;
			}
		}
		if(newVehicle.getCapacity() < load){
			return Double.MAX_VALUE;
		}
		return newVehicle.getType().getVehicleCostParams().fix - currentFix;
	}

	private double getDeltaRelativeFixCost(VehicleRoute route, Vehicle newVehicle, Job job) {
		int currentLoad = getCurrentMaxLoadInRoute(route);
		double load = currentLoad + job.getCapacityDemand();
		double currentRelFix = 0.0;
		if(route.getVehicle() != null){
			if(!(route.getVehicle() instanceof NoVehicle)){
				currentRelFix += route.getVehicle().getType().getVehicleCostParams().fix*currentLoad/route.getVehicle().getCapacity();
			}
		}
		if(newVehicle.getCapacity() < load){
			return Double.MAX_VALUE;
		}
		double relativeFixCost = newVehicle.getType().getVehicleCostParams().fix*(load/newVehicle.getCapacity()) - currentRelFix;
		return relativeFixCost;
	}

	private int getCurrentMaxLoadInRoute(VehicleRoute route) {
		return (int) stateGetter.getRouteState(route, StateFactory.MAXLOAD).toDouble();
	}

}
