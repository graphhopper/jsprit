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

import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.misc.JobInsertionContext;
import com.graphhopper.jsprit.core.problem.solution.route.activity.*;

/**
 * @author stefan schroeder
 * @author balage (generic job refactor)
 */
public class ServiceDeliveriesFirstConstraint implements HardActivityConstraint {

    @Override
    public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
        // ConstraintsStatus oldRes = old(prevAct, newAct, nextAct);
        ConstraintsStatus newRes = newMethod(prevAct, newAct, nextAct);
        // if (oldRes != newRes) {
        // newRes = newMethod(prevAct, newAct, nextAct);
        // }
        // System.out.format("%14s (%4s) > %14s (%4s) > %14s (%4s) ====> %20s :
        // %20s (%4s)\n",
        // prevAct.getName(), isShipment(prevAct),
        // newAct.getName(), isShipment(newAct),
        // nextAct.getName(), isShipment(nextAct),
        // oldRes, newRes, (oldRes == newRes));
        return newRes;
    }

    protected ConstraintsStatus newMethod(TourActivity prevAct, TourActivity newAct,
                                          TourActivity nextAct) {
        if (isShipment(newAct)) {
            // The new activity is a shipment
            if (nextAct instanceof DeliveryActivityNEW && isService(nextAct)) {
                // Next activity can't be a service delivery
                return ConstraintsStatus.NOT_FULFILLED;
            }
        } else {
            // The new activity is a service
            if (newAct instanceof PickupActivityNEW || newAct instanceof ServiceActivityNEW) {
                // The new activity is a pickup or a service
                if (nextAct instanceof DeliveryActivityNEW && isService(nextAct)) {
                    // Next activity can't be a service delivera
                    return ConstraintsStatus.NOT_FULFILLED;
                }
            } else if (newAct instanceof DeliveryActivityNEW) {
                // The new activity is a delivery
                if (prevAct instanceof PickupActivityNEW || prevAct instanceof ServiceActivityNEW
                    || (prevAct instanceof DeliveryActivityNEW
                    && isShipment(prevAct))) {
                    // The previous activity can't be a pickup or service (of
                    // any type of Job), nor a shipment delivery
                    // (Only service delivery.)
                    return ConstraintsStatus.NOT_FULFILLED_BREAK;
                }
            }
        }

        return ConstraintsStatus.FULFILLED;
    }

    protected boolean isShipment(TourActivity newAct) {
        return newAct instanceof JobActivity && ((JobActivity) newAct).getJob() instanceof Shipment;
    }

    protected boolean isService(TourActivity newAct) {
        return newAct instanceof JobActivity && ((JobActivity) newAct).getJob() instanceof Service;
    }

    // protected ConstraintsStatus old(TourActivity prevAct, TourActivity
    // newAct,
    // TourActivity nextAct) {
    // if (newAct instanceof PickupServiceDEPRECATED
    // && nextAct instanceof DeliverServiceDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED;
    // }
    // if (newAct instanceof ServiceActivityNEW && nextAct instanceof
    // DeliverServiceDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED;
    // }
    // if (newAct instanceof DeliverServiceDEPRECATED
    // && prevAct instanceof PickupServiceDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED_BREAK;
    // }
    // if (newAct instanceof DeliverServiceDEPRECATED && prevAct instanceof
    // ServiceActivityNEW) {
    // return ConstraintsStatus.NOT_FULFILLED_BREAK;
    // }
    //
    // if (newAct instanceof DeliverServiceDEPRECATED
    // && prevAct instanceof PickupShipmentDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED_BREAK;
    // }
    // if (newAct instanceof DeliverServiceDEPRECATED
    // && prevAct instanceof DeliverShipmentDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED_BREAK;
    // }
    // if (newAct instanceof PickupShipmentDEPRECATED
    // && nextAct instanceof DeliverServiceDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED;
    // }
    // if (newAct instanceof DeliverShipmentDEPRECATED
    // && nextAct instanceof DeliverServiceDEPRECATED) {
    // return ConstraintsStatus.NOT_FULFILLED;
    // }
    //
    // return ConstraintsStatus.FULFILLED;
    // }


}
