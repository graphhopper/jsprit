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
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;


/**
 * Delivery job implementation.
 * <p>
 * Delivery is intend to represent a kind of job where something is unloaded.
 * </p>
 *
 * <h3>Warning!</h3>
 * <p>
 * This class and are here for convenience. Most of the time using the
 * {@linkplain CustomJob} is a better choice. Note that this class may most
 * likely be deprecated and be removed in the future.
 * </p>
 *
 * @author schroeder
 * @author Balage
 *
 * @see {@linkplain CustomJob.BuilderBase#addDelivery(Location)}
 * @see {@linkplain CustomJob.BuilderBase#addDelivery(Location, SizeDimension)}
 * @see {@linkplain CustomJob.BuilderBase#addDelivery(Location, SizeDimension, double)}
 * @see {@linkplain CustomJob.BuilderBase#addDelivery(Location, SizeDimension, double, TimeWindow)}
 */
public class DeliveryJob extends AbstractSingleActivityJob<DeliveryActivity> {

    /**
     * Builder for {@linkplain PickupJob}.
     *
     * @author Balage
     */
    public static final class Builder
    extends AbstractSingleActivityJob.BuilderBase<DeliveryJob, Builder> {

        /**
         * Constructor.
         *
         * @param id
         *            The unique id.
         */
        public Builder(String id) {
            super(id);
            setType("delivery");
        }

        @Override
        protected DeliveryJob createInstance() {
            return new DeliveryJob(this);
        }
    }

    private DeliveryJob(BuilderBase<? extends DeliveryJob, ?> builder) {
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
        return new Builder(id);
    }

}
