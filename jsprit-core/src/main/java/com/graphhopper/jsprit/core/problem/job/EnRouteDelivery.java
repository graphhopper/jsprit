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
 * Delivery extends Service and is intended to model a Service where smth is UNLOADED (i.e. delivered) from a transport unit.
 *
 * @author schroeder
 */
public class EnRouteDelivery extends Service {

    public static class Builder extends Service.Builder<EnRouteDelivery> {

        /**
         * Returns a new instance of builder that builds a delivery.
         *
         * @param id the id of the delivery
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        Builder(String id) {
            super(id);
        }


        public Builder setMaxTimeInVehicle(double maxTimeInVehicle) {
            if (maxTimeInVehicle < 0) throw new IllegalArgumentException("maxTimeInVehicle should not be negative");
            this.maxTimeInVehicle = maxTimeInVehicle;
            return this;
        }

        /**
         * Builds Delivery.
         *
         * @return delivery
         * @throws IllegalArgumentException if neither locationId nor coord is set
         */
        public EnRouteDelivery build() {
            if (location == null) throw new IllegalArgumentException("location is missing");
            this.setType("en_route_delivery");
            super.capacity = super.capacityBuilder.build();
            super.skills = super.skillBuilder.build();
            super.activity = new Activity.Builder(location, Activity.Type.EN_ROUTE_DELIVERY).setTimeWindows(timeWindows.getTimeWindows()).setServiceTime(serviceTime).build();
            return new EnRouteDelivery(this);
        }

    }

    EnRouteDelivery(Builder builder) {
        super(builder);

    }

    public Type getJobType() {
        return Type.EN_ROUTE_DELIVERY;
    }

    @Override
    public boolean isPickedUpAtVehicleStart() {
        return false;
    }

    @Override
    public boolean isDeliveredToVehicleEnd() {
        return false;
    }

}
