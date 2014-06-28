package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class PickupTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherLocationIdNorCoordIsSet_itThrowsException(){
		Pickup.Builder.newInstance("p").build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Pickup one = (Pickup)Pickup.Builder.newInstance("s").setLocationId("foofoo")
				.addSizeDimension(0,2)
				.addSizeDimension(1,4)
				.build();
		assertEquals(2,one.getSize().getNuOfDimensions());
		assertEquals(2,one.getSize().get(0));
		assertEquals(4,one.getSize().get(1));
		
	}
	
	@Test
	public void whenPickupIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Pickup one = (Pickup)Pickup.Builder.newInstance("s").setLocationId("foofoo")
				.build();
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(0,one.getSize().get(0));
	}
	
	@Test
	public void whenPickupIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Pickup one = (Pickup)Pickup.Builder.newInstance("s").addSizeDimension(0, 1).setLocationId("foofoo")
				.build();
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(1,one.getSize().get(0));
	}
	
	@Test
	public void whenAddingSkills_theyShouldBeAddedCorrectly(){
		Pickup s = (Pickup) Pickup.Builder.newInstance("s").setLocationId("loc")
				.addSkill("drill").addSkill("screwdriver").build();
		assertTrue(s.getRequiredSkills().contains("drill"));
		assertTrue(s.requiresSkill("drill"));
		assertTrue(s.requiresSkill("ScrewDriver"));
	}
	
	@Test
	public void whenAddingSkillsCaseSens_theyShouldBeAddedCorrectly(){
		Pickup s = (Pickup) Pickup.Builder.newInstance("s").setLocationId("loc")
				.addSkill("DriLl").addSkill("screwDriver").build();
		assertTrue(s.getRequiredSkills().contains("drill"));
		assertTrue(s.requiresSkill("drilL"));
	}


}
