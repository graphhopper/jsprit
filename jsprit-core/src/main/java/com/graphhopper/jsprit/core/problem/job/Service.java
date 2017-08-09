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
import java.util.List;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.job.CustomJob.BuilderBase.ActivityType;
import com.graphhopper.jsprit.core.problem.job.CustomJob.BuilderBase.BuilderActivityInfo;
import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindowsImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

/**
 * Service implementation of a job.
 *
 * @deprecated Use {@linkplain CustomJob} instead
 *
 *
 *             <p>
 *             <h1><em>Warning!</em></h1>
 *             </p>
 *
 *             <p>
 *             <strong>This class is deprecated and only available for backward
 *             compatibility and for easier migration.</strong>
 *             </p>
 *             <p>
 *             This class wraps a new CustomJob instance and delegates its
 *             values and the values from its sole activity. It is strongly
 *             recommended to switch to the {@linkplain CustomJob} and use one
 *             of the following functions of its builder to add the service
 *             activity:
 *
 *             <ul>
 *             <li>{@linkplain CustomJob.Builder#addService(Location)}</li>
 *             <li>{@linkplain CustomJob.Builder#addService(Location, double)}
 *             </li>
 *             <li>
 *             {@linkplain CustomJob.Builder#addService(Location, double, TimeWindow)}
 *             </li>
 *             </ul>
 *
 *             or if you need more control on the activity, use the
 *             {@linkplain CustomJob.Builder#addActivity(BuilderActivityInfo)}
 *             function:
 *
 *             <pre>
 *    BuilderActivityInfo activityInfo = new BuilderActivityInfo(ActivityType.SERVICE, <i>location</i>);

        activityInfo.withName(<i>activity name</i>);
        activityInfo.withOperationTime(<i>serviceTime</i>);
        activityInfo.withSize((SizeDimension) <i>capacity</i>);
        activityInfo.withTimeWindows(<i>timeWindows</i>);
        activityInfo.withTimeWindow(<i>timeWindow</i>);

        CustomJob.Builder customJobBuilder = new CustomJob.Builder(<i>id</i>);
        customJobBuilder
            .addActivity(activityInfo)
            .addAllRequiredSkills(<i>skills<i>)
            .setName(<i>job name</i>)
            .setPriority(<i>priority</i>);

        job = customJobBuilder.build();
 *             </pre>
 *
 *             </p>
 *
 * @author schroeder
 * @author Balage
 *
 * @see {@linkplain CustomJob}
 * @see {@linkplain CustomJob.Builder}
 * @see {@linkplain CustomJob.BuilderBase.BuilderActivityInfo}
 */
@Deprecated
public class Service extends AbstractJob {

    /**
     * Builder that builds a service.
     *
     * @deprecated Use {@linkplain CustomJob.Builder} instead
     *
     * @author schroeder
     */
    @Deprecated
    public static class Builder<T extends Service> {

        /**
         * Returns a new instance of builder that builds a service.
         *
         * @param id the id of the service
         * @return the builder
         */
        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        private String id;

        protected String locationId;

        private String type = "service";

        protected Coordinate coord;

        protected double serviceTime;

        protected TimeWindow timeWindow = TimeWindow.newInstance(0.0, Double.MAX_VALUE);

        protected Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        protected Capacity capacity;

        protected Skills.Builder skillBuilder = Skills.Builder.newInstance();

        protected Skills skills;

        private String name = "no-name";

        protected Location location;

        protected TimeWindowsImpl timeWindows;

        private boolean twAdded = false;

        private int priority = 2;

        protected Object userData;

