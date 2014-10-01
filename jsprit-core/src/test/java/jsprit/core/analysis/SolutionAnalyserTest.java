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
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.ManhattanCosts;
import jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;

public class SolutionAnalyserTest {

    private VehicleRoutingProblem vrp;

    private VehicleRoutingProblemSolution solution;

    private StateManager stateManager;

    private SolutionAnalyser.DistanceCalculator distanceCalculator;

    @Before
    public void doBefore(){

        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0,15).build();

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
        
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addVehicle(vehicle2).addJob(s1)
                .addJob(s2).addJob(shipment1).addJob(s3).addJob(s4).addJob(shipment2).setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        vrpBuilder.setRoutingCost(new ManhattanCosts(vrpBuilder.getLocations()));
        vrp = vrpBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(vrp,"src/test/resources/algorithmConfig.xml");
        vraBuilder.addDefaultCostCalculators();
        vraBuilder.addCoreConstraints();
        stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp,stateManager);
        vraBuilder.setStateAndConstraintManager(stateManager,constraintManager);

        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setMaxIterations(100);
        solution = Solutions.bestOf(vra.searchSolutions());

    }

    @Test
    public void constructionShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
            @Override
            public double getDistance(String fromLocationId, String toLocationId) {
                return vrp.getTransportCosts().getTransportCost(fromLocationId,toLocationId,0.,null,null);
            }
        });
        Assert.assertTrue(true);
    }

    @Test
    public void loadAtBeginningOfRoute1ShouldWork(){
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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
        SolutionAnalyser analyser = new SolutionAnalyser(vrp,solution.getRoutes(),stateManager,new SolutionAnalyser.DistanceCalculator() {
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



}
