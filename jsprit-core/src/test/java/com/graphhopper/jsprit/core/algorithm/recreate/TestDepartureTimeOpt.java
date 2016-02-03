/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.algorithm.VehicleRoutingAlgorithm;
import com.graphhopper.jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.cost.VehicleRoutingActivityCosts;
import com.graphhopper.jsprit.core.problem.driver.Driver;
import com.graphhopper.jsprit.core.problem.job.Service;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TimeWindow;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;
import com.graphhopper.jsprit.core.problem.vehicle.Vehicle;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleImpl;
import com.graphhopper.jsprit.core.problem.vehicle.VehicleTypeImpl;
import com.graphhopper.jsprit.core.util.Coordinate;
import com.graphhopper.jsprit.core.util.Solutions;
import com.graphhopper.jsprit.core.util.TestUtils;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Collection;

@Ignore
public class TestDepartureTimeOpt {

    @Test
    public void whenSettingOneCustWithTWAnd_NO_DepTimeChoice_totalCostsShouldBe50() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s").setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(TestUtils.loc("vehLoc", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(20.0 + 30.0, Solutions.bestOf(solutions).getCost(), 0.1);

    }

    @Test
    public void whenSettingOneCustWithTWAnd_NO_DepTimeChoice_depTimeShouldBe0() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s")
            .setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.Builder.newInstance().setId("vehLoc").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addVehicle(vehicle).build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfig.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(0.0, Solutions.bestOf(solutions).getRoutes().iterator().next().getStart().getEndTime(), 0.1);

    }

    @Test
    public void whenSettingOneCustWithTWAndDepTimeChoice_totalCostsShouldBe50() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s").setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(TestUtils.loc("vehLoc", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addVehicle(vehicle).build();


        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(20.0, Solutions.bestOf(solutions).getCost(), 0.1);

    }

    @Test
    public void whenSettingOneCustWithTWAndDepTimeChoice_depTimeShouldBe0() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s").setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();
        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(TestUtils.loc("vehLoc", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addVehicle(vehicle).build();


        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(30.0, Solutions.bestOf(solutions).getRoutes().iterator().next().getStart().getEndTime(), 0.1);

    }

    @Test
    public void whenSettingTwoCustWithTWAndDepTimeChoice_totalCostsShouldBe50() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s").setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();

        Service service2 = Service.Builder.newInstance("s2").setLocation(TestUtils.loc("servLoc2", Coordinate.newInstance(0, 20))).
            setTimeWindow(TimeWindow.newInstance(30, 40)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(TestUtils.loc("vehLoc", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addJob(service2).addVehicle(vehicle).build();


        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(40.0, Solutions.bestOf(solutions).getCost(), 0.1);

    }

    @Test
    public void whenSettingTwoCustWithTWAndDepTimeChoice_depTimeShouldBe10() {
        TimeWindow timeWindow = TimeWindow.newInstance(40, 45);
        Service service = Service.Builder.newInstance("s").setLocation(TestUtils.loc("servLoc", Coordinate.newInstance(0, 10))).setTimeWindow(timeWindow).build();

        Service service2 = Service.Builder.newInstance("s2").setLocation(TestUtils.loc("servLoc2", Coordinate.newInstance(0, 20))).
            setTimeWindow(TimeWindow.newInstance(30, 40)).build();

        Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(TestUtils.loc("vehLoc", Coordinate.newInstance(0, 0)))
            .setType(VehicleTypeImpl.Builder.newInstance("vType").build()).build();

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        vrpBuilder.setActivityCosts(new VehicleRoutingActivityCosts() {

            @Override
            public double getActivityCost(TourActivity tourAct, double arrivalTime, Driver driver, Vehicle vehicle) {
                double waiting = Math.max(0, tourAct.getTheoreticalEarliestOperationStartTime() - arrivalTime) * 1;
                double late = Math.max(0, arrivalTime - tourAct.getTheoreticalLatestOperationStartTime()) * 100;
                return waiting + late;
            }

        });
        VehicleRoutingProblem vrp = vrpBuilder.addJob(service).addJob(service2).addVehicle(vehicle).build();


        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/algorithmConfigWithDepartureTimeChoice.xml");
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        Assert.assertEquals(10.0, Solutions.bestOf(solutions).getRoutes().iterator().next().getStart().getEndTime(), 0.1);

    }

}
