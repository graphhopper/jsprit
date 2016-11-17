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

import com.graphhopper.jsprit.core.problem.constraint.*;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.misc.ActivityContext;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


final class GeneralJobInsertionCalculator implements JobInsertionCostsCalculator {

    private static final Logger logger = LoggerFactory.getLogger(GeneralJobInsertionCalculator.class);

    private HardRouteConstraint hardRouteLevelConstraint;

    private HardActivityConstraint hardActivityLevelConstraint;

    private SoftRouteConstraint softRouteConstraint;

    private SoftActivityConstraint softActivityConstraint;

    private ActivityInsertionCostsCalculator activityInsertionCostsCalculator;

    private VehicleRoutingTransportCosts transportCosts;

    private VehicleRoutingActivityCosts activityCosts;

    private AdditionalAccessEgressCalculator additionalAccessEgressCalculator;

    public GeneralJobInsertionCalculator(VehicleRoutingTransportCosts routingCosts, VehicleRoutingActivityCosts activityCosts, ActivityInsertionCostsCalculator activityInsertionCostsCalculator, ConstraintManager constraintManager) {
        super();
        this.activityInsertionCostsCalculator = activityInsertionCostsCalculator;
        hardRouteLevelConstraint = constraintManager;
        hardActivityLevelConstraint = constraintManager;
        softActivityConstraint = constraintManager;
        softRouteConstraint = constraintManager;
        transportCosts = routingCosts;
        this.activityCosts = activityCosts;
        additionalAccessEgressCalculator = new AdditionalAccessEgressCalculator(routingCosts);
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
        List<JobActivity> actList = jobToInsert.getActivityList().getAllDuplicated();
        insertionContext.getAssociatedActivities().addAll(actList);
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
        additionalICostsAtRouteLevel += additionalAccessEgressCalculator.getCosts(insertionContext);

        Start start = new Start(newVehicle.getStartLocation(), newVehicle.getEarliestDeparture(), newVehicle.getLatestArrival());
        start.setEndTime(newVehicleDepartureTime);
        End end = new End(newVehicle.getEndLocation(), 0.0, newVehicle.getLatestArrival());

        List<TourActivity> newRoute = new ArrayList<>();
        newRoute.add(start);
        newRoute.addAll(currentRoute.getTourActivities().getActivities());
        newRoute.add(end);

        List<InsertionData> bestData = calculateInsertionCosts(insertionContext, 1, actList, newRoute, additionalICostsAtRouteLevel, newVehicleDepartureTime);
        if (bestData.isEmpty()) {
            return InsertionData.createEmptyInsertionData();
        } else {
            InsertionData best = InsertionData.createEmptyInsertionData();
            for (InsertionData iD : bestData) {
                if (iD.getInsertionCost() < best.getInsertionCost()) {
                    best = iD;
                }
            }
            return best;
        }
    }

    private List<InsertionData> calculateInsertionCosts(JobInsertionContext insertionContext, int index, List<JobActivity> actList, List<TourActivity> newRoute, double additionalCosts, double departureTime) {
        List<InsertionData> iData = new ArrayList<>();
        double departureTimeAtPrevAct = departureTime;
        TourActivity prevAct = newRoute.get(index - 1);
        for (int i = index; i < newRoute.size(); i++) {
            JobActivity jobActivity = actList.get(0);
            if (jobActivity.getTimeWindows().isEmpty()) {
                throw new IllegalStateException("at least a single time window must be set");
            }
            for (TimeWindow timeWindow : jobActivity.getTimeWindows()) {
                JobActivity copiedJobActivity = (JobActivity) jobActivity.duplicate();
                copiedJobActivity.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                copiedJobActivity.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(i);
                insertionContext.setActivityContext(activityContext);
                HardActivityConstraint.ConstraintsStatus constraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct, copiedJobActivity, newRoute.get(i), departureTimeAtPrevAct);
                if (constraintStatus.equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED)) {
                    continue;
                } else if (constraintStatus.equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    return iData;
                }
                double miscCosts = softActivityConstraint.getCosts(insertionContext, prevAct, copiedJobActivity, newRoute.get(i), departureTimeAtPrevAct);
                double c = calculate(insertionContext, prevAct, copiedJobActivity, newRoute.get(i), departureTimeAtPrevAct);
                List<TourActivity> modifiedRoute = new ArrayList<>(newRoute);
                modifiedRoute.add(i, copiedJobActivity);
                double totalCosts = additionalCosts + c + miscCosts;
                if (actList.size() == 1) {
                    InsertionData iD = new InsertionData(totalCosts, insertionContext.getNewDepTime(), insertionContext.getNewVehicle(), insertionContext.getNewDriver());
                    iD.getEvents().add(new SwitchVehicle(insertionContext.getRoute(), insertionContext.getNewVehicle(), insertionContext.getNewDepTime()));
                    iD.getEvents().addAll(getInsertActivityEvents(insertionContext, modifiedRoute));
                    iData.add(iD);

                } else {
                    double departureTimeFromJobActivity = getDeparture(prevAct, copiedJobActivity, departureTimeAtPrevAct, insertionContext.getNewDriver(), insertionContext.getNewVehicle());
                    List<InsertionData> insertions = calculateInsertionCosts(insertionContext, i + 1, actList.subList(1, actList.size()), modifiedRoute, totalCosts, departureTimeFromJobActivity);
                    iData.addAll(insertions);
                }
            }
            departureTimeAtPrevAct = getDeparture(prevAct, newRoute.get(i), departureTimeAtPrevAct, insertionContext.getNewDriver(), insertionContext.getNewVehicle());
            prevAct = newRoute.get(i);
        }
        return iData;
    }

    private double getDeparture(TourActivity prevAct, TourActivity activity, double departureTimeAtPrevAct, Driver driver, Vehicle vehicle) {
        double actArrTime = departureTimeAtPrevAct + transportCosts.getTransportTime(prevAct.getLocation(), activity.getLocation(), departureTimeAtPrevAct, driver, vehicle);
        double actStart = Math.max(actArrTime, activity.getTheoreticalEarliestOperationStartTime());
        return actStart + activityCosts.getActivityDuration(activity, actArrTime, driver, vehicle);
    }

    private Collection<? extends Event> getInsertActivityEvents(JobInsertionContext insertionContext, List<TourActivity> modifiedRoute) {
        List<InsertActivity> insertActivities = new ArrayList<>();
        for (int i = insertionContext.getAssociatedActivities().size() - 1; i >= 0; i--) {
            TourActivity activity = insertionContext.getAssociatedActivities().get(i);
            int activityIndexInModifiedRoute = modifiedRoute.indexOf(activity);
            TourActivity activityInModifiedRoute = modifiedRoute.get(activityIndexInModifiedRoute);
            insertActivities.add(new InsertActivity(insertionContext.getRoute(), insertionContext.getNewVehicle(),
                activityInModifiedRoute, activityIndexInModifiedRoute - i - 1));
        }
        return insertActivities;
    }

    private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
        return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);

    }
}
