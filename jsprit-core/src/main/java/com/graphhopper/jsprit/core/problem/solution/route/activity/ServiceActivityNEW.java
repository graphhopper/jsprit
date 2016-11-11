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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import java.util.Collection;

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.Service;

public class ServiceActivityNEW extends JobActivity {

    public ServiceActivityNEW(AbstractJob job, String name, Location location, double operationTime,
                    Capacity capacity, Collection<TimeWindow> timeWindows) {
        super(job, name, location, operationTime, capacity, timeWindows);
    }

    public ServiceActivityNEW(ServiceActivityNEW sourceActivity) {
        super(sourceActivity);
    }

    // NOTE: Only for testing purposes
    public static ServiceActivityNEW newInstance(Service service) {
        return new ServiceActivityNEW(service, service.getName(), service.getLocation(),
                        service.getServiceDuration(), service.getSize(), service.getTimeWindows());
    }

}
