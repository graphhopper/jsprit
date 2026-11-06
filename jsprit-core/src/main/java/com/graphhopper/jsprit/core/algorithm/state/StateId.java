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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.HasIndex;

/**
 * Created by schroeder on 28.07.14.
 */
public interface StateId extends HasIndex {

    /**
     * True if the state behind this id is a double. Such states live in primitive arrays of the
     * {@link StateManager} (no boxing on put, no cast on get); every other state is stored as an Object.
     * Created with {@link StateManager#createDoubleStateId(String)}.
     */
    default boolean isDouble() {
        return getDoubleIndex() >= 0;
    }

    /**
     * Index of a double state in the StateManager's primitive arrays, -1 for an object state. Double
     * states have their own compact index space so that the primitive arrays are sized by the number of
     * double states, not by the number of all states (relation-heavy problems create hundreds of ids).
     */
    default int getDoubleIndex() {
        return -1;
    }

}
