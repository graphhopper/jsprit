package com.graphhopper.jsprit.core.algorithm;

import com.graphhopper.jsprit.core.IntegrationTest;
import com.graphhopper.jsprit.core.algorithm.box.Jsprit;
import com.graphhopper.jsprit.core.algorithm.state.StateManager;
import com.graphhopper.jsprit.core.analysis.SolutionAnalyser;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.constraint.ConstraintManager;
import com.graphhopper.jsprit.core.problem.cost.TransportDistance;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.SolutionCostCalculator;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.util.CostFactory;
import com.graphhopper.jsprit.core.util.Solutions;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

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
    public void doBefore() {
        activityCosts = new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return vehicle.getType().getVehicleCostParams().perWaitingTimeUnit * Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime);
            }

            @Override
            public double getActivityDuration(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                return tourAct.getOperationTime();
            }

        };
        algorithmFactory = new AlgorithmFactory() {
            @Override
            public VehicleRoutingAlgorithm createAlgorithm(final VehicleRoutingProblem vrp) {
                StateManager stateManager = new StateManager(vrp);
                ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);

                return Jsprit.Builder.newInstance(vrp)
                    .addCoreStateAndConstraintStuff(true)
                    .setStateAndConstraintManager(stateManager, constraintManager)
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
    @Category(IntegrationTest.class)
    public void plainSetupShouldWork() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(20, 0)).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2).addVehicle(v)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .setRoutingCost(CostFactory.createManhattanCosts())
            .setActivityCosts(activityCosts)
            .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40., solution.getCost());
    }

    @Test
    @Category(IntegrationTest.class)
    public void withTimeWindowsShouldWork() {
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        Service s1 = Service.Builder.newInstance("s1").setTimeWindow(TimeWindow.newInstance(1010, 1100)).setLocation(Location.newInstance(10, 0)).build();
        Service s2 = Service.Builder.newInstance("s2").setTimeWindow(TimeWindow.newInstance(1020, 1100)).setLocation(Location.newInstance(20, 0)).build();
        final VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance()
            .addJob(s1).addJob(s2).addVehicle(v)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE)
            .setRoutingCost(CostFactory.createManhattanCosts())
            .setActivityCosts(activityCosts)
            .build();
        VehicleRoutingAlgorithm vra = algorithmFactory.createAlgorithm(vrp);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(vra.searchSolutions());
        Assert.assertEquals(40. + 1000., solution.getCost());
    }


}
