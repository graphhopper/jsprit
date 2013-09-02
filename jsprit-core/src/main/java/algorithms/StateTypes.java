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

class StateTypes {
	final static String LOAD = "load";
	
	final static String LOAD_AT_DEPOT = "loadAtDepot";
	
	final static String DURATION = "duration";
	
	final static String LATEST_OPERATION_START_TIME = "latestOST";
	
	final static String EARLIEST_OPERATION_START_TIME = "earliestOST";

	static final String COSTS = "costs";
	
	final static String FUTURE_PICKS = "futurePicks";
	
	final static String PAST_DELIVERIES = "pastDeliveries";
}
