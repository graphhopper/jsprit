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

import java.util.Collection;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;

/**
 * A {@linkplain JobActivity} representing a activity where something is picked
 * up (loaded to the vehicle).
 *
 * @author Balage
 */
public class PickupActivity extends JobActivity {

    /**
     * Constructor.
     *
     * @param job
     *            The job the activity is part of.
     * @param type
     *            The type of the activity.
     * @param location
     *            The location of the activity.
     * @param operationTime
     *            The duration of the activity.
     * @param capacity
     *            The cargo change of the activity.
     * @param timeWindows
     *            The time windows of the activity.
     */
    public PickupActivity(AbstractJob job, String name, Location location, double operationTime,
            SizeDimension capacity, Collection<TimeWindow> timeWindows) {
        super(job, name, location, operationTime, capacity, timeWindows);
    }

    /**
     * Copy constructor.
     * <p>
     * This makes a <b>shallow</b> copy of the <code>sourceActivity</code>.
     * </p>
     *
     * @param sourceActivity
     *            The activity to copy.
     */
    public PickupActivity(PickupActivity sourceActivity) {
        super(sourceActivity);
    }

}
