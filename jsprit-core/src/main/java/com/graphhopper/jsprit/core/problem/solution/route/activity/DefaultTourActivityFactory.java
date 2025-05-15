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

import com.graphhopper.jsprit.core.problem.AbstractActivity;
import com.graphhopper.jsprit.core.problem.job.*;

public class DefaultTourActivityFactory implements TourActivityFactory {
    @Override
    public AbstractActivity createActivity(Service service) {
        if (service.getJobType().equals(Job.Type.EN_ROUTE_DELIVERY)) {
            return new EnRouteDeliveryActivity((EnRouteDelivery) service);
        }

        if (service.getJobType().equals(Job.Type.EN_ROUTE_PICKUP)) {
            return new EnRoutePickupActivity((EnRoutePickup) service);
        }

        if (service.getJobType().isPickup()) {
            return new PickupService((Pickup) service);
        }

        if (service.getJobType().isDelivery()) {
            return new DeliverService((Delivery) service);
        }

        return createDefaultServiceActivity(service);
    }

    private AbstractActivity createDefaultServiceActivity(Service service) {
        return service.getLocation() == null
            ? new ActWithoutStaticLocation(service)
            : new PickupService(service);
    }

}
