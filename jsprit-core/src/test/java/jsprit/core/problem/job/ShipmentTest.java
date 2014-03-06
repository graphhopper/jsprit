package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

import org.junit.Test;

@SuppressWarnings("deprecation")
public class ShipmentTest {
	
	@Test
	public void whenTwoShipmentsHaveTheSameId_theyReferencesShouldBeUnEqual(){
		Shipment one = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").
				setDeliveryLocation("foofoo").setPickupServiceTime(10).setDeliveryServiceTime(20).build();
		Shipment two = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").
				setDeliveryLocation("foofoo").setPickupServiceTime(10).setDeliveryServiceTime(20).build();
		
		assertTrue(one != two);
	}

	@Test
	public void whenTwoShipmentsHaveTheSameId_theyShouldBeEqual(){
		Shipment one = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").
				setDeliveryLocation("foofoo").setPickupServiceTime(10).setDeliveryServiceTime(20).build();
		Shipment two = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").
				setDeliveryLocation("foofoo").setPickupServiceTime(10).setDeliveryServiceTime(20).build();
		
		assertTrue(one.equals(two));
	}

	@Test
	public void whenShipmentIsInstantiatedWithASizeOf10_theSizeShouldBe10(){
		Shipment one = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").
				setDeliveryLocation("foofoo").setPickupServiceTime(10).setDeliveryServiceTime(20).build();
		assertEquals(10,one.getCapacityDemand());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment one = Shipment.Builder.newInstance("s", -10).setPickupLocation("foo").setDeliveryLocation("foofoo").build();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenShipmentIsBuiltWithNegativeDemand_itShouldThrowException_v2(){
		@SuppressWarnings("unused")
		Shipment one = Shipment.Builder.newInstance("s").addSizeDimension(0, -10).setPickupLocation("foo").setDeliveryLocation("foofoo").build();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenIdIsNull_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment one = Shipment.Builder.newInstance(null, 10).setPickupLocation("foo").setDeliveryLocation("foofoo").build();
	}
	
	@Test
	public void whenCallingForANewBuilderInstance_itShouldReturnBuilderCorrectly(){
		Shipment.Builder builder = Shipment.Builder.newInstance("s", 0);
		assertNotNull(builder);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherPickupLocationIdNorPickupCoord_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenNeitherDeliveryLocationIdNorDeliveryCoord_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setPickupLocation("pickLoc").build();
	}
	
	@Test
	public void whenPickupLocationIdIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals("pickLoc",s.getPickupLocation());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenPickupLocationIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment.Builder builder = Shipment.Builder.newInstance("s", 0).setPickupLocation(null);
	}
	
	@Test
	public void whenPickupCoordIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").setPickupCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0,s.getPickupCoord().getX(),0.01);
		assertEquals(2.0,s.getPickupCoord().getY(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenPickupCoordIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment.Builder builder = Shipment.Builder.newInstance("s", 0).setPickupCoord(null);
	}
	
	@Test
	public void whenDeliveryLocationIdIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals("delLoc",s.getDeliveryLocation());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenDeliveryLocationIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment.Builder builder = Shipment.Builder.newInstance("s", 0).setDeliveryLocation(null);
	}
	
	@Test
	public void whenDeliveryCoordIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").setDeliveryCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0,s.getDeliveryCoord().getX(),0.01);
		assertEquals(2.0,s.getDeliveryCoord().getY(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenDeliveryCoordIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Shipment.Builder builder = Shipment.Builder.newInstance("s", 0).setDeliveryCoord(null);
	}
	
	@Test
	public void whenPickupServiceTimeIsNotSet_itShouldBeZero(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(0.0,s.getPickupServiceTime(),0.01);
	}
	
	@Test
	public void whenDeliveryServiceTimeIsNotSet_itShouldBeZero(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(0.0,s.getDeliveryServiceTime(),0.01);
	}
	
	@Test
	public void whenPickupServiceTimeIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setPickupServiceTime(2.0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(2.0,s.getPickupServiceTime(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenPickupServiceIsSmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setPickupServiceTime(-2.0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
	}
	
	@Test
	public void whenDeliveryServiceTimeIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryServiceTime(2.0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(2.0,s.getDeliveryServiceTime(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenDeliveryServiceIsSmallerThanZero_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryServiceTime(-2.0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
	}
	
	@Test
	public void whenPickupTimeWindowIsNotSet_itShouldBeTheDefaultOne(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(0.0,s.getPickupTimeWindow().getStart(),0.01);
		assertEquals(Double.MAX_VALUE,s.getPickupTimeWindow().getEnd(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenPickupTimeWindowIsNull_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setPickupTimeWindow(null).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
	}
	
	@Test
	public void whenPickupTimeWindowIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setPickupTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(1.0,s.getPickupTimeWindow().getStart(),0.01);
		assertEquals(2.0,s.getPickupTimeWindow().getEnd(),0.01);
	}
	
	@Test
	public void whenDeliveryTimeWindowIsNotSet_itShouldBeTheDefaultOne(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(0.0,s.getDeliveryTimeWindow().getStart(),0.01);
		assertEquals(Double.MAX_VALUE,s.getDeliveryTimeWindow().getEnd(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenDeliveryTimeWindowIsNull_itShouldThrowException(){
		@SuppressWarnings("unused")
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryTimeWindow(null).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
	}
	
	@Test
	public void whenDeliveryTimeWindowIsSet_itShouldBeDoneCorrectly(){
		Shipment s = Shipment.Builder.newInstance("s", 0).setDeliveryTimeWindow(TimeWindow.newInstance(1, 2)).setDeliveryLocation("delLoc").setPickupLocation("pickLoc").build();
		assertEquals(1.0,s.getDeliveryTimeWindow().getStart(),0.01);
		assertEquals(2.0,s.getDeliveryTimeWindow().getEnd(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenShipmentHasNegativeCapacityVal_throwIllegalStateExpception(){
		@SuppressWarnings("unused")
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setDeliveryLocation("foofoo")
		.addSizeDimension(0, -2)
		.build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setDeliveryLocation("foofoo")
				.addSizeDimension(0,2)
				.addSizeDimension(1,4)
				.build();
		assertEquals(2,one.getSize().getNuOfDimensions());
	}
	
	@Test
	public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setPickupCoord(Coordinate.newInstance(0, 0))
				.setDeliveryLocation("foofoo").build();
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(0,one.getSize().get(0));
	}
	
	@Test
	public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Shipment one = Shipment.Builder.newInstance("s",1).setPickupLocation("foo").setPickupCoord(Coordinate.newInstance(0, 0))
				.setDeliveryLocation("foofoo").build();
		assertEquals(1,one.getCapacityDemand());
		assertEquals(1,one.getSize().getNuOfDimensions());
		assertEquals(1,one.getSize().get(0));
	}
}
