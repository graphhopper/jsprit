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

package jsprit.core.analysis;


import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Capacity;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.ManhattanCosts;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class SolutionAnalyserTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    private StateManager stateManager;

    private ConstraintManager constraintManager;

    private SolutionAnalyser.DistanceCalculator distanceCalculator;

    @Before
    public void doBefore(){

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type)
                .setStartLocationCoordinate(Coordinate.newInstance(-5, 0)).build();

        VehicleImpl vehicle2 = VehicleImpl.Builder.newInstance("v2").setType(type)
                .setStartLocationCoordinate(Coordinate.newInstance(5, 0)).build();

        Service s1 = Service.Builder.newInstance("s1")
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .setCoord(Coordinate.newInstance(-10, 1)).addSizeDimension(0, 2).build();
        Service s2 = Service.Builder.newInstance("s2").setCoord(Coordinate.newInstance(-10, 10)).addSizeDimension(0,3).build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1").setPickupCoord(Coordinate.newInstance(-15, 2))
                .setDeliveryCoord(Coordinate.newInstance(-16, 5)).addSizeDimension(0,10)
                .setPickupServiceTime(20.).setDeliveryServiceTime(20.).build();

        Service s3 = Service.Builder.newInstance("s3")
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .setCoord(Coordinate.newInstance(10, 1)).addSizeDimension(0,2).build();
        Service s4 = Service.Builder.newInstance("s4").setCoord(Coordinate.newInstance(10, 10)).addSizeDimension(0,3).build();
        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupCoord(Coordinate.newInstance(15, 2))
                .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
                .setDeliveryCoord(Coordinate.newInstance(16, 5)).addSizeDimension(0,10).build();
        
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle)
                .addVehicle(vehicle2)
                .addJob(s1)
                .addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        vraBuilder.addCoreConstraints();
        stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();
        constraintManager = new ConstraintManager(vrp,stateManager);
        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setMaxIterations(100);
        solution = Solutions.bestOf(vra.searchSolutions());

    }

    @Test
    public void constructionShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        Assert.assertTrue(true);
    }

    @Test
    public void loadAtBeginningOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    public void loadAtBeginningOfRoute2ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();

        Assert.assertEquals(0, analyser.getLoadAtBeginning(route).get(0));
    }

    @Test
    public void loadAtEnd_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    public void loadAtEnd_OfRoute2ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        Iterator<VehicleRoute> iterator = solution.getRoutes().iterator();
        iterator.next();
        VehicleRoute route = iterator.next();

        Assert.assertEquals(5, analyser.getLoadAtEnd(route).get(0));
    }

    @Test
    public void loadAfterActivity_ofStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadRightAfterActivity(route.getStart(), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getLoadRightAfterActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadRightAfterActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadRightAfterActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    public void loadAfterActivity_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadRightAfterActivity(route.getEnd(), route).get(0));
    }

    @Test
    public void whatShouldHappenWithAllActStatistics_IfSpecifiedActIsNotInVehicleRoute(){
        /*
        there might be the need to add .containsActivity(act) to VehicleRoute
         */
        Assert.assertTrue(false);
    }

    @Test
    public void whatShouldHappenWithAllActStatistics_IfSpecifiedActIsNull(){
        Assert.assertTrue(false);
    }



    @Test
    public void loadBeforeActivity_ofStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadJustBeforeActivity(route.getStart(), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLoadJustBeforeActivity(route.getActivities().get(0), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(1), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getLoadJustBeforeActivity(route.getActivities().get(2), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(2, analyser.getLoadJustBeforeActivity(route.getActivities().get(3), route).get(0));
    }

    @Test
    public void loadBeforeActivity_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(5, analyser.getLoadJustBeforeActivity(route.getEnd(), route).get(0));
    }

    @Test
    public void maxLoad_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12, analyser.getMaxLoad(route).get(0));
    }

    @Test
    public void operationTime_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(46. + 40., analyser.getOperationTime(route), 0.01);
    }

    @Test
    public void waitingTime_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4., analyser.getWaitingTime(route), 0.01);
    }

    @Test
    public void transportTime_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getTransportTime(route), 0.01);
    }

    @Test
    public void serviceTime_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(40., analyser.getServiceTime(route), 0.01);
    }

    @Test
    public void distance_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getDistance(route), 0.01);
    }

    @Test
    public void waitingTime_atStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getWaitingTimeAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(4., analyser.getWaitingTimeAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void waitingTime_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void waitingTime_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getWaitingTimeAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void distance_atStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getDistanceAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void distance_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(6., analyser.getDistanceAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void distance_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12., analyser.getDistanceAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void distance_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(16., analyser.getDistanceAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void distance_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(27., analyser.getDistanceAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void distance_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42., analyser.getDistanceAtActivity(route.getEnd(), route), 0.01);
    }



    @Test
    public void whatHappensWhenUserSpecifiedOwnEndTime_stateManagerShouldNotOverwriteThis(){
        assertFalse(true);
    }

    @Test
    public void lateArrivalTimes_atStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getLateArrivalTimesAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimesAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimesAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimesAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimesAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void lateArrivalTimes_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimesAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void lateArrTimes_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0., analyser.getLateArrivalTimes(route), 0.01);
    }

    @Test
    public void variableTransportCosts_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(84., analyser.getVariableTransportCosts(route), 0.01);
    }

    @Test
    public void fixedCosts_OfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(100., analyser.getFixedCosts(route), 0.01);
    }

    @Test
    public void transportCosts_atStartActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(0, analyser.getVariableTransportCostsAtActivity(route.getStart(), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct1ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(6.*2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(0), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct2ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(12.*2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(1), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct3ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(16.*2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(2), route), 0.01);
    }

    @Test
    public void transportCosts_ofAct4ofRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(27.*2., analyser.getVariableTransportCostsAtActivity(route.getActivities().get(3), route), 0.01);
    }

    @Test
    public void transportCosts_ofEndActOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Assert.assertEquals(42.*2., analyser.getVariableTransportCostsAtActivity(route.getEnd(), route), 0.01);
    }

    @Test
    public void whatShouldHappenIf_Route_or_Activity_isEmpty_or_notPartOfRoute(){
        assertFalse(true);
    }

    @Test
    public void capacityViolationAtBeginning_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atBeginning = analyser.getCapacityViolationAtBeginning(route);
        for(int i=0;i<atBeginning.getNuOfDimensions();i++){
            assertTrue(atBeginning.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAtEnd_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        for(int i=0;i<atEnd.getNuOfDimensions();i++){
            assertTrue(atEnd.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationOnRoute_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolation(route);
        assertEquals(50,cap.get(0));
    }

    @Test
    public void capacityViolationAtEnd_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity atEnd = analyser.getCapacityViolationAtEnd(route);
        assertEquals(5,atEnd.get(0));
    }

    @Test
    public void capacityViolationAfterStart_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getStart();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }



    @Test
    public void capacityViolationAtBeginning_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAtBeginning(route);
        assertEquals(25,cap.get(0));
    }



    @Test
    public void capacityViolationAfterStart_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getStart(), route);
        assertEquals(25,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct1_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(0),route);
        assertEquals(35,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct2_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(1),route);
        assertEquals(50,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct3_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(2),route);
        assertEquals(35,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct4_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(3),route);
        assertEquals(15,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct5_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(4),route);
        assertEquals(0,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct6_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(5),route);
        assertEquals(10,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct7_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(6),route);
        assertEquals(0,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct8_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getActivities().get(7),route);
        assertEquals(5,cap.get(0));
    }

    @Test
    public void capacityViolationAfterEnd_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Capacity cap = analyser.getCapacityViolationAfterActivity(route.getEnd(),route);
        assertEquals(5,cap.get(0));
    }

    @Test
    public void capacityViolationAfterAct1_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(0);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct2_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(1);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct3_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(2);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterAct4_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getActivities().get(3);
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void capacityViolationAfterEnd_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        TourActivity act = route.getEnd();
        Capacity cap = analyser.getCapacityViolationAfterActivity(act,route);
        for(int i=0;i<cap.getNuOfDimensions();i++){
            assertTrue(cap.get(i) == 0);
        }
    }

    @Test
    public void timeWindowViolation_shouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals(0.,violation,0.01);
    }

    @Test
    public void timeWindowViolation_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolation(route);
        assertEquals((2+26+57+77+90+114+144+20),violation,0.01);
    }

    @Test
    public void timeWindowViolationAtStart_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getStart(), route);
        assertEquals(0.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct1_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(0),route);
        assertEquals(0.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct2_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(1),route);
        assertEquals(2.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct3_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(2),route);
        assertEquals(26.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct4_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(3),route);
        assertEquals(57.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct5_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(4),route);
        assertEquals(77.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct6_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(5),route);
        assertEquals(90.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct7_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(6),route);
        assertEquals(114.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtAct8_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getActivities().get(7),route);
        assertEquals(144.,violation,0.01);
    }

    @Test
    public void timeWindowViolationAtEnd_shouldWorkWhenViolated(){
        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);

        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        VehicleRoute route = solution.getRoutes().iterator().next();
        Double violation = analyser.getTimeWindowViolationAtActivity(route.getEnd(),route);
        assertEquals(20.,violation,0.01);
    }

    @Test
    public void testRealViolation(){
        assertFalse(true);
    }



    public void buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore(){
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").setFixedCost(100.).setCostPerDistance(2.).addCapacityDimension(0, 15).build();

        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v1").setType(type)
                .setStartLocationCoordinate(Coordinate.newInstance(-5, 0))
                .setLatestArrival(150.)
                .build();

        Pickup s1 = (Pickup) Pickup.Builder.newInstance("s1")
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .setCoord(Coordinate.newInstance(-10, 1))
                .addSizeDimension(0, 10)
                .build();
        Delivery s2 = (Delivery) Delivery.Builder.newInstance("s2")
                .setCoord(Coordinate.newInstance(-10, 10))
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .addSizeDimension(0, 20)
                .build();
        Shipment shipment1 = Shipment.Builder.newInstance("ship1").setPickupCoord(Coordinate.newInstance(-15, 2)).setDeliveryCoord(Coordinate.newInstance(-16, 5))
                .addSizeDimension(0, 15)
                .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
                .setPickupTimeWindow(TimeWindow.newInstance(10,20)).setDeliveryTimeWindow(TimeWindow.newInstance(10,20))
                .build();

        Pickup s3 = (Pickup) Pickup.Builder.newInstance("s3")
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .setCoord(Coordinate.newInstance(10, 1))
                .addSizeDimension(0, 10)
                .build();
        Delivery s4 = (Delivery) Delivery.Builder.newInstance("s4").setCoord(Coordinate.newInstance(10, 10))
                .addSizeDimension(0, 20)
                .setTimeWindow(TimeWindow.newInstance(10, 20))
                .build();
        Shipment shipment2 = Shipment.Builder.newInstance("ship2").setPickupCoord(Coordinate.newInstance(15, 2))
                .setPickupServiceTime(20.).setDeliveryServiceTime(20.)
                .setDeliveryCoord(Coordinate.newInstance(16, 5))
                .setPickupTimeWindow(TimeWindow.newInstance(10, 20)).setDeliveryTimeWindow(TimeWindow.newInstance(10, 20))
                .addSizeDimension(0, 15).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle)
                .addJob(s1)
                .addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();

        //adds updater
        stateManager = new StateManager(vrp);
        stateManager.updateLoadStates();
        stateManager.updateTimeWindowStates();

        //but no constraints to simulate violation
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setMaxIterations(100);
        solution = Solutions.bestOf(vra.searchSolutions());

    }
