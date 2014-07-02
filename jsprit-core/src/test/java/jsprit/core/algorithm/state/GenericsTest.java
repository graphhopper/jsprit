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
package jsprit.core.algorithm.state;

import java.util.HashMap;
import java.util.Map;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.problem.solution.route.state.StateFactory;
import jsprit.core.problem.solution.route.state.StateFactory.State;

public class GenericsTest {
	
	
	
//	static class State<T> {
//		Class<T> type;
//		T state;
//		public State(Class<T> type, T state) {
//			super();
//			this.type = type;
//			this.state = state;
//		}
//		
//	}
	
	static class States {
		
		private Map<String,Object> states = new HashMap<String,Object>();
		
		public <T> void putState(String id, Class<T> type, T state){
			states.put(id, type.cast(state));
		}
		
		public <T> T getState(String id, Class<T> type){
			T s = type.cast(states.get(id));
			return s;
		}
		
	}
	
	public static void main(String[] args) {
		States states = new States();
		states.putState("load", Double.class, 0.1);
		states.putState("state", String.class, "foo");
		states.putState("job", Job.class, Service.Builder.newInstance("foo").setLocationId("loc").build());
		states.putState("cap", Capacity.class, Capacity.Builder.newInstance().addDimension(0, 1).build());
		
		Double load = states.getState("load", Double.class);
		String state = states.getState("state", String.class);
		Job service = states.getState("job", Job.class);
		Capacity capacity = states.getState("cap", Capacity.class);
		
		states.putState("st", State.class, StateFactory.createState(10.));
		
		System.out.println(load);
		System.out.println(state);
		System.out.println(service);
		System.out.println(capacity);
		System.out.println(states.getState("st", State.class).toDouble());
			
	}

	
}
