package jsprit.core.algorithm;

import jsprit.core.algorithm.box.GreedySchrimpfFactory;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.util.Coordinate;
import org.junit.Test;

public class InitialSolutionTest {

	@Test
	public void testPenaltyVehicleWithInitialSolution() {
		VehicleRoutingProblem.Builder builder = VehicleRoutingProblem.Builder.newInstance();
		builder.addVehicle(VehicleImpl.Builder.newInstance("v1").setEarliestStart(0).setLatestArrival(12).setStartLocationCoordinate(Coordinate.newInstance(1, 1)).build());
		builder.addJob(Service.Builder.newInstance("j1").setCoord(Coordinate.newInstance(0, 0)).setTimeWindow(TimeWindow.newInstance(0, 12)).setServiceTime(1).build());
		builder.addJob(Service.Builder.newInstance("j2").setCoord(Coordinate.newInstance(2, 2)).setTimeWindow(TimeWindow.newInstance(12, 24)).setServiceTime(1).build());
		builder.addPenaltyVehicles(2.0);
		VehicleRoutingProblem vrp = builder.build();
		VehicleRoutingAlgorithm algorithm = new GreedySchrimpfFactory().createAlgorithm(vrp);
		algorithm.searchSolutions();
	}
}
