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

    /**
     * Protected base builder class for {@linkplain CustomJob}.
     * <p>
     * The class is the protected part of the inheritable builder pattern. For
     * more information, see {@linkplain AbstractJob.JobBuilder}.
     * </p>
     *
     * @author Balage
     *
     * @param <T>
     *            The type of the job it creates.
     * @param <B>
     *            Self-refering generic value.
     */
    protected static abstract class BuilderBase<T extends CustomJob, B extends CustomJob.BuilderBase<T, B>>
    extends JobBuilder<T, B> {

        /**
         * The possible activity types.
         *
         * <p>
         * Note, that the set of activity types are final.
         * </p>
         *
         * @author Balage
         *
         */
        public enum ActivityType {
            /**
             * Service activity type.
             * <p>
             * The service activity type represents an activity with no cargo
             * change (nothing is loaded or unloaded).
             * </p>
             */
            SERVICE {

                @Override
                protected JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new ServiceActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            /**
             * Pickup activity type.
             * <p>
             * The pickup activity type represents an activity where something
             * is picked up (loaded). It has a positive impact on the cargo
             * size.
             * </p>
             */
            PICKUP {

                @Override
                protected JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new PickupActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            /**
             * Delivery activity type.
             * <p>
             * The delivery activity type represents an activity where something
             * is delivered (unloaded). It has a negative impact on the cargo
             * size.
             * </p>
             */
            DELIVERY {

                @Override
                protected JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new DeliveryActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            },
            /**
             * Exchange activity type.
             * <p>
             * The exchange activity type represents an activity where something
             * is delivered and something else is picked up at the same time.
             * (loaded and unloaded). It has a mixed (may be even zero) impact
             * on the cargo size. It may increase one dimension and reduce
             * another one.
             * </p>
             */
            EXCHANGE {

                @Override
                protected JobActivity create(CustomJob job, BuilderActivityInfo info) {
                    return new ExchangeActivity(job, info.getName() == null ? name().toLowerCase() : info.getName(),
                            info.getLocation(), info.getOperationTime(), info.getSize(), prepareTimeWindows(info));
                }
            };

            protected abstract JobActivity create(CustomJob job, BuilderActivityInfo builderActivityInfo);

            private static Collection<TimeWindow> prepareTimeWindows(BuilderActivityInfo info) {
                TimeWindows tws = info.getTimeWindows();
                if (tws.getTimeWindows().isEmpty()) {
                    tws = TimeWindows.ANY_TIME;
                }
                return tws.getTimeWindows();
            }
        }


        /**
         * Class for defining custom activities when the standard methods of
         * {@linkplain Builder} are not enough. The class applies the fluent API
         * pattern.
         * <p>
         * Note that this class is <b>NOT</b> immutable, so always create a new
         * instance for each activity!
         * </p>
         *
         * @author Balage
         *
         */
        public static class BuilderActivityInfo {
            private ActivityType type;
            private Location locs;
            private SizeDimension size = SizeDimension.EMPTY;
            private String name = null;
            private double operationTime = 0;
            private TimeWindowsImpl timeWindows = new TimeWindowsImpl();


            /**
             * Constructs a new instance.
             *
             * @param type
             *            The type of the activity.
             * @param locs
             *            The location of the activity.
             */
            public BuilderActivityInfo(ActivityType type, Location locs) {
                super();
                this.type = type;
                this.locs = locs;
            }

            /**
             * @return The type of the activity.
             */
            public ActivityType getType() {
                return type;
            }

            /**
             * @return The location of the activity.
             */
            public Location getLocation() {
                return locs;
            }

            /**
             * @return The size dimensions (cargo change) of the activity.
             */
            public SizeDimension getSize() {
                return size;
            }

            /**
             * Sets the size dimensions (cargo change) of the activity.
             *
             * @param size
             *            The size dimensions. (May be negative.)
             * @return The info object.
             */
            public BuilderActivityInfo withSize(SizeDimension size) {
                this.size = size;
                return this;
            }

            /**
             * @return The name of the activity (for debug and reporting).
             */
            public String getName() {
                return name;
            }

            /**
             * Sets the name of the activity for debugging and reporting
             * purpose.
             *
             * @param name
             *            The name.
             * @return The info object.
             */
            public BuilderActivityInfo withName(String name) {
                this.name = name;
                return this;
            }

            /**
             * @return The time windows of the activity.
             */
            public TimeWindows getTimeWindows() {
                return timeWindows;
            }

            /**
             * Adds a time window to the activity.
             *
             * @param timeWindow
             *            A time window.
             * @return The info object.
             */
            public BuilderActivityInfo withTimeWindow(TimeWindow timeWindow) {
                timeWindows.add(timeWindow);
                return this;
            }

            /**
             * Adds several time windows to the activity.
             *
             * @param timeWindows
             *            The list of time windows.
             * @return The info object.
             */
            public BuilderActivityInfo withTimeWindows(TimeWindow... timeWindows) {
                this.timeWindows.addAll(timeWindows);
                return this;
            }

            /**
             * Adds several time windows.
             *
             * @param tws
             *            The collection of time windows.
             * @return The info object.
             */
            public BuilderActivityInfo withTimeWindows(Collection<TimeWindow> tws) {
                timeWindows.addAll(tws);
                return this;
            }

            /**
             * @return The operation time (time taken to fulfill the activity at
             *         the location) of the activity.
             */
            public double getOperationTime() {
                return operationTime;
            }

            /**
             * Sets the operation time (time taken to fulfill the activity at
             * the location).
             *
             * @param operationTime
             *            The operation time.
             * @return The info object.
             */
            public BuilderActivityInfo withOperationTime(double operationTime) {
                this.operationTime = operationTime;
                return this;
            }
        }

        private List<BuilderActivityInfo> acts = new ArrayList<>();

        /**
         * Constructor.
         *
         * @param id
         *            The id of the job. Should be unique within the problem.
         */
        public BuilderBase(String id) {
            super(id);
        }

        @SuppressWarnings("unchecked")
        public B addActivity(BuilderActivityInfo act) {
            acts.add(act);
            return (B) this;
        }


        /**
         * General activity add method.
         * <p>
         * It constructs a {@linkplain BuilderActivityInfo} objects and calls
         * the {@linkplain #addActivity(BuilderActivityInfo)} function.
         * </p>
         *
         * @param type
         *            The type of the activity.
         * @param location
         *            The location of the activity.
         * @param operationTime
         *            The operation time of the activity.
         * @param size
         *            The cargo change of the activity. May be null.
         * @param name
         *            The name of the activity. May be null.
         * @param timeWindows
         *            The time windows of the activity. May be null.
         */
        private void add(ActivityType type, Location location, double operationTime, SizeDimension size, String name,
                Collection<TimeWindow> timeWindows) {
            BuilderActivityInfo builderActivityInfo = new BuilderActivityInfo(type, location);
            builderActivityInfo.withOperationTime(operationTime);
            if (name != null) {
                builderActivityInfo.withName(name);
            }
            if (size != null) {
                builderActivityInfo.withSize(size);
            }
            if (timeWindows != null) {
                builderActivityInfo.withTimeWindows(timeWindows);
            }

            acts.add(builderActivityInfo);
        }

        // Service

        /**
         * Adds a {@linkplain ActivityType#SERVICE} activity to the job with 0
         * operation time, without time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addService(Location location) {
            add(ActivityType.SERVICE, location, 0d, null, null, null);
            return (B) this;
        }


        /**
         * Adds a {@linkplain ActivityType#SERVICE} activity to the job without
         * time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param operationTime
         *            The operation time of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addService(Location location, double operationTime) {
            add(ActivityType.SERVICE, location, operationTime, null, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#SERVICE} activity to the job without
         * name and with a single time window.
         *
         * @param location
         *            The location of the activity.
         * @param operationTime
         *            The operation time of the activity.
         * @param timeWindow
         *            The time window of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addService(Location location, double operationTime, TimeWindow timeWindow) {
            add(ActivityType.SERVICE, location, operationTime, null, null, Collections.singleton(timeWindow));
            return (B) this;
        }


        // Pickup

        /**
         * Adds a {@linkplain ActivityType#PICKUP} activity to the job with 0
         * operation time, without cargo change, time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addPickup(Location location) {
            add(ActivityType.PICKUP, location, 0d, null, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#PICKUP} activity to the job with 0
         * operation time, without time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the pickup. Should be positive.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size) {
            add(ActivityType.PICKUP, location, 0d, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#PICKUP} activity to the job without
         * time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the pickup. Should be positive.
         * @param operationTime
         *            The operation time of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.PICKUP, location, operationTime, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#PICKUP} activity to the job without
         * name and with a single time window.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the pickup. Should be positive.
         * @param operationTime
         *            The operation time of the activity.
         * @param timeWindow
         *            The time window of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addPickup(Location location, SizeDimension size, double operationTime,
                TimeWindow timeWindow) {
            add(ActivityType.PICKUP, location, operationTime, size, null, Collections.singleton(timeWindow));
            return (B) this;
        }

        // Delivery

        /**
         * Adds a {@linkplain ActivityType#DELIVERY} activity to the job with 0
         * operation time, without cargo change, time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addDelivery(Location location) {
            add(ActivityType.DELIVERY, location, 0d, null, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#DELIVERY} activity to the job with 0
         * operation time, without time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the delivery. Should be negative.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size) {
            add(ActivityType.DELIVERY, location, 0d, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#DELIVERY} activity to the job without
         * time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the delivery. Should be negative.
         * @param operationTime
         *            The operation time of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.DELIVERY, location, operationTime, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#DELIVERY} activity to the job without
         * name and with a single time window.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the delivery. Should be negative.
         * @param operationTime
         *            The operation time of the activity.
         * @param timeWindow
         *            The time window of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addDelivery(Location location, SizeDimension size, double operationTime,
                TimeWindow tw) {
            add(ActivityType.DELIVERY, location, operationTime, size, null, Collections.singleton(tw));
            return (B) this;
        }

        // Exchange

        /**
         * Adds a {@linkplain ActivityType#EXCHANGE} activity to the job with 0
         * operation time, without cargo change, time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addExchange(Location location) {
            add(ActivityType.EXCHANGE, location, 0d, null, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#EXCHANGE} activity to the job with 0
         * operation time, without time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the exchange. May be negative,
         *            positive or mixed.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addExchange(Location location, SizeDimension size) {
            add(ActivityType.EXCHANGE, location, 0d, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#EXCHANGE} activity to the job without
         * time windows and name.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the exchange. May be negative,
         *            positive or mixed.
         * @param operationTime
         *            The operation time of the activity.
         * @return The builder instance.
         */
        @SuppressWarnings("unchecked")
        public B addExchange(Location location, SizeDimension size, double operationTime) {
            add(ActivityType.EXCHANGE, location, operationTime, size, null, null);
            return (B) this;
        }

        /**
         * Adds a {@linkplain ActivityType#EXCHANGE} activity to the job without
         * name and with a single time window.
         *
         * @param location
         *            The location of the activity.
         * @param size
         *            The cargo change of the exchange. May be negative,
         *            positive or mixed.
         * @param operationTime
         *            The operation time of the activity.
         * @param timeWindow
         *            The time window of the activity.
         * @return The builder instance.
         */
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

        public List<BuilderActivityInfo> getActivities() {
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
     * The main difference between the jobs and activities known from version 1
     * and 2 is the bias shift from job to activity. Before version 2 the jobs
     * has holden most of the business information and activities were
     * second-class entities, meanwhile the algorithm worked on activities. This
     * has driven to a state where the code had no indication which job field
     * belonged to which activities.
     * </p>
     * <p>
     * In the new concept a stronger encapsulation ensures the right behavior.
     * This led to most of the business data to move from job to activity. These
     * are:
     * <ul>
     * <li>Load change (how the cargo size change (increase or decrease) on the
     * vehicle)</li>
     * <li>Location (where the activity should be executed)</li>
     * <li>Time windows (when the activity should be performed)</li>
     * <li>Operation time (how much time it takes to fulfill the activity)</li>
     * </ul>
     * These parameters are now defined per activity.
     * </p>
     * <p>
     * Some information has left in the scope of the job, because they affects
     * the whole job:
     * <ul>
     * <li>Required skills</li>
     * <li>Priority</li>
     * </ul>
     * </p>
     * <p>
     * The builder contains methods for simply configuring basic activities.
     * They are the counterparts of the version 1 job builders. If more control
     * is needed on the activity creation, an {@linkplain BuilderActivityInfo}
     * record has to be created and passed to the builder. (<i>This indirection
     * is required to keep immutable behavior of a job and its activities after
     * creation.</i>)
     * </p>
     *
     * @author Balage
     *
     */
    public static final class Builder extends CustomJob.BuilderBase<CustomJob, CustomJob.Builder> {
        /**
         * Constructor.
         *
         * @param id
         *            The id of the job. Should be unique within a problem.
         */
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
        for (CustomJob.Builder.BuilderActivityInfo info : builder.getActivities()) {
            JobActivity act = info.getType().create(this, info);
            list.addActivity(act);
        }
        setActivities(list);
    }
}

