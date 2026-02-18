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

package com.graphhopper.jsprit.core.problem.solution.spec;

import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipment;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Specifies one activity in a route.
 * <p>
 * This is pure data - no actual Activity objects are referenced.
 * The spec is materialized into real activities when building the solution.
 *
 * @param jobId   the ID of the job this activity belongs to
 * @param type    which activity of the job (VISIT for single-stop, PICKUP/DELIVERY for shipments)
 * @param options optional overrides (null = use defaults)
 */
public record ActivitySpec(
        String jobId,
        ActivityType type,
        ActivityOptions options
) {
    /**
     * Creates an activity spec with default options.
     */
    public static ActivitySpec of(String jobId, ActivityType type) {
        return new ActivitySpec(jobId, type, null);
    }

    /**
     * Creates a VISIT activity spec (for single-stop jobs).
     */
    public static ActivitySpec visit(String jobId) {
        return new ActivitySpec(jobId, ActivityType.VISIT, null);
    }

    /**
     * Creates a PICKUP activity spec (for shipments).
     */
    public static ActivitySpec pickup(String jobId) {
        return new ActivitySpec(jobId, ActivityType.PICKUP, null);
    }

    /**
     * Creates a DELIVERY activity spec (for shipments).
     */
    public static ActivitySpec delivery(String jobId) {
        return new ActivitySpec(jobId, ActivityType.DELIVERY, null);
    }

    /**
     * Extracts an activity spec from an existing job activity.
     *
     * @param activity the job activity to extract from
     * @return a spec representing the activity
     */
    public static ActivitySpec from(TourActivity.JobActivity activity) {
        String jobId = activity.getJob().getId();
        ActivityType type;

        if (activity instanceof PickupShipment) {
            type = ActivityType.PICKUP;
        } else if (activity instanceof DeliverShipment) {
            type = ActivityType.DELIVERY;
        } else {
            type = ActivityType.VISIT;
        }

        return new ActivitySpec(jobId, type, null);
    }
}
