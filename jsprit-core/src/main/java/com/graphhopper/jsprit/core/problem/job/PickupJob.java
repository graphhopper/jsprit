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
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;

/**
 * Pickup job implementation.
 * <p>
 * Pickup is intend to represent a kind of job where something is loaded.
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
 * @see {@linkplain CustomJob.BuilderBase#addPickup(Location)}
 * @see {@linkplain CustomJob.BuilderBase#addPickup(Location, SizeDimension)}
 * @see {@linkplain CustomJob.BuilderBase#addPickup(Location, SizeDimension, double)}
 * @see {@linkplain CustomJob.BuilderBase#addPickup(Location, SizeDimension, double, TimeWindow)}
 */
public class PickupJob extends AbstractSingleActivityJob<PickupActivity> {

    /**
     * Builder for {@linkplain PickupJob}.
     *
     * @author Balage
     */
    public static final class Builder
    extends AbstractSingleActivityJob.BuilderBase<PickupJob, Builder> {

        /**
         * Constructor.
         *
         * @param id
         *            The unique id.
         */
        public Builder(String id) {
            super(id);
            setType("pickup");
        }

        @Override
        protected PickupJob createInstance() {
            return new PickupJob(this);
        }
    }

    private PickupJob(Builder builder) {
        super(builder);
    }

    @Override
    protected PickupActivity createActivity(
            AbstractSingleActivityJob.BuilderBase<? extends AbstractSingleActivityJob<?>, ?> builder) {
        return new PickupActivity(this, builder.type, builder.location, builder.serviceTime,
                builder.getCapacity(), builder.timeWindows.getTimeWindows());
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder getBuilder(String id) {
        return new Builder(id);
    }

}
