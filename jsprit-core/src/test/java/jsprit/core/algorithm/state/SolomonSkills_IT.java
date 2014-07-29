package jsprit.core.algorithm.state;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.VehicleRoutingAlgorithmBuilder;
import jsprit.core.algorithm.recreate.NoSolutionFoundException;
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
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

/**
 * to test skills with penalty vehicles
 */
public class SolomonSkills_IT {

    @Test
    public void itShouldMakeCorrectAssignmentAccordingToSkills(){
        VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
        new VrpXMLReader(vrpBuilder).read("src/test/resources/solomon_c101.xml");
        VehicleRoutingProblem vrp = vrpBuilder.build();

        //y >= 50 skill1 otherwise skill2
        //two vehicles: v1 - skill1 #5; v2 - skill2 #6
        Vehicle solomonVehicle = vrp.getVehicles().iterator().next();
        VehicleType newType = solomonVehicle.getType();
        VehicleRoutingProblem.Builder skillProblemBuilder = VehicleRoutingProblem.Builder.newInstance();
        for(int i=0;i<6;i++) {
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
            Service.Builder skillServiceBuilder = Service.Builder.newInstance(service.getId()).setServiceTime(service.getServiceDuration())
                    .setCoord(service.getCoord()).setLocationId(service.getLocationId()).setTimeWindow(service.getTimeWindow())
                    .addSizeDimension(0,service.getSize().get(0));
            if(service.getCoord().getY()<50) skillServiceBuilder.addRequiredSkill("skill2");
            else skillServiceBuilder.addRequiredSkill("skill1");
            skillProblemBuilder.addJob(skillServiceBuilder.build());
        }
        skillProblemBuilder.addPenaltyVehicles(3.);
        skillProblemBuilder.setFleetSize(VehicleRoutingProblem.FleetSize.FINITE);
        VehicleRoutingProblem skillProblem = skillProblemBuilder.build();

        VehicleRoutingAlgorithmBuilder vraBuilder = new VehicleRoutingAlgorithmBuilder(skillProblem,"src/test/resources/algorithmConfig.xml");
        vraBuilder.addCoreConstraints();
        vraBuilder.addDefaultCostCalculators();

        StateManager stateManager = new StateManager(skillProblem);
        stateManager.updateSkillStates();

        ConstraintManager constraintManager = new ConstraintManager(skillProblem,stateManager);
        constraintManager.addSkillsConstraint();

        VehicleRoutingAlgorithm vra = vraBuilder.build();
        vra.setNuOfIterations(500);

        try {
            Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
            VehicleRoutingProblemSolution solution = Solutions.bestOf(solutions);
            assertEquals(828.94, solution.getCost(), 0.01);
            for(VehicleRoute route : solution.getRoutes()){
                Skills vehicleSkill = route.getVehicle().getSkills();
                for(Job job : route.getTourActivities().getJobs()){
                    for(String skill : job.getRequiredSkills().values()){
                        if(!vehicleSkill.containsSkill(skill)){
                            assertFalse(true);
                        }
                    }
                }
            }
            assertTrue(true);
        }
        catch (NoSolutionFoundException e){
            System.out.println(e.toString());
            assertFalse(true);
        }
    }
}
