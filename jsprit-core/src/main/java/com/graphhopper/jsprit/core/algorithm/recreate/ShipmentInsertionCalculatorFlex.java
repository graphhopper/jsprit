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
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public final class ShipmentInsertionCalculatorFlex extends AbstractInsertionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ShipmentInsertionCalculatorFlex.class);

    private final ConstraintManager constraintManager;

//    private HardRouteConstraint hardRouteLevelConstraint;
//
//    private HardActivityConstraint hardActivityLevelConstraint;

    private SoftRouteConstraint softRouteConstraint;

    private SoftActivityConstraint softActivityConstraint;

    private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private VehicleRoutingTransportCosts transportCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private JobActivityFactory activityFactory;

    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private int evalIndexPickup = Integer.MAX_VALUE;
    private int evalIndexDelivery = Integer.MAX_VALUE;

    public void setEvalIndexPickup(int evalIndexPickup) {
        this.evalIndexPickup = evalIndexPickup;
    }

    public void setEvalIndexDelivery(int evalIndexDelivery) {
        this.evalIndexDelivery = evalIndexDelivery;
    }

    public ShipmentInsertionCalculatorFlex(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager) {
        super();
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        this.constraintManager = constraintManager;
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
            TourActivity nextAct;
            if (i < activities.size()) {
                nextAct = activities.get(i);
            } else {
                nextAct = end;
                tourEnd = true;
            }
            if (i > evalIndexPickup) break;
            if (i == evalIndexPickup || evalIndexPickup == Integer.MAX_VALUE) {
                boolean pickupInsertionNotFulfilledBreak = true;
                for (TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                    pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                    pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                    ActivityContext activityContext = new ActivityContext();
                    activityContext.setInsertionIndex(i);
                    insertionContext.setActivityContext(activityContext);
                    ConstraintsStatus pickupShipmentConstraintStatus = fulfilled(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime, failedActivityConstraints, constraintManager);
                    if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                        pickupInsertionNotFulfilledBreak = false;
                        continue;
                    } else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                        continue;
                    } else if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                        pickupInsertionNotFulfilledBreak = false;
                    }
                    double additionalPickupICosts = softActivityConstraint.getCosts(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);
                    double pickupAIC = calculate(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime);

                    double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                    double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);

                    pickupContext.setArrivalTime(shipmentPickupArrTime);
                    pickupContext.setEndTime(shipmentPickupEndTime);
                    pickupContext.setInsertionIndex(i);
                    insertionContext.setRelatedActivityContext(pickupContext);

                    TourActivity prevActForDeliveryLoop = pickupShipment;
                    double prevActEndTimeForDeliveryLoop = shipmentPickupEndTime;

			/*
            --------------------------------
			 */
                    //deliverShipmentLoop
                    int j = i;
                    boolean tourEndInDeliveryLoop = false;
                    while (!tourEndInDeliveryLoop) {
                        TourActivity nextActForDeliveryLoop;
                        if (j < activities.size()) {
                            nextActForDeliveryLoop = activities.get(j);
                        } else {
                            nextActForDeliveryLoop = end;
                            tourEndInDeliveryLoop = true;
                        }
                        if (j > evalIndexDelivery) break;
                        if (j == evalIndexDelivery || evalIndexDelivery == Integer.MAX_VALUE) {
                            boolean deliveryInsertionNotFulfilledBreak = true;
                            for (TimeWindow deliveryTimeWindow : shipment.getDeliveryTimeWindows()) {
                                deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                                deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                                ActivityContext activityContext_ = new ActivityContext();
                                activityContext_.setInsertionIndex(j);
                                insertionContext.setActivityContext(activityContext_);
                                ConstraintsStatus deliverShipmentConstraintStatus = fulfilled(insertionContext, prevActForDeliveryLoop, deliverShipment, nextActForDeliveryLoop, prevActEndTimeForDeliveryLoop, failedActivityConstraints, constraintManager);
                                if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                                    double additionalDeliveryICosts = softActivityConstraint.getCosts(insertionContext, prevActForDeliveryLoop, deliverShipment, nextActForDeliveryLoop, prevActEndTimeForDeliveryLoop);
                                    double deliveryAIC = calculate(insertionContext, prevActForDeliveryLoop, deliverShipment, nextActForDeliveryLoop, prevActEndTimeForDeliveryLoop);
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
                        }
                        //update prevAct and endTime
                        double nextActArrTime = prevActEndTimeForDeliveryLoop + transportCosts.getTransportTime(prevActForDeliveryLoop.getLocation(), nextActForDeliveryLoop.getLocation(), prevActEndTimeForDeliveryLoop, newDriver, newVehicle);
                        prevActEndTimeForDeliveryLoop = Math.max(nextActArrTime, nextActForDeliveryLoop.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextActForDeliveryLoop, nextActArrTime, newDriver, newVehicle);
                        prevActForDeliveryLoop = nextActForDeliveryLoop;
                        j++;
                    }
                }
                if (pickupInsertionNotFulfilledBreak) {
                    break;
                }
            }

            //update prevAct and endTime
            double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
            prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct, nextActArrTime, newDriver, newVehicle);
            prevAct = nextAct;
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
