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
package com.graphhopper.jsprit.core.problem.cost;

import com.graphhopper.jsprit.core.problem.Location;

/**
 * Implemented by a cost function that can tell which locations are the same for it: every time, duration and
 * distance it returns from, to or at one of them equals the one for the other. Vehicles that start or end at
 * such locations can share their vehicle-dependent states although the locations have different ids, e.g. when
 * every vehicle of a depot comes with its own location id.
 */
public interface EquivalentLocations {

    /** A value with equals and hashCode that is equal for exactly the locations that are the same for this function. */
    Object locationKey(Location location);
}
