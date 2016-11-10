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


import com.graphhopper.jsprit.core.problem.solution.route.activity.BreakActivity;

/**
 * Pickup extends Service and is intended to model a Service where smth is LOADED (i.e. picked up) to a transport unit.
 *
 * @author schroeder
 */
public class Break extends Service implements InternalJobMarker {

    public static class Builder extends Service.ServiceBuilderBase<Builder> {


        private boolean variableLocation = true;

        public Builder(String id) {
            super(id);
        }

        /**
         * Builds Pickup.
         * <p>
         * <p>Pickup type is "pickup"
         *
         * @return pickup
         * @throws IllegalStateException if neither locationId nor coordinate has been set
         */
        @SuppressWarnings("unchecked")
        @Override
        public Break build() {
            if (location != null) {
                variableLocation = false;
            }
            setType("break");
            preProcess();
            Break instance = new Break(this);
            postProcess(instance);
            return instance;
        }

    }

    private boolean variableLocation = true;

    Break(Builder builder) {
        super(builder);
        variableLocation = builder.variableLocation;
    }

    @Override
    protected void createActivities() {
        JobActivityList list = new SequentialJobActivityList(this);
        list.addActivity(BreakActivity.newInstance(this));
        setActivities(list);
    }


    public boolean hasVariableLocation() {
        return variableLocation;
    }

}
