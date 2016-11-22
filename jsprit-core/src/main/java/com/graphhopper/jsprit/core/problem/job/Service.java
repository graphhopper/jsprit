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

import com.graphhopper.jsprit.core.problem.solution.route.activity.ServiceActivity;

/**
 * Service implementation of a job.
 * <p>
 * <p>
 * <p>Note that two services are equal if they have the same id.
 *
 * @author schroeder
 */
public class Service extends AbstractSingleActivityJob<ServiceActivity> {


    public static final class Builder
    extends AbstractSingleActivityJob.BuilderBase<Service, Builder> {

        public Builder(String id) {
            super(id);
            setType("pickup");
        }

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        @Override
        protected Service createInstance() {
            return new Service(this);
        }
    }

    Service(Builder builder) {
        super(builder);
    }

    @Override
    protected ServiceActivity createActivity(
                    AbstractSingleActivityJob.BuilderBase<? extends AbstractSingleActivityJob<?>, ?> builder) {
        return new ServiceActivity(this, builder.type,
                        builder.location, builder.serviceTime, builder.getCapacity(),
                        builder.timeWindows.getTimeWindows());
        // return new PickupActivityNEW(this, builder.type, builder.location,
        // builder.serviceTime,
        // builder.getCapacity(), builder.timeWindows.getTimeWindows());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder getBuilder(String id) {
        return Builder.newInstance(id);
    }

}
