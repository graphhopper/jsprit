package jsprit.core.algorithm;


import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.problem.AbstractJob;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.VehicleRoutingProblem.FleetSize;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleImpl.Builder;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertFalse;


public class PDTW_IT {

    int nJobs = 200;
    int nVehicles = 40;
    Random random = new Random(1623);
    int nextShipmentId=1;
    int nextVehicleId=1;

    @Test
    public void whenDealingWithShipments_timeWindowsShouldNOTbeBroken() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i =0 ; i < nVehicles ; i++){
            vrpBuilder.addVehicle(createVehicle());
        }
        for(int i =0 ; i < nJobs;i++){
            vrpBuilder.addJob(createShipment());
        }
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);
        algorithm.setMaxIterations(0);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        for(VehicleRoute route : bestSolution.getRoutes()){
            Vehicle v = route.getVehicle();
            for(TourActivity ta : route.getActivities()){
                if(ta.getArrTime() > v.getLatestArrival() * 1.00001){
                    assertFalse(true);
                }
            }
        }
    }


    @Test
    public void whenDealingWithServices_timeWindowsShouldNOTbeBroken() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i =0 ; i < nVehicles ; i++){
            vrpBuilder.addVehicle(createVehicle());
        }
        for(int i =0 ; i < nJobs;i++){
            vrpBuilder.addJob(createService());
        }
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(problem);
        algorithm.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        for(VehicleRoute route : bestSolution.getRoutes()){
            Vehicle v = route.getVehicle();
            for(TourActivity ta : route.getActivities()){
                if(ta.getArrTime() * 1.000001 > v.getLatestArrival()){
                    assertFalse(true);
                }
            }
        }
    }

    @Test
    public void whenDealingWithShipments_usingJsprit_timeWindowsShouldNOTbeBroken() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i =0 ; i < nVehicles ; i++){
            vrpBuilder.addVehicle(createVehicle());
        }
        for(int i =0 ; i < nJobs;i++){
            vrpBuilder.addJob(createShipment());
        }
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setMaxIterations(0);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        for(VehicleRoute route : bestSolution.getRoutes()){
            Vehicle v = route.getVehicle();
            for(TourActivity ta : route.getActivities()){
                if(ta.getArrTime() > v.getLatestArrival() * 1.00001){
                    assertFalse(true);
                }
            }
        }
    }


    @Test
    public void whenDealingWithServices_usingJsprit_timeWindowsShouldNOTbeBroken() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i =0 ; i < nVehicles ; i++){
            vrpBuilder.addVehicle(createVehicle());
        }
        for(int i =0 ; i < nJobs;i++){
            vrpBuilder.addJob(createService());
        }
        vrpBuilder.setFleetSize(FleetSize.FINITE);
        VehicleRoutingProblem problem = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.createAlgorithm(problem);
        algorithm.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = algorithm.searchSolutions();
        VehicleRoutingProblemSolution bestSolution = Solutions.bestOf(solutions);

        for(VehicleRoute route : bestSolution.getRoutes()){
            Vehicle v = route.getVehicle();
            for(TourActivity ta : route.getActivities()){
                if(ta.getArrTime() * 1.000001 > v.getLatestArrival()){
                    assertFalse(true);
                }
            }
        }
    }

    private AbstractJob createService() {
        Service.Builder b = Service.Builder.newInstance(Integer.toString(nextShipmentId++));
        b.addSizeDimension(0, 1);
        b.setServiceTime(random.nextDouble() * 5);
        b.setLocation(createLocation());
        return b.build();
    }

    private Location createLocation(){
        return loc(new Coordinate(50*random.nextDouble(), 50*random.nextDouble()));
    }

    private Shipment createShipment(){
        Shipment.Builder b = Shipment.Builder.newInstance(Integer.toString(nextShipmentId++));
        b.addSizeDimension(0, 1);
        b.setPickupServiceTime(random.nextDouble() * 5);
        b.setDeliveryServiceTime(random.nextDouble() * 5);
        b.setDeliveryLocation(createLocation());
        b.setPickupLocation(createLocation());
        return b.build();
    }
    private VehicleImpl createVehicle(){
        VehicleTypeImpl.Builder vehicleTypeBuilder = VehicleTypeImpl.Builder.newInstance("vehicleType" + nextVehicleId).addCapacityDimension(0, 2);
        vehicleTypeBuilder.setCostPerDistance(1.0);
        vehicleTypeBuilder.setCostPerTime(1);
        vehicleTypeBuilder.setFixedCost(1000);
        VehicleType vehicleType = vehicleTypeBuilder.build();
        Builder v = VehicleImpl.Builder.newInstance("vehicle" + nextVehicleId);
        Location l = createLocation();
        v.setStartLocation(l);
        v.setEndLocation(l);
        v.setType(vehicleType);
        v.setEarliestStart(50);
        v.setLatestArrival(200);
        nextVehicleId++;
        return v.build();
    }


    private static Location loc(Coordinate coordinate){
        return Location.Builder.newInstance().setCoordinate(coordinate).build();
    }
}

