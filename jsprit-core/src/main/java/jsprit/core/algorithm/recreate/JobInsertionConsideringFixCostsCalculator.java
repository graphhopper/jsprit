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
import jsprit.core.algorithm.state.InternalStates;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.constraint.SoftRouteConstraint;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

final class JobInsertionConsideringFixCostsCalculator implements JobInsertionCostsCalculator, SoftRouteConstraint{

	private static final Logger logger = LogManager.getLogger(JobInsertionConsideringFixCostsCalculator.class);
	
	private final JobInsertionCostsCalculator standardServiceInsertion;
	
	private double weight_deltaFixCost = 0.5;
	
	private double solution_completeness_ratio = 0.5;
	
	private RouteAndActivityStateGetter stateGetter;

	public JobInsertionConsideringFixCostsCalculator(final JobInsertionCostsCalculator standardInsertionCalculator, RouteAndActivityStateGetter stateGetter) {
		super();
		this.standardServiceInsertion = standardInsertionCalculator;
		this.stateGetter = stateGetter;
		logger.debug("inialise {}", this);
	}

	@Override
	public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownPrice) {
		double fixcost_contribution = getFixCostContribution(currentRoute,jobToInsert, newVehicle);
		if(fixcost_contribution > bestKnownPrice){
			return InsertionData.createEmptyInsertionData();
		}
		InsertionData iData = standardServiceInsertion.getInsertionData(currentRoute, jobToInsert, newVehicle, newVehicleDepartureTime, newDriver, bestKnownPrice);
		if(iData instanceof NoInsertionFound){
			return iData;
		} 
		double totalInsertionCost = iData.getInsertionCost() + fixcost_contribution;
		InsertionData insertionData = new InsertionData(totalInsertionCost, iData.getPickupInsertionIndex(), iData.getDeliveryInsertionIndex(), newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		insertionData.getEvents().addAll(iData.getEvents());
		return insertionData;
	}

	private double getFixCostContribution(final VehicleRoute currentRoute,
			final Job jobToInsert, final Vehicle newVehicle) {
		double relFixCost = getDeltaRelativeFixCost(currentRoute, newVehicle, jobToInsert);
		double absFixCost = getDeltaAbsoluteFixCost(currentRoute, newVehicle, jobToInsert);
		double deltaFixCost = (1-solution_completeness_ratio)*relFixCost + solution_completeness_ratio*absFixCost;
		double fixcost_contribution = weight_deltaFixCost*solution_completeness_ratio*deltaFixCost;
		return fixcost_contribution;
	}
	
	public void setWeightOfFixCost(double weight){
		weight_deltaFixCost = weight;
		logger.debug("set weightOfFixCostSaving to {}", weight);
	}
	
	@Override
	public String toString() {
		return "[name=calculatesServiceInsertionConsideringFixCost][weightOfFixedCostSavings="+weight_deltaFixCost+"]";
	}

	public void setSolutionCompletenessRatio(double ratio){
		solution_completeness_ratio = ratio;
	}

	private double getDeltaAbsoluteFixCost(VehicleRoute route, Vehicle newVehicle, Job job) {
		Capacity load = Capacity.addup(getCurrentMaxLoadInRoute(route), job.getSize());
//		double load = getCurrentMaxLoadInRoute(route) + job.getCapacityDemand();
		double currentFix = 0.0;
		if(route.getVehicle() != null){
			if(!(route.getVehicle() instanceof NoVehicle)){
				currentFix += route.getVehicle().getType().getVehicleCostParams().fix;
			}
		}
		if(!newVehicle.getType().getCapacityDimensions().isGreaterOrEqual(load)){
			return Double.MAX_VALUE;
		}
		return newVehicle.getType().getVehicleCostParams().fix - currentFix;
	}

	private double getDeltaRelativeFixCost(VehicleRoute route, Vehicle newVehicle, Job job) {
		Capacity currentLoad = getCurrentMaxLoadInRoute(route);
//		int currentLoad = getCurrentMaxLoadInRoute(route);
		Capacity load = Capacity.addup(currentLoad, job.getSize());
//		double load = currentLoad + job.getCapacityDemand();
		double currentRelFix = 0.0;
		if(route.getVehicle() != null){
			if(!(route.getVehicle() instanceof NoVehicle)){
				currentRelFix += route.getVehicle().getType().getVehicleCostParams().fix * Capacity.divide(currentLoad, route.getVehicle().getType().getCapacityDimensions());
			}
		}
		if(!newVehicle.getType().getCapacityDimensions().isGreaterOrEqual(load)){
			return Double.MAX_VALUE;
		}
		double relativeFixCost = newVehicle.getType().getVehicleCostParams().fix* (Capacity.divide(load, newVehicle.getType().getCapacityDimensions())) - currentRelFix;
		return relativeFixCost;
	}

	private Capacity getCurrentMaxLoadInRoute(VehicleRoute route) {
        Capacity maxLoad = stateGetter.getRouteState(route, InternalStates.MAXLOAD, Capacity.class);
        if(maxLoad == null) maxLoad = Capacity.Builder.newInstance().build();
        return  maxLoad;
	}

	@Override
	public double getCosts(JobInsertionContext insertionContext) {
		return getFixCostContribution(insertionContext.getRoute(), insertionContext.getJob(), insertionContext.getNewVehicle());
	}

}
