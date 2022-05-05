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
package com.graphhopper.jsprit.io.problem;

final class Schema {

    public static final String PROBLEM = "problem";
    public static final String VEHICLE = "vehicle";
    public static final String TYPES = "vehicleTypes";
    public static final String VEHICLES = "vehicles";
    public static final String SHIPMENTS = "shipments";
    public static final String SHIPMENT = "shipment";
    public static final String SERVICETIME = "serviceTime";
    public static final String PICKUP = "pickup";
    public static final String TYPE = "type";


    public void dot() {

    }

    public static class PathBuilder {

        StringBuilder stringBuilder = new StringBuilder();
        boolean justCreated = true;


        public PathBuilder dot(String string) {
            stringBuilder.append(".").append(string);
            return this;
        }

        public PathBuilder append(String string) {
            stringBuilder.append(string);
            return this;
        }

        public String build() {
            return stringBuilder.toString();
        }

    }

    public static PathBuilder builder() {
        return new PathBuilder();
    }

    private Schema() {

    }
}
