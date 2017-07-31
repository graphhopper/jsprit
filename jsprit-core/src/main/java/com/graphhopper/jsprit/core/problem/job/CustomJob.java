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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ExchangeActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindows;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;

/**
 * This is a general purpose, highly configurable job.
 *
 * <p>
 * This job offers enough flexibility for most of the problems. It could hold
 * any number of sequential activities. With the <code>userData</code> field,
 * any associated business data can be linked to the job.
 * </p>
 *
 * <p>
 * For details see its {@linkplain Builder}.
 * </p>
 *
 * Created by schroeder on 16/11/16.
 *
 * @author schroeder
 * @author balage
 */
public class CustomJob extends AbstractJob {

    protected static abstract class BuilderBase<T extends CustomJob, B extends CustomJob.BuilderBase<T, B>>
    extends JobBuilder<T, B> {

        public enum ActivityType {
            SERVICE {

                @Override
                public JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new ServiceActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            PICKUP {

                @Override
                public JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new PickupActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            DELIVERY {

                @Override
                public JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new DeliveryActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            EXCHANGE {

                @Override
                public JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new ExchangeActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            };

            public abstract JobActivity create(CustomJob job, BuilderActivityInfo builderActivityInfo);

            private static Collection<TimeWindow> prepareTimeWindows(BuilderActivityInfo info) {
                TimeWindows tws = info.getTimeWindows();
                if (tws.getTimeWindows().isEmpty()) {
                    tws = TimeWindows.ANY_TIME;
                }
                return tws.getTimeWindows();
            }
        }


        public static class BuilderActivityInfo {
            private ActivityType type;
            private Location locs;
            private SizeDimension size = SizeDimension.EMPTY;
            private String name = null;
            private double operationTime = 0;
            private TimeWindowsImpl timeWindows = new TimeWindowsImpl();


            public BuilderActivityInfo(ActivityType type, Location locs) {
                super();
                this.type = type;
                this.locs = locs;
            }

            public ActivityType getType() {
                return type;
            }

            public Location getLocation() {
                return locs;
            }

            public SizeDimension getSize() {
                return size;
            }

            public BuilderActivityInfo withSize(SizeDimension size) {
                this.size = size;
                return this;
            }

            public String getName() {
                return name;
            }

            public BuilderActivityInfo withName(String name) {
                this.name = name;
                return this;
            }

            public TimeWindows getTimeWindows() {
                return timeWindows;
            }

            public BuilderActivityInfo withTimeWindow(TimeWindow timeWindow) {
                timeWindows.add(timeWindow);
                return this;
            }

            public BuilderActivityInfo withTimeWindows(TimeWindow... tws) {
                timeWindows.addAll(tws);
                return this;
            }

            public BuilderActivityInfo withTimeWindows(Collection<TimeWindow> tws) {
                timeWindows.addAll(tws);
                return this;
            }

            public double getOperationTime() {
                return operationTime;
            }

            public BuilderActivityInfo withOperationTime(double operationTime) {
                this.operationTime = operationTime;
                return this;
            }
        }

        List<BuilderActivityInfo> acts = new ArrayList<>();

        public BuilderBase(String id) {
            super(id);
        }

        @SuppressWarnings("unchecked")
        public B addActivity(BuilderActivityInfo act) {
            acts.add(act);
            return (B) this;
        }


        private void add(ActivityType type, Location location, double operationTime, SizeDimension size, String name,
                Collection<TimeWindow> tws) {
            BuilderActivityInfo builderActivityInfo = new BuilderActivityInfo(type, location);
            builderActivityInfo.withOperationTime(operationTime);
            if (name != null) {
                builderActivityInfo.withName(name);
            }
            if (size != null) {
                builderActivityInfo.withSize(size);
            }
            if (tws != null) {
                builderActivityInfo.withTimeWindows(tws);
            }

            acts.add(builderActivityInfo);
        }

        // Service

        @SuppressWarnings("unchecked")
        public B addService(Location location) {
            add(ActivityType.SERVICE, location, 0d, null, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addService(Location location, SizeDimension size) {
            add(ActivityType.SERVICE, location, 0d, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addService(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.SERVICE, location, operationTime, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addService(Location location, SizeDimension size, double operationTime,
                TimeWindow tw) {
            add(ActivityType.SERVICE, location, operationTime, size, null, Collections.singleton(tw));
            return (B) this;
        }


        // Pickup

        @SuppressWarnings("unchecked")
        public B addPickup(Location location) {
            add(ActivityType.PICKUP, location, 0d, null, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size) {
            add(ActivityType.PICKUP, location, 0d, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.PICKUP, location, operationTime, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size, double operationTime,
                TimeWindow tw) {
            add(ActivityType.PICKUP, location, operationTime, size, null, Collections.singleton(tw));
            return (B) this;
        }

        // Delivery

        @SuppressWarnings("unchecked")
        public B addDelivery(Location location) {
            add(ActivityType.DELIVERY, location, 0d, null, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size) {
            add(ActivityType.DELIVERY, location, 0d, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.DELIVERY, location, operationTime, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size, double operationTime,
                TimeWindow tw) {
            add(ActivityType.DELIVERY, location, operationTime, size, null, Collections.singleton(tw));
            return (B) this;
        }

        // Exchange

        @SuppressWarnings("unchecked")
        public B addExchange(Location location) {
            add(ActivityType.EXCHANGE, location, 0d, null, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addExchange(Location location, SizeDimension size) {
            add(ActivityType.EXCHANGE, location, 0d, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addExchange(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.EXCHANGE, location, operationTime, size, null, null);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addExchange(Location location, SizeDimension size, double operationTime,
                TimeWindow tw) {
            add(ActivityType.EXCHANGE, location, operationTime, size, null, Collections.singleton(tw));
            return (B) this;
        }

        @Override
        protected void validate() {
            if (acts.isEmpty())
                throw new IllegalStateException("There is no activities defined on this job.");
        }

        public List<BuilderActivityInfo> getActs() {
            return Collections.unmodifiableList(acts);
        }

    }

    /**
     * This is the builder of the {@linkplain CustomJob}.
     *
     * <p>
     * A CustomJob is a job with any number of activities of any type. These
     * activities will be executed by the same vehicle and on the same route.
     * They will keep they order and either all of them or none of them will be
     * included into the solution.
     * </p>
     * <p>
     * The builder contains methods for simply configuring basic activities. If
     * more control needed on the activity creation, an ActivityBuild
     * </p>
     *
     *
     * @author Balage
     *
     */
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
        for (CustomJob.Builder.BuilderActivityInfo info : builder.getActs()) {
            JobActivity act = info.getType().create(this, info);
            list.addActivity(act);
        }
        setActivities(list);
    }
}

