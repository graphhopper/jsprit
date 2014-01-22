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
	
	@Test
	public void whenStartLocationIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").build();
		assertEquals("startLoc", v.getLocationId());
		assertEquals("startLoc", v.getStartLocationId());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenStartLocationIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId(null).build();
	}
	
	@Test
	public void whenStartLocationCoordIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0, v.getStartLocationCoordinate().getX(),0.01);
		assertEquals(2.0, v.getStartLocationCoordinate().getY(),0.01);
		
		assertEquals(1.0, v.getCoord().getX(),0.01);
		assertEquals(2.0, v.getCoord().getY(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenStartLocationCoordIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(null).build();
	}
	
	@Test
	public void whenEndLocationIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setEndLocationId("endLoc").build();
		assertEquals("startLoc", v.getStartLocationId());
		assertEquals("endLoc", v.getEndLocationId());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenEndLocationIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setEndLocationId(null).build();
	}
	
	@Test
	public void whenEndLocationCoordIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setEndLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0, v.getEndLocationCoordinate().getX(),0.01);
		assertEquals(2.0, v.getEndLocationCoordinate().getY(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenEndLocationCoordIsNull_itThrowsException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setEndLocationCoordinate(null).build();
	}
	
	@Test
	public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationIdMustBeEqualToStartLocationId(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").build();
		assertEquals("startLoc", v.getEndLocationId());
	}
	
	@Test
	public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoord(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").build();
		assertEquals(v.getEndLocationCoordinate(), v.getStartLocationCoordinate());
	}
	
	@Test
	public void whenNeitherEndLocationIdNorEndLocationCoordAreSet_endLocationCoordMustBeEqualToStartLocationCoordV2(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).build();
		assertEquals(v.getEndLocationCoordinate(), v.getStartLocationCoordinate());
	}
	
	@Test
	public void whenEndLocationCoordinateIsSetButNoId_idMustBeCoordToString(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).setEndLocationCoordinate(Coordinate.newInstance(3.0, 4.0)).build();
		assertEquals(v.getEndLocationCoordinate().toString(), v.getEndLocationId());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenEndLocationIdIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).setEndLocationId("endLoc").setReturnToDepot(false).build();
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenEndLocationCoordIsSpecifiedANDReturnToDepotIsFalse_itShouldThrowException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).setEndLocationCoordinate(Coordinate.newInstance(3, 4)).setReturnToDepot(false).build();
	}
	
	@Test
	public void whenEndLocationCoordIsNotSpecifiedANDReturnToDepotIsFalse_endLocationCoordMustBeStartLocationCoord(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
		assertEquals(v.getStartLocationCoordinate(),v.getEndLocationCoordinate());
	}
	
	@Test
	public void whenEndLocationIdIsNotSpecifiedANDReturnToDepotIsFalse_endLocationIdMustBeStartLocationId(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1.0, 2.0)).setReturnToDepot(false).build();
		assertEquals(v.getStartLocationCoordinate().toString(),v.getEndLocationId());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenStartAndEndAreUnequalANDReturnToDepotIsFalse_itShouldThrowException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("end").setReturnToDepot(false).build();
	}
	
	@Test
	public void whenStartAndEndAreEqualANDReturnToDepotIsFalse_itShouldThrowException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("start").setReturnToDepot(false).build();
		assertTrue(true);
	}

}
