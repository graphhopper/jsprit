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
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliveryActivity;

/**
 * Delivery extends Service and is intended to model a Service where smth is UNLOADED (i.e. delivered) from a transport unit.
 *
 * @author schroeder
 */
public class DeliveryJob extends AbstractSingleActivityJob<DeliveryActivity> {

    public static final class Builder
    extends AbstractSingleActivityJob.BuilderBase<DeliveryJob, Builder> {

        public Builder(String id) {
            super(id);
            setType("delivery");
        }

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        @Override
        protected DeliveryJob createInstance() {
            return new DeliveryJob(this);
        }
    }

    DeliveryJob(BuilderBase<? extends DeliveryJob, ?> builder) {
        super(builder);
    }


    @Override
    protected DeliveryActivity createActivity(
                    BuilderBase<? extends AbstractSingleActivityJob<?>, ?> builder) {
            return new DeliveryActivity(this, builder.type, builder.location,
                            builder.serviceTime,
                            builder.getCapacity().invert(), builder.timeWindows.getTimeWindows());
    }

    @Override
    @Deprecated
    public SizeDimension getSize() {
        return super.getSize().abs();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder getBuilder(String id) {
        return Builder.newInstance(id);
    }

}
