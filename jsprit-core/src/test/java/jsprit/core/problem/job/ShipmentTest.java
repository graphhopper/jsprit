package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.job.Shipment;
import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

import org.junit.Test;


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
	
	@Test
	public void whenShipmentIsDefined_itsFieldsShouldBeDefinedCorrectly(){
		Shipment one = Shipment.Builder.newInstance("s", 10).setPickupLocation("foo").setPickupCoord(Coordinate.newInstance(0, 0)).setPickupServiceTime(1.0)
				.setPickupTimeWindow(TimeWindow.newInstance(0.0, 1.0))
				.setDeliveryLocation("foofoo").setDeliveryServiceTime(20).setDeliveryCoord(Coordinate.newInstance(1, 1)).
				setDeliveryTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
		assertEquals("s",one.getId());
		assertEquals(10,one.getCapacityDemand());
		assertEquals("foo",one.getPickupLocation());
		assertEquals(0,one.getPickupCoord().getX(),0.01);
		assertEquals(1.0,one.getPickupServiceTime(),0.01);
		assertEquals("foofoo",one.getDeliveryLocation());
		assertEquals(20.0,one.getDeliveryServiceTime(),0.01);
		assertEquals(1.0,one.getDeliveryCoord().getX(),0.01);
		assertEquals(1.0,one.getDeliveryTimeWindow().getStart(),0.01);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenShipmentHasNegativeCapacityVal_throwIllegalStateExpception(){
		@SuppressWarnings("unused")
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setDeliveryLocation("foofoo")
		.addCapacityDimension(0, -2)
		.build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setDeliveryLocation("foofoo")
				.addCapacityDimension(0,2)
				.addCapacityDimension(1,4)
				.build();
		assertEquals(2,one.getCapacity().getNuOfDimensions());
	}
	
	@Test
	public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Shipment one = Shipment.Builder.newInstance("s").setPickupLocation("foo").setPickupCoord(Coordinate.newInstance(0, 0))
				.setDeliveryLocation("foofoo").build();
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(0,one.getCapacity().get(0));
	}
	
	@Test
	public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Shipment one = Shipment.Builder.newInstance("s",1).setPickupLocation("foo").setPickupCoord(Coordinate.newInstance(0, 0))
				.setDeliveryLocation("foofoo").build();
		assertEquals(1,one.getCapacityDemand());
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(1,one.getCapacity().get(0));
	}
}
