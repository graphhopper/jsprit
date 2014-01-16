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

	@Test
	public void whenCallingStaticNewBuilderInstance_itShouldReturnNewBuilderInstance(){
		VehicleTypeImpl.Builder builder = VehicleTypeImpl.Builder.newInstance("foo", 0);
		assertNotNull(builder);
	}
	
	@Test
	public void whenBuildingTypeJustByCallingNewInstance_typeIdMustBeCorrect(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo", 0).build();
		assertEquals("foo",type.getTypeId());
	}
	
	@Test
	public void whenBuildingTypeJustByCallingNewInstance_capMustBeCorrect(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo", 0).build();
		assertEquals(0,type.getCapacity());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenBuildingTypeWithCapSmallerThanZero_throwIllegalStateException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("foo", -10).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenBuildingTypeWithNullId_throwIllegalStateException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance(null, 10).build();
	}
	
	@Test
	public void whenSettingMaxVelocity_itShouldBeSetCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setMaxVelocity(10).build();
		assertEquals(10,type.getMaxVelocity(),0.0);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenMaxVelocitySmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setMaxVelocity(-10).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenFixedCostsSmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setFixedCost(-10).build();
	}
	
	public void whenSettingFixedCosts_itShouldBeSetCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setFixedCost(10).build();
		assertEquals(10.0, type.getVehicleCostParams().fix,0.0);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenPerDistanceCostsSmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setCostPerDistance(-10).build();
	}
	
	public void whenSettingPerDistanceCosts_itShouldBeSetCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setCostPerDistance(10).build();
		assertEquals(10.0, type.getVehicleCostParams().perDistanceUnit,0.0);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenPerTimeCostsSmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setCostPerTime(-10).build();
	}
	
	public void whenSettingPerTimeCosts_itShouldBeSetCorrectly(){
		VehicleTypeImpl type = VehicleTypeImpl.Builder.newInstance("type", 10).setCostPerTime(10).build();
		assertEquals(10.0, type.getVehicleCostParams().perDistanceUnit,0.0);
	}
	

}
