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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;

/**
 * An {@linkplain InternalActivity} marking the start of a route.
 *
 * @author Balage
 *
 */
public final class Start extends InternalActivity {

    /**
     * Factory method to create a new Start activity.
     *
     * @param locationId
     *            The location id (depo) of the start of the route.
     * @param theoreticalStart
     *            The earliest possible start time of the activity.
     * @param theoreticalEnd
     *            The latest possible start time of the activity.
     * @return The new {@linkplain Start} instance.
     */
    public static Start newInstance(String locationId, double theoreticalStart, double theoreticalEnd) {
        Location loc = null;
        if (locationId != null) {
            loc = Location.Builder.newInstance().setId(locationId).build();
        }
        return new Start(loc, theoreticalStart, theoreticalEnd);
    }

    /**
     * Copies the the activity.
     *
     * @param start
     *            The activity to copy.
     * @return The shallow copy of the activity.
     */
    public static Start copyOf(Start start) {
        return new Start(start);
    }

    /**
     * Constructor.
     *
     * @param locationId
     *            The location id (depo) of the start of the route.
     * @param theoreticalStart
     *            The earliest possible start time of the activity.
     * @param theoreticalEnd
     *            The latest possible start time of the activity.
     */
    public Start(Location location, double theoreticalStart, double theoreticalEnd) {
        super("start", location, SizeDimension.EMPTY);
        setTheoreticalEarliestOperationStartTime(theoreticalStart);
        setTheoreticalLatestOperationStartTime(theoreticalEnd);
        endTime = theoreticalStart;
        setIndex(-1);
    }

    /**
     * Copy constructor.
     * <p>
     * Makes a shallow copy.
     * </p>
     *
     * @param start
     *            The activity to copy.
     */
    private Start(Start start) {
        super(start);
    }

    /**
     * Sets the start location.
     * 
     * @param location
     *            The location.
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public double getOperationTime() {
        return 0.0;
    }

    @Override
    public String toString() {
        return "[type=" + getName() + "][location=" + location
                + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
                + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

}
