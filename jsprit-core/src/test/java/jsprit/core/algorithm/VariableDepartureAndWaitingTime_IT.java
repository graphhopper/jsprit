package jsprit.core.algorithm;

import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateDepartureTime;
import jsprit.core.analysis.SolutionAnalyser;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.cost.TransportDistance;
import jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import jsprit.core.problem.driver.Driver;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.SolutionCostCalculator;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.CostFactory;
import jsprit.core.util.RandomNumberGeneration;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Random;

/**
 * Created by schroeder on 22/07/15.
 */
public class VariableDepartureAndWaitingTime_IT {

    static interface AlgorithmFactory {
        VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp);
    }

    VehicleRoutingActivityCosts activityCosts;

    AlgorithmFactory algorithmFactory;

    @Before
    public void doBefore(){
        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return vehicle.getType().getVehicleCostParams().perWaitingTimeUnit * Math.max(0,tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime);
            }

        };
        algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
                ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

                return  Jsprit.Builder.newInstance(vrp)
                        .addCoreStateAndConstraintStuff(true)
                        .setStateAndConstraintManager(stateManager,constraintManager)
                        .setObjectiveFunction(new SolutionCostCalculator() {
                            @Override
                            public double getCosts(VehicleRoutingProblemSolution solution) {
                                SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, new TransportDistance() {
                                    @Override
                                    public double getDistance(Location from, Location to) {
                                        return vrp.getTransportCosts().getTransportCost(from, to, 0., null, null);
                                    }
                                });
                                return sa.getWaitingTime() + sa.getDistance();
                            }
                        })
                        .buildAlgorithm();
            }
        };
    }

    @Test
    public void plainSetupShouldWork(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10,0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(20,0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createManhattanCosts())
                .setActivityCosts(activityCosts)
                .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40.,solution.getCost());
    }

    @Test
    public void withTimeWindowsShouldWork(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0,0)).build();
        Service s1 = Service.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(1010,1100)).setLocation(Location.newInstance(10,0)).build();
        Service s2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(1020,1100)).setLocation(Location.newInstance(20,0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createManhattanCosts())
                .setActivityCosts(activityCosts)
                .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40.+1000.,solution.getCost());
    }

    @Test
    public void variableDepartureShouldWork(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(1010,1100)).setLocation(Location.newInstance(10,0)).build();
        Service s2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(1020,1100)).setLocation(Location.newInstance(20,0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createManhattanCosts())
                .setActivityCosts(activityCosts)
                .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40.,solution.getCost());
    }

    @Test
    public void variableDepartureShouldWork2(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(1010,1100)).setLocation(Location.newInstance(10,0)).build();
        Service s2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(1020,1100)).setLocation(Location.newInstance(20,0)).build();
        Service s3 = Service.Builder.newInstance("s3").setTimeWindow(TimeWindow.newInstance(1020,1100)).setLocation(Location.newInstance(15,0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createManhattanCosts())
                .setActivityCosts(activityCosts)
                .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40.,solution.getCost());
    }

    @Test
    public void variableDepartureShouldWork3(){
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0,10)).setTimeWindow(TimeWindow.newInstance(10,10)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(5,15)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(10,10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createEuclideanCosts())
                .setActivityCosts(activityCosts)
                .build();
        AlgorithmFactory algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
                ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

                return  Jsprit.Builder.newInstance(vrp)
                        .addCoreStateAndConstraintStuff(true)
                        .setStateAndConstraintManager(stateManager,constraintManager)
                        .setObjectiveFunction(new SolutionCostCalculator() {
                            @Override
                            public double getCosts(VehicleRoutingProblemSolution solution) {
                                SolutionAnalyser sa = new SolutionAnalyser(vrp, solution, new TransportDistance() {
                                    @Override
                                    public double getDistance(Location from, Location to) {
                                        return vrp.getTransportCosts().getTransportCost(from, to, 0., null, null);
                                    }
                                });
                                return sa.getWaitingTime() + sa.getDistance();
                            }
                        })
                        .buildAlgorithm();
            }
        };
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        Assert.assertEquals("s1", ((TourActivity.JobActivity) solution.getRoutes().iterator().next().getActivities().get(0)).getJob().getId());
        Assert.assertEquals("s2",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(1)).getJob().getId());
        Assert.assertEquals("s3",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(2)).getJob().getId());
    }

    @Test
    public void variableDepartureShouldWork4(){
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(1.0).setCostPerWaitingTime(1.).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setHasVariableDepartureTime(false).setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0,10)).setTimeWindow(TimeWindow.newInstance(15,15)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0,11)).setTimeWindow(TimeWindow.newInstance(100,100)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(10,10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addVehicle(v)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createEuclideanCosts())
                .build();
        AlgorithmFactory algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
                ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

                return  Jsprit.Builder.newInstance(vrp)
                        .addCoreStateAndConstraintStuff(true)
                        .setStateAndConstraintManager(stateManager,constraintManager)
                        .setObjectiveFunction(new SolutionCostCalculator() {
                            @Override
                            public double getCosts(VehicleRoutingProblemSolution solution) {
                                double costs = 0.;
                                for(VehicleRoute route : solution.getRoutes()){
                                    costs += route.getVehicle().getType().getVehicleCostParams().fix;
                                    TourActivity prevAct = route.getStart();
                                    for(TourActivity act : route.getActivities()){
                                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),act.getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                        costs += vrp.getActivityCosts().getActivityCost(act,act.getArrTime(),route.getDriver(),route.getVehicle());
                                        prevAct = act;
                                    }
                                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),route.getEnd().getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                }
                                costs += solution.getUnassignedJobs().size() * 1000;
                                return costs;
                            }
                        })
                        .buildAlgorithm();
            }
        };
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
        Assert.assertEquals("s1", ((TourActivity.JobActivity) solution.getRoutes().iterator().next().getActivities().get(0)).getJob().getId());
        Assert.assertEquals("s3",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(1)).getJob().getId());
        Assert.assertEquals("s2",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(2)).getJob().getId());
    }

    @Test
    public void variableDepartureShouldWork5(){
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(1.0).setCostPerWaitingTime(1.0).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0,10)).setTimeWindow(TimeWindow.newInstance(15,15)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(0,11)).setTimeWindow(TimeWindow.newInstance(100,100)).build();
        Service s3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance(10,10)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
                .addJob(s1).addJob(s2).addJob(s3).addVehicle(v).addVehicle(v2)
                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
                .setRoutingCost(CostFactory.createEuclideanCosts())
                .build();
        AlgorithmFactory algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
                ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

                return  Jsprit.Builder.newInstance(vrp)
                        .addCoreStateAndConstraintStuff(true)
                        .setStateAndConstraintManager(stateManager,constraintManager)
                        .setObjectiveFunction(new SolutionCostCalculator() {
                            @Override
                            public double getCosts(VehicleRoutingProblemSolution solution) {
                                double costs = 0.;
                                for(VehicleRoute route : solution.getRoutes()){
                                    costs += route.getVehicle().getType().getVehicleCostParams().fix;
                                    TourActivity prevAct = route.getStart();
                                    for(TourActivity act : route.getActivities()){
                                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),act.getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                        costs += vrp.getActivityCosts().getActivityCost(act,act.getArrTime(),route.getDriver(),route.getVehicle());
                                        prevAct = act;
                                    }
                                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),route.getEnd().getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                }
                                costs += solution.getUnassignedJobs().size() * 1000;
                                return costs;
                            }
                        })
                        .buildAlgorithm();
            }
        };
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
//        Assert.assertEquals("s1", ((TourActivity.JobActivity) solution.getRoutes().iterator().next().getActivities().get(0)).getJob().getId());
//        Assert.assertEquals("s3",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(1)).getJob().getId());
//        Assert.assertEquals("s2",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(2)).getJob().getId());
    }

    @Test
    public void variableDepartureShouldWork6(){
        VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type").setCostPerDistance(1.0).setCostPerWaitingTime(1.0).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(type).setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleImpl v2 = VehicleImpl.Builder.newInstance("v2").setType(type).setHasVariableDepartureTime(true).setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        Random r = RandomNumberGeneration.newInstance();
        for(int i=0;i<10;i++){
            Service s = Service.Builder.newInstance("s"+i).setServiceTime(5).setLocation(Location.newInstance(0, 10 + r.nextInt(10))).build();
            vrpBuilder.addJob(s);
        }
        Service s2 = Service.Builder.newInstance("s10").setLocation(Location.newInstance(10,21)).setTimeWindow(TimeWindow.newInstance(100,110)).build();
        Service s3 = Service.Builder.newInstance("s11").setLocation(Location.newInstance(10,10)).setTimeWindow(TimeWindow.newInstance(300,310)).build();
        vrpBuilder.addJob(s2).addJob(s3).addVehicle(v).addVehicle(v2);
        vrpBuilder.setRoutingCost(CostFactory.createEuclideanCosts());
        vrpBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        final VehicleRoutingProblem vrp = vrpBuilder.build();
//        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
//                .addJob(s1).addJob(s2).addJob(s3).addVehicle(v).addVehicle(v2)
//                .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
//                .setRoutingCost(CostFactory.createEuclideanCosts())
//                .build();
        AlgorithmFactory algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                stateManager.addStateUpdater(new UpdateDepartureTime(vrp.getTransportCosts()));
                ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);

                return  Jsprit.Builder.newInstance(vrp)
                        .addCoreStateAndConstraintStuff(true)
                        .setStateAndConstraintManager(stateManager,constraintManager)
                        .setObjectiveFunction(new SolutionCostCalculator() {
                            @Override
                            public double getCosts(VehicleRoutingProblemSolution solution) {
                                double costs = 0.;
                                for(VehicleRoute route : solution.getRoutes()){
                                    costs += route.getVehicle().getType().getVehicleCostParams().fix;
                                    TourActivity prevAct = route.getStart();
                                    for(TourActivity act : route.getActivities()){
                                        costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),act.getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                        costs += vrp.getActivityCosts().getActivityCost(act,act.getArrTime(),route.getDriver(),route.getVehicle());
                                        prevAct = act;
                                    }
                                    costs += vrp.getTransportCosts().getTransportCost(prevAct.getLocation(),route.getEnd().getLocation(),prevAct.getEndTime(),route.getDriver(),route.getVehicle());
                                }
                                costs += solution.getUnassignedJobs().size() * 1000;
                                return costs;
                            }
                        })
                        .buildAlgorithm();
            }
        };
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        System.out.println("c: " + solution.getCost());
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
//        Assert.assertEquals("s1", ((TourActivity.JobActivity) solution.getRoutes().iterator().next().getActivities().get(0)).getJob().getId());
//        Assert.assertEquals("s3",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(1)).getJob().getId());
//        Assert.assertEquals("s2",((TourActivity.JobActivity)solution.getRoutes().iterator().next().getActivities().get(2)).getJob().getId());
    }




}
