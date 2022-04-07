package com.graphhopper.jsprit.core.pando;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.pando.constraints.DepotCapacityRouteLevelConstraint;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;

import com.graphhopper.jsprit.core.util.Solutions;
import com.poiji.bind.Poiji;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ChartRouteSolutionFinder {
    public static void runChartRouteAlgo() throws IOException {
        /*
         * build the types and vehicles from table above
         * here it is assumed the variable costs are dependent on distance (rather than time or any other measure)
         */

        /*
         * 1. Construct Motherhubs (depots) - shipment start (from excel)
         * 2. construct shipments (based on historical data)
         * 3. construct fleet of same vehicle type ()
         * 4. capacity constraint
         * 5. Contract constraint
         * */

        /* Doubts
         * 1. What should the vehicle start locations be?
         * 2. What should the threadcount be set to ? Why is it 4?
         * 3. WHat is the number of Vehicles ?
         * 4. How many transporters ?
        * */

        //creating workbook instance that refers to .xls file

        //Extracting depot data from depots excel.
        File depotFile = new File("../data/Depots.xlsx");
        List<Hub> hubs = Poiji.fromExcel(depotFile, Hub.class);

        Map<String, Hub> hubMap = hubs.stream()
            .collect(Collectors.toMap(Hub::getDepotId, Function.identity()));


        //Extracting Contract data from depots excel. Only contract type is city to city .
        File contractFile = new File("../data/Contracts.xlsx");
        List<Contract> contracts = Poiji.fromExcel(contractFile, Contract.class);


        //Extracting shipment data from depots excel.
        File shipmentsFile = new File("../data/Shipments.xlsx");
        List<Shipment> shipments = Poiji.fromExcel(shipmentsFile, Shipment.class);

        System.out.println("Printing List Data: " + hubs);


        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        List<com.graphhopper.jsprit.core.problem.job.Shipment> shipmentList=new ArrayList<>();
        for (Shipment inputLoad : shipments) {
            Hub srcHub = hubMap.get(inputLoad.getSrc());
            Hub destHub = hubMap.get(inputLoad.getDest());
            com.graphhopper.jsprit.core.problem.job.Shipment shipment =
                com.graphhopper.jsprit.core.problem.job.Shipment.Builder.newInstance(inputLoad.getDelivery()).
                    addSizeDimension(0, inputLoad.getQuantity())
                    .setPickupLocation(loc(srcHub.getName(),Coordinate.newInstance(srcHub.getLat(), srcHub.getLng())))
                    .setDeliveryLocation(loc(destHub.getName(), Coordinate.newInstance(destHub.getLat(), destHub.getLng())))
                    .setUserData(null).build();
            shipmentList.add(shipment);

        }

        addVehicles(vrpBuilder);

        //set fleetSize to FleetSize.FINITE
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.addAllJobs(shipmentList);

        //add jobs as you know it from SimpleExample and build the routing problem
        VehicleRoutingProblem vrp = vrpBuilder.build();
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

        //Adding Constraints
        /* Depot capacity
        Contract adherence
        * */
        addConstraints(constraintManager);

        //construct Algorithm
        VehicleRoutingAlgorithm vehicleRoutingAlgorithm = getVehicleRoutingAlgorithm(vrp, stateManager, constraintManager);

        //Search Solutions
        Collection<VehicleRoutingProblemSolution> vehicleRoutingProblemSolutions = vehicleRoutingAlgorithm.searchSolutions();


        Solutions.bestOf(vehicleRoutingProblemSolutions);
    }



    private static VehicleRoutingAlgorithm getVehicleRoutingAlgorithm(VehicleRoutingProblem vrp, StateManager stateManager, ConstraintManager constraintManager) {
        VehicleRoutingAlgorithm vehicleRoutingAlgorithm = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.THREADS, "" + 4)
            .setProperty(Jsprit.Parameter.FIXED_COST_PARAM, "10")
            .setProperty(Jsprit.Strategy.CLUSTER_REGRET, "0.")
            .setProperty(Jsprit.Strategy.RADIAL_REGRET, "0.")
            .setProperty(Jsprit.Strategy.RANDOM_REGRET, "1.")
            .setProperty(Jsprit.Strategy.WORST_REGRET, "0.")
            .setProperty(Jsprit.Strategy.CLUSTER_BEST, "0.")
            .setProperty(Jsprit.Strategy.RADIAL_BEST, "0.")
            .setProperty(Jsprit.Strategy.RANDOM_BEST, "1.")
            .setProperty(Jsprit.Parameter.ITERATIONS, "1024")
            .setStateAndConstraintManager(stateManager, constraintManager).buildAlgorithm();
        return vehicleRoutingAlgorithm;
    }

    private static void addConstraints(ConstraintManager constraintManager) {
        DepotCapacityRouteLevelConstraint depotCapacityRouteLevelConstraint = new DepotCapacityRouteLevelConstraint();
        constraintManager.addConstraint(depotCapacityRouteLevelConstraint);
    }

    private static void addVehicles(VehicleRoutingProblem.Builder vrpBuilder) {
        VehicleTypeImpl vehicleType1 = VehicleTypeImpl.Builder.newInstance("type1").addCapacityDimension(0, 100).setCostPerDistance(1.0).build();
        VehicleImpl vehicle1_1 = VehicleImpl.Builder.newInstance("vehicle1_1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();
        VehicleImpl vehicle1_2 = VehicleImpl.Builder.newInstance("vehicle1_2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType1).build();

        VehicleTypeImpl vehicleType2 = VehicleTypeImpl.Builder.newInstance("type2").addCapacityDimension(0, 160).setCostPerDistance(1.2).build();
        VehicleImpl vehicle2_1 = VehicleImpl.Builder.newInstance("vehicle2_1").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();
        VehicleImpl vehicle2_2 = VehicleImpl.Builder.newInstance("vehicle2_2").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType2).build();

        VehicleTypeImpl vehicleType3 = VehicleTypeImpl.Builder.newInstance("type3").addCapacityDimension(0, 300).setCostPerDistance(1.4).build();
        VehicleImpl vehicle3 = VehicleImpl.Builder.newInstance("vehicle3").setStartLocation(Location.newInstance(40, 40)).setType(vehicleType3).build();

        //Use VehicleRoutingProblem.Builder to specify the problem
        vrpBuilder.addVehicle(vehicle1_1).addVehicle(vehicle1_2).addVehicle(vehicle2_1).addVehicle(vehicle2_2).addVehicle(vehicle3);
    }


    private static Location loc(String name, Coordinate coordinate) {
        return Location.Builder.newInstance().setCoordinate(coordinate).build();
    }
}
