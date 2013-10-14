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
package basics.route;

import static org.junit.Assert.*;

import org.junit.Test;

import basics.Service;
import basics.Service.Builder;
import basics.route.ServiceActivity;

public class ServiceActTest {

	@Test
	public void whenTwoDeliveriesHaveTheSameUnderlyingJob_theyAreEqual(){
		Service s1 = Service.Builder.newInstance("s", 10).setLocationId("loc").build();
		Service s2 = Service.Builder.newInstance("s", 10).setLocationId("loc").build();
		
		ServiceActivity d1 = ServiceActivity.newInstance(s1);
		ServiceActivity d2 = ServiceActivity.newInstance(s2);
		
		assertTrue(d1.equals(d2));
	}
	
	@Test
	public void whenTwoDeliveriesHaveTheDifferentUnderlyingJob_theyAreNotEqual(){
		Service s1 = Service.Builder.newInstance("s", 10).setLocationId("loc").build();
		Service s2 = Service.Builder.newInstance("s1", 10).setLocationId("loc").build();
		
		ServiceActivity d1 = ServiceActivity.newInstance(s1);
		ServiceActivity d2 = ServiceActivity.newInstance(s2);
		
		assertFalse(d1.equals(d2));
	}
}
