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
package algorithms;


class StateTypes {
	
//	final static StateId LOAD = new StateIdImpl("load");
	
	final static String LOAD = "load";
	
	final static String LOAD_AT_DEPOT = "loadAtDepot";
	
	final static String DURATION = "duration";
	
	final static String LATEST_OPERATION_START_TIME = "latestOST";
	
	final static String EARLIEST_OPERATION_START_TIME = "earliestOST";

	static final String COSTS = "costs";
	
	final static String FUTURE_PICKS = "futurePicks";
	
	final static String PAST_DELIVERIES = "pastDeliveries";
	
}
