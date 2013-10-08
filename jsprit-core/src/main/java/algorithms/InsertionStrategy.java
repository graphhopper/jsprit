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
package algorithms;

import java.util.Collection;

import basics.Job;
import basics.algo.InsertionListener;
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

public interface InsertionStrategy {

	/**
	 * Assigns the unassigned jobs to service-providers
	 * 
	 * @param vehicleRoutes
	 * @param unassignedJobs
	 */
	public void insertJobs(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs);
	
	public void addListener(InsertionListener insertionListener);
	
	public void removeListener(InsertionListener insertionListener);
	
	public Collection<InsertionListener> getListeners();

}
