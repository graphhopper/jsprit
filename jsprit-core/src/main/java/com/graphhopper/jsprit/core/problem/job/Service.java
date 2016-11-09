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

import java.util.Collection;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupServiceDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindows;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

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
    public abstract static class ServiceBuilderBase<B extends ServiceBuilderBase<B>> {

        protected String id;

        protected String locationId;

        protected String type = "service";

        protected Coordinate coord;

        protected double serviceTime;

        protected TimeWindow timeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

        protected Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        protected Capacity capacity;

        protected Skills.Builder skillBuilder = Skills.Builder.newInstance();

        protected Skills skills;

        protected String name = "no-name";

        protected Location location;

        protected TimeWindowsImpl timeWindows;

        protected boolean twAdded = false;

        protected int priority = 2;

        public ServiceBuilderBase(String id) {
            this.id = id;
            timeWindows = new TimeWindowsImpl();
            timeWindows.add(timeWindow);
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
            if(tw == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            timeWindow = tw;
            timeWindows = new TimeWindowsImpl();
            timeWindows.add(tw);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addTimeWindow(TimeWindow timeWindow) {
            if(timeWindow == null) {
                throw new IllegalArgumentException("time-window arg must not be null");
            }
            if(!twAdded){
                timeWindows = new TimeWindowsImpl();
                twAdded = true;
            }
            timeWindows.add(timeWindow);
            return (B) this;
        }

        public B addTimeWindow(double earliest, double latest) {
            return addTimeWindow(TimeWindow.newInstance(earliest, latest));
        }


        @SuppressWarnings("unchecked")
        public B addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B setName(String name) {
            this.name = name;
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addAllRequiredSkills(Skills skills) {
            for(String s : skills.values()){
                skillBuilder.addSkill(s);
            }
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addAllSizeDimensions(Capacity size) {
            for(int i=0;i<size.getNuOfDimensions();i++){
                capacityBuilder.addDimension(i,size.get(i));
            }
            return (B) this;
        }

        /**
         * Set priority to service. Only 1 = high priority, 2 = medium and 3 = low are allowed.
         * <p>
         * Default is 2 = medium.
         *
         * @param priority
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public B setPriority(int priority) {
            if(priority < 1 || priority > 3) {
                throw new IllegalArgumentException("incorrect priority. only 1 = high, 2 = medium and 3 = low is allowed");
            }
            this.priority = priority;
            return (B) this;
        }

        /**
         * Builds the service.
         *
         * <p>
         * The implementation of the builder <b>may</b> call the function {@linkplain #preProcess()} prior creating the
         * instant and <b>MUST</b> call the {@linkplain #postProcess(Service)} method after the instance is constructed:
         *
         * <pre>
         *    &#64;Override
         *    public Service build() {
         *        [...]
         *        preProcess();
         *        Service service = new Service(this);
         *        postProcess(service);
         *        return service;
         *    }
         * </pre>
         *
         * </p>
         *
         * @return {@link Service}
         * @throws IllegalArgumentException
         *             if neither locationId nor coordinate is set.
         */
        public abstract <T extends Service> T build();

        protected <T extends Service> void preProcess() {
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
        }

        protected <T extends Service> void postProcess(T service) {
            // initiate caches
            service.addLocations();
            service.createActivities();
            service.addOperationTimeWindows();
        }
    }


    public static class Builder extends ServiceBuilderBase<Builder> {

        public Builder(String id) {
            super(id);
        }

        /**
         * Builds the service.
         *
         * @return {@link Service}
         * @throws IllegalArgumentException
         *             if neither locationId nor coordinate is set.
         */
        @SuppressWarnings("unchecked")
        @Override
        public Service build() {
            if (location == null) {
                throw new IllegalArgumentException("location is missing");
            }
            setType("service");
            preProcess();
            Service service = new Service(this);
            postProcess(service);
            return service;
        }

    }


    private String id;

    private String type;

    private double serviceTime;

    private TimeWindow timeWindow;

    private Capacity size;

    private Skills skills;

    private String name;

    private Location location;

    private TimeWindows timeWindowManager;

    private int priority;

    Service(ServiceBuilderBase<?> builder) {
        id = builder.id;
        serviceTime = builder.serviceTime;
        timeWindow = builder.timeWindow;
        type = builder.type;
        size = builder.capacity;
        skills = builder.skills;
        name = builder.name;
        location = builder.location;
        timeWindowManager = builder.timeWindows;
        priority = builder.priority;
    }


    @Override
    protected void createActivities() {
        JobActivityList list = new SequentialJobActivityList(this);
        // TODO - Balage1551
//        list.addActivity(new ServiceActivityNEW(this, "service", getLocation(), getServiceDuration(), getSize()));
        list.addActivity(new PickupServiceDEPRECATED(this));
        setActivities(list);
    }

    @Override
    protected void addOperationTimeWindows() {
        operationTimeWindows.add(getTimeWindow());
    }


    @Override
    protected void addLocations() {
        addLocation(location);
    }

    public Collection<TimeWindow> getTimeWindows(){
        return timeWindowManager.getTimeWindows();
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns location.
     *
     * @return location
     */
    public Location getLocation() {
        return location;
    }


    /**
     * Returns the service-time/duration a service takes at service-location.
     *
     * @return service duration
     */
    public double getServiceDuration() {
        return serviceTime;
    }

    /**
     * Returns the time-window a service(-operation) is allowed to start.
     * It is recommended to use getTimeWindows() instead. If you still use this, it returns the first time window of getTimeWindows() collection.
     *
     * @return time window
     *
     */
    public TimeWindow getTimeWindow() {
        return timeWindowManager.getTimeWindows().iterator().next();
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
        return "[id=" + id + "][name=" + name + "][type=" + type + "][location=" + location + "][capacity=" + size + "][serviceTime=" + serviceTime + "][timeWindow=" + timeWindow + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Two services are equal if they have the same id.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Service other = (Service) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public Capacity getSize() {
        return size;
    }

    @Override
    public Skills getRequiredSkills() {
        return skills;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Get priority of service. Only 1 = high priority, 2 = medium and 3 = low are allowed.
     * <p>
     * Default is 2 = medium.
     *
     * @return priority
     */
    @Override
    public int getPriority() {
        return priority;
    }




}
