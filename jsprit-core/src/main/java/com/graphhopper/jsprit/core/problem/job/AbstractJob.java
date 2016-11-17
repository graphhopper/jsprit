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
import com.graphhopper.jsprit.core.problem.Skills;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

import java.util.*;

/**
 * Abstract base class for all Job implementations.
 * <p>
 * See {@linkplain JobBuilder} for detailed instruction how to implement your
 * Job.
 * </p>
 * <p>
 * Created by schroeder on 14.07.14.
 * </p>
 *
 * @author schroeder
 * @author balage
 * @see JobBuilder
 */
public abstract class AbstractJob implements Job {


    /**
     * Base builder for all direct descendants.
     * <p>
     * The is an abstract implementation of the builder pattern providing the
     * base functionality for inheritence. When you create a new AbstractJob
     * implementation and would like to provide builder for it follow the
     * guidlines below:
     * </p>
     * <p>
     * First of all, you have to decide whether you would like to create a final
     * class (no further inheritence from it) or not. If you decide to make your
     * implementation <code>final</code> you can make your concrete builder in
     * one step, but make the Job class final to emphasize this fact.
     * </p>
     * <p>
     * If you wish to allow your Job implementation to be extended, first create
     * your own abstract Builder class. The signature of your abstract builder
     * should be something like this (<i>self referencing generics</i>):
     * <p>
     * <pre>
     * public static abstract class BuilderBase&lt;T extends MyJob, B extends BuilderBase&lt;T, B>>
     *                 extends JobBuilder&lt;T, B> {
     * }
     * </pre>
     * <p>
     * This implenetation should contain all new fields, the new setters
     * following the pattern:
     * <p>
     * <pre>
     * &#64;SuppressWarnings("unchecked")
     * public B setField(FieldType field) {
     *     this.field = field;
     *     return (B) this;
     * }
     * </pre>
     * <p>
     * Usually, the {@linkplain #validate()} method is implemented in this class
     * (and it should call <code>super.validate()</code>) as well, but the
     * abstract {@linkplain #createInstance()} is never. It is recommended that
     * getters are provided for the fields as well.
     * </p>
     * <p>
     * This BuilderBase class is for the new descendents to base their Builder
     * on. If you don't need to refere to this class outside the descedents,
     * make it protected.
     * </p>
     * <p>
     * Now you can create the "real" builder class, which is simple, hides the
     * complex generic pattern and makes it safe (see <a href=
     * "http://stackoverflow.com/questions/7354740/is-there-a-way-to-refer-to-the-current-type-with-a-type-variable">
     * the answer of this topic</a> for more information about the pitfalls of
     * the self-refering generics pattern):
     * <p>
     * <pre>
     * public static class Builder extends BuilderBase&lt;MyJob, Builder> {
     *     public Builder(String id) {
     *         super(id);
     *     }
     *
     *     &#64;Override
     *     protected MyJob createInstance() {
     *         return new MyJob(this);
     *     }
     * }
     * </pre>
     * <p>
     * The sole method to be implemented is {@linkplain #createInstance()}. This
     * is now type-safe and generic-less.
     * </p>
     *
     * @author balage
     */
    public abstract static class JobBuilder<T extends AbstractJob, B extends JobBuilder<T, B>> {

        protected SizeDimension.Builder capacityBuilder = SizeDimension.Builder.newInstance();

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
        public B addAllSizeDimensions(SizeDimension size) {
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
         * Builds the job.
         * <p>
         * <p>
         * You never has to override this method. Override the
         * {@linkplain #validate()} and {@linkplain #createInstance()} methods
         * instead. (See for detailed implementation guidlines at
         * {@linkplain JobBuilder}!)
         * </p>
         *
         * @return {@link T} The new implementation of the corresponding Job.
         * @author balage
         * @see JobBuilder
         */
        public final T build() {
            validate();
            T job = createInstance();
            job.createActivities(this);
            return job;
        }

        protected abstract void validate();

        protected abstract T createInstance();

        public SizeDimension getCapacity() {
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

    private SizeDimension sizeAtStart;

    private SizeDimension sizeAtEnd;



    /**
     * Builder based constructor.
     *
     * @param builder The builder instance.
     * @see JobBuilder
     */
    protected AbstractJob(JobBuilder<?, ?> builder) {
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
        allTimeWindows = new LinkedHashSet<>();
        activityList.getAll().stream().forEach(ja -> {
            addLocation(ja.getLocation());
            addTimeWindows(ja.getTimeWindows());
        });
        sizeAtStart = calcSizeAt(true);
        sizeAtEnd = calcSizeAt(false);
    }

    private SizeDimension calcSizeAt(boolean start) {
        SizeDimension size = SizeDimension.EMPTY;
        for (JobActivity act : activityList.getAll()) {
            size = size.add(act.getLoadChange());
        }
        if (start) return size.getNegativeDimensions().abs();
        else return size.getPositiveDimensions();
    }

    private void addTimeWindows(Collection<TimeWindow> timeWindows) {
        if (timeWindows != null && !timeWindows.isEmpty()) {
            allTimeWindows.addAll(timeWindows);
        }
    }

    public SizeDimension getSizeAtStart() {
        return sizeAtStart;
    }

    public SizeDimension getSizeAtEnd() {
        return sizeAtEnd;
    }

    /**
     * Creates the activities.
     * <p>
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

