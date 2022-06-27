//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
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
import java.util.Iterator;
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
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        this.constraintManager = constraintManager;
        this.softActivityConstraint = constraintManager;
        this.softRouteConstraint = constraintManager;
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = jobActivityFactory;
        logger.debug("initialise {}", this);
    }

    public String toString() {
        return "[name=calculatesShipmentInsertion]";
    }

    public InsertionData getInsertionData(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownCosts) {
        JobInsertionContext insertionContext = new JobInsertionContext(currentRoute, jobToInsert, newVehicle, newDriver, newVehicleDepartureTime);
        Shipment shipment = (Shipment)jobToInsert;
        TourActivity pickupShipment = (TourActivity)this.activityFactory.createActivities(shipment).get(0);
        TourActivity deliverShipment = (TourActivity)this.activityFactory.createActivities(shipment).get(1);
        insertionContext.getAssociatedActivities().add(pickupShipment);
        insertionContext.getAssociatedActivities().add(deliverShipment);
        InsertionData noInsertion = this.checkRouteContraints(insertionContext, this.constraintManager);
        if (noInsertion != null) {
            return noInsertion;
        } else {
            double additionalICostsAtRouteLevel = this.softRouteConstraint.getCosts(insertionContext);
            double bestCost = bestKnownCosts;
            additionalICostsAtRouteLevel += this.additionalAccessEgressCalculator.getCosts(insertionContext);
            int pickupInsertionIndex = InsertionData.NO_INDEX;
            int deliveryInsertionIndex = InsertionData.NO_INDEX;
            TimeWindow bestPickupTimeWindow = null;
            TimeWindow bestDeliveryTimeWindow = null;
            Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
            start.setEndTime(newVehicleDepartureTime);
            End end = new End(newVehicle.getEndLocation(), 0.0D, newVehicle.getLatestArrival());
            ActivityContext pickupContext = new ActivityContext();
            TourActivity prevAct = start;
            double prevActEndTime = newVehicleDepartureTime;
            int i = 0;
            boolean tourEnd = false;
            List<TourActivity> activities = currentRoute.getTourActivities().getActivities();
            ArrayList failedActivityConstraints = new ArrayList();

            label93:
            while(!tourEnd) {
                Object nextAct_PickupLoop;
                if (i < activities.size()) {
                    nextAct_PickupLoop = (TourActivity)activities.get(i);
                } else {
                    nextAct_PickupLoop = end;
                    tourEnd = true;
                }

                TourActivity nextPickup = (TourActivity)nextAct_PickupLoop;
                if (nextAct_PickupLoop instanceof BreakForMultipleTimeWindowsActivity)
                    nextPickup = this.getBreakCopyWithUpdatedLocation(pickupShipment.getLocation(), (TourActivity)nextAct_PickupLoop);
                else if (nextAct_PickupLoop instanceof RelativeBreakActivity)
                    nextPickup = this.getRelativeBreakCopyWithUpdatedLocation(pickupShipment.getLocation(), (TourActivity)nextAct_PickupLoop);

                boolean pickupInsertionNotFulfilledBreak = true;
                Iterator var35 = shipment.getPickupTimeWindows().iterator();

                while(true) {
                    while(var35.hasNext()) {
                        TimeWindow pickupTimeWindow = (TimeWindow)var35.next();
                        pickupShipment.setTheoreticalEarliestOperationStartTime(pickupTimeWindow.getStart());
                        pickupShipment.setTheoreticalLatestOperationStartTime(pickupTimeWindow.getEnd());
                        ActivityContext activityContext = new ActivityContext();
                        activityContext.setInsertionIndex(i);
                        insertionContext.setActivityContext(activityContext);
                        ConstraintsStatus pickupShipmentConstraintStatus = this.fulfilled(insertionContext, (TourActivity)prevAct, pickupShipment, (TourActivity)nextPickup, prevActEndTime, failedActivityConstraints, this.constraintManager);
                        if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED)) {
                            pickupInsertionNotFulfilledBreak = false;
                        } else if (!pickupShipmentConstraintStatus.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                            if (pickupShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                                pickupInsertionNotFulfilledBreak = false;
                            }

                            double additionalPickupICosts = this.softActivityConstraint.getCosts(insertionContext, (TourActivity)prevAct, pickupShipment, (TourActivity)nextPickup, prevActEndTime);
                            double pickupAIC = this.calculate(insertionContext, (TourActivity)prevAct, pickupShipment, (TourActivity)nextPickup, prevActEndTime);
                            TourActivity prevAct_deliveryLoop = pickupShipment;
                            double shipmentPickupArrTime = prevActEndTime + this.transportCosts.getTransportTime(((TourActivity)prevAct).getLocation(), pickupShipment.getLocation(), prevActEndTime, newDriver, newVehicle);
                            double shipmentPickupEndTime = Math.max(shipmentPickupArrTime, pickupShipment.getTheoreticalEarliestOperationStartTime()) + this.activityCosts.getActivityDuration((TourActivity)prevAct, pickupShipment, shipmentPickupArrTime, newDriver, newVehicle);
                            pickupContext.setArrivalTime(shipmentPickupArrTime);
                            pickupContext.setEndTime(shipmentPickupEndTime);
                            pickupContext.setInsertionIndex(i);
                            insertionContext.setRelatedActivityContext(pickupContext);
                            double prevActEndTime_deliveryLoop = shipmentPickupEndTime;
                            int j = i;

                            for(boolean tourEnd_deliveryLoop = false; !tourEnd_deliveryLoop; ++j) {
                                Object nextAct_deliveryLoop;
                                if (j < activities.size()) {
                                    nextAct_deliveryLoop = (TourActivity)activities.get(j);
                                } else {
                                    nextAct_deliveryLoop = end;
                                    tourEnd_deliveryLoop = true;
                                }

                                TourActivity next = (TourActivity)nextAct_deliveryLoop;

                                if (nextAct_deliveryLoop instanceof BreakForMultipleTimeWindowsActivity)
                                    next = this.getBreakCopyWithUpdatedLocation(deliverShipment.getLocation(), (TourActivity)nextAct_deliveryLoop);
                                else if (nextAct_deliveryLoop instanceof RelativeBreakActivity) // add this also in service
                                    next = this.getRelativeBreakCopyWithUpdatedLocation(deliverShipment.getLocation(), (TourActivity)nextAct_deliveryLoop);

                                boolean deliveryInsertionNotFulfilledBreak = true;
                                Iterator var55 = shipment.getDeliveryTimeWindows().iterator();

                                while(var55.hasNext()) {
                                    TimeWindow deliveryTimeWindow = (TimeWindow)var55.next();
                                    deliverShipment.setTheoreticalEarliestOperationStartTime(deliveryTimeWindow.getStart());
                                    deliverShipment.setTheoreticalLatestOperationStartTime(deliveryTimeWindow.getEnd());
                                    ActivityContext activityContext_ = new ActivityContext();
                                    activityContext_.setInsertionIndex(j);
                                    insertionContext.setActivityContext(activityContext_);
                                    ConstraintsStatus deliverShipmentConstraintStatus = this.fulfilled(insertionContext, (TourActivity)prevAct_deliveryLoop, deliverShipment, (TourActivity)next, prevActEndTime_deliveryLoop, failedActivityConstraints, this.constraintManager);
                                    if (deliverShipmentConstraintStatus.equals(ConstraintsStatus.FULFILLED)) {
                                        double additionalDeliveryICosts = this.softActivityConstraint.getCosts(insertionContext, (TourActivity)prevAct_deliveryLoop, deliverShipment, (TourActivity)next, prevActEndTime_deliveryLoop);
                                        double deliveryAIC = this.calculate(insertionContext, (TourActivity)prevAct_deliveryLoop, deliverShipment, (TourActivity)next, prevActEndTime_deliveryLoop);
                                        double totalActivityInsertionCosts = pickupAIC + deliveryAIC + additionalICostsAtRouteLevel + additionalPickupICosts + additionalDeliveryICosts;
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

                                if (deliveryInsertionNotFulfilledBreak) {
                                    break;
                                }

                                double nextActArrTime = prevActEndTime_deliveryLoop + this.transportCosts.getTransportTime(((TourActivity)prevAct_deliveryLoop).getLocation(), ((TourActivity)next).getLocation(), prevActEndTime_deliveryLoop, newDriver, newVehicle);
                                prevActEndTime_deliveryLoop = Math.max(nextActArrTime, ((TourActivity)next).getTheoreticalEarliestOperationStartTime()) + this.activityCosts.getActivityDuration((TourActivity)prevAct_deliveryLoop, (TourActivity)next, nextActArrTime, newDriver, newVehicle);
                                prevAct_deliveryLoop = (TourActivity)nextAct_deliveryLoop;
                            }
                        }
                    }

                    if (pickupInsertionNotFulfilledBreak) {
                        break label93;
                    }

                    double nextActArrTime = prevActEndTime + this.transportCosts.getTransportTime(((TourActivity)prevAct).getLocation(), ((TourActivity)nextPickup).getLocation(), prevActEndTime, newDriver, newVehicle);
                    prevActEndTime = Math.max(nextActArrTime, ((TourActivity)nextPickup).getTheoreticalEarliestOperationStartTime()) + this.activityCosts.getActivityDuration((TourActivity)prevAct, (TourActivity)nextPickup, nextActArrTime, newDriver, newVehicle);
                    prevAct = (TourActivity)nextAct_PickupLoop;
                    ++i;
                    break;
                }
            }

            if (pickupInsertionIndex == InsertionData.NO_INDEX) {
                InsertionData emptyInsertionData = new NoInsertionFound();
                emptyInsertionData.getFailedConstraintNames().addAll(failedActivityConstraints);
                return emptyInsertionData;
            } else {
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
        }
    }

    private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
        return this.activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);
    }
}
