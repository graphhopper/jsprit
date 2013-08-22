package algorithms;

import org.junit.Test;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;

public class BuildFastCVRPTest {
	
	@Test
	public void buildFastCVRPAlgoTest(){
		
		VehicleRoutingProblem vrp = null;
		
		VehicleRoutingAlgorithm vra = createVRA(vrp);
		
		
		
	}
	
	public VehicleRoutingAlgorithm createVRA(VehicleRoutingProblem vrp){
		VehicleFleetManager fleetManager = new InfiniteVehicles(vrp.getVehicles());
		
		return null;
	}

}
