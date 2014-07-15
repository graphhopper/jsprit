/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package jsprit.core.problem.solution.route.state;

import jsprit.core.problem.HasIndex;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StateFactory {
	
	
	public interface StateId extends HasIndex{

		
	}
	
	public interface State {
		double toDouble();
	}
	

	
	public interface States {
		
		State getState(StateId key);
		
		void putState(StateId id, State state);
		
	}
	
		
	static class StateImpl implements State{
		double state;

		public StateImpl(double state) {
			super();
			this.state = state;
		}

		@Override
		public double toDouble() {
			return state;
		}
		
	}
	
	static class StatesImpl implements States{

		private Map<StateId,State> states = new HashMap<StateId, State>();
		
		public void putState(StateId key, State state) {
			states.put(key, state);
		}

		@Override
		public State getState(StateId key) {
			return states.get(key);
		}

	}
	
	public final static StateId MAXLOAD = new StateIdImpl("maxload", 0);
	
	public final static StateId LOAD = new StateIdImpl("load", 1);
	
	public final static StateId COSTS = new StateIdImpl("costs", 2);
	
	public final static StateId LOAD_AT_BEGINNING = new StateIdImpl("loadAtBeginning", 3);
	
	public final static StateId LOAD_AT_END = new StateIdImpl("loadAtEnd", 4);
	
	public final static StateId DURATION = new StateIdImpl("duration", 5);
	
	public final static StateId LATEST_OPERATION_START_TIME = new StateIdImpl("latestOST", 6);
	
	public final static StateId EARLIEST_OPERATION_START_TIME = new StateIdImpl("earliestOST", 7);
	
	public final static StateId FUTURE_MAXLOAD = new StateIdImpl("futureMaxload", 8);
	
	public final static StateId PAST_MAXLOAD = new StateIdImpl("pastMaxload", 9);
	
	final static List<String> reservedIds = Arrays.asList("maxload","load","costs","loadAtBeginning","loadAtEnd","duration","latestOST","earliestOST"
			,"futureMaxload","pastMaxload");
			
	
	public static States createStates(){
		return new StatesImpl();
	}
	
	public static StateId createId(String name){
		if(reservedIds.contains(name)){ throwReservedIdException(name); }
		return new StateIdImpl(name, -1);
	}

    public static StateId createId(String name, int index){
        if(reservedIds.contains(name)) throwReservedIdException(name);
        if(index < 10) throwReservedIdException(name);
        return new StateIdImpl(name, index);
    }
	
	public static State createState(double value){
		return new StateImpl(value);
	}
	
	 public static boolean isReservedId(String stateId){
         if(reservedIds.contains(stateId)) return true;
         return false;
	 }

	 public static boolean isReservedId(StateId stateId){
		 if(reservedIds.contains(stateId.toString())) return true;
		 return false;
	 }

	public static void throwReservedIdException(String name) {
		throw new IllegalStateException("state-id with name '" + name + "' cannot be created. it is already reserved internally.");
	}

	
	static class StateIdImpl implements StateId {

        private int index;

        public int getIndex(){ return index; }

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			StateIdImpl other = (StateIdImpl) obj;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}

		private String name;

		public StateIdImpl(String name, int index) {
			super();
			this.name = name;
            this.index = index;
		}
		
		public String toString(){
			return name;
		}
	}
}
