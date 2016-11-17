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

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupServiceDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;

import java.util.Collection;

/**
 * Service implementation of a job.
 * <p>
 * <p>
 * <p>Note that two services are equal if they have the same id.
 *
 * @author schroeder
 */
public class Service extends AbstractJob {

    /**
     * Builder that builds a service.
     *
     * @author schroeder
     */
    public static abstract class BuilderBase<T extends Service, B extends BuilderBase<T, B>>
        extends JobBuilder<T, B> {

        protected String type = "service";

        protected double serviceTime;

        protected Location location;

        protected TimeWindowsImpl timeWindows;

        public BuilderBase(String id) {
            super(id);
            this.id = id;
            timeWindows = new TimeWindowsImpl();
        }

        /**
         * Protected method to set the type-name of the service.
         * <p>
         * <p>Currently there are {@link Service}, {@link Pickup} and {@link Delivery}.
         *
         * @param name the name of service
         * @return the builder
         */
        @SuppressWarnings("unchecked")
        protected B setType(String name) {
            type = name;
            return (B) this;
        }

        /**
         * Sets location
         *
         * @param location location
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public B setLocation(Location location) {
            this.location = location;
            return (B) this;
        }

        /**
         * Sets the serviceTime of this service.
         * <p>
         * <p>It is understood as time that a service or its implied activity takes at the service-location, for instance
         * to unload goods.
         *
         * @param serviceTime the service time / duration of service to be set
         * @return builder
         * @throws IllegalArgumentException if serviceTime < 0
         */
        @SuppressWarnings("unchecked")
        public B setServiceTime(double serviceTime) {
            if (serviceTime < 0) {
                throw new IllegalArgumentException("serviceTime must be greater than or equal to zero");
            }
            this.serviceTime = serviceTime;
            return (B) this;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex the dimension index of the capacity value
         * @param dimensionValue the capacity value
         * @return the builder
         * @throws IllegalArgumentException if dimensionValue < 0
         */
        @Override
        @SuppressWarnings("unchecked")
        public B addSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0) {
                throw new IllegalArgumentException("capacity value cannot be negative");
            }
            capacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setTimeWindow(TimeWindow tw) {
            if (tw == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            timeWindows = new TimeWindowsImpl();
            timeWindows.add(tw);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addTimeWindow(TimeWindow timeWindow) {
            if (timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            timeWindows.add(timeWindow);
            return (B) this;
        }

        public B addTimeWindow(double earliest, double latest) {
            return addTimeWindow(TimeWindow.newInstance(earliest, latest));
        }

        /**
         * Builds the service.
         * <p>
         * <p>
         * The implementation of the builder <b>may</b> call the function {@linkplain #preProcess()} prior creating the
         * instant and <b>MUST</b> call the {@linkplain #postProcess(Service)} method after the instance is constructed:
         * <p>
         * <pre>
         *    &#64;Override
         *    public Service build() {
         *        [...]
         *        Service service = new Service(this);
         *        postProcess(service);
         *        return service;
         *    }
         * </pre>
         * <p>
         * </p>
         *
         * @return {@link Service}
         * @throws IllegalArgumentException if neither locationId nor coordinate is set.
         */

        @Override
        protected void validate() {
            if (location == null) {
                throw new IllegalArgumentException("location is missing");
            }
            if (timeWindows.isEmpty()) {
                timeWindows.add(TimeWindow.ETERNITY);
            }
        }

        public String getType() {
            return type;
        }

        public double getServiceTime() {
            return serviceTime;
        }

        public Location getLocation() {
            return location;
        }

        public TimeWindowsImpl getTimeWindows() {
            return timeWindows;
        }
    }

    public static class Builder extends BuilderBase<Service, Builder> {

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        public Builder(String id) {
            super(id);
        }

        @Override
        protected Service createInstance() {
            return new Service(this);
        }
    }


    private String type;

    Service(BuilderBase<? extends Service, ?> builder) {
        super(builder);
        type = builder.type;
    }


    @Override
    protected void createActivities(JobBuilder<?, ?> builder) {
        Builder serviceBuilder = (Builder) builder;
        JobActivityList list = new SequentialJobActivityList(this);
        // TODO - Balage1551
        //        list.addActivity(new ServiceActivityNEW(this, "service", getLocation(), getServiceDuration(), getSize()));
        list.addActivity(new PickupServiceDEPRECATED(this, serviceBuilder));
        setActivities(list);
    }

    public JobActivity getServiceActivity() {
        return getActivityList().getAll().get(0);
    }

    /**
     * Returns location.
     *
     * @return location
     */
    @Deprecated
    public Location getLocation() {
        return getServiceActivity().getLocation();
    }


    /**
     * Returns the service-time/duration a service takes at service-location.
     *
     * @return service duration
     */
    @Deprecated
    public double getServiceDuration() {
        return getServiceActivity().getOperationTime();
    }

    /**
     * Returns the time-window a service(-operation) is allowed to start.
     * It is recommended to use getTimeWindows() instead. If you still use this, it returns the first time window of getTimeWindows() collection.
     *
     * @return time window
     */
    @Deprecated
    public TimeWindow getTimeWindow() {
        return getServiceTimeWindows().iterator().next();
    }

    @Deprecated
    public Collection<TimeWindow> getServiceTimeWindows() {
        return getServiceActivity().getTimeWindows();
    }

    /**
     * @return the name
     */
    public String getType() {
        return type;
    }

    /**
     * Returns a string with the service's attributes.
     * <p>
     * <p>String is built as follows: [attr1=val1][attr2=val2]...
     */
    @Override
    public String toString() {
        return "[id=" + getId() + "][name=" + getName() + "][type=" + type + "][location="
            + getLocation() + "][capacity=" + getSize() + "][serviceTime="
            + getServiceDuration() + "][timeWindow=" + getTimeWindows() + "]";
    }


    @Override
    @Deprecated
    public SizeDimension getSize() {
        return getServiceActivity().getLoadChange();
    }


}
