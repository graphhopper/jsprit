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


import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;

/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 *
 * @author schroeder
 */
public class Break extends AbstractSingleActivityJob<BreakActivity> implements InternalJobMarker {

    public static final class Builder extends ServiceJob.BuilderBase<Break, Builder> {

        private static final Location VARIABLE_LOCATION = Location
                .newInstance("@@@VARIABLE_LOCATION");

        public Builder(String id) {
            super(id);
            setType("break");
            setLocation(VARIABLE_LOCATION);
        }

        public static Builder newInstance(String id) {
            return new Builder(id);
        }

        @Override
        protected void validate() {
            super.validate();
            // This is a trick: Service requires a location, but after
            // validation we could remove it.
            if (location.equals(VARIABLE_LOCATION)) {
                location = null;
            }
        }

        @Override
        protected Break createInstance() {
            return new Break(this);
        }

    }


    private boolean variableLocation = true;

    Break(Builder builder) {
        super(builder);
        variableLocation = (builder.getLocation() == null);
    }


    @Override
    protected BreakActivity createActivity(
            BuilderBase<? extends AbstractSingleActivityJob<?>, ?> builder) {
        return BreakActivity.newInstance(this, (Builder) builder);
    }


    public boolean hasVariableLocation() {
        return variableLocation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Builder getBuilder(String id) {
        return Builder.newInstance(id);
    }

}
