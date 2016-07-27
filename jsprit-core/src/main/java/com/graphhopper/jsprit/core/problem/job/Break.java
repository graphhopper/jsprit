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
package com.graphhopper.jsprit.core.problem.job;


import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Skills;

/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 *
 * @author schroeder
 */
public class Break extends Service {

    public static class Builder extends Service.Builder<Break> {

        /**
         * Returns a new instance of builder that builds a pickup.
         *
         * @param id the id of the pickup
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        private boolean variableLocation = true;

        Builder(String id) {
            super(id);
        }

        /**
         * Builds Pickup.
         * <p>
         * <p>Pickup type is "pickup"
         *
         * @return pickup
         * @throws IllegalStateException if neither locationId nor coordinate has been set
         */
        public Break build() {
            if (location != null) {
                variableLocation = false;
            }
            this.setType("break");
            super.capacity = Capacity.Builder.newInstance().build();
            super.skills = Skills.Builder.newInstance().build();
            return new Break(this);
        }

    }

    private boolean variableLocation = true;

    Break(Builder builder) {
        super(builder);
        this.variableLocation = builder.variableLocation;
    }

    public boolean hasVariableLocation() {
        return variableLocation;
    }

}
