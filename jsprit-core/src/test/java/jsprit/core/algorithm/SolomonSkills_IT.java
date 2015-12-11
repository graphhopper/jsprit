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

import jsprit.core.IntegrationTest;
import jsprit.core.algorithm.recreate.NoSolutionFoundException;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Skills;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.util.Solutions;
import jsprit.core.util.TestUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * to test skills with penalty vehicles
 */
public class SolomonSkills_IT {

    @Test
    @Category(IntegrationTest.class)
    public void itShouldMakeCorrectAssignmentAccordingToSkills() {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/solomon_c101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //y >= 50 skill1 otherwise skill2
        //two vehicles: v1 - skill1 #5; v2 - skill2 #6
        Vehicle solomonVehicle = vrp.getVehicles().iterator().next();
        VehicleType newType = solomonVehicle.getType();
        VehicleRoutingProblem.Builder skillProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        for (int i = 0; i < 6; i++) {
            VehicleImpl skill1Vehicle = VehicleImpl.Builder.newInstance("skill1_vehicle_" + i).addSkill("skill1")
                .setStartLocation(TestUtils.loc(solomonVehicle.getStartLocation().getId(), solomonVehicle.getStartLocation().getCoordinate()))
                .setEarliestStart(solomonVehicle.getEarliestDeparture())
                .setType(newType).build();
            VehicleImpl skill2Vehicle = VehicleImpl.Builder.newInstance("skill2_vehicle_" + i).addSkill("skill2")
                .setStartLocation(TestUtils.loc(solomonVehicle.getStartLocation().getId(), solomonVehicle.getStartLocation().getCoordinate()))
                .setEarliestStart(solomonVehicle.getEarliestDeparture())
                .setType(newType).build();
            skillProblemBuilder.addVehicle(skill1Vehicle).addVehicle(skill2Vehicle);
        }
        for (Job job : vrp.getJobs().values()) {
            Service service = (Service) job;
            Service.Builder skillServiceBuilder = Service.Builder.newInstance(service.getId()).setServiceTime(service.getServiceDuration())
                .setLocation(TestUtils.loc(service.getLocation().getId(), service.getLocation().getCoordinate())).setTimeWindow(service.getTimeWindow())
                .addSizeDimension(0, service.getSize().get(0));
            if (service.getLocation().getCoordinate().getY() < 50) skillServiceBuilder.addRequiredSkill("skill2");
            else skillServiceBuilder.addRequiredSkill("skill1");
            skillProblemBuilder.addJob(skillServiceBuilder.build());
        }
        skillProblemBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem skillProblem = skillProblemBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem, "src/test/resources/algorithmConfig.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();

        StateManager stateManager = new StateManager(skillProblem);
        stateManager.updateSkillStates();

        ConstraintManager constraintManager = new ConstraintManager(skillProblem, stateManager);
        constraintManager.addSkillsConstraint();

        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setMaxIterations(2000);

        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
            assertEquals(828.94, solution.getCost(), 0.01);
            for (VehicleRoute route : solution.getRoutes()) {
                Skills vehicleSkill = route.getVehicle().getSkills();
                for (Job job : route.getTourActivities().getJobs()) {
                    for (String skill : job.getRequiredSkills().values()) {
                        if (!vehicleSkill.containsSkill(skill)) {
                            assertFalse(true);
                        }
                    }
                }
            }
            assertTrue(true);
        } catch (NoSolutionFoundException e) {
            System.out.println(e.toString());
            assertFalse(true);
        }
    }
}