        Builder(String id){
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
        protected Builder<T> setType(String name) {
            this.type = name;
            return this;
        }

        /**
         * Sets location
         *
         * @param location location
         * @return builder
         */
        public Builder<T> setLocation(Location location) {
            this.location = location;
            return this;
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
        public Builder<T> setServiceTime(double serviceTime) {
            if (serviceTime < 0)
                throw new IllegalArgumentException("serviceTime must be greater than or equal to zero");
            this.serviceTime = serviceTime;
            return this;
        }

        /**
         * Sets user specific domain data associated with the object.
         *
         * <p>
         * The user data is a black box for the framework, it only stores it,
         * but never interacts with it in any way.
         * </p>
         *
         * @param userData
         *            any object holding the domain specific user data
         *            associated with the object.
         * @return builder
         */
        public Builder<T> setUserData(Object userData) {
            this.userData = userData;
            return this;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex the dimension index of the capacity value
         * @param dimensionValue the capacity value
         * @return the builder
         * @throws IllegalArgumentException if dimensionValue < 0
         */
        public Builder<T> addSizeDimension(int dimensionIndex, int dimensionValue) {
            if (dimensionValue < 0) throw new IllegalArgumentException("capacity value cannot be negative");
            capacityBuilder.addDimension(dimensionIndex, dimensionValue);
            return this;
        }

        public Builder<T> setTimeWindow(TimeWindow tw){
            if(tw == null) throw new IllegalArgumentException("time-window arg must not be null");
            this.timeWindow = tw;
            this.timeWindows = new TimeWindowsImpl();
            timeWindows.add(tw);
            return this;
        }

        public Builder<T> addTimeWindow(TimeWindow timeWindow) {
            if(timeWindow == null) throw new IllegalArgumentException("time-window arg must not be null");
            if(!twAdded){
                timeWindows = new TimeWindowsImpl();
                twAdded = true;
            }
            timeWindows.add(timeWindow);
            return this;
        }

        public Builder<T> addTimeWindow(double earliest, double latest) {
            return addTimeWindow(TimeWindow.newInstance(earliest, latest));
        }

        /**
         * Builds the service.
         *
         * @return {@link Service}
         * @throws IllegalArgumentException if neither locationId nor coordinate is set.
         */
        public T build() {
            if (location == null) throw new IllegalArgumentException("location is missing");
            this.setType("service");
            capacity = capacityBuilder.build();
            skills = skillBuilder.build();
            return (T) new Service(this);
        }

        public Builder<T> addRequiredSkill(String skill) {
            skillBuilder.addSkill(skill);
            return this;
        }

        public Builder<T> setName(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> addAllRequiredSkills(Skills skills){
            for(String s : skills.values()){
                skillBuilder.addSkill(s);
            }
            return this;
        }

        public Builder<T> addAllSizeDimensions(Capacity size){
            for(int i=0;i<size.getNuOfDimensions();i++){
                capacityBuilder.addDimension(i,size.get(i));
            }
            return this;
        }

        /**
         * Set priority to service. Only 1 (very high) to 10 (very low) are allowed.
         * <p>
         * Default is 2.
         *
         * @param priority
         * @return builder
         */
        public Builder<T> setPriority(int priority) {
            if (priority < 1 || priority > 10)
                throw new IllegalArgumentException("incorrect priority. only priority values from 1 to 10 are allowed where 1 = high and 10 is low");
            this.priority = priority;
            return this;
        }
    }


    private CustomJob theRealJob;
    private ServiceActivity theRealActivity;

    Service(Builder<?> builder) {
        BuilderActivityInfo activityInfo = new BuilderActivityInfo(ActivityType.SERVICE,
                builder.location);

        activityInfo.withName(builder.name);
        activityInfo.withOperationTime(builder.serviceTime);
        // Safe cast because SizeDimension is the only implementation of
        // Capacity
        activityInfo.withSize((SizeDimension) builder.capacity);
        activityInfo.withTimeWindows(builder.timeWindows.getTimeWindows());

        CustomJob.Builder customJobBuilder = new CustomJob.Builder(builder.id);
        customJobBuilder.addActivity(activityInfo).addAllRequiredSkills(builder.skills)
        .setName(builder.name)
        .addUserData(builder.userData)
        .setPriority(builder.priority);
        theRealJob = customJobBuilder.build();
        theRealActivity = (ServiceActivity) theRealJob.getActivityList().getAll().get(0);
    }

    @Override
    public Collection<TimeWindow> getTimeWindows() {
        return theRealJob.getTimeWindows();
    }

    @Override
    public String getId() {
        return theRealJob.getId();
    }

    /**
     * Returns location.
     *
     * @return location
     */
    public Location getLocation() {
        return theRealActivity.getLocation();
    }


    /**
     * Returns the service-time/duration a service takes at service-location.
     *
     * @return service duration
     */
    public double getServiceDuration() {
        return theRealActivity.getOperationTime();
    }

    /**
     * Returns the time-window a service(-operation) is allowed to start.
     *
     * @deprecated It is recommended to use getTimeWindows() instead. If you
     *             still use this, it returns the first time window of
     *             getTimeWindows() collection.
     *
     * @return time window
     *
     */
    @Deprecated
    public TimeWindow getTimeWindow() {
        return theRealActivity.getTimeWindows().iterator().next();
    }

    /**
     * @return the name
     */
    public String getType() {
        return "service";
    }

    /**
     * Returns a string with the service's attributes.
     * <p>
     * <p>String is built as follows: [attr1=val1][attr2=val2]...
     */
    @Override
    public String toString() {
        return "[id=" + getId() + "][name=" + getName() + "][type=" + getType() + "][location="
                + getLocation() + "][capacity=" + getSize() + "][serviceTime="
                + getServiceDuration() + "][timeWindow=" + getTimeWindow() + "]";
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        return result;
    }

    /**
     * Two services are equal if they have the same id.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Service other = (Service) obj;
        if (getId() == null) {
            if (other.getId() != null)
                return false;
        } else if (!getId().equals(other.getId()))
            return false;
        return true;
    }

    @Override
    public SizeDimension getSize() {
        return theRealActivity.getLoadSize();
    }

    @Override
    public Skills getRequiredSkills() {
        return theRealJob.getRequiredSkills();
    }

    @Override
    public String getName() {
        return theRealJob.getName();
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
        return theRealJob.getPriority();
    }

    @Override
    public Object getUserData() {
        return theRealJob.getUserData();
    }

    @Override
    protected void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder) {
        // This is unused being a legacy implementation
    }

    @Override
    public int getIndex() {
        return theRealJob.getIndex();
    }

    @Override
    public void impl_setIndex(int index) {
        theRealJob.impl_setIndex(index);
    }

    @Override
    public List<Location> getAllLocations() {
        return theRealJob.getAllLocations();
    }

    @Override
    public SizeDimension getSizeAtStart() {
        return theRealJob.getSizeAtStart();
    }

    @Override
    public SizeDimension getSizeAtEnd() {
        return theRealJob.getSizeAtEnd();
    }

    @Override
    public JobActivityList getActivityList() {
        return theRealJob.getActivityList();
    }

    public CustomJob getTheRealJob() {
        return theRealJob;
    }

    public ServiceActivity getTheRealActivity() {
        return theRealActivity;
    }

}
