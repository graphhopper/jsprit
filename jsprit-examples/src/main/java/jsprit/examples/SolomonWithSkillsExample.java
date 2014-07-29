package jsprit.examples;


import jsprit.analysis.toolbox.Plotter;
import jsprit.analysis.toolbox.SolutionPrinter;
import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.state.StateManager;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.constraint.ConstraintManager;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
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
            VehicleImpl skill1Vehicle = VehicleImpl.Builder.newInstance("skill1_vehicle_"+i).addSkill("skill1")
                    .setStartLocationCoordinate(solomonVehicle.getStartLocationCoordinate()).setStartLocationId(solomonVehicle.getStartLocationId())
                    .setEarliestStart(solomonVehicle.getEarliestDeparture())
                    .setType(newType).build();
            VehicleImpl skill2Vehicle = VehicleImpl.Builder.newInstance("skill2_vehicle_"+i).addSkill("skill2")
                    .setStartLocationCoordinate(solomonVehicle.getStartLocationCoordinate()).setStartLocationId(solomonVehicle.getStartLocationId())
                    .setEarliestStart(solomonVehicle.getEarliestDeparture())
                    .setType(newType).build();
            skillProblemBuilder.addVehicle(skill1Vehicle).addVehicle(skill2Vehicle);
        }
        for(Job job : vrp.getJobs().values()){
            Service service = (Service) job;
            Service.Builder skillServiceBuilder;
            if(service.getCoord().getY()<50.){
                skillServiceBuilder = Service.Builder.newInstance(service.getId()+"_skill2").setServiceTime(service.getServiceDuration())
                .setCoord(service.getCoord()).setLocationId(service.getLocationId()).setTimeWindow(service.getTimeWindow())
                        .addSizeDimension(0,service.getSize().get(0));
                skillServiceBuilder.addSkill("skill2");
            }
            else {
                skillServiceBuilder = Service.Builder.newInstance(service.getId()+"_skill1").setServiceTime(service.getServiceDuration())
                        .setCoord(service.getCoord()).setLocationId(service.getLocationId()).setTimeWindow(service.getTimeWindow())
                        .addSizeDimension(0,service.getSize().get(0));
                skillServiceBuilder.addSkill("skill1");
            }
            skillProblemBuilder.addJob(skillServiceBuilder.build());
        }
        skillProblemBuilder.addPenaltyVehicles(3.,100.);
        skillProblemBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem skillProblem = skillProblemBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem,"input/algorithmConfig_solomon.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();

        StateManager stateManager = new StateManager(skillProblem);
        stateManager.updateSkillStates();

        ConstraintManager constraintManager = new ConstraintManager(skillProblem,stateManager);
        constraintManager.addSkillsConstraint();

        VehicleRoutingAlgorithm vra = vraBuilder.build();
//        vra.setNuOfIterations(500);


        Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
        VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);

        SolutionPrinter.print(skillProblem, solution, SolutionPrinter.Print.VERBOSE);

        new Plotter(skillProblem,solution).plot("output/skill_solution","solomon_with_skills");
    }
}
