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

/**
 * Specifies which activity of a job to schedule in a route.
 */
public enum ActivityType {
    /**
     * The pickup activity of a shipment (first activity).
     */
    PICKUP,

    /**
     * The delivery activity of a shipment (second activity).
     */
    DELIVERY,

    /**
     * The single activity of a one-stop job (Service, Pickup, or Delivery job).
     */
    VISIT
}
