package com.graphhopper.jsprit.examples;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.algorithm.state.UpdateActivityTimes;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import com.graphhopper.jsprit.core.problem.job.Shipment;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleType;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.reporting.SolutionPrinter;
import com.graphhopper.jsprit.core.util.ActivityTimeTracker;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.VehicleRoutingTransportCostsMatrix;
import com.graphhopper.jsprit.util.Examples;

import java.time.Instant;
import java.util.Collection;

public class DynamicServiceTimeExample {

    public static void main(String[] args) {
        /*
         * some preparation - create output folder
         */
        Examples.createOutputFolder();

        //define a symmetric travel time matrix
        VehicleRoutingTransportCostsMatrix.Builder costMatrixBuilder = VehicleRoutingTransportCostsMatrix.Builder.newInstance(true);
        costMatrixBuilder.addTransportTime("vehicle:location", "shipment:pickup", 60D * 1);
        costMatrixBuilder.addTransportTime("vehicle:location", "shipment:dropoff", 60D * 2);
        costMatrixBuilder.addTransportTime("shipment:pickup", "shipment:dropoff", 60D * 1);
        costMatrixBuilder.addTransportTime("vehicle:location", "vehicle:location", 0D);
        costMatrixBuilder.addTransportTime("shipment:pickup", "shipment:pickup", 0D);
        costMatrixBuilder.addTransportTime("shipment:dropoff", "shipment:dropoff", 0D);

        costMatrixBuilder.addTransportTime("vehicle:location", "new:pickup", 60D * 1);
        costMatrixBuilder.addTransportTime("vehicle:location", "new:dropoff", 60D * 2);
        costMatrixBuilder.addTransportTime("new:pickup", "new:dropoff", 60D * 1);
        costMatrixBuilder.addTransportTime("new:pickup", "new:pickup", 0D);
        costMatrixBuilder.addTransportTime("new:dropoff", "new:dropoff", 0D);

        costMatrixBuilder.addTransportTime("new:pickup", "shipment:dropoff", 60D * 1);
        costMatrixBuilder.addTransportTime("new:dropoff", "shipment:pickup", 60D * 1);
        costMatrixBuilder.addTransportTime("new:pickup", "shipment:pickup", 0D);
        costMatrixBuilder.addTransportTime("new:dropoff", "shipment:dropoff", 0D);

        VehicleRoutingTransportCosts costMatrix = costMatrixBuilder.build();

        Instant vehicleStartTime = Instant.parse("2020-10-05T12:00:00Z");
        Instant vehicleEndTime = Instant.parse("2020-10-05T14:00:00Z");
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type")
            .addCapacityDimension(0, 7)
            .setCostPerTransportTime(1)
            .setCostPerDistance(0.0)
            .setCostPerWaitingTime(0.0)
            .setCostPerServiceTime(0.0)
            .build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("vehicle")
            .setStartLocation(Location.Builder.newInstance().setId("vehicle:location").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setEarliestStart(vehicleStartTime.getEpochSecond())
            .setLatestArrival(vehicleEndTime.getEpochSecond())
            .setReturnToDepot(false)
            .setType(type)
            .build();

        Instant earliestPickupTime = Instant.parse("2020-10-05T12:00:00Z");
        Instant latestPickupTime = Instant.parse("2020-10-05T12:05:00Z");
        Instant earliestDeliveryTime = Instant.parse("2020-10-05T12:01:00Z");
        Instant latestDeliveryTime = Instant.parse("2020-10-05T12:10:00Z");
        Shipment shipment = Shipment.Builder.newInstance("shipment")
            .addSizeDimension(0, 1)
            .setPickupLocation(Location.Builder.newInstance().setId("shipment:pickup").setCoordinate(Coordinate.newInstance(2, 2)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("shipment:dropoff").setCoordinate(Coordinate.newInstance(4, 4)).build())
            .addPickupTimeWindow(earliestPickupTime.getEpochSecond(), latestPickupTime.getEpochSecond())
            .addDeliveryTimeWindow(earliestDeliveryTime.getEpochSecond(), latestDeliveryTime.getEpochSecond())
            .setDeliveryServiceTime(120D)
            .setPickupServiceTime(120D)
            .build();

        Instant newEarliestPickupTime = Instant.parse("2020-10-05T12:00:00Z");
        Instant newLatestPickupTime = Instant.parse("2020-10-05T12:05:00Z");
        Instant newEarliestDeliveryTime = Instant.parse("2020-10-05T12:01:00Z");
        Instant newLatestDeliveryTime = Instant.parse("2020-10-05T12:10:00Z");
        Shipment newShipment = Shipment.Builder.newInstance("new")
            .addSizeDimension(0, 1)
            .setPickupLocation(Location.Builder.newInstance().setId("new:pickup").setCoordinate(Coordinate.newInstance(2, 2)).build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("new:dropoff").setCoordinate(Coordinate.newInstance(4, 4)).build())
            .addPickupTimeWindow(newEarliestPickupTime.getEpochSecond(), newLatestPickupTime.getEpochSecond())
            .addDeliveryTimeWindow(newEarliestDeliveryTime.getEpochSecond(), newLatestDeliveryTime.getEpochSecond())
            .setDeliveryServiceTime(60D)
            .setPickupServiceTime(60D)
            .build();

        VehicleRoute vehicleRoute = VehicleRoute.Builder.newInstance(vehicle)
            .addPickup(shipment)
            .addDelivery(shipment)
            .build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .setRoutingCost(costMatrix)
            .addInitialVehicleRoute(vehicleRoute)
            .addJob(newShipment)
            .build();

        SolutionCostCalculator objectiveFunction = new SolutionCostCalculator() {
            @Override
            public double getCosts(VehicleRoutingProblemSolution solution) {
                double costs = 0;
                for (VehicleRoute route : solution.getRoutes()) {
                    for (TourActivity activity : route.getActivities()) {
                        costs += vrp.getActivityCosts().getActivityCost(activity, activity.getArrTime(), route.getDriver(), route.getVehicle());
                    }
                }
                return costs;
            }
        };

        StateManager stateManager = new StateManager(vrp);
        stateManager.addStateUpdater(new UpdateActivityTimes(vrp.getTransportCosts(),ActivityTimeTracker.ActivityPolicy.AS_SOON_AS_TIME_WINDOW_OPENS_WITHIN_GROUP,  vrp.getActivityCosts()));

        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addTimeWindowConstraint();
        constraintManager.addLoadConstraint();
        constraintManager.addSkillsConstraint();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp)
            .setStateAndConstraintManager(stateManager, constraintManager)
            .setObjectiveFunction(objectiveFunction)
            .setProperty(Jsprit.Parameter.FAST_REGRET, "true")
            .buildAlgorithm();

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        System.out.println("---------------------- Route Activity Times ----------------------");
        Solutions.bestOf(solutions).getRoutes().stream().forEach(route -> {
            route.getActivities().stream().forEach(activity -> {
                System.out.println("Arrival time   " + activity.getName() + " " + Instant.ofEpochSecond((long) activity.getArrTime()));
                System.out.println("Departure time " + activity.getName() + " " + Instant.ofEpochSecond((long) activity.getEndTime()));
            });
        });
        System.out.println("---------------------- Route Activity Times ----------------------");

        SolutionPrinter.print(vrp, Solutions.bestOf(solutions), SolutionPrinter.Print.VERBOSE);
    }
}
