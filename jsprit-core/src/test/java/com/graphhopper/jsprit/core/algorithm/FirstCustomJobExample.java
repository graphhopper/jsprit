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

package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.JobActivityList;
import com.graphhopper.jsprit.core.problem.job.SequentialJobActivityList;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.Solutions;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by schroeder on 11/11/16.
 */
public class FirstCustomJobExample {

    static class CustomJob extends AbstractJob {

        public static abstract class BuilderBase<T extends CustomJob, B extends BuilderBase<T, B>>
            extends JobBuilder<T, B> {

            List<Location> locs = new ArrayList<>();

            public BuilderBase(String id) {
                super(id);
            }

            public BuilderBase<T,B> addPickup(Location location){
                locs.add(location);
                return this;
            }

            public List<Location> getLocs() {
                return locs;
            }

            protected void validate(){

            }
        }

        public static final class Builder extends BuilderBase<CustomJob, Builder> {

            public static Builder newInstance(String id) {
                return new Builder(id);
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
        public Capacity getSize() {
            return Capacity.EMPTY;
        }

        @Override
        protected void createActivities(JobBuilder<? extends AbstractJob, ?> jobBuilder) {
            Builder builder = (Builder) jobBuilder;
            JobActivityList list = new SequentialJobActivityList(this);
            for(Location loc : builder.getLocs()){
                list.addActivity(new PickupActivityNEW(this,"pick",loc,0,Capacity.EMPTY, Arrays.asList(TimeWindow.ETERNITY)));
            }
            setActivities(list);
        }
    }



    @Test
    public void test(){
        CustomJob cj = CustomJob.Builder.newInstance("job").addPickup(Location.newInstance(10,0)).build();
        Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(cj).addVehicle(v).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertTrue(solution.getUnassignedJobs().isEmpty());
    }
}
