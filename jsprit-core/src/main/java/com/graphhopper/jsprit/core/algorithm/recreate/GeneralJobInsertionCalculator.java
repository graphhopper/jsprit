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

    static class ActAndIndex {

        private int index;

        private TourActivity act;

        public ActAndIndex(int index, TourActivity act) {
            this.index = index;
            this.act = act;
        }
    }

    static class IndexedTourActivity {

        int index;

        TourActivity act;

        public IndexedTourActivity(int index, TourActivity act) {
            this.index = index;
            this.act = act;
        }

        void setTourActivity(TourActivity act) {
            this.act = act;
        }
    }

    static class Route {

        private IndexedTourActivity[] acts;

        private int[] successors;

        private int[] predecessors;

        private IndexedTourActivity first;

        private List<IndexedTourActivity> actsToInsert;

        public Route(List<IndexedTourActivity> currentRoute, List<IndexedTourActivity> toInsert) {
            actsToInsert = toInsert;
            successors = new int[currentRoute.size() + toInsert.size()];
            predecessors = new int[currentRoute.size() + toInsert.size()];
            for (int i = 0; i < successors.length; i++) {
                successors[i] = -1;
                predecessors[i] = -1;
            }
            first = currentRoute.get(0);
            ini(currentRoute, toInsert);
        }

        private void ini(List<IndexedTourActivity> currentRoute, List<IndexedTourActivity> toInsert) {
            acts = new IndexedTourActivity[currentRoute.size() + toInsert.size()];
            IndexedTourActivity prevAct = currentRoute.get(0);
            acts[prevAct.index] = prevAct;
            for (int i = 1; i < currentRoute.size(); i++) {
                acts[currentRoute.get(i).index] = currentRoute.get(i);
                setSuccessor(prevAct, currentRoute.get(i));
                setPredecessor(currentRoute.get(i), prevAct);
                prevAct = currentRoute.get(i);
            }
            for (IndexedTourActivity actToInsert : toInsert) {
                acts[actToInsert.index] = actToInsert;
            }
        }

        public IndexedTourActivity getFirst() {
            return first;
        }

        void addAfter(IndexedTourActivity toInsert, IndexedTourActivity after) {
            IndexedTourActivity actAfterAfter = getSuccessor(after);
            setSuccessor(after, toInsert);
            setSuccessor(toInsert, actAfterAfter);
            setPredecessor(toInsert, after);
            setPredecessor(actAfterAfter, toInsert);
        }

        IndexedTourActivity addAfter(JobActivity toInsert_, IndexedTourActivity after) {
            IndexedTourActivity toInsert = find(toInsert_);
            toInsert.setTourActivity(toInsert_);
            IndexedTourActivity actAfterAfter = getSuccessor(after);
            setSuccessor(after, toInsert);
            setSuccessor(toInsert, actAfterAfter);
            setPredecessor(toInsert, after);
            setPredecessor(actAfterAfter, toInsert);
            return toInsert;
        }

        private IndexedTourActivity find(JobActivity toInsert_) {
            for (IndexedTourActivity a : actsToInsert) {
                if (a.act.getIndex() == toInsert_.getIndex()) {
                    return a;
                }
            }
            throw new IllegalStateException("should not be");
        }


        void setSuccessor(IndexedTourActivity act, IndexedTourActivity successor) {
            if (successor == null) successors[act.index] = -1;
            else successors[act.index] = successor.index;
        }

        void setPredecessor(IndexedTourActivity act, IndexedTourActivity predecessor) {
            if (predecessor == null) predecessors[act.index] = -1;
            else predecessors[act.index] = predecessor.index;
        }

        void remove(IndexedTourActivity toRemove) {
            IndexedTourActivity predecessor = getPredecessor(toRemove);
            IndexedTourActivity successor = getSuccessor(toRemove);
            setSuccessor(toRemove, null);
            setPredecessor(toRemove, null);
            setSuccessor(predecessor, successor);
            setPredecessor(successor, predecessor);
        }

        void remove(JobActivity toRemove_) {
            IndexedTourActivity toRemove = find(toRemove_);
            IndexedTourActivity predecessor = getPredecessor(toRemove);
            IndexedTourActivity successor = getSuccessor(toRemove);
            setSuccessor(toRemove, null);
            setPredecessor(toRemove, null);
            setSuccessor(predecessor, successor);
            setPredecessor(successor, predecessor);
        }

        IndexedTourActivity getSuccessor(IndexedTourActivity act) {
            if (hasSuccessor(act)) {
                return acts[successors[act.index]];
            }
            return null;
        }

        IndexedTourActivity getPredecessor(IndexedTourActivity act) {
            if (hasPredecessor(act)) {
                return acts[predecessors[act.index]];
            }
            return null;
        }

        boolean hasSuccessor(IndexedTourActivity act) {
            return successors[act.index] != -1;
        }

        boolean hasPredecessor(IndexedTourActivity act) {
            return predecessors[act.index] != -1;
        }

        Route copy() {
            return null;
        }

        public ActAndIndex indexOf(TourActivity activity) {
            int i = 0;
            IndexedTourActivity prev = getFirst();
            while (hasSuccessor(prev)) {
                IndexedTourActivity succ = getSuccessor(prev);
                if (succ.act.getIndex() == activity.getIndex()) {
                    return new ActAndIndex(i + 1, succ.act);
                }
                i++;
                prev = succ;
            }
            return null;
        }

    }

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
     * assumption that cost changes can entirely covered by only looking at the predecessors i-1 and its successor i+1.
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

        List<IndexedTourActivity> current = makeIndices(newRoute, 0);
        List<IndexedTourActivity> actsToInsert = makeIndices(actList, current.size());
        Route route = new Route(current, actsToInsert);

        List<Integer> insertionIndices = new ArrayList<>();
        List<InsertionData> bestData = calculateInsertionCosts(insertionContext, 0, route.getFirst(), route, actList, additionalICostsAtRouteLevel, newVehicleDepartureTime, insertionIndices);
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

    private List<IndexedTourActivity> makeIndices(List<? extends TourActivity> acts, int startIndex) {
        int index = startIndex;
        List<IndexedTourActivity> indexed = new ArrayList<>();
        for (TourActivity act : acts) {
            indexed.add(new IndexedTourActivity(index, act));
            index++;
        }
        return indexed;
    }

    private List<InsertionData> calculateInsertionCosts(JobInsertionContext insertionContext, int startIndex, IndexedTourActivity startAct, Route route, List<JobActivity> actList, double additionalCosts, double departureTime, List<Integer> insertionIndeces) {
        List<InsertionData> iData = new ArrayList<>();
        double departureTimeAtPrevAct = departureTime;
        IndexedTourActivity prevAct = startAct;
        int index = startIndex;
        while (route.hasSuccessor(prevAct)) {
            JobActivity jobActivity = actList.get(0);
            if (jobActivity.getTimeWindows().isEmpty()) {
                throw new IllegalStateException("at least a single time window must be set");
            }
            for (TimeWindow timeWindow : jobActivity.getTimeWindows()) {
                JobActivity copiedJobActivity = (JobActivity) jobActivity.duplicate();
                copiedJobActivity.setTheoreticalEarliestOperationStartTime(timeWindow.getStart());
                copiedJobActivity.setTheoreticalLatestOperationStartTime(timeWindow.getEnd());
                //Todo: add correct info, e.g. arrival and end time - assure functionality by unit tests - current no info set, but no unit test fails -> this should not be
                ActivityContext activityContext = new ActivityContext();
                activityContext.setInsertionIndex(index + 1);
//                activityContext.setArrivalTime();
                insertionContext.setActivityContext(activityContext);
                HardActivityConstraint.ConstraintsStatus constraintStatus = hardActivityLevelConstraint.fulfilled(insertionContext, prevAct.act, copiedJobActivity, route.getSuccessor(prevAct).act, departureTimeAtPrevAct);
                if (constraintStatus.equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED)) {
                    continue;
                } else if (constraintStatus.equals(HardActivityConstraint.ConstraintsStatus.NOT_FULFILLED_BREAK)) {
                    return iData;
                }
                double miscCosts = softActivityConstraint.getCosts(insertionContext, prevAct.act, copiedJobActivity, route.getSuccessor(prevAct).act, departureTimeAtPrevAct);
                double c = calculate(insertionContext, prevAct.act, copiedJobActivity, route.getSuccessor(prevAct).act, departureTimeAtPrevAct);
                IndexedTourActivity toInsert = route.addAfter(copiedJobActivity, prevAct);
                double totalCosts = additionalCosts + c + miscCosts;
                if (actList.size() == 1) {
                    InsertionData iD = new InsertionData(totalCosts, insertionContext.getNewDepTime(), insertionContext.getNewVehicle(), insertionContext.getNewDriver());
                    iD.getEvents().add(new SwitchVehicle(insertionContext.getRoute(), insertionContext.getNewVehicle(), insertionContext.getNewDepTime()));
                    iD.getEvents().addAll(getInsertActivityEvents(insertionContext, route));
                    iData.add(iD);

                } else {
                    double departureTimeFromJobActivity = getDeparture(prevAct.act, copiedJobActivity, departureTimeAtPrevAct, insertionContext.getNewDriver(), insertionContext.getNewVehicle());
                    insertionIndeces.add(index + 1);
                    List<InsertionData> insertions = calculateInsertionCosts(insertionContext, index + 1, toInsert, route, actList.subList(1, actList.size()), totalCosts, departureTimeFromJobActivity, insertionIndeces);
                    iData.addAll(insertions);
                }
                route.remove(toInsert);
            }
            departureTimeAtPrevAct = getDeparture(prevAct.act, route.getSuccessor(prevAct).act, departureTimeAtPrevAct, insertionContext.getNewDriver(), insertionContext.getNewVehicle());
            prevAct = route.getSuccessor(prevAct);
            index++;
        }
        return iData;
    }

    private double getDeparture(TourActivity prevAct, TourActivity activity, double departureTimeAtPrevAct, Driver driver, Vehicle vehicle) {
        double actArrTime = departureTimeAtPrevAct + transportCosts.getTransportTime(prevAct.getLocation(), activity.getLocation(), departureTimeAtPrevAct, driver, vehicle);
        double actStart = Math.max(actArrTime, activity.getTheoreticalEarliestOperationStartTime());
        return actStart + activityCosts.getActivityDuration(activity, actArrTime, driver, vehicle);
    }

    private Collection<? extends Event> getInsertActivityEvents(JobInsertionContext insertionContext, Route modifiedRoute) {
        List<InsertActivity> insertActivities = new ArrayList<>();
        for (int i = insertionContext.getAssociatedActivities().size() - 1; i >= 0; i--) {
            TourActivity activity = insertionContext.getAssociatedActivities().get(i);
            ActAndIndex actAndIndex = modifiedRoute.indexOf(activity);
            insertActivities.add(new InsertActivity(insertionContext.getRoute(), insertionContext.getNewVehicle(),
                actAndIndex.act, actAndIndex.index - i - 1));
        }
        return insertActivities;
    }

    private double calculate(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double departureTimeAtPrevAct) {
        return activityInsertionCostsCalculator.getCosts(iFacts, prevAct, nextAct, newAct, departureTimeAtPrevAct);

    }
}
