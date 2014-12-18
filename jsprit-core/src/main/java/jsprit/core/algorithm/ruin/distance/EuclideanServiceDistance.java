/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
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
package jsprit.core.algorithm.ruin.distance;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Service;
import jsprit.core.util.EuclideanDistanceCalculator;

public class EuclideanServiceDistance implements JobDistance {

	public EuclideanServiceDistance() {
		super();
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
				if(s_i.getLocation().getCoordinate() == null || s_j.getLocation().getCoordinate() == null) throw new IllegalStateException("cannot calculate euclidean distance. since service coords are missing");
				avgCost = EuclideanDistanceCalculator.calculateDistance(s_i.getLocation().getCoordinate(), s_j.getLocation().getCoordinate());
			}
		} else {
			throw new UnsupportedOperationException(
					"currently, this class just works with shipments and services.");
		}
		return avgCost;
	}

}
