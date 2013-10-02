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

import util.CrowFlyCosts;
import util.Locations;
import basics.Job;
import basics.Service;



class JobDistanceBeeline implements JobDistance {

	private Locations locations;

	public JobDistanceBeeline(Locations locations) {
		super();
		this.locations = locations;
	}

	@Override
	public double getDistance(Job i, Job j) {
		double avgCost = 0.0;
		if (i instanceof Service && j instanceof Service) {
			if (i.equals(j)) {
				avgCost = 0.0;
			} else {
				Service s_i = (Service) i;
				Service s_j = (Service) j;
				avgCost = calcDist(s_i.getLocationId(), s_j.getLocationId());
			}
		} else {
			throw new UnsupportedOperationException(
					"currently, this class just works with shipments and services.");
		}
		return avgCost;
	}

	private double calcDist(String from, String to) {
		return new CrowFlyCosts(locations).getTransportCost(from, to, 0.0,null, null);
	}

}
