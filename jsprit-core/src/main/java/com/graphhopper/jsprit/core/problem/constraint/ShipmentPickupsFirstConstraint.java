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
import com.graphhopper.jsprit.core.problem.solution.route.activity.DeliverShipmentDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.PickupShipmentDEPRECATED;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

public class ShipmentPickupsFirstConstraint implements HardActivityConstraint {

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        if (newAct instanceof DeliverShipmentDEPRECATED && nextAct instanceof PickupShipmentDEPRECATED) {
            return ConstraintsStatus.NOT_FULFILLED;
        }
        if (newAct instanceof PickupShipmentDEPRECATED && prevAct instanceof DeliverShipmentDEPRECATED) {
            return ConstraintsStatus.NOT_FULFILLED_BREAK;
        }
        return ConstraintsStatus.FULFILLED;
    }

}
