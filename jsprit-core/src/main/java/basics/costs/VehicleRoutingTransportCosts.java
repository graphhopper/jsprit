/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package basics.costs;


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
