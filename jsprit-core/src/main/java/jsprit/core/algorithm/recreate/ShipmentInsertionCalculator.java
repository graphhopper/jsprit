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

import java.util.List;

import jsprit.core.algorithm.recreate.ActivityInsertionCostsCalculator.ActivityInsertionCosts;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint;
import jsprit.core.problem.constraint.HardActivityStateLevelConstraint.ConstraintsStatus;
import jsprit.core.problem.constraint.HardRouteStateLevelConstraint;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.DefaultShipmentActivityFactory;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.solution.route.activity.TourShipmentActivityFactory;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import jsprit.core.util.CalculationUtils;
import jsprit.core.util.Neighborhood;

import org.apache.log4j.Logger;




final class ShipmentInsertionCalculator implements JobInsertionCostsCalculator{

	private static final Logger logger = Logger.getLogger(ShipmentInsertionCalculator.class);

	private HardRouteStateLevelConstraint hardRouteLevelConstraint;
	
	private HardActivityStateLevelConstraint hardActivityLevelConstraint;
	
	private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	private TourShipmentActivityFactory activityFactory;
	
	public void setNeighborhood(Neighborhood neighborhood) {
		logger.info("initialise neighborhood " + neighborhood);
	}
	
	public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, HardRouteStateLevelConstraint hardRouteLevelConstraint, HardActivityStateLevelConstraint hardActivityLevelConstraint) {
		super();
		this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
		this.hardRouteLevelConstraint = hardRouteLevelConstraint;
		this.hardActivityLevelConstraint = hardActivityLevelConstraint;
		this.transportCosts = routingCosts;
		activityFactory = new DefaultShipmentActivityFactory();
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
	public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
		if(jobToInsert == null) throw new IllegalStateException("jobToInsert is missing.");
		if(newVehicle == null || newVehicle instanceof NoVehicle) throw new IllegalStateException("newVehicle is missing.");
		if(!(jobToInsert instanceof Shipment)) throw new IllegalStateException("jobToInsert should be of type Shipment!");
		
		JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
		if(!hardRouteLevelConstraint.fulfilled(insertionContext)){
			return InsertionData.createEmptyInsertionData();
		}
		
		double bestCost = bestKnownCosts;
		Shipment shipment = (Shipment)jobToInsert;
		TourActivity pickupShipment = activityFactory.createPickup(shipment);
		TourActivity deliverShipment = activityFactory.createDelivery(shipment);
		
		int pickupInsertionIndex = InsertionData.NO_INDEX;
		int deliveryInsertionIndex = InsertionData.NO_INDEX;
		
		Start start = Start.newInstance(newVehicle.getLocationId(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
		start.setEndTime(newVehicleDepartureTime);
		
		End end = End.newInstance(newVehicle.getLocationId(), 0.0, newVehicle.getLatestArrival());
		
		TourActivity prevAct = start;
		double prevActEndTime = newVehicleDepartureTime;
		boolean pickupShipmentLoopBroken = false;
		//pickupShipmentLoop
		List<TourActivity> activities = currentRoute.getTourActivities().getActivities();
		for(int i=0;i<activities.size();i++){
			ConstraintsStatus pickupShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, pickupShipment, activities.get(i), prevActEndTime);
			if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)){
				double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocationId(), activities.get(i).getLocationId(), prevActEndTime, newDriver, newVehicle);
				prevActEndTime = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(i));
				prevAct = activities.get(i);
				continue;
			}
			else if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				pickupShipmentLoopBroken = true;
				break;
			}
			ActivityInsertionCosts pickupAIC = calculate(insertionContext,prevAct,pickupShipment,activities.get(i),prevActEndTime);
			TourActivity prevAct_deliveryLoop = pickupShipment;
			double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocationId(), pickupShipment.getLocationId(), prevActEndTime, newDriver, newVehicle);
			double shipmentPickupEndTime = CalculationUtils.getActivityEndTime(shipmentPickupArrTime, pickupShipment);
			double prevActEndTime_deliveryLoop = shipmentPickupEndTime;
			boolean deliverShipmentLoopBroken = false;
			//deliverShipmentLoop
			for(int j=i;j<activities.size();j++){
				ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, activities.get(j), prevActEndTime_deliveryLoop); 
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					ActivityInsertionCosts deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,activities.get(j),prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC.getAdditionalCosts() + deliveryAIC.getAdditionalCosts();
					if(totalActivityInsertionCosts < bestCost){
						bestCost = totalActivityInsertionCosts;
						pickupInsertionIndex = i;
						deliveryInsertionIndex = j;
					}
				}
				else if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
					deliverShipmentLoopBroken = true;
					break;
				}	
				//update prevAct and endTime
				double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct_deliveryLoop.getLocationId(), activities.get(j).getLocationId(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
				prevActEndTime_deliveryLoop = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(j));
				prevAct_deliveryLoop = activities.get(j);
			}
			if(!deliverShipmentLoopBroken){ //check insertion between lastAct and endOfTour
				ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					ActivityInsertionCosts deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,end,prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC.getAdditionalCosts() + deliveryAIC.getAdditionalCosts();
					if(totalActivityInsertionCosts < bestCost){
						bestCost = totalActivityInsertionCosts;
						pickupInsertionIndex = i;
						deliveryInsertionIndex = activities.size();
					}
				}
			}
			//update prevAct and endTime
			double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocationId(), activities.get(i).getLocationId(), prevActEndTime, newDriver, newVehicle);
			prevActEndTime = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(i));
			prevAct = activities.get(i);
		}
		if(!pickupShipmentLoopBroken){ //check insertion of pickupShipment and deliverShipment at just before tour ended
			ConstraintsStatus pickupShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, pickupShipment, end, prevActEndTime);
			if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
				ActivityInsertionCosts pickupAIC = calculate(insertionContext,prevAct,pickupShipment,end,prevActEndTime);

				TourActivity prevAct_deliveryLoop = pickupShipment;
				double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocationId(), pickupShipment.getLocationId(), prevActEndTime, newDriver, newVehicle);
				double shipmentPickupEndTime = CalculationUtils.getActivityEndTime(shipmentPickupArrTime, pickupShipment);
				double prevActEndTime_deliveryLoop = shipmentPickupEndTime;
				
				ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					ActivityInsertionCosts deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,end,prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC.getAdditionalCosts() + deliveryAIC.getAdditionalCosts();
					if(totalActivityInsertionCosts < bestCost){
						bestCost = totalActivityInsertionCosts;
						pickupInsertionIndex = activities.size();
						deliveryInsertionIndex = activities.size();
					}
				}
			}
		}
		if(pickupInsertionIndex == InsertionData.NO_INDEX) {
			return InsertionData.createEmptyInsertionData();
		}
		InsertionData insertionData = new InsertionData(bestCost, pickupInsertionIndex, deliveryInsertionIndex, newVehicle, newDriver);
		insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
		return insertionData;
	}

	private ActivityInsertionCosts calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {	
		return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);
		
	}
}
