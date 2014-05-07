package jsprit.core.algorithm;

import static org.junit.Assert.*;

import java.util.Collection;

import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.Solutions;

import org.junit.Test;

public class PickupsAndDeliveries_IT {
	
	@Test
	public void whenSolvingLR101InstanceOfLiLim_solutionsMustNoBeWorseThan5PercentOfBestKnownSolution(){
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		new VrpXMLReader(vrpBuilder).read("src/test/resources/lilim_lr101.xml");
		VehicleRoutingProblem vrp = vrpBuilder.build();
		VehicleRoutingAlgorithm vra = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/lilim_algorithmConfig.xml");
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		assertEquals(1650.8,Solutions.bestOf(solutions).getCost(),80.);
		assertEquals(19,Solutions.bestOf(solutions).getRoutes().size(),1);
	}

}
