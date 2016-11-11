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

import com.graphhopper.jsprit.core.problem.job.Service;

public final class PickupServiceDEPRECATED extends PickupActivityNEW {

    public PickupServiceDEPRECATED(Service service,
                    Service.BuilderBase<? extends Service, ?> builder) {
        super(service, builder.getType(), builder.getLocation(),
                        builder.getServiceTime(),
                        builder.getCapacity(),
                        builder.getTimeWindows().getTimeWindows());
    }



    public PickupServiceDEPRECATED(PickupServiceDEPRECATED sourceActivity) {
        super(sourceActivity);
    }

    @Deprecated
    public PickupServiceDEPRECATED(Service service) {
        super(service, service.getType(), service.getLocation(), service.getServiceDuration(),
                        service.getSize(), service.getServiceTimeWindows());
    }



    @Override
    public Service getJob() {
        return (Service) super.getJob();
    }

}
