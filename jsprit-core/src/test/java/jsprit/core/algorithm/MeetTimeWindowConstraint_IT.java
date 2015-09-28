/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.algorithm;

import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.algorithm.recreate.listener.JobInsertedListener;
import jsprit.core.algorithm.recreate.listener.VehicleSwitchedListener;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.util.FastVehicleRoutingTransportCostsMatrix;
import jsprit.core.util.Solutions;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MeetTimeWindowConstraint_IT {

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_nRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        System.out.println("failed " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job2CanNeverBeInVehicle21() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

//		assertEquals(2,Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

//		assertEquals(2,Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }


    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_nRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        System.out.println("failed " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job2CanNeverBeInVehicle21() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/schrimpf_vehicleSwitchNotAllowed.xml");
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }


    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_nRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        System.out.println("failed " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_job2CanNeverBeInVehicle21() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

//		assertEquals(2,Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.createAlgorithm(vrp);
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

//		assertEquals(2,Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }


    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_jsprit_and_vehicleSwitchIsNotAllowed_nRoutesShouldBeCorrect() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_jsprit_certainJobsCanNeverBeAssignedToCertainVehicles() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new JobInsertedListener() {

            @Override
            public void informJobInserted(Job job2insert, VehicleRoute inRoute, double additionalCosts, double additionalTime) {
                if (job2insert.getId().equals("1")) {
                    if (inRoute.getVehicle().getId().equals("19")) {
                        testFailed.add(true);
                    }
                }
                if (job2insert.getId().equals("2")) {
                    if (inRoute.getVehicle().getId().equals("21")) {
                        testFailed.add(true);
                    }
                }
            }

        });
        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_jsprit_certainVehiclesCanNeverBeAssignedToCertainRoutes() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        final List<Boolean> testFailed = new ArrayList<Boolean>();
        vra.addListener(new VehicleSwitchedListener() {

            @Override
            public void vehicleSwitched(VehicleRoute vehicleRoute, Vehicle oldVehicle, Vehicle newVehicle) {
                if (oldVehicle == null) return;
                if (oldVehicle.getId().equals("21") && newVehicle.getId().equals("19")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("1")) {
                            testFailed.add(true);
                        }
                    }
                }
                if (oldVehicle.getId().equals("19") && newVehicle.getId().equals("21")) {
                    for (Job j : vehicleRoute.getTourActivities().getJobs()) {
                        if (j.getId().equals("2")) {
                            testFailed.add(true);
                        }
                    }
                }
            }

        });


        @SuppressWarnings("unused")
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        System.out.println("failed " + testFailed.size());
        assertTrue(testFailed.isEmpty());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_jsprit_job2CanNeverBeInVehicle21() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_jsprit_job1ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("1"), getRoute("21", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenEmployingVehicleWithDifferentWorkingShifts_and_vehicleSwitchIsNotAllowed_jsprit_job2ShouldBeAssignedCorrectly() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/simpleProblem.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(vrp).setProperty(Jsprit.Parameter.VEHICLE_SWITCH, "false").buildAlgorithm();
        vra.setMaxIterations(100);
        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();

        assertEquals(2, Solutions.bestOf(solutions).getRoutes().size());
        assertTrue(containsJob(vrp.getJobs().get("2"), getRoute("19", Solutions.bestOf(solutions))));
    }

    @Test
    public void whenUsingJsprit_driverTimesShouldBeMet() throws IOException {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/twbug.xml");
        final FastVehicleRoutingTransportCostsMatrix matrix = createMatrix();
        vrpBuilder.setRoutingCost(matrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = Jsprit.Builder.newInstance(vrp).buildAlgorithm();
        algorithm.setMaxIterations(1000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        for (VehicleRoute r : solution.getRoutes()) {
            assertTrue(r.getVehicle().getEarliestDeparture() <= r.getDepartureTime());
            assertTrue(r.getVehicle().getLatestArrival() >= r.getEnd().getArrTime());
        }
    }

    @Test
    public void whenUsingSchrimpf_driverTimesShouldBeMet() throws IOException {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/twbug.xml");
        final FastVehicleRoutingTransportCostsMatrix matrix = createMatrix();
        vrpBuilder.setRoutingCost(matrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = new SchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(1000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        for (VehicleRoute r : solution.getRoutes()) {
            assertTrue(r.getVehicle().getEarliestDeparture() <= r.getDepartureTime());
            assertTrue(r.getVehicle().getLatestArrival() >= r.getEnd().getArrTime());
        }
    }

    @Test
    public void whenUsingGreedySchrimpf_driverTimesShouldBeMet() throws IOException {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/twbug.xml");
        final FastVehicleRoutingTransportCostsMatrix matrix = createMatrix();
        vrpBuilder.setRoutingCost(matrix);
        VehicleRoutingProblem vrp = vrpBuilder.build();
        VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
        algorithm.setMaxIterations(1000);
        VehicleRoutingProblemSolution solution = Solutions.bestOf(algorithm.searchSolutions());
        for (VehicleRoute r : solution.getRoutes()) {
            assertTrue(r.getVehicle().getEarliestDeparture() <= r.getDepartureTime());
            assertTrue(r.getVehicle().getLatestArrival() >= r.getEnd().getArrTime());
        }
    }


    private FastVehicleRoutingTransportCostsMatrix createMatrix() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(new File("src/test/resources/matrix.txt")));
        String line;
        FastVehicleRoutingTransportCostsMatrix.Builder builder = FastVehicleRoutingTransportCostsMatrix.Builder.newInstance(11, false);
        while ((line = reader.readLine()) != null) {
            String[] split = line.split("\t");
            builder.addTransportDistance(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[2]));
            builder.addTransportTime(Integer.parseInt(split[0]), Integer.parseInt(split[1]), Double.parseDouble(split[3]));
        }
        return builder.build();
    }


    private boolean containsJob(Job job, VehicleRoute route) {
        if (route == null) return false;
        for (Job j : route.getTourActivities().getJobs()) {
            if (job == j) {
                return true;
            }
        }
        return false;
    }

    private VehicleRoute getRoute(String vehicleId, VehicleRoutingProblemSolution vehicleRoutingProblemSolution) {
        for (VehicleRoute r : vehicleRoutingProblemSolution.getRoutes()) {
            if (r.getVehicle().getId().equals(vehicleId)) {
                return r;
            }
        }
        return null;
    }

}
