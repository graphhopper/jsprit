package jsprit.core.algorithm.state;

import jsprit.core.problem.Skills;
import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.vehicle.Vehicle;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 01.07.14.
 */
public class UpdateRequiredSkillsTest {

    private VehicleRoute route;

    private StateManager stateManager;

    @Before
    public void doBefore(){
        Vehicle vehicle = mock(Vehicle.class);
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
    }

    @Test
    public void whenUpdatingRoute_skillsAtRouteLevelShouldContainAllSkills(){
        stateManager.update(route);
        Skills skills = stateManager.getRouteState(route, StateFactory.SKILLS, Skills.class);
        assertNotNull(skills);
        Assert.assertEquals(5,skills.values().size());
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
        assertTrue(skills.containsSkill("skill3"));
        assertTrue(skills.containsSkill("skill4"));
        assertTrue(skills.containsSkill("skill5"));
    }


}
