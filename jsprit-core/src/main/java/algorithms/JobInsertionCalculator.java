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

import basics.Job;
import basics.route.Driver;
import basics.route.Vehicle;
import basics.route.VehicleRoute;


 interface JobInsertionCalculator {
	
	public InsertionData calculate(VehicleRoute currentRoute, Job jobToInsert, Vehicle newVehicle, double newVehicleDepartureTime, Driver newDriver, double bestKnownScore);

}
