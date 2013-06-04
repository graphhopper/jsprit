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

import java.util.ArrayList;
import java.util.List;

import basics.Job;
import basics.route.VehicleRoute;

class JobRemoverImpl implements JobRemover{

	interface RemoverListener {
		public void informRemovedJob(Job j, VehicleRoute r);
	}
	
	private List<RemoverListener> remListeners = new ArrayList<RemoverListener>();
	
	@Override
	public boolean removeJobWithoutTourUpdate(Job job, VehicleRoute vehicleRoute) {
		boolean jobRemoved = vehicleRoute.getTourActivities().removeJob(job);
		if(jobRemoved) informRemovedJob(job,vehicleRoute);
		return jobRemoved;
	}

	private void informRemovedJob(Job job, VehicleRoute vehicleRoute) {
		for(RemoverListener l : remListeners) l.informRemovedJob(job, vehicleRoute);
	}

	public List<RemoverListener> getRemListeners() {
		return remListeners;
	}
	
	

}
