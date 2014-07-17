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
import java.util.List;


public class StateFactory {
	
	
	public interface StateId extends HasIndex{

	}

	public final static StateId MAXLOAD = new StateIdImpl("max_load", 0);
	
	public final static StateId LOAD = new StateIdImpl("load", 1);
	
	public final static StateId COSTS = new StateIdImpl("costs", 2);
	
	public final static StateId LOAD_AT_BEGINNING = new StateIdImpl("load_at_beginning", 3);
	
	public final static StateId LOAD_AT_END = new StateIdImpl("load_at_end", 4);
	
	public final static StateId DURATION = new StateIdImpl("duration", 5);
	
	public final static StateId LATEST_OPERATION_START_TIME = new StateIdImpl("latest_ost", 6);
	
	public final static StateId EARLIEST_OPERATION_START_TIME = new StateIdImpl("earliest_ost", 7);
	
	public final static StateId FUTURE_MAXLOAD = new StateIdImpl("future_max_load", 8);
	
	public final static StateId PAST_MAXLOAD = new StateIdImpl("past_max_load", 9);
	
	final static List<String> reservedIds = Arrays.asList("max_load","load","costs","load_at_beginning","load_at_end","duration","latest_ost","earliest_ost"
			,"future_max_load","past_max_load");
			


    @Deprecated
	public static StateId createId(String name){
		if(reservedIds.contains(name)){ throwReservedIdException(name); }
		return new StateIdImpl(name, -1);
	}

    @Deprecated
    public static StateId createId(String name, int index){
        if(reservedIds.contains(name)) throwReservedIdException(name);
        if(index < 10) throwReservedIdException(name);
        return new StateIdImpl(name, index);
    }

	
	 public static boolean isReservedId(String stateId){
         return reservedIds.contains(stateId);
	 }

	 public static boolean isReservedId(StateId stateId){
		 return reservedIds.contains(stateId.toString());
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
