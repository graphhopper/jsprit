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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.JobActivityList;
import com.graphhopper.jsprit.core.problem.job.SequentialJobActivityList;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ExchangeActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * Created by schroeder on 16/11/16.
 */
public class CustomJob extends AbstractJob {

    public static abstract class BuilderBase<T extends CustomJob, B extends CustomJob.BuilderBase<T, B>>
        extends JobBuilder<T, B> {

        List<Location> locs = new ArrayList<>();

        List<SizeDimension> cap = new ArrayList<>();

        List<String> types = new ArrayList<>();

        public BuilderBase(String id) {
            super(id);
        }

        public CustomJob.BuilderBase<T, B> addPickup(Location location, SizeDimension loadChange) {
            add(location, loadChange);
            types.add("pickup");
            return this;
        }

        private void add(Location location, SizeDimension loadChange) {
            locs.add(location);
            cap.add(loadChange);
        }

        public CustomJob.BuilderBase<T, B> addDelivery(Location location, SizeDimension loadChange) {
            add(location, loadChange);
            types.add("delivery");
            return this;
        }

        public CustomJob.BuilderBase<T, B> addExchange(Location location, SizeDimension loadChange) {
            add(location, loadChange);
            types.add("exchange");
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

    public static final class Builder extends CustomJob.BuilderBase<CustomJob, CustomJob.Builder> {

        public static CustomJob.Builder newInstance(String id) {
            return new CustomJob.Builder(id);
        }

        public Builder(String id) {
            super(id);
        }

        @Override
        protected CustomJob createInstance() {
            return new CustomJob(this);
        }

    }

    /**
     * Builder based constructor.
     *
     * @param builder The builder instance.
     * @see JobBuilder
     */
    protected CustomJob(JobBuilder<?, ?> builder) {
        super(builder);

    }

    @Override
    public SizeDimension getSize() {
        return SizeDimension.EMPTY;
    }

    @Override
    protected void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder) {
        CustomJob.Builder builder = (CustomJob.Builder) jobBuilder;
        JobActivityList list = new SequentialJobActivityList(this);
        for (int i = 0; i < builder.getLocs().size(); i++) {
            if (builder.getTypes().get(i).equals("pickup")) {
                list.addActivity(new PickupActivity(this, "pick", builder.getLocs().get(i), 0, builder.getCaps().get(i), Arrays.asList(TimeWindow.ETERNITY)));
            } else if (builder.getTypes().get(i).equals("delivery")) {
                list.addActivity(new DeliveryActivity(this, "delivery", builder.getLocs().get(i), 0, builder.getCaps().get(i).invert(), Arrays.asList(TimeWindow.ETERNITY)));
            } else {
                list.addActivity(new ExchangeActivity(this, "exchange", builder.getLocs().get(i), 0, builder.getCaps().get(i), Arrays.asList(TimeWindow.ETERNITY)));
            }
        }
        setActivities(list);
    }
}

