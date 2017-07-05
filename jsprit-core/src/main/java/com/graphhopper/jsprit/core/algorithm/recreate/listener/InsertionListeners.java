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
package com.graphhopper.jsprit.core.algorithm.recreate.listener;

import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class InsertionListeners {

    private Collection<InsertionListener> listeners = new ArrayList<InsertionListener>();

    public Collection<InsertionListener> getListeners() {
        return listeners;
    }

    public void informJobInserted(Job insertedJob, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
        for (InsertionListener l : listeners) {
            if (l instanceof JobInsertedListener) {
                ((JobInsertedListener) l).informJobInserted(insertedJob, inRoute, additionalCosts, additionalTime);
            }
        }
    }

    public void informVehicleSwitched(VehicleRoute route, Vehicle oldVehicle, Vehicle newVehicle) {
        for (InsertionListener l : listeners) {
            if (l instanceof VehicleSwitchedListener) {
                ((VehicleSwitchedListener) l).vehicleSwitched(route, oldVehicle, newVehicle);
            }
        }
    }

    public void informBeforeJobInsertion(Job job, InsertionData data, VehicleRoute route) {
        for (InsertionListener l : listeners) {
            if (l instanceof BeforeJobInsertionListener) {
                ((BeforeJobInsertionListener) l).informBeforeJobInsertion(job, data, route);
            }
        }
    }

    public void informInsertionStarts(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs) {
        for (InsertionListener l : listeners) {
            if (l instanceof InsertionStartsListener) {
                ((InsertionStartsListener) l).informInsertionStarts(vehicleRoutes, unassignedJobs);
            }
        }
    }

    public void informInsertionEndsListeners(Collection<VehicleRoute> vehicleRoutes) {
        for (InsertionListener l : listeners) {
            if (l instanceof InsertionEndsListener) {
                ((InsertionEndsListener) l).informInsertionEnds(vehicleRoutes);
            }
        }
    }

    public void informJobUnassignedListeners(Job unassigned, List<String> reasons) {
        for (InsertionListener l : listeners) {
            if (l instanceof JobUnassignedListener) {
                ((JobUnassignedListener) l).informJobUnassigned(unassigned, reasons);
            }
        }
    }

    public void addListener(InsertionListener insertionListener) {
        listeners.add(insertionListener);
    }

    public void removeListener(InsertionListener insertionListener) {
        listeners.remove(insertionListener);
    }

    public void addAllListeners(Collection<InsertionListener> listeners) {
        for (InsertionListener l : listeners) addListener(l);
    }

}
