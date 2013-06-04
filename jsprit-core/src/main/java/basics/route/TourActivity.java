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
package basics.route;

import basics.Job;


public interface TourActivity {

	public interface JobActivity<T extends Job> extends TourActivity {
		
		public T getJob();
		
	}
	
	public int getCapacityDemand();
	
	public abstract String getName();
	
	public abstract String getLocationId();
	
	public abstract double getTheoreticalEarliestOperationStartTime();
	
	public abstract double getTheoreticalLatestOperationStartTime();

	public abstract double getOperationTime();
	
	public abstract double getArrTime();
	
	public abstract double getEndTime();
	
	public abstract void setArrTime(double arrTime);
	
	public abstract void setEndTime(double endTime);
	
	public TourActivity duplicate();
		
}
