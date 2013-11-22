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
package jsprit.core.problem.cost;


/**
 * Interface for transportCost and transportTime.
 * 
 * <p>Transport here is what happens between two activities within the transport system, i.e. in the physical transport network. And
 * must give the answer of how long does it take from A to B, and how much does this cost.
 * 
 * @author schroeder
 * 
 */

public interface VehicleRoutingTransportCosts extends TransportTime, TransportCost {

}
