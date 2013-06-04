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
import basics.route.VehicleRoute;




/**
 * 
 * @author stefan schroeder
 * 
 */

interface InsertionStrategy {

	class Insertion {
		
		private final VehicleRoute route;
		
		private final InsertionData insertionData;

		public Insertion(VehicleRoute vehicleRoute, InsertionData insertionData) {
			super();
			this.route = vehicleRoute;
			this.insertionData = insertionData;
		}

		public VehicleRoute getRoute() {
			return route;
		}
		
		public InsertionData getInsertionData() {
			return insertionData;
		}
		
	}

	
	


	/**
	 * Assigns the unassigned jobs to service-providers
	 * 
	 * @param vehicleRoutes
	 * @param unassignedJobs
	 * @param result2beat
	 */
	public void run(Collection<VehicleRoute> vehicleRoutes, Collection<Job> unassignedJobs, double result2beat);

}
