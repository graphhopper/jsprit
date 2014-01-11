package jsprit.core.problem.vehicle;

import static org.junit.Assert.*;

import org.junit.Test;

public class VehicleTypeImplTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenTypeHasNegativeCapacityVal_throwIllegalStateExpception(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").addCapacityDimension(0,-10).build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t")
				.addCapacityDimension(0,2)
				.addCapacityDimension(1, 4)
				.build();
		assertEquals(2,type.getCapacityDimensions().getNuOfDimensions());
	}
	
	@Test
	public void whenAddingTwoCapDimension_dimValuesMustBeCorrect(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t")
				.addCapacityDimension(0,2)
				.addCapacityDimension(1,4)
				.build();
		assertEquals(2,type.getCapacityDimensions().get(0));
		assertEquals(4,type.getCapacityDimensions().get(1));
	}
	
	@Test
	public void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDim(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
		assertEquals(1,type.getCapacityDimensions().getNuOfDimensions());
	}
	
	@Test
	public void whenTypeIsBuiltWithoutSpecifyingCapacity_itShouldHvCapDimValOfZero(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t").build();
		assertEquals(0,type.getCapacityDimensions().get(0));
	}
	
	@Test
	public void whenTypeIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("t",20).build();
		assertEquals(20,type.getCapacity());
		assertEquals(20,type.getCapacityDimensions().get(0));
	}

}
