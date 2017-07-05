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

package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.algorithm.state.StateId;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.End;
import com.graphhopper.jsprit.core.problem.solution.route.activity.Start;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Collection;
import java.util.Map;

/**
 * Created by schroeder on 11/10/16.
 */
public class MaxDistanceConstraint implements HardActivityConstraint {

    private StateManager stateManager;

    private StateId distanceId;

    private TransportDistance distanceCalculator;

    private Double[] maxDistances;

    public MaxDistanceConstraint(StateManager stateManager, StateId distanceId, TransportDistance distanceCalculator, Map<Vehicle, Double> maxDistancePerVehicleMap) {
        this.stateManager = stateManager;
        this.distanceId = distanceId;
        this.distanceCalculator = distanceCalculator;
        makeArray(maxDistancePerVehicleMap);
    }

    private void makeArray(Map<Vehicle, Double> maxDistances) {
        int maxIndex = getMaxIndex(maxDistances.keySet());
        this.maxDistances = new Double[maxIndex + 1];
        for (Vehicle v : maxDistances.keySet()) {
            this.maxDistances[v.getIndex()] = maxDistances.get(v);
        }
    }

    private int getMaxIndex(Collection<Vehicle> vehicles) {
        int index = 0;
        for (Vehicle v : vehicles) {
            if (v.getIndex() > index) index = v.getIndex();
        }
        return index;
    }

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (!hasMaxDistance(iFacts.getNewVehicle())) return ConstraintsStatus.FULFILLED;
        Double currentDistance = 0d;
        boolean routeIsEmpty = iFacts.getRoute().isEmpty();
        if (!routeIsEmpty) {
            currentDistance = stateManager.getRouteState(iFacts.getRoute(), iFacts.getNewVehicle(), distanceId, Double.class);
        }
        double maxDistance = getMaxDistance(iFacts.getNewVehicle());
        if (currentDistance > maxDistance) return ConstraintsStatus.NOT_FULFILLED_BREAK;

        double distancePrevAct2NewAct = distanceCalculator.getDistance(prevAct.getLocation(), newAct.getLocation(), iFacts.getNewDepTime(), iFacts.getNewVehicle());
        double distanceNewAct2nextAct = distanceCalculator.getDistance(newAct.getLocation(), nextAct.getLocation(), iFacts.getNewDepTime(), iFacts.getNewVehicle());
        double distancePrevAct2NextAct = distanceCalculator.getDistance(prevAct.getLocation(), nextAct.getLocation(), prevActDepTime, iFacts.getNewVehicle());
        if (prevAct instanceof Start && nextAct instanceof End) distancePrevAct2NextAct = 0;
        if (nextAct instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
            distanceNewAct2nextAct = 0;
            distancePrevAct2NextAct = 0;
        }
        double additionalDistance = distancePrevAct2NewAct + distanceNewAct2nextAct - distancePrevAct2NextAct;
        if (currentDistance + additionalDistance > maxDistance) return ConstraintsStatus.NOT_FULFILLED;


        double additionalDistanceOfPickup = 0;
        if (newAct instanceof DeliverShipment) {
            int iIndexOfPickup = iFacts.getRelatedActivityContext().getInsertionIndex();
            TourActivity pickup = iFacts.getAssociatedActivities().get(0);
            TourActivity actBeforePickup;
            if (iIndexOfPickup > 0) actBeforePickup = iFacts.getRoute().getActivities().get(iIndexOfPickup - 1);
            else actBeforePickup = new Start(iFacts.getNewVehicle().getStartLocation(), 0, Double.MAX_VALUE);
            TourActivity actAfterPickup;
            if (iIndexOfPickup < iFacts.getRoute().getActivities().size())
                actAfterPickup = iFacts.getRoute().getActivities().get(iIndexOfPickup);
            else
                actAfterPickup = nextAct;
            double distanceActBeforePickup2Pickup = distanceCalculator.getDistance(actBeforePickup.getLocation(), pickup.getLocation(), actBeforePickup.getEndTime(), iFacts.getNewVehicle());
            double distancePickup2ActAfterPickup = distanceCalculator.getDistance(pickup.getLocation(), actAfterPickup.getLocation(), iFacts.getRelatedActivityContext().getEndTime(), iFacts.getNewVehicle());
            double distanceBeforePickup2AfterPickup = distanceCalculator.getDistance(actBeforePickup.getLocation(), actAfterPickup.getLocation(), actBeforePickup.getEndTime(), iFacts.getNewVehicle());
            if (routeIsEmpty) distanceBeforePickup2AfterPickup = 0;
            if (actAfterPickup instanceof End && !iFacts.getNewVehicle().isReturnToDepot()) {
                distancePickup2ActAfterPickup = 0;
                distanceBeforePickup2AfterPickup = 0;
            }
            additionalDistanceOfPickup = distanceActBeforePickup2Pickup + distancePickup2ActAfterPickup - distanceBeforePickup2AfterPickup;
        }


        if (currentDistance + additionalDistance + additionalDistanceOfPickup > maxDistance) {
            return ConstraintsStatus.NOT_FULFILLED;
        }

        return ConstraintsStatus.FULFILLED;
    }

    private boolean hasMaxDistance(Vehicle newVehicle) {
        if (newVehicle.getIndex() >= this.maxDistances.length) return false;
        return this.maxDistances[newVehicle.getIndex()] != null;
    }

    private double getMaxDistance(Vehicle newVehicle) {
        Double maxDistance = this.maxDistances[newVehicle.getIndex()];
        if (maxDistance == null) return Double.MAX_VALUE;
        return maxDistance;
    }
}
