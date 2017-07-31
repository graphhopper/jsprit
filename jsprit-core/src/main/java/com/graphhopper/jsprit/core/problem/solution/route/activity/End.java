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
 * An {@linkplain InternalActivity} marking the end of a route.
 *
 * @author Balage
 *
 */
public final class End extends InternalActivity {

    /**
     * Factory method to create a new End activity.
     *
     * @param locationId
     *            The location id (depo) of the end of the route.
     * @param theoreticalStart
     *            The earliest possible start time of the activity.
     * @param theoreticalEnd
     *            The latest possible start time of the activity.
     * @return The new {@linkplain End} instance.
     */
    public static End newInstance(String locationId, double theoreticalStart, double theoreticalEnd) {
        Location loc = null;
        if (locationId != null) {
            loc = Location.Builder.newInstance().setId(locationId).build();
        }
        return new End(loc, theoreticalStart, theoreticalEnd);
    }

    /**
     * Copies the the activity.
     *
     * @param start
     *            The activity to copy.
     * @return The shallow copy of the activity.
     */
    public static End copyOf(End end) {
        return new End(end);
    }

    /**
     * Constructor.
     *
     * @param locationId
     *            The location id (depo) of the end of the route.
     * @param theoreticalStart
     *            The earliest possible start time of the activity.
     * @param theoreticalEnd
     *            The latest possible start time of the activity.
     * @return The new {@linkplain End} instance.
     */
    public End(Location location, double theoreticalStart, double theoreticalEnd) {
        super("end", location, SizeDimension.EMPTY);
        setTheoreticalEarliestOperationStartTime(theoreticalStart);
        setTheoreticalLatestOperationStartTime(theoreticalEnd);
        endTime = theoreticalStart;
        setIndex(-2);
    }

    /**
     * Copy constructor.
     * <p>
     * Makes a shallow copy.
     * </p>
     *
     * @param end
     *            The activity to copy.
     */
    private End(End end) {
        super(end);
    }

    /**
     * Sets the end location.
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
