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

import util.EuclideanDistanceCalculator;
import basics.Job;
import basics.Service;

class EuclideanServiceDistance implements JobDistance {

	public EuclideanServiceDistance() {
		super();
	}

	@Override
	public double calculateDistance(Job i, Job j) {
		double avgCost = 0.0;
		if (i instanceof Service && j instanceof Service) {
			if (i.equals(j)) {
				avgCost = 0.0;
			} else {
				Service s_i = (Service) i;
				Service s_j = (Service) j;
				if(s_i.getCoord() == null || s_j.getCoord() == null) throw new IllegalStateException("cannot calculate euclidean distance. since service coords are missing");
				avgCost = EuclideanDistanceCalculator.calculateDistance(s_i.getCoord(), s_j.getCoord());
			}
		} else {
			throw new UnsupportedOperationException(
					"currently, this class just works with shipments and services.");
		}
		return avgCost;
	}

}
