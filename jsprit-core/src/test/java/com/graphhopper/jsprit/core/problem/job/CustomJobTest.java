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

import com.graphhopper.jsprit.core.algorithm.FirstCustomJobWithMultipleActivitiesExample;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivityNEW;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by schroeder on 16/11/16.
 */
public class CustomJobTest {

    static class CustomJob extends AbstractJob {

        public static abstract class BuilderBase<T extends CustomJob, B extends CustomJob.BuilderBase<T, B>>
            extends JobBuilder<T, B> {

            List<Location> locs = new ArrayList<>();

            List<SizeDimension> cap = new ArrayList<>();

            public BuilderBase(String id) {
                super(id);
            }

            public CustomJob.BuilderBase<T,B> addPickup(Location location, SizeDimension capacity){
                locs.add(location);
                cap.add(capacity);
                return this;
            }

            public List<Location> getLocs() {
                return locs;
            }

            public List<SizeDimension> getCaps() { return cap; }

            protected void validate(){

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
            for(int i=0;i<builder.getLocs().size();i++){
                list.addActivity(new PickupActivityNEW(this,"pick",builder.getLocs().get(i),0,builder.getCaps().get(i), Arrays.asList(TimeWindow.ETERNITY)));
            }
            setActivities(list);
        }
    }

    @Test
    public void whenCreatingANewJobWithThreeDistinctActivities_jobShouldContainActivities(){
        CustomJob cj = CustomJob.Builder.newInstance("job")
            .addPickup(Location.newInstance(10,0),SizeDimension.Builder.newInstance().addDimension(0,1).build())
            .addPickup(Location.newInstance(5,0),SizeDimension.Builder.newInstance().addDimension(0,2).build())
            .addPickup(Location.newInstance(20,0),SizeDimension.Builder.newInstance().addDimension(0,1).build())
            .build();
        Assert.assertEquals(3,cj.getActivityList().size());

    }

}
