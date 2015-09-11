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

package jsprit.core.algorithm;


import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.box.Jsprit.Builder;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateEndLocationIfRouteIsOpen;
import jsprit.core.algorithm.state.UpdateVariableCosts;
import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.constraint.ConstraintManager.Priority;
import jsprit.core.problem.constraint.ServiceLoadActivityLevelConstraint;
import jsprit.core.problem.constraint.ServiceLoadRouteLevelConstraint;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.activity.TourActivity;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

import static org.junit.Assert.*;

public class InitialRoutesTest {

    @Test
    public void whenReading_jobMapShouldOnlyContainJob2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReadingProblem2_jobMapShouldContain_service2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuServices(vrp));
        assertTrue(vrp.getJobs().containsKey("2"));
    }

    @Test
    public void whenReading_jobMapShouldContain_shipment4() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, getNuShipments(vrp));
        assertTrue(vrp.getJobs().containsKey("4"));
    }

    private int getNuShipments(VehicleRoutingProblem vrp) {
        int nuShipments = 0;
        for (Job job : vrp.getJobs().values()) {
            if (job instanceof Shipment) nuShipments++;
        }
        return nuShipments;
    }

    private int getNuServices(VehicleRoutingProblem vrp) {
        int nuServices = 0;
        for (Job job : vrp.getJobs().values()) {
            if (job instanceof Service) nuServices++;
        }
        return nuServices;
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeOnlyOneActAssociatedToJob2_v2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        assertEquals(1, vrp.getActivities(vrp.getJobs().get("2")).size());
    }

    @Test
    public void whenReading_thereShouldBeTwoActsAssociatedToShipment4() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        Job job = vrp.getJobs().get("4");
        List<AbstractActivity> activities = vrp.getActivities(job);

        assertEquals(2, activities.size());
    }

    @Test
    public void whenSolving_nuJobsInSolutionShouldBe2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(2, solution.getRoutes().iterator().next().getTourActivities().getJobs().size());
    }

    @Test
    public void whenSolvingProblem2_nuJobsInSolutionShouldBe4() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        int jobsInSolution = 0;
        for (VehicleRoute r : solution.getRoutes()) {
            jobsInSolution += r.getTourActivities().jobSize();
        }
        assertEquals(4, jobsInSolution);
    }

    @Test
    public void whenSolving_nuActsShouldBe2() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        assertEquals(2, solution.getRoutes().iterator().next().getActivities().size());
    }

    @Test
    public void whenSolvingProblem2_nuActsShouldBe6() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        int nuActs = 0;
        for (VehicleRoute r : solution.getRoutes()) {
            nuActs += r.getActivities().size();
        }
        assertEquals(6, nuActs);
    }

    @Test
    public void whenSolving_deliverService1_shouldBeInRoute() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        Job job = getInitialJob("1", vrp);
        assertTrue(hasActivityIn(solution, "veh1", job));
    }

    private Job getInitialJob(String jobId, VehicleRoutingProblem vrp) {
        for (VehicleRoute r : vrp.getInitialVehicleRoutes()) {
            for (Job j : r.getTourActivities().getJobs()) {
                if (j.getId().equals(jobId)) return j;
            }
        }
        return null;
    }

    @Test
    public void whenSolvingWithJsprit_deliverService1_shouldBeInRoute() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes_3.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(vrp, solution, SolutionPrinter.Print.VERBOSE);

        Job job = getInitialJob("1", vrp);
        assertTrue(hasActivityIn(solution, "veh1", job));
    }

    @Test
    public void whenSolvingProblem2With_deliverServices_and_allShipmentActs_shouldBeInRoute() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes(), "1"));
        assertTrue(hasActivityIn(solution.getRoutes(), "2"));
        assertTrue(hasActivityIn(solution.getRoutes(), "3"));
        assertTrue(hasActivityIn(solution.getRoutes(), "4"));

        assertTrue(hasActivityIn(solution, "veh1", getInitialJob("1", vrp)));
        assertTrue(hasActivityIn(solution, "veh2", getInitialJob("3", vrp)));
    }

    @Test
    public void whenSolvingProblem2WithJsprit_deliverServices_and_allShipmentActs_shouldBeInRoute() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_inclShipments_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes(), "1"));
        assertTrue(hasActivityIn(solution.getRoutes(), "2"));
        assertTrue(hasActivityIn(solution.getRoutes(), "3"));
        assertTrue(hasActivityIn(solution.getRoutes(), "4"));

        assertTrue(hasActivityIn(solution, "veh1", getInitialJob("1", vrp)));
        assertTrue(hasActivityIn(solution, "veh2", getInitialJob("3", vrp)));
    }

    private boolean hasActivityIn(Collection<VehicleRoute> routes, String jobId) {
        boolean isInRoute = false;
        for (VehicleRoute route : routes) {
            for (TourActivity act : route.getActivities()) {
                if (act instanceof TourActivity.JobActivity) {
                    if (((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
                }
            }
        }
        return isInRoute;
    }

    private boolean hasActivityIn(VehicleRoutingProblemSolution solution, String vehicleId, Job job) {
        for (VehicleRoute route : solution.getRoutes()) {
            String vehicleId_ = route.getVehicle().getId();
            if (vehicleId_.equals(vehicleId)) {
                if (route.getTourActivities().servesJob(job)) {
                    return true;
                }
            }
        }
        return false;
    }


    private boolean hasActivityIn(VehicleRoute route, String jobId) {
        boolean isInRoute = false;
        for (TourActivity act : route.getActivities()) {
            if (act instanceof TourActivity.JobActivity) {
                if (((TourActivity.JobActivity) act).getJob().getId().equals(jobId)) isInRoute = true;
            }
        }
        return isInRoute;
    }

    @Test
    public void whenSolving_deliverService2_shouldBeInRoute() {

        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        assertTrue(hasActivityIn(solution.getRoutes().iterator().next(), "2"));
    }

    @Test
    public void maxCapacityShouldNotBeExceeded() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("type").addCapacityDimension(0, 100).build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("veh")
            .setStartLocation(Location.Builder.newInstance().setId("start").setCoordinate(Coordinate.newInstance(0, 0)).build())
            .setType(type)
            .build();

        Shipment shipment = Shipment.Builder.newInstance("s")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).setId("pick").build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del").setCoordinate(Coordinate.newInstance(0, 10)).build())
            .addSizeDimension(0, 100)
            .build();

        Shipment another_shipment = Shipment.Builder.newInstance("another_s")
            .setPickupLocation(Location.Builder.newInstance().setCoordinate(Coordinate.newInstance(10, 0)).setId("pick").build())
            .setDeliveryLocation(Location.Builder.newInstance().setId("del").setCoordinate(Coordinate.newInstance(0, 10)).build())
            .addSizeDimension(0, 50)
            .build();

        VehicleRoute iniRoute = VehicleRoute.Builder.newInstance(vehicle).addPickup(shipment).addDelivery(shipment).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(shipment).addVehicle(vehicle).addJob(another_shipment)
            .setFleetSize(VehicleRoutingProblem.FleetSize.FINITE).addInitialVehicleRoute(iniRoute).build();

        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertFalse(secondActIsPickup(solutions));

    }

    @Test
    public void whenReadingProblemFromFile_maxCapacityShouldNotBeExceeded() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem_iniRoutes_2.xml");
        VehicleRoutingAlgorithm vra = new GreedySchrimpfFactory().createAlgorithm(vrpBuilder.build());
        vra.setMaxIterations(10);

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertFalse(secondActIsPickup(solutions));

    }

    private boolean secondActIsPickup(Collection<VehicleRoutingProblemSolution> solutions) {
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
        TourActivity secondAct = solution.getRoutes().iterator().next().getActivities().get(1);
        return secondAct instanceof PickupShipment;
    }

    @Test
    public void whenAllJobsInInitialRoute_itShouldWork() {
        Service s = Service.Builder.newInstance("s").setLocation(Location.newInstance(0, 10)).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance(0, 0)).build();
        VehicleRoute iniRoute = VehicleRoute.Builder.newInstance(v).addService(s).build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addInitialVehicleRoute(iniRoute).build();
        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        vra.searchSolutions();
        assertTrue(true);
    }

    @Test
    public void buildWithoutTimeConstraints() {
        Service s1 = Service.Builder.newInstance("s1").setLocation(Location.newInstance(0, 10)).addSizeDimension(0, 10).build();
        Service s2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance(10, 20)).addSizeDimension(0, 12).build();

        VehicleTypeImpl vt = VehicleTypeImpl.Builder.newInstance("vt").addCapacityDimension(0, 15).build();
        VehicleImpl v = VehicleImpl.Builder.newInstance("v").setType(vt).setStartLocation(Location.newInstance(0, 0)).build();

        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(s1).addJob(s2).addVehicle(v).build();
        Builder algBuilder = Jsprit.Builder.newInstance(vrp).addCoreStateAndConstraintStuff(false);

        // only required constraints
        StateManager stateManager = new StateManager(vrp);
        ConstraintManager constraintManager = new ConstraintManager(vrp, stateManager);
        constraintManager.addConstraint(new ServiceLoadRouteLevelConstraint(stateManager));
        constraintManager.addConstraint(new ServiceLoadActivityLevelConstraint(stateManager), Priority.LOW);
        stateManager.updateLoadStates();
        stateManager.addStateUpdater(new UpdateEndLocationIfRouteIsOpen());
        stateManager.addStateUpdater(new UpdateVariableCosts(vrp.getActivityCosts(), vrp.getTransportCosts(), stateManager));

        algBuilder.setStateAndConstraintManager(stateManager, constraintManager);
        VehicleRoutingAlgorithm vra = algBuilder.buildAlgorithm();
        vra.setMaxIterations(20);
        Collection<VehicleRoutingProblemSolution> searchSolutions = vra.searchSolutions();
        VehicleRoutingProblemSolution bestOf = Solutions.bestOf(searchSolutions);

        //ensure 2 routes
        assertEquals(2, bestOf.getRoutes().size());

        //ensure no time information in first service of first route
        assertEquals(0, bestOf.getRoutes().iterator().next().getActivities().iterator().next().getArrTime(), 0.001);
    }
}
