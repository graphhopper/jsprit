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

package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.solution.route.RouteVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class UpdateVehicleDependentPracticalTimeWindows implements RouteVisitor, StateUpdater {

    @Override
    public void visit(VehicleRoute route) {
        begin(route);
        Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator();
        while (revIterator.hasNext()) {
            visit(revIterator.next());
        }
        finish();
    }

    public static interface VehiclesToUpdate {

        public Collection<Vehicle> get(VehicleRoute route);

    }

    private VehiclesToUpdate vehiclesToUpdate = new VehiclesToUpdate() {

        @Override
        public Collection<Vehicle> get(VehicleRoute route) {
            return Arrays.asList(route.getVehicle());
        }

    };

    private final StateManager stateManager;

    private final VehicleRoutingTransportCosts transportCosts;

    private final VehicleRoutingActivityCosts activityCosts;

    private VehicleRoute route;

    private double[] latest_arrTimes_at_prevAct;

    private Location[] location_of_prevAct;
    
    private double[] setup_time_of_prevAct;

    private Collection<Vehicle> vehicles;

    public UpdateVehicleDependentPracticalTimeWindows(StateManager stateManager, VehicleRoutingTransportCosts tpCosts, VehicleRoutingActivityCosts activityCosts) {
        super();
        this.stateManager = stateManager;
        this.transportCosts = tpCosts;
        this.activityCosts = activityCosts;
        latest_arrTimes_at_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        location_of_prevAct = new Location[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
        setup_time_of_prevAct = new double[stateManager.getMaxIndexOfVehicleTypeIdentifiers() + 1];
    }

    public void setVehiclesToUpdate(VehiclesToUpdate vehiclesToUpdate) {
        this.vehiclesToUpdate = vehiclesToUpdate;
    }


    public void begin(VehicleRoute route) {
        this.route = route;
        vehicles = vehiclesToUpdate.get(route);
        for (Vehicle vehicle : vehicles) {
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getLatestArrival();
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = vehicle.getEndLocation();
            setup_time_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = 0.0;
        }
    }


    public void visit(TourActivity activity) {
        for (Vehicle vehicle : vehicles) {
            double coef = 1.0;
            if(vehicle != null)
            	coef = vehicle.getCoefSetupTime();
            double latestArrTimeAtPrevAct = latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            Location prevLocation = location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()];
            double setup_time_activity_prevLocation = 0.0;
            if(!activity.getLocation().equals(prevLocation))
            	setup_time_activity_prevLocation = setup_time_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] * coef;
            double transport_time_activity_prevLocation = setup_time_activity_prevLocation + transportCosts.getBackwardTransportTime(activity.getLocation(), prevLocation,
                    latestArrTimeAtPrevAct, route.getDriver(), vehicle);
            double potentialLatestArrivalTimeAtCurrAct = latestArrTimeAtPrevAct - transport_time_activity_prevLocation - activityCosts.getActivityDuration(activity, latestArrTimeAtPrevAct, route.getDriver(), route.getVehicle());
            double latestArrivalTime = Math.min(activity.getTheoreticalLatestOperationStartTime(), potentialLatestArrivalTimeAtCurrAct);
            if (latestArrivalTime < activity.getTheoreticalEarliestOperationStartTime()) {
                stateManager.putTypedInternalRouteState(route, vehicle, InternalStates.SWITCH_NOT_FEASIBLE, true);
            }
            stateManager.putInternalTypedActivityState(activity, vehicle, InternalStates.LATEST_OPERATION_START_TIME, latestArrivalTime);
            latest_arrTimes_at_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = latestArrivalTime;
            location_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getLocation();
            setup_time_of_prevAct[vehicle.getVehicleTypeIdentifier().getIndex()] = activity.getSetupTime();
        }
    }


    public void finish() {
    }

}

