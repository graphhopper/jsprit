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

package jsprit.examples;


import jsprit.analysis.toolbox.Plotter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.box.Jsprit;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.Location;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.io.VrpXMLWriter;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.reporting.SolutionPrinter;
import jsprit.core.util.Solutions;
import jsprit.instance.reader.SolomonReader;

import java.util.Collection;

public class SolomonWithSkillsExample {

    public static void main(String[] args) {
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new SolomonReader(vrpBuilder).read("input/C101_solomon.txt");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //y >= 50 skill1 otherwise skill2
        //two vehicles: v1 - skill1 #5; v2 - skill2 #6
        Vehicle solomonVehicle = vrp.getVehicles().iterator().next();
        VehicleType newType = solomonVehicle.getType();
        VehicleRoutingProblem.Builder skillProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i=0;i<5;i++) {
            VehicleImpl skill1Vehicle = VehicleImpl.Builder.newInstance("skill1_vehicle_" + i).addSkill("skill1")
                    .setStartLocation(Location.Builder.newInstance().setId(solomonVehicle.getStartLocation().getId()).setCoordinate(solomonVehicle.getStartLocation().getCoordinate()).build())
                    .setEarliestStart(solomonVehicle.getEarliestDeparture())
                    .setType(newType).build();
            VehicleImpl skill2Vehicle = VehicleImpl.Builder.newInstance("skill2_vehicle_" + i).addSkill("skill2")
                    .setStartLocation(Location.Builder.newInstance().setId(solomonVehicle.getStartLocation().getId())
                            .setCoordinate(solomonVehicle.getStartLocation().getCoordinate()).build())
                    .setEarliestStart(solomonVehicle.getEarliestDeparture())
                    .setType(newType).build();
            skillProblemBuilder.addVehicle(skill1Vehicle).addVehicle(skill2Vehicle);
        }
        for(Job job : vrp.getJobs().values()){
            Service service = (Service) job;
            Service.Builder skillServiceBuilder;
            if(service.getLocation().getCoordinate().getY()<50.){
                skillServiceBuilder = Service.Builder.newInstance(service.getId() + "_skill2").setServiceTime(service.getServiceDuration())
                        .setLocation(Location.Builder.newInstance().setId(service.getLocation().getId())
                                .setCoordinate(service.getLocation().getCoordinate()).build()).setTimeWindow(service.getTimeWindow())
                        .addSizeDimension(0, service.getSize().get(0));
                skillServiceBuilder.addRequiredSkill("skill2");
            }
            else {
                skillServiceBuilder = Service.Builder.newInstance(service.getId()+"_skill1").setServiceTime(service.getServiceDuration())
                        .setLocation(
                                Location.Builder.newInstance().setId(service.getLocation().getId())
                                        .setCoordinate(service.getLocation().getCoordinate()).build()
                        ).setTimeWindow(service.getTimeWindow())
                        .addSizeDimension(0,service.getSize().get(0));
                skillServiceBuilder.addRequiredSkill("skill1");
            }
            skillProblemBuilder.addJob(skillServiceBuilder.build());
        }
        skillProblemBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem skillProblem = skillProblemBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem,"input/algorithmConfig_solomon.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();

        StateManager stateManager = new StateManager(skillProblem);
        stateManager.updateSkillStates();

        ConstraintManager constraintManager = new ConstraintManager(skillProblem,stateManager);
        constraintManager.addSkillsConstraint();

        VehicleRoutingAlgorithm vra = Jsprit.Builder.newInstance(skillProblem).setStateAndConstraintManager(stateManager,constraintManager).buildAlgorithm();

        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(skillProblem, solution, SolutionPrinter.Print.VERBOSE);

        new Plotter(skillProblem,solution).plot("output/skill_solution","solomon_with_skills");

        new VrpXMLWriter(skillProblem,solutions).write("output/solomon_with_skills");
    }
}
