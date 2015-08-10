/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package jsprit.core.algorithm.recreate;


import jsprit.core.algorithm.recreate.listener.InsertionListener;
import jsprit.core.algorithm.recreate.listener.InsertionListeners;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.RandomNumberGeneration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public abstract class AbstractInsertionStrategy implements InsertionStrategy{

    protected class Insertion {

        private final VehicleRoute route;

        private final InsertionData insertionData;

        public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
            super();
            this.route = vehicleRoute;
            this.insertionData = insertionData;
        }

        public VehicleRoute getRoute() {
            return route;
        }

        public InsertionData getInsertionData() {
            return insertionData;
        }

    }

    private final static Logger logger = LogManager.getLogger();

    protected Random random = RandomNumberGeneration.getRandom();

    protected final static double NO_NEW_DEPARTURE_TIME_YET = -12345.12345;

    protected final static Vehicle NO_NEW_VEHICLE_YET = null;

    protected final static Driver NO_NEW_DRIVER_YET = null;

    private InsertionListeners insertionsListeners;

    private EventListeners eventListeners;

    protected VehicleRoutingProblem vrp;

    public AbstractInsertionStrategy(VehicleRoutingProblem vrp) {
        this.insertionsListeners = new InsertionListeners();
        this.vrp = vrp;
        eventListeners = new EventListeners();
    }

    public void setRandom(Random random) {
        this.random = random;
    }

    @Override
    public Collection<Job> insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs){
        insertionsListeners.informInsertionStarts(vehicleRoutes, unassignedJobs);
        Collection<Job> badJobs = insertUnassignedJobs(vehicleRoutes,unassignedJobs);
        insertionsListeners.informInsertionEndsListeners(vehicleRoutes);
        return badJobs;
    }

    public abstract Collection<Job> insertUnassignedJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs);

    @Override
    public void removeListener(InsertionListener insertionListener) {
        insertionsListeners.removeListener(insertionListener);
    }

    @Override
    public Collection<InsertionListener> getListeners() {
        return Collections.unmodifiableCollection(insertionsListeners.getListeners());
    }

    @Override
    public void addListener(InsertionListener insertionListener) {
        insertionsListeners.addListener(insertionListener);

    }

    protected void insertJob(Job unassignedJob, InsertionData iData, VehicleRoute inRoute){
        logger.trace("insert: [jobId={}]{}", unassignedJob.getId(), iData);
        insertionsListeners.informBeforeJobInsertion(unassignedJob, iData, inRoute);
        if(!(inRoute.getVehicle().getId().equals(iData.getSelectedVehicle().getId()))){
            insertionsListeners.informVehicleSwitched(inRoute, inRoute.getVehicle(), iData.getSelectedVehicle());
        }
        for(Event e : iData.getEvents()){
            eventListeners.inform(e);
        }
        insertionsListeners.informJobInserted(unassignedJob, inRoute, iData.getInsertionCost(), iData.getAdditionalTime());
    }

}
