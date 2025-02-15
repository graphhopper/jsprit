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
import com.graphhopper.jsprit.core.problem.constraint.HardConstraint;
import com.graphhopper.jsprit.core.problem.constraint.SoftActivityConstraint;
import com.graphhopper.jsprit.core.problem.constraint.SoftRouteConstraint;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
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
import java.util.Collection;
import java.util.Iterator;

/**
 * Calculator that calculates the best insertion position for a {@link Service}.
 *
 * @author schroeder
 */
final class ServiceInsertionCalculator extends AbstractInsertionCalculator {

    private static final Logger logger = LoggerFactory.getLogger(ServiceInsertionCalculator.class);

    private final SoftRouteConstraint softRouteConstraint;

    private final SoftActivityConstraint softActivityConstraint;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private final ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private final JobActivityFactory activityFactory;

    private final AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    private final ConstraintManager constraintManager;

    public ServiceInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager, JobActivityFactory activityFactory) {
        super();
        this.transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        this.constraintManager = constraintManager;
        softActivityConstraint = constraintManager;
        softRouteConstraint = constraintManager;
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
        this.activityFactory = activityFactory;
        logger.debug("initialise {}", this);
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
        InsertionData noInsertion = checkRouteConstraints(insertionContext, constraintManager);
        if (noInsertion != null) return noInsertion;

        Collection<HardConstraint> failedActivityConstraints = new ArrayList<>();

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

            ActivityContext activityContext = new ActivityContext();
            activityContext.setInsertionIndex(actIndex);
            insertionContext.setActivityContext(activityContext);
            boolean not_fulfilled_break = true;
			for(TimeWindow timeWindow : service.getTimeWindows(insertionContext)) {
                deliveryAct2Insert.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                deliveryAct2Insert.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());

                ConstraintsStatus status = fulfilled(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime, failedActivityConstraints, constraintManager);
                if (status.equals(ConstraintsStatus.FULFILLED)) {
                    double additionalICostsAtActLevel = softActivityConstraint.getCosts(insertionContext, prevAct, deliveryAct2Insert, nextAct, prevActStartTime);
                    double additionalTransportationCosts = activityInsertionCostsCalculator.getCosts(insertionContext, prevAct, nextAct, deliveryAct2Insert, prevActStartTime);
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
            for (HardConstraint c : failedActivityConstraints) {
                emptyInsertionData.addFailedConstrainName(c.getClass().getSimpleName());
            }
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


}
