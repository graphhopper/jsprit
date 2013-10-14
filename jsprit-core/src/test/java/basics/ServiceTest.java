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
package basics;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;


public class ServiceTest {
	
	@Test
	public void whenTwoServicesHaveTheSameId_theyShouldBeEqual(){
		Service one = Service.Builder.newInstance("service", 10).setLocationId("foo").build();
		Service two = Service.Builder.newInstance("service", 10).setLocationId("fo").build();
		
		assertTrue(one != two);
	}

	@Test
	public void whenTwoServicesHaveTheSameId_theyShouldBeEqual2(){
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
}
