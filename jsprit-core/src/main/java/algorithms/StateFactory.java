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
package algorithms;

import java.util.Arrays;
import java.util.List;

import algorithms.StateGetter.State;
import algorithms.StateGetter.StateId;
import algorithms.StateManager.StateImpl;

public class StateFactory {
	
	final static StateId MAXLOAD = new StateIdImpl("maxload");
	
	final static StateId LOAD = new StateIdImpl("load");
	
	final static StateId COSTS = new StateIdImpl("costs");
	
	final static StateId LOAD_AT_BEGINNING = new StateIdImpl("loadAtBeginning");
	
	final static StateId LOAD_AT_END = new StateIdImpl("loadAtEnd");
	
	final static StateId DURATION = new StateIdImpl("duration");
	
	final static StateId LATEST_OPERATION_START_TIME = new StateIdImpl("latestOST");
	
	final static StateId EARLIEST_OPERATION_START_TIME = new StateIdImpl("earliestOST");
	
	final static StateId FUTURE_PICKS = new StateIdImpl("futurePicks");
	
	final static StateId PAST_DELIVERIES = new StateIdImpl("pastDeliveries");
	
	final static List<String> reservedIds = Arrays.asList("maxload","load","costs","loadAtBeginning","loadAtEnd","duration","latestOST","earliestOST"
			,"futurePicks","pastDeliveries");
			
	
	public static StateId createId(String name){
		if(reservedIds.contains(name)){ throwException(name); }
		return new StateIdImpl(name);
	}
	
	public static State createState(double value){
		return new StateImpl(value);
	}

	private static void throwException(String name) {
		throw new IllegalStateException("state-id with name '" + name + "' cannot be created. it is already reserved internally.");
	}

	
	static class StateIdImpl implements StateId {
		
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

		public StateIdImpl(String name) {
			super();
			this.name = name;
		}
		
		public String toString(){
			return name;
		}
	}
}
