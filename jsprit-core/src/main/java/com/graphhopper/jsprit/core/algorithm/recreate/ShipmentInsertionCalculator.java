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

import com.graphhopper.jsprit.core.algorithm.state.InternalStates;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.JobActivityFactory;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.constraint.HardActivityConstraint.ConstraintsStatus;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.*;
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

    private StateManager stateManager;

    private BreakInsertionCalculator breakInsertionCalculator;

    public ShipmentInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager) {
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

    public void setStateManager(StateManager stateManager) {
        this.stateManager = stateManager;
    }

    public void setBreakInsertionCalculator(BreakInsertionCalculator breakInsertionCalculator) {
        this.breakInsertionCalculator = breakInsertionCalculator;
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

            boolean pickupInsertionNotFulfilledBreak = true;
            for(TimeWindow pickupTimeWindow : shipment.getPickupTimeWindows()) {
                pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(activityContext);
                ConstraintsStatus pickupShipmentConstraintStatus = fulfilled(insertionContext, prevAct, pickupShipment, nextAct, prevActEndTime, failedActivityConstraints, constraintManager);
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
                double shipmentPickupArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);

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
                        ConstraintsStatus deliverShipmentConstraintStatus = fulfilled(insertionContext, prevAct_deliveryLoop, deliverShipment, nextAct_deliveryLoop, prevActEndTime_deliveryLoop, failedActivityConstraints, constraintManager);

                        // check if new vehicle has break
                        Break aBreak = newVehicle.getBreak();
                        if (aBreak != null) {
                            // check if break has been inserted
                            if (!currentRoute.getTourActivities().servesJob(aBreak)) {
                                // get new route end time before insertion of newAct
                                Double routeEndTime = stateManager.getRouteState(currentRoute, newVehicle, InternalStates.END_TIME, Double.class);
                                if (routeEndTime == null) routeEndTime = newVehicle.getEarliestDeparture();
                                // get future waiting of nextAct in the new route
                                Double futureWaiting = stateManager.getActivityState(nextAct, newVehicle, InternalStates.FUTURE_WAITING, Double.class);
                                if (futureWaiting == null) futureWaiting = 0.;
                                // get nextAct end time delay after insertion of newAct in the new route
                                double newActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct.getLocation(), deliverShipment.getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                                double newActEndTime = Math.max(deliverShipment.getTheoreticalEarliestOperationStartTime(), newActArrTime) + activityCosts.getActivityDuration(deliverShipment, newActArrTime, newDriver, newVehicle);
                                double nextActArrTime = newActEndTime + transportCosts.getTransportTime(deliverShipment.getLocation(), nextAct.getLocation(), newActEndTime, newDriver, newVehicle);
                                double nextActEndTime = Math.max(nextAct.getTheoreticalEarliestOperationStartTime(), nextActArrTime) + activityCosts.getActivityDuration(nextAct, nextActArrTime, newDriver, newVehicle);
                                Double nextActEndTimeOld = stateManager.getActivityState(nextAct, newVehicle, InternalStates.END_TIME, Double.class);
                                if (nextActEndTimeOld == null) nextActEndTimeOld = routeEndTime;
                                double nextActEndTimeDelay = Math.max(0., nextActEndTime - nextActEndTimeOld);
                                // get new route end time after insertion of newAct
                                double routeEndTimeNew = routeEndTime + Math.max(0., nextActEndTimeDelay - futureWaiting);
                                // check if new route end time later than break time window
                                if (routeEndTimeNew > aBreak.getTimeWindow().getEnd()) {
                                    VehicleRoute.Builder routeBuilder = VehicleRoute.Builder.newInstance(newVehicle, newDriver);
                                    routeBuilder.setJobActivityFactory(activityFactory);
                                    for (int tourActIndex = 0; tourActIndex < currentRoute.getActivities().size(); tourActIndex++) {
                                        if (tourActIndex == i) {
                                            addJobActToRouteBuilder(routeBuilder, jobToInsert, pickupShipment);
                                        }
                                        if (tourActIndex == j) {
                                            addJobActToRouteBuilder(routeBuilder, jobToInsert, deliverShipment);
                                        }
                                        TourActivity tourActivity = currentRoute.getActivities().get(tourActIndex);
                                        if (tourActivity instanceof TourActivity.JobActivity) {
                                            addJobActToRouteBuilder(routeBuilder, ((TourActivity.JobActivity) tourActivity).getJob(), tourActivity);
                                        }
                                    }
                                    if (i == currentRoute.getActivities().size()) {
                                        addJobActToRouteBuilder(routeBuilder, jobToInsert, pickupShipment);
                                    }
                                    if (j == currentRoute.getActivities().size()) {
                                        addJobActToRouteBuilder(routeBuilder, jobToInsert, deliverShipment);
                                    }
                                    routeBuilder.setDepartureTime(newVehicleDepartureTime);
                                    VehicleRoute route = routeBuilder.build();
                                    stateManager.reCalculateStates(route);
                                    // check if break can be inserted
                                    InsertionData iData = breakInsertionCalculator.getInsertionData(route, aBreak, newVehicle, newVehicleDepartureTime, newDriver, Double.MAX_VALUE);
                                    if (iData instanceof InsertionData.NoInsertionFound) {
                                        deliverShipmentConstraintStatus = ConstraintsStatus.NOT_FULFILLED;
                                    }
                                    if (!currentRoute.isEmpty())
                                        stateManager.reCalculateStates(currentRoute);
                                }
                            }
                        }

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
                    double nextActArrTime = prevActEndTime_deliveryLoop + transportCosts.getTransportTime(prevAct_deliveryLoop.getLocation(), nextAct_deliveryLoop.getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                    prevActEndTime_deliveryLoop = Math.max(nextActArrTime, nextAct_deliveryLoop.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct_deliveryLoop,nextActArrTime,newDriver,newVehicle);
                    prevAct_deliveryLoop = nextAct_deliveryLoop;
                    j++;
                }
            }
            if(pickupInsertionNotFulfilledBreak){
                break;
            }
            //update prevAct and endTime
            double nextActArrTime = prevActEndTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActEndTime, newDriver, newVehicle);
            prevActEndTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct,nextActArrTime,newDriver,newVehicle);
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

    private void addJobActToRouteBuilder(VehicleRoute.Builder routeBuilder, Job job, TourActivity tourActivity) {
        if (job instanceof Pickup)
            routeBuilder.addPickup((Pickup) job);
        else if (job instanceof Delivery)
            routeBuilder.addDelivery((Delivery) job);
        else if (job instanceof Service)
            routeBuilder.addService((Service) job);
        else if (job instanceof Break)
            routeBuilder.addBreak((Break) job);
        else if (job instanceof Shipment) {
            if (tourActivity instanceof PickupShipment)
                routeBuilder.addPickup((Shipment) job);
            else if (tourActivity instanceof DeliverShipment)
                routeBuilder.addDelivery((Shipment) job);
            else
                throw new IllegalStateException("tourActivity " + tourActivity.getName());
        }
        else
            throw new IllegalStateException("job " + job.getName());
    }
}
