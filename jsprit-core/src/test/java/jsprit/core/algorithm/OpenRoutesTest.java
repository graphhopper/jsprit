package jsprit.core.algorithm;

import static org.junit.Assert.*;

import java.util.Collection;

import jsprit.core.algorithm.box.SchrimpfFactory;
import jsprit.core.algorithm.recreate.NoSolutionFoundException;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.problem.vehicle.Vehicle;
import jsprit.core.problem.vehicle.VehicleImpl;
import jsprit.core.problem.vehicle.VehicleType;
import jsprit.core.problem.vehicle.VehicleTypeImpl;
import jsprit.core.util.Coordinate;
import jsprit.core.util.Solutions;

import org.junit.Test;

public class OpenRoutesTest {
	
	@Test
	public void whenDealingWithOpenRoute_insertionShouldNotRequireRouteToBeClosed(){
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(9.)
				.setType(type).setReturnToDepot(false).setStartLocationCoordinate(Coordinate.newInstance(0, 0)).build();
		
		Service service = Service.Builder.newInstance("s").setCoord(Coordinate.newInstance(5, 0)).build();
		
		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
		
		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
		
		try{
			@SuppressWarnings("unused")
			Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
			assertTrue(true);
		}
		catch(NoSolutionFoundException e){
			assertFalse(true);
		}
		
	}
	
	@Test
	public void whenDealingWithOpenRoute_algorithmShouldCalculateCorrectCosts(){
		VehicleType type = VehicleTypeImpl.Builder.newInstance("type").build();
		Vehicle vehicle = VehicleImpl.Builder.newInstance("v").setLatestArrival(10.)
				.setType(type).setReturnToDepot(false).setStartLocationCoordinate(Coordinate.newInstance(0, 0)).build();
		
		Service service = Service.Builder.newInstance("s").setCoord(Coordinate.newInstance(5, 0)).build();
		
		VehicleRoutingProblem vrp = VehicleRoutingProblem.Builder.newInstance().addJob(service).addVehicle(vehicle).build();
		
		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(vrp);
		vra.setNuOfIterations(10);
		
		Collection<VehicleRoutingProblemSolution> solutions = vra.searchSolutions();
		
		assertEquals(5.,Solutions.bestOf(solutions).getCost(),0.01);

	}

}
