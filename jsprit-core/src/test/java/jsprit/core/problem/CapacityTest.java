
package jsprit.core.problem;

import static org.junit.Assert.assertEquals;
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
	
	@Test(expected=IndexOutOfBoundsException.class)
	public void whenGettingIndexWhichIsHigherThanNuOfCapDimensions_throwIndexOutOfBoundsException(){
		Capacity.Builder capBuilder = Capacity.Builder.newInstance();
		capBuilder.addDimension(0, 4);
		Capacity cap = capBuilder.build();
		cap.get(2);
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

}
