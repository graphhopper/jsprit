/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


final class ShipmentInsertionCalculator extends AbstractInsertionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentInsertionCalculator.class);

    private final ConstraintManager constraintManager;

    private final SoftRouteConstraint softRouteConstraint;

    private final SoftActivityConstraint softActivityConstraint;

    private final ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final JobActivityFactory activityFactory;

    private final AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager, JobActivityFactory jobActivityFactory) {
        super();
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        this.constraintManager = constraintManager;
        this.softActivityConstraint = constraintManager;
        this.softRouteConstraint = constraintManager;
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = jobActivityFactory;
        logger.debug("initialise {}", this);
    }

    @Override
    public String toString() {
        return "[name=calculatesShipmentInsertion]";
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
        InsertionData noInsertion = checkRouteContraints(insertionContext, constraintManager);
        if (noInsertion != null) return noInsertion;
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

        List<String> failedActivityConstraints = new ArrayList<>();
        while (!tourEnd) {
            TourActivity nextAct_PickupLoop;
            if (i < activities.size()) {
                nextAct_PickupLoop = activities.get(i);
            } else {
                nextAct_PickupLoop = end;
                tourEnd = true;
            }

            TourActivity nextPickup = nextAct_PickupLoop;
            if (nextAct_PickupLoop instanceof BreakForMultipleTimeWindowsActivity) {
                nextPickup = getBreakCopyWithUpdatedLocation(pickupShipment.getLocation(), nextAct_PickupLoop);
            }
            boolean pickupInsertionNotFulfilledBreak = true;
            for(TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(activityContext);

                ConstraintsStatus pickupShipmentConstraintStatus = fulfilled(insertionContext, prevAct, pickupShipment, nextPickup, prevActEndTime, failedActivityConstraints, constraintManager);
                if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                    continue;
                } else if(pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    continue;
                }
                else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                    pickupInsertionNotFulfilledBreak = false;
                }
                double additionalPickupICosts = softActivityConstraint.getCosts(insertionContext, prevAct, pickupShipment, nextPickup, prevActEndTime);
                double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextPickup, prevActEndTime);

                TourActivity prevAct_deliveryLoop = pickupShipment;
                double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);

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

                    TourActivity next = nextAct_deliveryLoop;
                    if (nextAct_deliveryLoop instanceof BreakForMultipleTimeWindowsActivity) {
                        next = getBreakCopyWithUpdatedLocation(deliverShipment.getLocation(), nextAct_deliveryLoop);
                    }

                    boolean deliveryInsertionNotFulfilledBreak = true;
                    for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                        deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                        deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                        ActivityContext activityContext_ = new ActivityContext();
                        activityContext_.setInsertionIndex(j);
                        insertionContext.setActivityContext(activityContext_);
                        ConstraintsStatus deliverShipmentConstraintStatus = fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, next, prevActEndTime_deliveryLoop, failedActivityConstraints, constraintManager);
                        if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                            double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevAct_deliveryLoop, deliverShipment, next, prevActEndTime_deliveryLoop);
                            double deliveryAIC = calculate(insertionContext, prevAct_deliveryLoop, deliverShipment, next, prevActEndTime_deliveryLoop);
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
                    double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct_deliveryLoop.getLocation(), next.getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                    prevActEndTime_deliveryLoop = Math.max(nextActArrTime, next.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct_deliveryLoop, next,nextActArrTime,newDriver,newVehicle);
                    prevAct_deliveryLoop = nextAct_deliveryLoop;
                    j++;
                }
            }
            if(pickupInsertionNotFulfilledBreak){
                break;
            }
            //update prevAct and endTime
            double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextPickup.getLocation(), prevActEndTime, newDriver, newVehicle);
            prevActEndTime = Math.max(nextActArrTime, nextPickup.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(prevAct, nextPickup,nextActArrTime,newDriver,newVehicle);
            prevAct = nextAct_PickupLoop;
            i++;
        }
        if (pickupInsertionIndex == InsertionData.NO_INDEX) {
            InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
            emptyInsertionData.getFailedConstraintNames().addAll(failedActivityConstraints);
            return emptyInsertionData;
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
