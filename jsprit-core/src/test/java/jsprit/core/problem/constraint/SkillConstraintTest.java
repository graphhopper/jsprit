package jsprit.core.problem.constraint;

import jsprit.core.algorithm.state.StateManager;
import jsprit.core.algorithm.state.UpdateSkills;
import jsprit.core.problem.Skills;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.Vehicle;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class SkillConstraintTest {

    private HardRouteStateLevelConstraint skillConstraint;

    private StateManager stateManager;

    private VehicleRoute route;

    private Vehicle vehicle;

    @Before
    public void doBefore(){
        vehicle = mock(Vehicle.class);
        Service service = mock(Service.class);
        Service service2 = mock(Service.class);
        Service service3 = mock(Service.class);
        when(service.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill1").build());
        when(service2.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill1").addSkill("skill2")
                .addSkill("skill3").build());
        when(service3.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill4")
                .addSkill("skill5").build());
        route = VehicleRoute.Builder.newInstance(vehicle).addService(service).addService(service2).addService(service3).build();
        stateManager = new StateManager(mock(VehicleRoutingTransportCosts.class));
        stateManager.addStateUpdater(new UpdateSkills(stateManager));
        stateManager.update(route);
        skillConstraint = new SkillConstraint(stateManager);
    }

    @Test
    public void whenJobToBeInsertedRequiresSkillsThatVehicleDoesNotHave_itShouldReturnFalse(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill5").build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance()
                .addAllSkills(Arrays.asList("skill1","skill2","skill3","skill4")).build());
        JobInsertionContext insertionContext = new JobInsertionContext(route,s4,vehicle,route.getDriver(),0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenJobToBeInsertedRequiresSkillsThatVehicleHave_itShouldReturnTrue(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill4").build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance()
                .addAllSkills(Arrays.asList("skill1","skill2","skill3","skill4","skill5")).build());
        JobInsertionContext insertionContext = new JobInsertionContext(route,s4,vehicle,route.getDriver(),0.);
        assertTrue(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenRouteToBeOvertakenRequiresSkillsThatVehicleDoesNotHave_itShouldReturnFalse(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill4").build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance()
                .addAllSkills(Arrays.asList("skill1","skill2","skill6","skill4")).build());
        JobInsertionContext insertionContext = new JobInsertionContext(route,s4,vehicle,route.getDriver(),0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenRouteToBeOvertakenRequiresSkillsThatVehicleDoesHave_itShouldReturnTrue(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill4").build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance()
                .addAllSkills(Arrays.asList("skill1","skill2","skill3","skill4","skill5")).build());
        JobInsertionContext insertionContext = new JobInsertionContext(route,s4,vehicle,route.getDriver(),0.);
        assertTrue(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenNoSkillsAreRequired_itShouldReturnTrue(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance().build());
        JobInsertionContext insertionContext = new JobInsertionContext(VehicleRoute.emptyRoute(),s4,vehicle,route.getDriver(),0.);
        assertTrue(skillConstraint.fulfilled(insertionContext));
    }

    @Test
    public void whenSkillsIsRequiredWhichVehicleDoesNotHave_itShouldReturnFalse(){
        Service s4 = mock(Service.class);
        when(s4.getRequiredSkills()).thenReturn(Skills.Builder.newInstance().addSkill("skill1").build());
        when(vehicle.getSkills()).thenReturn(Skills.Builder.newInstance().build());
        JobInsertionContext insertionContext = new JobInsertionContext(VehicleRoute.emptyRoute(),s4,vehicle,route.getDriver(),0.);
        assertFalse(skillConstraint.fulfilled(insertionContext));
    }
}
