package algorithms;

import org.junit.Test;

import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;

public class TestSchrimpf {
	
	@Test
	public void whenUsingSchrimpfFactory_itFindsTheConfig(){
		
		VehicleRoutingAlgorithm vra = new SchrimpfFactory().createAlgorithm(VehicleRoutingProblem.Builder.newInstance().build());
		
	}

}
