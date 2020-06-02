package com.graphhopper.jsprit.core.algorithm.state;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UpdateVehicleDependentPracticalTimeWindowsTest {

    @Test
    public void validateLatestArrivalTimes() {
        final VehicleRoutingTransportCosts vehicleRoutingTransportCosts = new VehicleRoutingTransportCosts() {
            @Override
            public double getDistance(Location from, Location to, double departureTime, Vehicle vehicle) {
                return 0;
            }

            @Override
            public double getTransportTime(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                if (from.getCoordinate().getY() < to.getCoordinate().getY())
                    return 15;
                return 5;
            }

            @Override
            public double getTransportCost(Location from, Location to, double departureTime, Driver driver, Vehicle vehicle) {
                return getTransportTime(to, from, departureTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportTime(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportTime(to, from, arrivalTime, driver, vehicle);
            }

            @Override
            public double getBackwardTransportCost(Location from, Location to, double arrivalTime, Driver driver, Vehicle vehicle) {
                return getTransportCost(to, from, arrivalTime, driver, vehicle);
            }
        };

        final VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setEarliestStart(0)
            .setLatestArrival(200)
            .setStartLocation(Location.newInstance(0, 0))
            .setEndLocation(Location.newInstance(0, 0))
            .build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .addJob(
                Service.Builder.newInstance("service 1")
                    .setTimeWindow(new TimeWindow(10, 117))
                    .setLocation(Location.newInstance(1, 1))
                    .setServiceTime(5).build()
            ).addJob(
                Service.Builder.newInstance("service 2")
                    .setTimeWindow(new TimeWindow(40, 60))
                    .setLocation(Location.newInstance(5, 5))
                    .setServiceTime(5).build()
            ).addJob(
                Service.Builder.newInstance("service 3")
                    .setTimeWindow(new TimeWindow(40, 120))
                    .setLocation(Location.newInstance(10, 10))
                    .setServiceTime(5).build()
            ).addVehicle(vehicle)
            .setRoutingCost(vehicleRoutingTransportCosts).build();

        final StateManager stateManager = new StateManager(vrp);
        final UpdateVehicleDependentPracticalTimeWindows updateVehicleDependentPracticalTimeWindows = new UpdateVehicleDependentPracticalTimeWindows(stateManager, vehicleRoutingTransportCosts, vrp.getActivityCosts());
        {
            final VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .addService((Service) vrp.getJobs().get("service 1"))
                .addService((Service) vrp.getJobs().get("service 2"))
                .addService((Service) vrp.getJobs().get("service 3"))
                .build();
            updateVehicleDependentPracticalTimeWindows.visit(route);
            assertEquals(60.0 - 15 - 5, stateManager.getActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(60.0, stateManager.getActivityState(route.getActivities().get(1), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(120.0, stateManager.getActivityState(route.getActivities().get(2), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
        }
        {
            final VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .addService((Service) vrp.getJobs().get("service 2"))
                .addService((Service) vrp.getJobs().get("service 1"))
                .addService((Service) vrp.getJobs().get("service 3"))
                .build();
            updateVehicleDependentPracticalTimeWindows.visit(route);
            assertEquals(60, stateManager.getActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(120 - 15 - 5, stateManager.getActivityState(route.getActivities().get(1), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(120.0, stateManager.getActivityState(route.getActivities().get(2), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
        }
        {
            final VehicleRoute route = VehicleRoute.Builder.newInstance(vehicle)
                .setJobActivityFactory(vrp.getJobActivityFactory())
                .addService((Service) vrp.getJobs().get("service 3"))
                .addService((Service) vrp.getJobs().get("service 2"))
                .addService((Service) vrp.getJobs().get("service 1"))
                .build();
            updateVehicleDependentPracticalTimeWindows.visit(route);
            assertEquals(60 - 10, stateManager.getActivityState(route.getActivities().get(0), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(60, stateManager.getActivityState(route.getActivities().get(1), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
            assertEquals(117.0, stateManager.getActivityState(route.getActivities().get(2), vehicle, InternalStates.LATEST_OPERATION_START_TIME, Double.class), 0.001);
        }
    }

}
