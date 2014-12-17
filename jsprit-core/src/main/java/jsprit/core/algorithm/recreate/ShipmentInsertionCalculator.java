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
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.JobActivityFactory;
import jsprit.core.problem.constraint.*;
import jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.misc.ActivityContext;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.End;
import jsprit.core.problem.solution.route.activity.Start;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.CalculationUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;




final class ShipmentInsertionCalculator implements JobInsertionCostsCalculator{

	private static final Logger logger = LogManager.getLogger(ShipmentInsertionCalculator.class);

	private HardRouteConstraint hardRouteLevelConstraint;
	
	private HardActivityConstraint hardActivityLevelConstraint;
	
	private SoftRouteConstraint softRouteConstraint;
	
	private SoftActivityConstraint softActivityConstraint;
	
	private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;
	
	private VehicleRoutingTransportCosts transportCosts;
	
	private JobActivityFactory activityFactory;
	
	private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;
	
	public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager) {
		super();
		this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
		this.hardRouteLevelConstraint = constraintManager;
		this.hardActivityLevelConstraint = constraintManager;
		this.softActivityConstraint = constraintManager;
		this.softRouteConstraint = constraintManager;
		this.transportCosts = routingCosts;
		additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
		logger.info("initialise " + this);
	}

    public void setJobActivityFactory(JobActivityFactory activityFactory){
        this.activityFactory = activityFactory;
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
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Shipment shipment = (Shipment)jobToInsert;
        TourActivity pickupShipment = activityFactory.createActivities(shipment).get(0);
        TourActivity deliverShipment = activityFactory.createActivities(shipment).get(1);
        insertionContext.getAssociatedActivities().add(pickupShipment);
        insertionContext.getAssociatedActivities().add(deliverShipment);

        /*
        check hard route constraints
         */
        if(!hardRouteLevelConstraint.fulfilled(insertionContext)){
			return InsertionData.createEmptyInsertionData();
		}
        /*
        check soft route constraints
         */
        double additionalICostsAtRouteLevel = softRouteConstraint.getCosts(insertionContext);

        double bestCost = bestKnownCosts;
        additionalICostsAtRouteLevel += additionalAccessEgressCalculator.getCosts(insertionContext);

        int pickupInsertionIndex = InsertionData.NO_INDEX;
		int deliveryInsertionIndex = InsertionData.NO_INDEX;
		
		Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
		start.setEndTime(newVehicleDepartureTime);
		
		End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        ActivityContext pickupContext = new ActivityContext();

		TourActivity prevAct = start;
		double prevActEndTime = newVehicleDepartureTime;
		boolean pickupShipmentLoopBroken = false;
		//pickupShipmentLoop
		List<TourActivity> activities = currentRoute.getTourActivities().getActivities();
		for(int i=0;i<activities.size();i++){
			ConstraintsStatus pickupShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, pickupShipment, activities.get(i), prevActEndTime);
			if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)){
				double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), activities.get(i).getLocation(), prevActEndTime, newDriver, newVehicle);
				prevActEndTime = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(i));
				prevAct = activities.get(i);
				continue;
			}
			else if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				pickupShipmentLoopBroken = true;
				break;
			}
			double additionalPickupICosts = softActivityConstraint.getCosts(insertionContext, prevAct, pickupShipment, activities.get(i), prevActEndTime);
			double pickupAIC = calculate(insertionContext,prevAct,pickupShipment,activities.get(i),prevActEndTime);
			TourActivity prevAct_deliveryLoop = pickupShipment;
			double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
			double shipmentPickupEndTime = CalculationUtils.getActivityEndTime(shipmentPickupArrTime, pickupShipment);

            pickupContext.setArrivalTime(shipmentPickupArrTime);
            pickupContext.setEndTime(shipmentPickupEndTime);
            pickupContext.setInsertionIndex(i);
            insertionContext.setRelatedActivityContext(pickupContext);

            double prevActEndTime_deliveryLoop = shipmentPickupEndTime;
			boolean deliverShipmentLoopBroken = false;
			//deliverShipmentLoop
			for(int j=i;j<activities.size();j++){
				ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, activities.get(j), prevActEndTime_deliveryLoop); 
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, activities.get(j), prevActEndTime_deliveryLoop);
					double deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,activities.get(j),prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC + deliveryAIC
							+ additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
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
				double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct_deliveryLoop.getLocation(), activities.get(j).getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
				prevActEndTime_deliveryLoop = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(j));
				prevAct_deliveryLoop = activities.get(j);
			}
			if(!deliverShipmentLoopBroken){ //check insertion between lastAct and endOfTour
				ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
					double deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,end,prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC + deliveryAIC
							+ additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
					if(totalActivityInsertionCosts < bestCost){
						bestCost = totalActivityInsertionCosts;
						pickupInsertionIndex = i;
						deliveryInsertionIndex = activities.size();
					}
				}
			}
			//update prevAct and endTime
			double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), activities.get(i).getLocation(), prevActEndTime, newDriver, newVehicle);
			prevActEndTime = CalculationUtils.getActivityEndTime(nextActArrTime, activities.get(i));
			prevAct = activities.get(i);
		}
		if(!pickupShipmentLoopBroken){ //check insertion of pickupShipment and deliverShipment at just before tour ended
			ConstraintsStatus pickupShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, pickupShipment, end, prevActEndTime);
			if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
				double additionalPickupICosts = softActivityConstraint.getCosts(insertionContext, prevAct, pickupShipment, end, prevActEndTime);
				double pickupAIC = calculate(insertionContext,prevAct,pickupShipment,end,prevActEndTime);
				TourActivity prevAct_deliveryLoop = pickupShipment;
				double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
				double shipmentPickupEndTime = CalculationUtils.getActivityEndTime(shipmentPickupArrTime, pickupShipment);
				double prevActEndTime_deliveryLoop = shipmentPickupEndTime;

                pickupContext.setArrivalTime(shipmentPickupArrTime);
                pickupContext.setEndTime(shipmentPickupEndTime);
                pickupContext.setInsertionIndex(activities.size());
                insertionContext.setRelatedActivityContext(pickupContext);

                ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
				if(deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)){
					double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, end, prevActEndTime_deliveryLoop);
					double deliveryAIC = calculate(insertionContext,prevAct_deliveryLoop,deliverShipment,end,prevActEndTime_deliveryLoop);
					double totalActivityInsertionCosts = pickupAIC + deliveryAIC
							+ additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
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

	private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
		return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);
		
	}
}
