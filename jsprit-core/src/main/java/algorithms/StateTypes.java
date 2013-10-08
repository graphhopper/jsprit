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

public class StateTypes {
	public final static String LOAD = "load";
	
	public final static String LOAD_AT_DEPOT = "loadAtDepot";
	
	public final static String DURATION = "duration";
	
	public final static String LATEST_OPERATION_START_TIME = "latestOST";
	
	public final static String EARLIEST_OPERATION_START_TIME = "earliestOST";

	public static final String COSTS = "costs";
	
	public final static String FUTURE_PICKS = "futurePicks";
	
	public final static String PAST_DELIVERIES = "pastDeliveries";
}
