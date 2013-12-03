package jsprit.core.problem.constraint;

import static org.junit.Assert.*;
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

}
