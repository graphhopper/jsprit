
package jsprit.core.problem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.Test;

public class CapacityTest {
	
	@Test
	public void whenSettingSimplyOneCapDimension_nuOfDimensionMustBeCorrect(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		Capacity cap = capBuilder.build();
		assertEquals(1,cap.getNuOfDimensions());
	}
	
	@Test
	public void whenSettingTwoCapDimension_nuOfDimensionMustBeCorrect(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		capBuilder.addDimension(1,10);
		Capacity cap = capBuilder.build();
		assertEquals(2,cap.getNuOfDimensions());
	}
	
	@Test
	public void whenSettingRandomNuOfCapDimension_nuOfDimensionMustBeCorrect(){
		Random rand = new Random();
		int nuOfCapDimensions = rand.nextInt(100);
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(nuOfCapDimensions-1, 4);
		Capacity cap = capBuilder.build();
		assertEquals(nuOfCapDimensions,cap.getNuOfDimensions());
	}
	
	@Test
	public void whenSettingOneDimValue_valueMustBeCorrect(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		Capacity cap = capBuilder.build();
		assertEquals(4,cap.get(0));
	}
	
	@Test
	public void whenGettingIndexWhichIsHigherThanNuOfCapDimensions_itShouldReturn0(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		Capacity cap = capBuilder.build();
		assertEquals(0,cap.get(2));
	}
	
	@Test
	public void whenSettingNoDim_DefaultIsOneDimWithDimValueOfZero(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		Capacity cap = capBuilder.build();
		assertEquals(1, cap.getNuOfDimensions());
		assertEquals(0, cap.get(0));
	}
	
	@Test
	public void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameNuOfDims(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		capBuilder.addDimension(1,10);
		Capacity cap = capBuilder.build();
		
		Capacity copiedCapacity = Capacity.copyOf(cap);
		assertEquals(2,copiedCapacity.getNuOfDimensions());
	}
	
	@Test
	public void whenCopyingCapacityWithTwoCapDim_copiedObjShouldHvSameValues(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		capBuilder.addDimension(1,10);
		Capacity cap = capBuilder.build();
		
		Capacity copiedCapacity = Capacity.copyOf(cap);
		assertEquals(4,copiedCapacity.get(0));
		assertEquals(10,copiedCapacity.get(1));
	}
	
	@Test
	public void whenCopyingNull_itShouldReturnNull(){
		Capacity nullCap = Capacity.copyOf(null);
		assertTrue(nullCap==null);
	}
	
