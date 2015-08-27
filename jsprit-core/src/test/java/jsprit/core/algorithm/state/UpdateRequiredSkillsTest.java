package jsprit.core.algorithm.state;

import jsprit.core.problem.Location;
import jsprit.core.problem.Skills;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests skill updater
 */
public class UpdateRequiredSkillsTest {

    private VehicleRoute route;

    private StateManager stateManager;

    @Before
    public void doBefore() {
        VehicleType type = VehicleTypeImpl.Builder.newInstance("t").build();
        VehicleImpl vehicle = VehicleImpl.Builder.newInstance("v").setStartLocation(Location.newInstance("start")).setType(type).build();
        Service service = Service.Builder.newInstance("s").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").build();
        Service service2 = Service.Builder.newInstance("s2").setLocation(Location.newInstance("loc")).addRequiredSkill("skill1").addRequiredSkill("skill2").addRequiredSkill("skill3").build();
        Service service3 = Service.Builder.newInstance("s3").setLocation(Location.newInstance("loc")).addRequiredSkill("skill4").addRequiredSkill("skill5").build();
        VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addVehicle(vehicle).addJob(service)
            .addJob(service2).addJob(service3).build();
        route = VehicleRoute.Builder.newInstance(vehicle).setJobActivityFactory(vrp.getJobActivityFactory()).addService(service).addService(service2).addService(service3).build();

        stateManager = new StateManager(vrp);
        stateManager.updateSkillStates();
        stateManager.informInsertionStarts(Arrays.asList(route), null);
    }

    @Test
    public void whenUpdatingRoute_skillsAtRouteLevelShouldContainAllSkills() {
        Skills skills = stateManager.getRouteState(route, InternalStates.SKILLS, Skills.class);
        assertNotNull(skills);
        Assert.assertEquals(5, skills.values().size());
        assertTrue(skills.containsSkill("skill1"));
        assertTrue(skills.containsSkill("skill2"));
        assertTrue(skills.containsSkill("skill3"));
        assertTrue(skills.containsSkill("skill4"));
        assertTrue(skills.containsSkill("skill5"));
    }


}
