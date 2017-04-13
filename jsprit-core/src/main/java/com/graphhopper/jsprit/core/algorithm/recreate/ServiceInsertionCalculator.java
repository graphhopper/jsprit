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
import java.util.Collection;
import java.util.Iterator;

/**
 * Calculator that calculates the best insertion position for a {@link Service}.
 *
 * @author schroeder
 */
final class ServiceInsertionCalculator extends AbstractInsertionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInsertionCalculator.class);

//    private HardRouteConstraint hardRouteLevelConstraint;

//    private HardActivityConstraint hardActivityLevelConstraint;

    private SoftRouteConstraint softRouteConstraint;

    private SoftActivityConstraint softActivityConstraint;

    private VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private ActivityInsertionCostsCalculator additionalTransportCostsCalculator;

    private JobActivityFactory activityFactory;

    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private ConstraintManager constraintManager;

    private StateManager stateManager;

    private BreakInsertionCalculator breakInsertionCalculator;

    public ServiceInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator additionalTransportCostsCalculator, ConstraintManager constraintManager) {
        super();
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        softActivityConstraint = constraintManager;
        softRouteConstraint = constraintManager;
        this.additionalTransportCostsCalculator = additionalTransportCostsCalculator;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        logger.debug("initialise {}", this);
    }

    public void setJobActivityFactory(JobActivityFactory jobActivityFactory) {
        this.activityFactory = jobActivityFactory;
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
        Service service = (Service) jobToInsert;
        int insertionIndex = InsertionData.NO_INDEX;

        TourActivity deliveryAct2Insert = activityFactory.createActivities(service).get(0);
        insertionContext.getAssociatedActivities().add(deliveryAct2Insert);

        /*
        check hard constraints at route level
         */
        InsertionData noInsertion = checkRouteContraints(insertionContext, constraintManager);
        if (noInsertion != null) return noInsertion;

        Collection<String> failedActivityConstraints = new ArrayList<>();

        /*
        check soft constraints at route level
         */
        double additionalICostsAtRouteLevel = softRouteConstraint.getCosts(insertionContext);

        double bestCost = bestKnownCosts;
        additionalICostsAtRouteLevel += additionalAccessEgressCalculator.getCosts(insertionContext);
		TimeWindow bestTimeWindow = null;

        /*
        generate new start and end for new vehicle
         */
        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), Double.MAX_VALUE);
        start.setEndTime(newVehicleDepartureTime);
        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        TourActivity prevAct = start;
        double prevActStartTime = newVehicleDepartureTime;
        int actIndex = 0;
        Iterator<TourActivity> activityIterator = currentRoute.getActivities().iterator();
        boolean tourEnd = false;
        while(!tourEnd){
            TourActivity nextAct;
            if(activityIterator.hasNext()) nextAct = activityIterator.next();
            else{
                nextAct = end;
                tourEnd = true;
            }
            boolean not_fulfilled_break = true;
			for(TimeWindow timeWindow : service.getTimeWindows()) {
                deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                deliveryAct2Insert.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(actIndex);
                insertionContext.setActivityContext(activityContext);
                ConstraintsStatus status = fulfilled(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, failedActivityConstraints, constraintManager);

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
                        double newActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), deliveryAct2Insert.getLocation(), prevActStartTime, newDriver, newVehicle);
                        double newActEndTime = Math.max(deliveryAct2Insert.getTheoreticalEarliestOperationStartTime(), newActArrTime) + activityCosts.getActivityDuration(deliveryAct2Insert, newActArrTime, newDriver, newVehicle);
                        double nextActArrTime = newActEndTime + transportCosts.getTransportTime(deliveryAct2Insert.getLocation(), nextAct.getLocation(), newActEndTime, newDriver, newVehicle);
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
                                if (tourActIndex == actIndex) {
                                    addJobActToRouteBuilder(routeBuilder, jobToInsert, deliveryAct2Insert);
                                }
                                TourActivity tourActivity = currentRoute.getActivities().get(tourActIndex);
                                if (tourActivity instanceof TourActivity.JobActivity) {
                                    addJobActToRouteBuilder(routeBuilder, ((TourActivity.JobActivity) tourActivity).getJob(), tourActivity);
                                }
                            }
                            if (actIndex == currentRoute.getActivities().size()) {
                                addJobActToRouteBuilder(routeBuilder, jobToInsert, deliveryAct2Insert);
                            }
                            routeBuilder.setDepartureTime(newVehicleDepartureTime);
                            VehicleRoute route = routeBuilder.build();
                            stateManager.reCalculateStates(route);
                            // check if break can be inserted
                            InsertionData iData = breakInsertionCalculator.getInsertionData(route, aBreak, newVehicle, newVehicleDepartureTime, newDriver, Double.MAX_VALUE);
                            if (iData instanceof InsertionData.NoInsertionFound) {
                                status = ConstraintsStatus.NOT_FULFILLED;
                            }
                            if (!currentRoute.isEmpty())
                                stateManager.reCalculateStates(currentRoute);
                        }
                    }
                }

                if (status.equals(ConstraintsStatus.FULFILLED)) {
                    double additionalICostsAtActLevel = softActivityConstraint.getCosts(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime);
                    double additionalTransportationCosts = additionalTransportCostsCalculator.getCosts(insertionContext, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
                    if (additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts < bestCost) {
                        bestCost = additionalICostsAtRouteLevel + additionalICostsAtActLevel + additionalTransportationCosts;
                        insertionIndex = actIndex;
                        bestTimeWindow = timeWindow;
                    }
                    not_fulfilled_break = false;
                } else if (status.equals(ConstraintsStatus.NOT_FULFILLED)) {
                    not_fulfilled_break = false;
                }
			}
            if(not_fulfilled_break) break;
            double nextActArrTime = prevActStartTime + transportCosts.getTransportTime(prevAct.getLocation(), nextAct.getLocation(), prevActStartTime, newDriver, newVehicle);
            prevActStartTime = Math.max(nextActArrTime, nextAct.getTheoreticalEarliestOperationStartTime()) + activityCosts.getActivityDuration(nextAct,nextActArrTime,newDriver,newVehicle);
            prevAct = nextAct;
            actIndex++;
        }
        if(insertionIndex == InsertionData.NO_INDEX) {
            InsertionData emptyInsertionData = new InsertionData.NoInsertionFound();
            emptyInsertionData.getFailedConstraintNames().addAll(failedActivityConstraints);
            return emptyInsertionData;
        }
        InsertionData insertionData = new InsertionData(bestCost, InsertionData.NO_INDEX, insertionIndex, newVehicle, newDriver);
        deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(bestTimeWindow.getStart());
        deliveryAct2Insert.setTheoreticalLatestOperationStartTime(bestTimeWindow.getEnd());
        insertionData.getEvents().add(new InsertActivity(currentRoute, newVehicle, deliveryAct2Insert, insertionIndex));
        insertionData.getEvents().add(new SwitchVehicle(currentRoute,newVehicle,newVehicleDepartureTime));
        insertionData.setVehicleDepartureTime(newVehicleDepartureTime);
        return insertionData;
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
