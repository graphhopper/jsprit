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


/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 *
 * @author schroeder
 */
public class Pickup extends Service {

    public static class Builder extends Service.Builder<Pickup> {

        /**
         * Returns a new instance of builder that builds a pickup.
         *
         * @param id the id of the pickup
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        Builder(String id) {
            super(id);
        }

        public Builder setMaxTimeInVehicle(double maxTimeInVehicle){
            throw new UnsupportedOperationException("maxTimeInVehicle is not yet supported for Pickups and Services (only for Deliveries and Shipments)");
//            if(maxTimeInVehicle < 0) throw new IllegalArgumentException("maxTimeInVehicle should be positive");
//            this.maxTimeInVehicle = maxTimeInVehicle;
//            return this;
        }

        /**
         * Builds Pickup.
         * <p>
         * <p>Pickup type is "pickup"
         *
         * @return pickup
         * @throws IllegalArgumentException if neither locationId nor coordinate has been set
         */
        public Pickup build() {
            if (location == null) throw new IllegalArgumentException("location is missing");
            this.setType("pickup");
            super.capacity = super.capacityBuilder.build();
            super.skills = super.skillBuilder.build();
            return new Pickup(this);
        }

    }

    Pickup(Builder builder) {
        super(builder);
    }

}
