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
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.util.Collection;

public class Activity {

    public enum Type {
        PICKUP, DELIVERY, SERVICE, BREAK;
    }

    public static class Builder {

        private final Type activityType;

        private Location location;

        Collection<TimeWindow> timeWindows;

        private double serviceTime;

        public Builder(Location location, Type activityType) {
            this.location = location;
            this.activityType = activityType;
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

    private Location location;

    private Collection<TimeWindow> timeWindows;

    private double serviceTime;

    private Activity.Type activityType;

    Activity(Builder builder) {
        location = builder.location;
        timeWindows = builder.timeWindows;
        serviceTime = builder.serviceTime;
        activityType = builder.activityType;
    }

    public Type getActivityType() {
        return activityType;
    }

    public Location getLocation() {
        return location;
    }

    public Collection<TimeWindow> getTimeWindows() {
        return timeWindows;
    }

    public double getServiceTime() {
        return serviceTime;
    }
}
