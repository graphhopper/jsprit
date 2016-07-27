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

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Hard constraint that evaluates whether a new activity can be inserted between an activity segment (prevAct,nextAct).
 */
public interface HardActivityConstraint extends HardConstraint {

    /**
     * Indicates whether a hard activity constraint is fulfilled or not
     */
    static enum ConstraintsStatus {

        NOT_FULFILLED_BREAK, NOT_FULFILLED, FULFILLED

    }

    /**
     * Returns whether newAct can be inserted in between prevAct and nextAct.
     * <p>
     * <p>
     * When you check activities, you need to understand the following:
     * <p>
     * Let us assume an existing route;
     * <p>
     * start, ..., i-1, i, j, j+1, ..., end
     * <p>
     * When inserting a shipment, two activities will be inserted, pickupShipment k_pick and deliverShipment k_deliver,
     * i.e. jsprit loops through this route (activity sequence) and checks hard and soft constraints and calculates (marginal) insertion costs. For
     * the activity sequence above, it means:
     * <p>
     * start, k_pick, start+1 (prevAct, newAct, nextAct)<br>
     * ...<br>
     * i-1, k_pick, i<br>
     * i, k_pick, j<br>
     * ...<br>
     * <p>
     * accordingly:<br>
     * start, k_pick, k_delivery (prevAct, newAct, nextAct)<br>
     * ...<br>
     * i-1, k_delivery, i<br>
     * i, k_delivery, j<br>
     * ...<br>
     * <p>
     * You specify a hard activity constraint, you to check whether for example k_pick can be inserted between prevActivity and nextActivity at all.
     * If so, your hard constraint should return ConstraintsStatus.FULFILLED.<br>
     * If not, you can return ConstraintsStatus.NOT_FULFILLED or ConstraintsStatus.NOT_FULFILLED_BREAK.<br>
     * <p>
     * Latter should be used, if your constraint can never be fulfilled anymore when looping further through your route.
     * <p>
     * Since constraint checking at activity level is rather time consuming (you need to do this thousand/millions times),
     * you can memorize states behind activities to avoid additional loopings through your activity sequence and thus to
     * check your constraint locally (only by looking at prevAct, newAct, nextAct) in constant time.
     *
     * @param iFacts         JobInsertionContext provides additional information that might be important when evaluating the insertion of newAct
     * @param prevAct        the previous activity, i.e. the activity before the new activity
     * @param newAct         the new activity to be inserted in between prevAct and nextAct
     * @param nextAct        the next activity, i.e. the activity after the new activity
     * @param prevActDepTime the departure time at previous activity (prevAct) with the new vehicle (iFacts.getNewVehicle())
     * @return fulfilled if hard constraint is met, other not fulfilled.
     */
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

}
