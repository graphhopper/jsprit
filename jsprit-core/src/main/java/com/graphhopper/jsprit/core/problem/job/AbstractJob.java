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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractJob implements Job {

    /**
     * Builder that builds a service.
     *
     * @author schroeder
     */
    public abstract static class JobBuilder<T extends AbstractJob, B extends JobBuilder<T, B>> {

        protected Capacity.Builder capacityBuilder = Capacity.Builder.newInstance();

        protected Skills.Builder skillBuilder = Skills.Builder.newInstance();

        protected String id;

        protected String name = "no-name";

        protected int priority = 2;

        public JobBuilder(String id) {
            if (id == null) {
                throw new IllegalArgumentException("id must not be null");
            }
            this.id = id;
        }

        /**
         * Adds capacity dimension.
         *
         * @param dimensionIndex
         *            the dimension index of the capacity value
         * @param dimensionValue
         *            the capacity value
         * @return the builder
         * @throws IllegalArgumentException
         *             if dimensionValue < 0
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
            for (String s : skills.values()) {
                skillBuilder.addSkill(s);
            }
            return (B) this;
        }

        @SuppressWarnings("unchecked")
        public B addAllSizeDimensions(Capacity size) {
            for (int i = 0; i < size.getNuOfDimensions(); i++) {
                capacityBuilder.addDimension(i, size.get(i));
            }
            return (B) this;
        }

        /**
         * Set priority to service. Only 1 = high priority, 2 = medium and 3 =
         * low are allowed.
         * <p>
         * Default is 2 = medium.
         *
         * @param priority
         * @return builder
         */
        @SuppressWarnings("unchecked")
        public B setPriority(int priority) {
            if (priority < 1 || priority > 3) {
                throw new IllegalArgumentException(
                                "incorrect priority. only 1 = high, 2 = medium and 3 = low is allowed");
            }
            this.priority = priority;
            return (B) this;
        }

        /**
         * Builds the service.
         *
         * <p>
         * The implementation of the builder <b>MUST</b> call the
         * {@linkplain #postProcess(Service)} method after the instance is
         * constructed:
         *
         * <pre>
         *    &#64;Override
         *    public Service build() {
         *        [...]
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
        public T build() {
            validate();
            T job = createInstance();
            job.createActivities(this);
            return job;
        }

        protected abstract void validate();

        protected abstract T createInstance();

        public Capacity getCapacity() {
            return capacityBuilder.build();
        }

        public Skills getSkills() {
            return skillBuilder.build();
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

    }

    private int index;

    private String id;

    private Skills skills;

    private String name;

    private int priority;

    protected List<Location> allLocations;

    private JobActivityList activityList;

    protected Set<TimeWindow> allTimeWindows;

    public AbstractJob(JobBuilder<?, ?> builder) {
        super();
        activityList = new SequentialJobActivityList(this);
        id = builder.getId();
        skills = builder.getSkills();
        name = builder.getName();
        priority = builder.getPriority();
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }


    private void addLocation(Location location) {
        if (location != null) {
            allLocations.add(location);
        }
    }

    @Override
    public List<Location> getAllLocations() {
        return allLocations;
    }

    protected void prepareCaches() {
        allLocations = new ArrayList<>();
        allTimeWindows = new HashSet<>();
        activityList.getAll().stream().forEach(ja -> {
            addLocation(ja.getLocation());
            addTimeWindows(ja.getTimeWindows());
        });
    }

    private void addTimeWindows(Collection<TimeWindow> timeWindows) {
        if (timeWindows != null && !timeWindows.isEmpty()) {
            allTimeWindows.addAll(timeWindows);
        }
    }

    /**
     * Creates the activities.
     *
     * <p>
     * This functions contract specifies that the implementation has to call
     * {@linkplain #prepareCaches()} function at the end, after all activities
     * are added.
     * </p>
     */
    // protected abstract void createActivities();
    protected abstract void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Two shipments are equal if they have the same id.
     *
     * @return true if shipments are equal (have the same id)
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
        AbstractJob other = (AbstractJob) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    protected void setActivities(JobActivityList list) {
        activityList = list;
        prepareCaches();
    }

    @Override
    public JobActivityList getActivityList() {
        return activityList;
    }


    @Override
    public Set<TimeWindow> getTimeWindows() {
        return allTimeWindows;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public Skills getRequiredSkills() {
        return skills;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPriority() {
        return priority;
    }


}

