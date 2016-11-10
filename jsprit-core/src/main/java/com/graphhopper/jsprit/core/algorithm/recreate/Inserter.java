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
package com.graphhopper.jsprit.core.algorithm.recreate;

import java.util.List;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData.NoInsertionFound;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.InsertionListeners;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.DefaultTourActivityFactory;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

class Inserter {

    private abstract class JobInsertionHandler {

        private JobInsertionHandler delegator = null;

        public abstract void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route);

        public JobInsertionHandler withNextHandler(JobInsertionHandler handler) {
            delegator = handler;
            return this;
        }

        protected void delegate(Job job, InsertionData iData, VehicleRoute route) {
            if (delegator != null) {
                delegator.handleJobInsertion(job, iData, route);
            }
        }
    }

    class JobExceptionHandler extends JobInsertionHandler {

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            throw new IllegalStateException("job insertion is not supported. Do not know job type.");
        }

    }


    private class UnifiedInsertionHandler extends JobInsertionHandler {

        public UnifiedInsertionHandler() {
        }

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            route.setVehicleAndDepartureTime(iData.getSelectedVehicle(), iData.getVehicleDepartureTime());
            if (!iData.getSelectedVehicle().isReturnToDepot()) {
                if (iData.getDeliveryInsertionIndex() >= route.getActivities().size()) {
                    route.getEnd().setLocation(job.getEndLocation());
                }
            }

            List<JobActivity> acts = job.getActivityList().getAllDuplicated();
            acts.forEach(act -> route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), act));

            // Handles all // delegator.handleJobInsertion(job, iData, route);
        }
    }

    private class ServiceInsertionHandler extends JobInsertionHandler {

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            if (job instanceof Service) {
                route.setVehicleAndDepartureTime(iData.getSelectedVehicle(),
                                iData.getVehicleDepartureTime());
                if (!iData.getSelectedVehicle().isReturnToDepot()) {
                    if (iData.getDeliveryInsertionIndex() >= route.getTourActivities()
                                    .getActivities().size()) {
                        setEndLocation(route, (Service) job);
                    }
                }
                TourActivity activity = job.getActivityList().getAllDuplicated().get(0);
                route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(), activity);
            } else {
                delegate(job, iData, route);
            }
        }

        private void setEndLocation(VehicleRoute route, Service service) {
            route.getEnd().setLocation(service.getLocation());
        }
    }

    private class ShipmentInsertionHandler extends JobInsertionHandler {

        @Override
        public void handleJobInsertion(Job job, InsertionData iData, VehicleRoute route) {
            if (job instanceof Shipment) {
                List<JobActivity> acts = job.getActivityList().getAllDuplicated(); // vehicleRoutingProblem.copyAndGetActivities(job);
                TourActivity pickupShipment = acts.get(0);
                TourActivity deliverShipment = acts.get(1);
                route.setVehicleAndDepartureTime(iData.getSelectedVehicle(),
                                iData.getVehicleDepartureTime());
                if (!iData.getSelectedVehicle().isReturnToDepot()) {
                    if (iData.getDeliveryInsertionIndex() >= route.getActivities().size()) {
                        setEndLocation(route, (Shipment) job);
                    }
                }
                route.getTourActivities().addActivity(iData.getDeliveryInsertionIndex(),
                                deliverShipment);
                route.getTourActivities().addActivity(iData.getPickupInsertionIndex(),
                                pickupShipment);
            } else {
                delegate(job, iData, route);
            }
        }

        private void setEndLocation(VehicleRoute route, Shipment shipment) {
            route.getEnd().setLocation(shipment.getDeliveryLocation());
        }
    }

    private InsertionListeners insertionListeners;

    private JobInsertionHandler jobInsertionHandler;

    private VehicleRoutingProblem vehicleRoutingProblem;

    public Inserter(InsertionListeners insertionListeners, VehicleRoutingProblem vehicleRoutingProblem) {
        this.insertionListeners = insertionListeners;
        new DefaultTourActivityFactory();

        // Balage1551 - The new, unified handler
        // There is a problem: how to handle uniformly insertation indexes for
        // multiple activities
        // jobInsertionHandler = new UnifiedInsertionHandler();

        //The old ones:
        jobInsertionHandler = new ServiceInsertionHandler().withNextHandler(
                        new ShipmentInsertionHandler().withNextHandler(new JobExceptionHandler()));
    }

    public void insertJob(Job job, InsertionData insertionData, VehicleRoute vehicleRoute) {
        insertionListeners.informBeforeJobInsertion(job, insertionData, vehicleRoute);

        if (insertionData == null || (insertionData instanceof NoInsertionFound)) {
            throw new IllegalStateException("insertionData null. cannot insert job.");
        }
        if (job == null) {
            throw new IllegalStateException("cannot insert null-job");
        }
        if (!(vehicleRoute.getVehicle().getId().equals(insertionData.getSelectedVehicle().getId()))) {
            insertionListeners.informVehicleSwitched(vehicleRoute, vehicleRoute.getVehicle(), insertionData.getSelectedVehicle());
            vehicleRoute.setVehicleAndDepartureTime(insertionData.getSelectedVehicle(), insertionData.getVehicleDepartureTime());
        }
        jobInsertionHandler.handleJobInsertion(job, insertionData, vehicleRoute);

        insertionListeners.informJobInserted(job, vehicleRoute, insertionData.getInsertionCost(), insertionData.getAdditionalTime());
    }
}
