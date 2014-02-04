/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package jsprit.core.problem.job;

import static org.junit.Assert.assertEquals;

import static org.junit.Assert.assertNotNull;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import jsprit.core.problem.solution.route.activity.TimeWindow;
import jsprit.core.util.Coordinate;

import org.junit.Test;


public class ServiceTest {
	
	@Test
	public void whenTwoServicesHaveTheSameId_theirReferencesShouldBeUnEqual(){
		Service one = Service.Builder.newInstance("service", 10).setLocationId("foo").build();
		Service two = Service.Builder.newInstance("service", 10).setLocationId("fo").build();
		
		assertTrue(one != two);
	}

	@Test
	public void whenTwoServicesHaveTheSameId_theyShouldBeEqual(){
		Service one = Service.Builder.newInstance("service", 10).setLocationId("foo").build();
		Service two = Service.Builder.newInstance("service", 10).setLocationId("fo").build();
		
		assertTrue(one.equals(two));
	}
	
	@Test
	public void noName(){
		Set<Service> serviceSet = new HashSet<Service>();
		Service one = Service.Builder.newInstance("service", 10).setLocationId("foo").build();
		Service two = Service.Builder.newInstance("service", 10).setLocationId("fo").build();
		serviceSet.add(one);
//		assertTrue(serviceSet.contains(two));
		serviceSet.remove(two);
		assertTrue(serviceSet.isEmpty());
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenCapacityDimValueIsNegative_throwIllegalStateExpception(){
		@SuppressWarnings("unused")
		Service s = Service.Builder.newInstance("s").setLocationId("foo").addCapacityDimension(0, -10).build();
	}
	
	@Test
	public void whenAddingTwoCapDimension_nuOfDimsShouldBeTwo(){
		Service one = Service.Builder.newInstance("s").setLocationId("foofoo")
				.addCapacityDimension(0,2)
				.addCapacityDimension(1,4)
				.build();
		assertEquals(2,one.getCapacity().getNuOfDimensions());
	}
	
	@Test
	public void whenShipmentIsBuiltWithoutSpecifyingCapacity_itShouldHvCapWithOneDimAndDimValOfZero(){
		Service one = Service.Builder.newInstance("s").setLocationId("foofoo")
				.build();
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(0,one.getCapacity().get(0));
	}
	
	@Test
	public void whenShipmentIsBuiltWithConstructorWhereSizeIsSpecified_capacityShouldBeSetCorrectly(){
		Service one = Service.Builder.newInstance("s",1).setLocationId("foofoo")
				.build();
		assertEquals(1,one.getCapacityDemand());
		assertEquals(1,one.getCapacity().getNuOfDimensions());
		assertEquals(1,one.getCapacity().get(0));
	}

	@Test
	public void whenCallingForNewInstanceOfBuilder_itShouldReturnBuilderCorrectly(){
		Service.Builder builder = Service.Builder.newInstance("s", 0);
		assertNotNull(builder);
		assertTrue(builder instanceof Service.Builder);
	}
	
	@Test
	public void whenSettingNoType_itShouldReturn_service(){
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").build();
		assertEquals("service",s.getType());
	}
	
	@Test
	public void whenSettingLocation_itShouldBeSetCorrectly(){
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").build();
		assertEquals("loc",s.getLocationId());
	}
	
	@Test
	public void whenSettingLocationCoord_itShouldBeSetCorrectly(){
		Service s = Service.Builder.newInstance("s", 0).setCoord(Coordinate.newInstance(1, 2)).build();
		assertEquals(1.0,s.getCoord().getX(),0.01);
		assertEquals(2.0,s.getCoord().getY(),0.01);
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenSettingNeitherLocationIdNorCoord_throwsException(){
		@SuppressWarnings("unused")
		Service s = Service.Builder.newInstance("s", 0).build();
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenServiceTimeSmallerZero_throwIllegalStateException(){
		@SuppressWarnings("unused")
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").setServiceTime(-1).build();
	}
	
	@Test
	public void whenSettingServiceTime_itShouldBeSetCorrectly(){
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").setServiceTime(1).build();
		assertEquals(1.0,s.getServiceDuration(),0.01);
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void whenTimeWindowIsNull_throwException(){
		@SuppressWarnings("unused")
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").setTimeWindow(null).build();
	}
	
	@Test
	public void whenSettingTimeWindow_itShouldBeSetCorrectly(){
		Service s = Service.Builder.newInstance("s", 0).setLocationId("loc").setTimeWindow(TimeWindow.newInstance(1.0, 2.0)).build();
		assertEquals(1.0,s.getTimeWindow().getStart(),0.01);
		assertEquals(2.0,s.getTimeWindow().getEnd(),0.01);
	}

}
