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

import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.job.Shipment.BuilderBase;

public final class PickupShipmentDEPRECATED extends PickupActivityNEW{

    public static final String NAME = "pickupShipment";

    public PickupShipmentDEPRECATED(Shipment shipment, BuilderBase<? extends Shipment, ?> builder) {
        super(shipment, NAME, builder.getPickupLocation(),
                        builder.getPickupServiceTime(), builder.getCapacity(),
                        builder.getPickupTimeWindows().getTimeWindows());
    }

    public PickupShipmentDEPRECATED(PickupShipmentDEPRECATED sourceActivity) {
        super(sourceActivity);
    }

    // Only for testing
    @Deprecated
    public PickupShipmentDEPRECATED(Shipment s) {
        super(s, NAME, s.getPickupLocation(), s.getPickupServiceTime(), s.getSize(),
                        s.getPickupTimeWindows());
    }


}