	@Test
	public void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.addup(cap1, cap2);
		assertEquals(3, result.get(0));
	}

	@Test
	public void whenAddingUpTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.addup(cap1, cap2);
		assertEquals(1, result.getNuOfDimensions());
	}
	
	@Test
	public void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity result = Capacity.addup(cap1, cap2);
		assertEquals(3, result.getNuOfDimensions());
	}
	
	@Test
	public void whenAddingUpTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity result = Capacity.addup(cap1, cap2);
		assertEquals(3, result.get(0));
		assertEquals(5, result.get(1));
		assertEquals(7, result.get(2));
	}
	
	public void whenAddingUpTwoCapacitiesWithDifferentNuOfDimensions_itShouldAddThemCorrectly(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.addup(cap1, cap2);
		assertEquals(3,result.get(0));
		assertEquals(2,result.get(1));
	}
	
	@Test(expected=NullPointerException.class)
	public void whenOneOfArgsIsNullWhenAdding_itShouldThrowException(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
		@SuppressWarnings("unused")
		Capacity result = Capacity.addup(cap1, null);
	}
	
	
	
	@Test
	public void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectCapacityValues(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.subtract(cap2, cap1);
		assertEquals(1, result.get(0));
	}

	@Test
	public void whenSubtractingTwoOneDimensionalCapacities_itShouldReturnCorrectNuOfDimensions(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.subtract(cap2, cap1);
		assertEquals(1, result.getNuOfDimensions());
	}
	
	@Test
	public void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectNuOfDimensions(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity result = Capacity.subtract(cap2, cap1);
		assertEquals(3, result.getNuOfDimensions());
	}
	
	@Test
	public void whenSubtractingTwoThreeDimensionalCapacities_itShouldReturnCorrectCapValues(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity result = Capacity.subtract(cap2, cap1);
		assertEquals(1, result.get(0));
		assertEquals(1, result.get(1));
		assertEquals(1, result.get(2));
	}
	
	@Test
	public void whenSubtractingTwoCapacitiesWithDifferentNuOfDimensions_itShouldSubtractCorrectly(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).build();
		Capacity result = Capacity.subtract(cap2, cap1);
		assertEquals(1,result.get(0));
		assertEquals(-2,result.get(1));
	}
	
	@Test(expected=NullPointerException.class)
	public void whenOneOfArgsIsNullWhenSubtracting_itShouldThrowException(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).build();
		@SuppressWarnings("unused")
		Capacity result = Capacity.subtract(cap1, null);
	}
	
	@Test
	public void whenSubtractingBiggerFromLower_itShouldSubtractCorrectly(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity result = Capacity.subtract(cap1, cap2);
		assertEquals(-1,result.get(0));
		assertEquals(-1,result.get(1));
		assertEquals(-1,result.get(2));
	}
	
	@Test
	public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 1).addDimension(1, 2).addDimension(2, 3).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		assertTrue(cap1.isLessOrEqual(cap2));
	}
	
	@Test
	public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v2(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 2).addDimension(2, 4).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		assertTrue(cap1.isLessOrEqual(cap2));
	}
	
	@Test
	public void whenOneCapIsLessThanAnother_itShouldReturnCorrectBoolean_v3(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		assertTrue(cap1.isLessOrEqual(cap2));
	}
	
	@Test
	public void whenOneCapIsBiggerThanAnother_itShouldReturnCorrectBoolean(){
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity cap2 = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 4).addDimension(2, 4).build();
		assertFalse(cap2.isLessOrEqual(cap1));
	}
	
	@Test
	public void whenAddingTwo_itShouldReturnCorrectCap(){
		int wheelChairSpace = 0;
		int passengerSeats = 1;
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).build();
		Capacity wheelChair = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 1).build();
		Capacity passenger = Capacity.Builder.newInstance().addDimension(passengerSeats, 1).build();
		Capacity wheelChair_plus_passenger = Capacity.addup(wheelChair, passenger);
		assertEquals(1,wheelChair_plus_passenger.get(wheelChairSpace));
		assertEquals(1,wheelChair_plus_passenger.get(passengerSeats));
		assertTrue(wheelChair_plus_passenger.isLessOrEqual(cap1));
	}
	
	@Test
	public void whenAddingTwo_itShouldReturnCorrectCap_v2(){
		int wheelChairSpace = 0;
		int passengerSeats = 1;
		int weight = 2;
		Capacity cap1 = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 2).addDimension(passengerSeats, 10).addDimension(2, 100).build();
		Capacity wheelChair = Capacity.Builder.newInstance().addDimension(wheelChairSpace, 1).addDimension(weight, 80).build();
		Capacity passenger = Capacity.Builder.newInstance().addDimension(passengerSeats, 1).addDimension(weight, 30).build();
		Capacity wheelChair_plus_passenger = Capacity.addup(wheelChair, passenger);
		assertEquals(1,wheelChair_plus_passenger.get(wheelChairSpace));
		assertEquals(1,wheelChair_plus_passenger.get(passengerSeats));
		assertEquals(110,wheelChair_plus_passenger.get(weight));
		assertFalse(wheelChair_plus_passenger.isLessOrEqual(cap1));
	}
	
	@Test
	public void whenInvertingCap_itShouldBeDoneCorrectly(){
		Capacity cap = Capacity.Builder.newInstance().addDimension(0, 2).addDimension(1, 3).addDimension(2, 4).build();
		Capacity inverted = Capacity.invert(cap);
		assertEquals(-2,inverted.get(0));
		assertEquals(-3,inverted.get(1));
		assertEquals(-4,inverted.get(2));
	}
	
}
