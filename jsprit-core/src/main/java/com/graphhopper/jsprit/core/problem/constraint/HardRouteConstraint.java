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
package com.graphhopper.jsprit.core.problem.constraint;

import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;

/**
 * Hard constraint that evaluates whether a new job (insertionContext.getJob()) can be inserted
 * existing route (insertionContext.getRoute()).
 */
public interface HardRouteConstraint extends HardConstraint {

    /**
     * Returns whether a job can be inserted in route.
     *
     * @param insertionContext provides context information about inserting a new job, i.e. the new job (<code>insertionContext.getJob()</code>),
     *                         the route where the new job should be inserted (<code>insertionContext.getRoute()</code>), the new vehicle that
     *                         should operate the route plus the new job (<code>insertionContext.getNewVehicle()</code>) and the new departure
     *                         time at this vehicle's start location (<code>insertionContext.getNewDepartureTime()</code>).
     * @return true if constraint is met, false otherwise
     */
    public boolean fulfilled(JobInsertionContext insertionContext);

}
