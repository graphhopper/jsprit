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




/**
 * collection of hard constrainters bot at activity and at route level.
 * 
 * <p>HardPickupAndDeliveryLoadConstraint requires LOAD_AT_DEPOT and LOAD (i.e. load at end) at route-level
 * 
 * <p>HardTimeWindowConstraint requires LATEST_OPERATION_START_TIME
 * 
 * <p>HardPickupAndDeliveryConstraint requires LOAD_AT_DEPOT and LOAD at route-level and FUTURE_PICKS and PAST_DELIVIERS on activity-level
 * 
 * <p>HardPickupAndDeliveryBackhaulConstraint requires LOAD_AT_DEPOT and LOAD at route-level and FUTURE_PICKS and PAST_DELIVIERS on activity-level
 * 
 * @author stefan
 *
 */
class HardConstraints {
	
	

}
