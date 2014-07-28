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
package jsprit.core.algorithm.state;

public class InternalStates {


    public final static StateId MAXLOAD = new StateFactory.StateIdImpl("max_load", 0);
	
	public final static StateId LOAD = new StateFactory.StateIdImpl("load", 1);
	
	public final static StateId COSTS = new StateFactory.StateIdImpl("costs", 2);
	
	public final static StateId LOAD_AT_BEGINNING = new StateFactory.StateIdImpl("load_at_beginning", 3);
	
	public final static StateId LOAD_AT_END = new StateFactory.StateIdImpl("load_at_end", 4);
	
	public final static StateId DURATION = new StateFactory.StateIdImpl("duration", 5);
	
	public final static StateId LATEST_OPERATION_START_TIME = new StateFactory.StateIdImpl("latest_operation_start_time", 6);
	
	public final static StateId EARLIEST_OPERATION_START_TIME = new StateFactory.StateIdImpl("earliest_operation_start_time", 7);
	
	public final static StateId FUTURE_MAXLOAD = new StateFactory.StateIdImpl("future_max_load", 8);
	
	public final static StateId PAST_MAXLOAD = new StateFactory.StateIdImpl("past_max_load", 9);


}
