package jsprit.core.problem.job;

import org.junit.Test;

public class PickupTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherLocationIdNorCoordIsSet_itThrowsException(){
		Pickup.Builder.newInstance("p", 0).build();
	}

}
