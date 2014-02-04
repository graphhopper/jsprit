package jsprit.core.problem.job;

import org.junit.Test;

public class DeliveryTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherLocationIdNorCoordIsSet_itThrowsException(){
		Delivery.Builder.newInstance("p", 0).build();
	}

}
