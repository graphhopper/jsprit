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

import basics.Job;
import basics.route.VehicleRoute;



class ScoredJob {
	
	private final Job job;
	
	private final double score;
	
	private final InsertionData insertionData;
	
	private final VehicleRoute route;

	public ScoredJob(final Job job, final double score, final InsertionData insertionData, final VehicleRoute route) {
		super();
		this.job = job;
		this.score = score;
		this.insertionData = insertionData;
		this.route = route;
	}

	public InsertionData getInsertionData() {
		return insertionData;
	}

	public VehicleRoute getRoute() {
		return route;
	}

	public Job getJob() {
		return job;
	}

	public double getScore() {
		return score;
	}


}
