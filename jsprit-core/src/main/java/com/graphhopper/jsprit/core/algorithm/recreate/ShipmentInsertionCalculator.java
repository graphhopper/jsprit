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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.*;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.cost.SetupTime;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


final class ShipmentInsertionCalculator implements JobInsertionCostsCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentInsertionCalculator.class);

    private HardRouteConstraint hardRouteLevelConstraint;

    private HardActivityConstraint hardActivityLevelConstraint;

    private SoftRouteConstraint softRouteConstraint;

    private SoftActivityConstraint softActivityConstraint;

    private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private VehicleRoutingTransportCosts transportCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private SetupTime setupCosts = new SetupTime();

    private JobActivityFactory activityFactory;

    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager) {
        super();
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        this.hardRouteLevelConstraint = constraintManager;
        this.hardActivityLevelConstraint = constraintManager;
        this.softActivityConstraint = constraintManager;
        this.softRouteConstraint = constraintManager;
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        logger.debug("initialise {}", this);
    }

    public void setJobActivityFactory(JobActivityFactory activityFactory) {
        this.activityFactory = activityFactory;
    }

    @Override
    public String toString() {
        return "[name=calculatesServiceInsertion]";
    }

    /**
     * Calculates the marginal cost of inserting job i locally. This is based on the
     * assumption that cost changes can entirely covered by only looking at the predecessor i-1 and its successor i+1.
     */
    @Override
    public InsertionData getInsertionData(final VehicleRoute currentRoute, final Job jobToInsert, final Vehicle newVehicle, double newVehicleDepartureTime, final Driver newDriver, final double bestKnownCosts) {
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Shipment shipment = (Shipment) jobToInsert;
        TourActivity pickupShipment = activityFactory.createActivities(shipment).get(0);
        TourActivity deliverShipment = activityFactory.createActivities(shipment).get(1);
        insertionContext.getAssociatedActivities().add(pickupShipment);
        insertionContext.getAssociatedActivities().add(deliverShipment);

        /*
        check hard route constraints
         */
        if (!hardRouteLevelConstraint.fulfilled(insertionContext)) {
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

        TimeWindow bestPickupTimeWindow = null;
        TimeWindow bestDeliveryTimeWindow = null;

        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
        start.setEndTime(newVehicleDepartureTime);

        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        ActivityContext pickupContext = new ActivityContext();

        TourActivity prevAct = start;
        double prevActEndTime = newVehicleDepartureTime;

        //loops
        int i = 0;
        boolean tourEnd = false;
        //pickupShipmentLoop
        List<TourActivity> activities = currentRoute.getTourActivities().getActivities();

        while (!tourEnd) {
            TourActivity nextAct;
            if (i < activities.size()) {
                nextAct = activities.get(i);
            } else {
                nextAct = end;
                tourEnd = true;
            }

            boolean pickupInsertionNotFulfilledBreak = true;
            for(TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(activityContext);
                ConstraintsStatus pickupShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                    continue;
                } else if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    continue;
                }
                else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                }
                double additionalPickupICosts = softActivityConstraint.getCosts(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);

                TourActivity prevAct_deliveryLoop = pickupShipment;

                double shipmentPickupSetupTime = setupCosts.getSetupTime(prevAct, pickupShipment, newVehicle);
                double transportTime_prevAct_pickupShipment = transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);

                double shipmentPickupArrTime = prevActEndTime + transportTime_prevAct_pickupShipment;
                double shipmentPickupReadyTime = shipmentPickupArrTime + shipmentPickupSetupTime;
                double shipmentPickupEndTime = Math.max(shipmentPickupReadyTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(pickupShipment, shipmentPickupReadyTime, newDriver, newVehicle);

                pickupContext.setArrivalTime(shipmentPickupArrTime);
                pickupContext.setEndTime(shipmentPickupEndTime);
                pickupContext.setInsertionIndex(i);
                insertionContext.setRelatedActivityContext(pickupContext);

                double prevActEndTime_deliveryLoop = shipmentPickupEndTime;

			/*
            --------------------------------
			 */
                //deliverShipmentLoop
                int j = i;
                boolean tourEnd_deliveryLoop = false;
                while (!tourEnd_deliveryLoop) {
                    TourActivity nextAct_deliveryLoop;
                    if (j < activities.size()) {
                        nextAct_deliveryLoop = activities.get(j);
                    } else {
                        nextAct_deliveryLoop = end;
                        tourEnd_deliveryLoop = true;
                    }

                    boolean deliveryInsertionNotFulfilledBreak = true;
                    for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                        deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                        deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                        ActivityContext activityContext_ = new ActivityContext();
                        activityContext_.setInsertionIndex(j);
                        insertionContext.setActivityContext(activityContext_);
                        ConstraintsStatus deliverShipmentConstraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                        if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                            double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                            double deliveryAIC = calculate(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop);
                            double totalActivityInsertionCosts = pickupAIC + deliveryAIC
                                + additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
                            if (totalActivityInsertionCosts < bestCost) {
                                bestCost = totalActivityInsertionCosts;
                                pickupInsertionIndex = i;
                                deliveryInsertionIndex = j;
                                bestPickupTimeWindow = pickupTimeWindow;
                                bestDeliveryTimeWindow = deliveryTimeWindow;
                            }
                            deliveryInsertionNotFulfilledBreak = false;
                        } else if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                            deliveryInsertionNotFulfilledBreak = false;
                        }
                    }
                    if (deliveryInsertionNotFulfilledBreak) break;
                    //update prevAct and endTime
                    double setupTime_prevActdLoop_nextAct_dLoop = setupCosts.getSetupTime(prevAct_deliveryLoop, nextAct_deliveryLoop, newVehicle);
                    double transportTime_prevActdLoop_nextActdLoop = transportCosts.getTransportTime(prevAct_deliveryLoop.getLocation(), nextAct_deliveryLoop.getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                    double nextActArrTime = prevActEndTime_deliveryLoop + transportTime_prevActdLoop_nextActdLoop;
                    double nextActReadyTime = nextActArrTime + setupTime_prevActdLoop_nextAct_dLoop;
                    prevActEndTime_deliveryLoop = Math.max(nextActReadyTime, nextAct_deliveryLoop.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct_deliveryLoop,nextActReadyTime,newDriver,newVehicle);
                    prevAct_deliveryLoop = nextAct_deliveryLoop;
                    j++;
                }
            }
            if(pickupInsertionNotFulfilledBreak){
                break;
            }
            //update prevAct and endTime
            double setupTime_prevAct_nextAct = setupCosts.getSetupTime(prevAct, nextAct, newVehicle);
            double transportTime_prevAct_nextAct = transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
            double nextActArrTime = prevActEndTime + transportTime_prevAct_nextAct;
            double nextActReadyTime = nextActArrTime + setupTime_prevAct_nextAct;
            prevActEndTime = Math.max(nextActReadyTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct,nextActReadyTime,newDriver,newVehicle);
            prevAct = nextAct;
            i++;
        }
        if (pickupInsertionIndex == InsertionData.NO_INDEX) {
            return InsertionData.createEmptyInsertionData();
        }
        InsertionData insertionData = new InsertionData(bestCost, pickupInsertionIndex, deliveryInsertionIndex, newVehicle, newDriver);
        pickupShipment.setTheoreticalEarliestOperationStartTime(bestPickupTimeWindow.getStart());
        pickupShipment.setTheoreticalLatestOperationStartTime(bestPickupTimeWindow.getEnd());
        deliverShipment.setTheoreticalEarliestOperationStartTime(bestDeliveryTimeWindow.getStart());
        deliverShipment.setTheoreticalLatestOperationStartTime(bestDeliveryTimeWindow.getEnd());
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        insertionData.getEvents().add(new InsertActivity(currentRoute, newVehicle, deliverShipment, deliveryInsertionIndex));
        insertionData.getEvents().add(new InsertActivity(currentRoute, newVehicle, pickupShipment, pickupInsertionIndex));
        insertionData.getEvents().add(new SwitchVehicle(currentRoute, newVehicle, newVehicleDepartureTime));
        return insertionData;
    }

    private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
        return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);

    }
}
