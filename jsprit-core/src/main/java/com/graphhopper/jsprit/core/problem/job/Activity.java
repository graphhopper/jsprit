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

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocation;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupLocationsImpl;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.util.ArrayList;
import java.util.Collection;

public class Activity {

    public enum Type {
        PICKUP, EN_ROUTE_PICKUP, EN_ROUTE_DELIVERY, DELIVERY, SERVICE, BREAK;
    }

    public static class Builder {

        private final Type activityType;

        private final Location location;

        Collection<PickupLocation> pickupLocations = new ArrayList<>();

        Collection<TimeWindow> timeWindows = new ArrayList<>();;

        private double serviceTime;

        public Builder(Location location, Type activityType) {
            this.location = location;
            this.activityType = activityType;
            if (location != null) {
                this.pickupLocations.add(PickupLocation.newInstance(location));
            }
        }

        public Builder(Type activityType) {
            this.location = null;
            this.activityType = activityType;
            this.pickupLocations = null;
        }

        public Builder(PickupLocationsImpl pickupLocations, Type activityType) {
            this.location = null;
            this.activityType = activityType;
            this.pickupLocations = pickupLocations.getPickupLocations();
        }

        public Builder setPickupLocations(Collection<PickupLocation> pickupLocations, Type activityType) {
            this.pickupLocations = pickupLocations;
            return this;
        }

        public Builder setTimeWindows(Collection<TimeWindow> timeWindows) {
            this.timeWindows = timeWindows;
            return this;
        }

        public Builder setServiceTime(double serviceTime) {
            this.serviceTime = serviceTime;
            return this;
        }

        public Activity build() {
            return new Activity(this);
        }
    }

    private final Location location;

    private final Collection<PickupLocation> pickupLocations;

    private final Collection<TimeWindow> timeWindows;

    private final double serviceTime;

    private final Activity.Type activityType;

    Activity(Builder builder) {
        location = builder.location;
        timeWindows = builder.timeWindows;
        serviceTime = builder.serviceTime;
        activityType = builder.activityType;
        pickupLocations = builder.pickupLocations;
    }

    public Type getActivityType() {
        return activityType;
    }

    public Location getLocation() {
        if (location == null && getPickupLocations().size() >= 1) {
            return getPickupLocations().stream().findFirst().get().getLocation();
        }
        return location;
    }

    public Collection<TimeWindow> getTimeWindows() {
        return timeWindows;
    }

    public Collection<PickupLocation> getPickupLocations() { return pickupLocations; }

    public double getServiceTime() {
        return serviceTime;
    }
}
