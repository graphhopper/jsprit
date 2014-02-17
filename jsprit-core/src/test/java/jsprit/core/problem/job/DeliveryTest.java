package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DeliveryTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherLocationIdNorCoordIsSet_itThrowsException(){
		Delivery.Builder.newInstance("p", 0).build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s").setLocationId("foofoo")
				.addCapacityDimension(0,2)
				.addCapacityDimension(1,4)
				.build();
		assertEquals(2,one.getCapacity().getNuOfDimensions());
		assertEquals(2,one.getCapacity().get(0));
		assertEquals(4,one.getCapacity().get(1));
		
	}
	
	@Test
	public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s").setLocationId("foofoo")
				.build();
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(0,one.getCapacity().get(0));
	}
	
	@Test
	public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Delivery one = (Delivery)Delivery.Builder.newInstance("s",1).setLocationId("foofoo")
				.build();
		assertEquals(1,one.getCapacityDemand());
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(1,one.getCapacity().get(0));
	}


}
