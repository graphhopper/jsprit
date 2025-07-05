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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.HasIndex;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.Collection;

/**
 * Basic interface for tour-activities.
 * <p>
 * <p>A tour activity is the basic element of a tour, which is consequently a sequence of tour-activities.
 *
 * @author schroeder
 */
public interface TourActivity extends HasIndex {

    void setTheoreticalEarliestOperationStartTime(double earliest);

    void setTheoreticalLatestOperationStartTime(double latest);

    void setLocation(Location location);

    /**
     * Basic interface of job-activies.
     * <p>
     * <p>A job activity is related to a {@link Job}.
     *
     * @author schroeder
     */
    interface JobActivity extends TourActivity {

        /**
         * Returns the job that is involved with this activity.
         *
         * @return job
         */
        Job getJob();

    }

    /**
     * Returns the name of this activity.
     *
     * @return name
     */
    String getName();

    /**
     * Returns location.
     *
     * @return location
     */
    Location getLocation();


    Boolean isMultipleLocation();

    Collection<PickupLocation> getPickupLocations();

    /**
     * Returns the theoretical earliest operation start time, which is the time that is just allowed
     * (not earlier) to start this activity, that is for example <code>service.getTimeWindow().getStart()</code>.
     *
     * @return earliest start time
     */
    double getTheoreticalEarliestOperationStartTime();

    /**
     * Returns the theoretical latest operation start time, which is the time that is just allowed
     * (not later) to start this activity, that is for example <code>service.getTimeWindow().getEnd()</code>.
     *
     * @return latest start time
     */
    double getTheoreticalLatestOperationStartTime();

    /**
     * Returns the operation-time this activity takes.
     * <p>
     * <p>Note that this is not necessarily the duration of this activity, but the
     * service time a pickup/delivery actually takes, that is for example <code>service.getServiceTime()</code>.
     *
     * @return operation time
     */
    double getOperationTime();

    /**
     * Returns the arrival-time of this activity.
     *
     * @return arrival time
     */
    double getArrTime();

    /**
     * Returns end-time of this activity.
     *
     * @return end time
     */
    double getEndTime();

    /**
     * Sets the arrival time of that activity.
     *
     * @param arrTime
     */
    void setArrTime(double arrTime);

    /**
     * Sets the end-time of this activity.
     *
     * @param endTime
     */
    void setEndTime(double endTime);

    /**
     * Returns the capacity-demand of that activity, in terms of what needs to be loaded or unloaded at
     * this activity.
     *
     * @return capacity
     */
    Capacity getSize();

    /**
     * Makes a deep copy of this activity.
     *
     * @return copied activity
     */
    TourActivity duplicate();

}
