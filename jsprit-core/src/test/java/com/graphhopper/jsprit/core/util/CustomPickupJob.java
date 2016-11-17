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

package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.JobActivityList;
import com.graphhopper.jsprit.core.problem.job.SequentialJobActivityList;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schroeder on 16/11/16.
 */
public class CustomPickupJob extends AbstractJob {

    public static abstract class BuilderBase<T extends CustomPickupJob, B extends CustomPickupJob.BuilderBase<T, B>>
        extends JobBuilder<T, B> {

        List<Location> locs = new ArrayList<>();

        List<SizeDimension> cap = new ArrayList<>();

        List<String> types = new ArrayList<>();

        public BuilderBase(String id) {
            super(id);
        }

        public CustomPickupJob.BuilderBase<T, B> addPickup(Location location, SizeDimension capacity) {
            locs.add(location);
            cap.add(capacity);
            types.add("pickup");
            return this;
        }

        public CustomPickupJob.BuilderBase<T, B> addDelivery(Location location, SizeDimension capacity) {
            locs.add(location);
            cap.add(capacity);
            types.add("delivery");
            return this;
        }

        public List<Location> getLocs() {
            return locs;
        }

        public List<SizeDimension> getCaps() {
            return cap;
        }

        public List<String> getTypes() {
            return types;
        }

        protected void validate() {

        }
    }

    public static final class Builder extends CustomPickupJob.BuilderBase<CustomPickupJob, CustomPickupJob.Builder> {

        public static CustomPickupJob.Builder newInstance(String id) {
            return new CustomPickupJob.Builder(id);
        }

        public Builder(String id) {
            super(id);
        }

        @Override
        protected CustomPickupJob createInstance() {
            return new CustomPickupJob(this);
        }

    }

    /**
     * Builder based constructor.
     *
     * @param builder The builder instance.
     * @see JobBuilder
     */
    protected CustomPickupJob(JobBuilder<?, ?> builder) {
        super(builder);

    }

    @Override
    public SizeDimension getSize() {
        return SizeDimension.EMPTY;
    }

    @Override
    protected void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder) {
        CustomPickupJob.Builder builder = (CustomPickupJob.Builder) jobBuilder;
        JobActivityList list = new SequentialJobActivityList(this);
        for (int i = 0; i < builder.getLocs().size(); i++) {
            if (builder.getTypes().get(i).equals("pickup")) {
                list.addActivity(new PickupActivityNEW(this, "pick", builder.getLocs().get(i), 0, builder.getCaps().get(i), Arrays.asList(TimeWindow.ETERNITY)));
            } else
                list.addActivity(new DeliveryActivityNEW(this, "delivery", builder.getLocs().get(i), 0, builder.getCaps().get(i).invert(), Arrays.asList(TimeWindow.ETERNITY)));
        }
        setActivities(list);
    }
}

