package jsprit.core.problem.vehicle;


import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import jsprit.core.util.Coordinate;

import org.junit.Test;


public class VehicleImplTest {
	
	@Test
	public void whenSettingTypeWithBuilder_typeShouldBeSet(){
		VehicleType type = mock(VehicleType.class);
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").setType(type).build();
		assertEquals(type,v.getType());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenTypeIsNull_itThrowsIllegalStateException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").setType(null).build();
	}
	
	@Test
	public void whenTypeIsNotSet_defaultTypeIsSet(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").build();
		assertEquals("default",v.getType().getTypeId());
		assertEquals(0,v.getType().getCapacity());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenVehicleIsBuiltWithoutSettingNeitherLocationNorCoord_itThrowsAnIllegalStateException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").build();
	}
	
	@Test
	public void whenVehicleIsBuiltAndReturnToDepotFlagIsNotSet_itShouldReturnToDepot(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").build();
		assertTrue(v.isReturnToDepot());
	}
	
	@Test
	public void whenVehicleIsBuiltToReturnToDepot_itShouldReturnToDepot(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(true).setLocationId("loc").build();
		assertTrue(v.isReturnToDepot());
	}
	
	@Test
	public void whenVehicleIsBuiltToNotReturnToDepot_itShouldNotReturnToDepot(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(false).setLocationId("loc").build();
		assertFalse(v.isReturnToDepot());
	}
	
	@Test
	public void whenVehicleIsBuiltWithLocation_itShouldHvTheCorrectLocation(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationId("loc").build();
		assertEquals("loc",v.getLocationId());
	}
	
	@Test
	public void whenVehicleIsBuiltWithCoord_itShouldHvTheCorrectCoord(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0,v.getCoord().getX(),0.01);
		assertEquals(2.0,v.getCoord().getY(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndEarliestStartIsNotSet_itShouldSetTheDefaultOfZero(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(0.0,v.getEarliestDeparture(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndEarliestStartSet_itShouldBeSetCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10.0).setLocationCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(10.0,v.getEarliestDeparture(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndLatestArrivalIsNotSet_itShouldSetDefaultOfDoubleMaxValue(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLocationCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(Double.MAX_VALUE,v.getLatestArrival(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndLatestArrivalIsSet_itShouldBeSetCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLatestArrival(30.0).setLocationCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(30.0,v.getLatestArrival(),0.01);
	}
	
	@Test
	public void whenNoVehicleIsCreate_itShouldHvTheCorrectId(){
		Vehicle v = VehicleImpl.createNoVehicle();
		assertTrue(v instanceof NoVehicle);
		assertEquals("noVehicle",v.getId());
	}

}
