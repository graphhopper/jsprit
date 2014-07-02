/*******************************************************************************
 * Copyright (c) 2014 Stefan Schroeder.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 *  
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.problem.vehicle;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jsprit.core.problem.vehicle.VehicleImpl.NoVehicle;
import jsprit.core.util.Coordinate;

import org.junit.Test;


public class VehicleImplTest {
	
	
	
	@Test(expected=IllegalStateException.class)
	public void whenVehicleIsBuiltWithoutSettingNeitherLocationNorCoord_itThrowsAnIllegalStateException(){
		@SuppressWarnings("unused")
		Vehicle v = VehicleImpl.Builder.newInstance("v").build();
	}
	
	@Test
	public void whenVehicleIsBuiltToReturnToDepot_itShouldReturnToDepot(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(true).setStartLocationId("loc").build();
		assertTrue(v.isReturnToDepot());
	}
	
	@Test
	public void whenVehicleIsBuiltToNotReturnToDepot_itShouldNotReturnToDepot(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setReturnToDepot(false).setStartLocationId("loc").build();
		assertFalse(v.isReturnToDepot());
	}
	
	@Test
	public void whenVehicleIsBuiltWithLocation_itShouldHvTheCorrectLocation(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("loc").build();
		assertEquals("loc",v.getStartLocationId());
	}
	
	@Test
	public void whenVehicleIsBuiltWithCoord_itShouldHvTheCorrectCoord(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0,v.getStartLocationCoordinate().getX(),0.01);
		assertEquals(2.0,v.getStartLocationCoordinate().getY(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndEarliestStartIsNotSet_itShouldSetTheDefaultOfZero(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(0.0,v.getEarliestDeparture(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndEarliestStartSet_itShouldBeSetCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setEarliestStart(10.0).setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(10.0,v.getEarliestDeparture(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndLatestArrivalIsNotSet_itShouldSetDefaultOfDoubleMaxValue(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(Double.MAX_VALUE,v.getLatestArrival(),0.01);
	}
	
	@Test
	public void whenVehicleIsBuiltAndLatestArrivalIsSet_itShouldBeSetCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setLatestArrival(30.0).setStartLocationCoordinate(Coordinate.newInstance(1, 2)).build();
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
	}
	
	@Test
	public void whenEndLocationIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setEndLocationId("endLoc").build();
		assertEquals("startLoc", v.getStartLocationId());
		assertEquals("endLoc", v.getEndLocationId());
	}
	
	@Test
	public void whenEndLocationCoordIsSet_itIsDoneCorrectly(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("startLoc").setEndLocationCoordinate(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0, v.getEndLocationCoordinate().getX(),0.01);
		assertEquals(2.0, v.getEndLocationCoordinate().getY(),0.01);
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

	@Test
	public void whenTwoVehiclesHaveTheSameId_theyShouldBeEqual(){
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("start").setReturnToDepot(false).build();
		Vehicle v2 = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setEndLocationId("start").setReturnToDepot(false).build();
		assertTrue(v.equals(v2));
	}
	
	@Test
	public void whenTwoVehiclesHaveTheSameIdButDiffType_theyShouldNotBeEqual(){
		VehicleTypeImpl type1 = VehicleTypeImpl.Builder.newInstance("type").build();
		Vehicle v = VehicleImpl.Builder.newInstance("v").setStartLocationId("start").setType(type1).setEndLocationId("start").setReturnToDepot(false).build();
		PenaltyVehicleType penType = new PenaltyVehicleType(type1);
		Vehicle v2 = VehicleImpl.Builder.newInstance("v").setType(penType).setStartLocationId("start").setEndLocationId("start").setReturnToDepot(false).build();
		assertTrue(!v.equals(v2));
	}
	
	
}