//
//    public void simulateTW_and_LoadConstraint(){
//        constraintManager.addLoadConstraint();
//        constraintManager.addTimeWindowConstraint();
//    }

//    @Test
//    public void violatedConstraint_shouldReturnTWandCapConstraints(){
//        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
//
//        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
//
//        simulateTW_and_LoadConstraint();
//
//        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
//            @Override
//            public double getDistance(String fromLocationId, String toLocationId) {
//                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
//            }
//        });
//        VehicleRoute route = solution.getRoutes().iterator().next();
//        List<HardRouteConstraint> violatedConstraints = analyser.getViolatedHardConstraints(route).getHardRouteConstraints();
////        Assert.assertEquals(2,violatedConstraints.size());
//        for(HardRouteConstraint c : violatedConstraints) System.out.println("violated: " + c.getClass().toString());
//    }
//
//    @Test
//    public void violatedActivityConstraint_shouldWorkForAct1(){
//        buildAnotherScenarioWithOnlyOneVehicleAndWithoutAnyConstraintsBefore();
//
//        SolutionPrinter.print(vrp,solution, SolutionPrinter.Print.VERBOSE);
//
//        simulateTW_and_LoadConstraint();
//
//        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution, new SolutionAnalyser.DistanceCalculator() {
//            @Override
//            public double getDistance(String fromLocationId, String toLocationId) {
//                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
//            }
//        });
//
//        VehicleRoute route = solution.getRoutes().iterator().next();
//        List<HardActivityConstraint> violatedConstraints = analyser.getViolatedHardConstraintsAtActivity(route.getActivities().get(0),route).getHardActivityConstraints();
////        Assert.assertEquals(2,violatedConstraints.size());
//        for(HardActivityConstraint c : violatedConstraints) System.out.println("violated: " + c.getClass().toString());
//    }

}
