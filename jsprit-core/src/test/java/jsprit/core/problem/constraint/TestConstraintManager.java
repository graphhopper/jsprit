package jsprit.core.problem.constraint;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.solution.route.state.RouteAndActivityStateGetter;

import org.junit.Test;

public class TestConstraintManager {

	@Test
	public void whenGettingConstraintsViaConstructor_theyShouldBeResolvedCorrectly(){
		List<Constraint> constraints = new ArrayList<Constraint>();
		constraints.add(new ServiceDeliveriesFirstConstraint());
		constraints.add(mock(HardRouteStateLevelConstraint.class));
		ConstraintManager cManager = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class),constraints);
		assertEquals(2,cManager.getConstraints().size());
	}
	
	@Test
	public void whenGettingConstraintsViaConstructorAndAtLeastOneConstraintCannotBeResolved_itShouldOnlyAddTheKnownConstraints(){
		List<Constraint> constraints = new ArrayList<Constraint>();
		constraints.add(new ServiceDeliveriesFirstConstraint());
		constraints.add(mock(Constraint.class));
		ConstraintManager cManager = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class),constraints);
		assertEquals(1,cManager.getConstraints().size());
	}
	
	@Test
	public void whenAddingSoftRouteConstraint_managerShouldHaveIt(){
		SoftRouteConstraint c = mock(SoftRouteConstraint.class);
		ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class));
		assertEquals(0,man.getConstraints().size());
		man.addConstraint(c);
		assertEquals(1,man.getConstraints().size());
	}
	
	@Test
	public void whenAddingTwoSoftRouteConstraint_managerShouldHaveIt(){
		SoftRouteConstraint c1 = mock(SoftRouteConstraint.class);
		SoftRouteConstraint c2 = mock(SoftRouteConstraint.class);
		ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class));
		assertEquals(0,man.getConstraints().size());
		man.addConstraint(c1);
		man.addConstraint(c2);
		assertEquals(2,man.getConstraints().size());
	}
	
	@Test
	public void whenAddingSoftActivityConstraint_managerShouldHaveIt(){
		SoftActivityConstraint c = mock(SoftActivityConstraint.class);
		ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class));
		assertEquals(0,man.getConstraints().size());
		man.addConstraint(c);
		assertEquals(1,man.getConstraints().size());
	}
	
	@Test
	public void whenAddingTwoSoftActivityConstraints_managerShouldHaveIt(){
		SoftActivityConstraint c1 = mock(SoftActivityConstraint.class);
		SoftActivityConstraint c2 = mock(SoftActivityConstraint.class);
		ConstraintManager man = new ConstraintManager(mock(VehicleRoutingProblem.class),mock(RouteAndActivityStateGetter.class));
		assertEquals(0,man.getConstraints().size());
		man.addConstraint(c1);
		man.addConstraint(c2);
		assertEquals(2,man.getConstraints().size());
	}

}
