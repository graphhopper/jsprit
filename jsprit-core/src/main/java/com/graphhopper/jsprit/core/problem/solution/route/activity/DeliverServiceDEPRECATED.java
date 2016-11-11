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

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.job.Delivery;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Service.BuilderBase;

public final class DeliverServiceDEPRECATED extends DeliveryActivityNEW {

    public DeliverServiceDEPRECATED(Service service, BuilderBase<? extends Service, ?> builder) {
        super(service, builder.getType(), builder.getLocation(),
                        builder.getServiceTime(),
                        Capacity.invert(builder.getCapacity()),
                        builder.getTimeWindows().getTimeWindows());
    }

    public DeliverServiceDEPRECATED(Delivery delivery) {
        super(delivery, delivery.getType(), delivery.getLocation(), delivery.getServiceDuration(),
                        Capacity.invert(delivery.getSize()), delivery.getServiceTimeWindows());
    }

    public DeliverServiceDEPRECATED(DeliverServiceDEPRECATED sourceActivity) {
        super(sourceActivity);
    }



    @Override
    public Delivery getJob() {
        return (Delivery) super.getJob();
    }

}
