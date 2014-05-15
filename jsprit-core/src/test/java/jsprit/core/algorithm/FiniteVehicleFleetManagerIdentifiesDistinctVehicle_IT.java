package jsprit.core.algorithm;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.recreate.NoSolutionFoundException;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.io.VrpXMLReader;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;

import org.junit.Test;

public class FiniteVehicleFleetManagerIdentifiesDistinctVehicle_IT {
	
	@Test
	public void whenEmployingVehicleWhereOnlyOneDistinctVehicleCanServeAParticularJob_algorithmShouldFoundDistinctSolution(){
		final List<Boolean> testFailed = new ArrayList<Boolean>();
		for(int i=0;i<10;i++){
			VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
			new VrpXMLReader(vrpBuilder).read("src/test/resources/biggerProblem.xml");
			VehicleRoutingProblem vrp = vrpBuilder.build();

			VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
			vra.setNuOfIterations(10);
			try{
				@SuppressWarnings("unused")
				Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
			}
			catch(NoSolutionFoundException e){
				testFailed.add(true);
			}
		}
		System.out.println("failed: " + testFailed.size());
		assertTrue(testFailed.isEmpty());
	}

}
