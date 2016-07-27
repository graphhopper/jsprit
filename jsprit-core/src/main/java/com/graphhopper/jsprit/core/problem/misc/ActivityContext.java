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

package com.graphhopper.jsprit.core.problem.misc;

/**
 * Provides insertion context information about a particular activity.
 */
public class ActivityContext {

    private double arrivalTime;

    private double endTime;

    private int insertionIndex;

    /**
     * Returns arrival time at associated activity.
     *
     * @return arrival time
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Returns end time of associated activity.
     *
     * @return end time
     */
    public double getEndTime() {
        return endTime;
    }

    /**
     * Returns the insertion index of the associated vehicle.
     * <p>
     * <p>The associated activity is not inserted yet. The actual insertion position is still to be evaluated.
     * Thus this insertion index is related to the potential insertion index which is the position before
     * the activity at this index in the existing route.
     * <p>
     * if insertionIndex == 0, the associated activity will be inserted between start of vehicle and the first
     * activity in activity sequence.
     * <p>
     * if insertionIndex == relatedRoute.getActivities().size(), the associated activity will be inserted between
     * the last activity in the activity sequence and the end of vehicle.
     *
     * @return insertion index
     */
    public int getInsertionIndex() {
        return insertionIndex;
    }

    /**
     * Sets arrivalTime of associated vehicle at activity.
     *
     * @param arrivalTime arrival time of associated vehicle at activity
     */
    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    /**
     * Sets end time of associated activity.
     *
     * @param endTime end time
     */
    public void setEndTime(double endTime) {
        this.endTime = endTime;
    }

    /**
     * Sets insertion index of associated activity.
     *
     * @param insertionIndex insertion index of associated activity
     */
    public void setInsertionIndex(int insertionIndex) {
        this.insertionIndex = insertionIndex;
    }
}
