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
package jsprit.core.algorithm.recreate;

import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;

public interface ActivityInsertionCostsCalculator {
	
	public class ActivityInsertionCosts {
		
		private double additionalCosts;
		private double additionalTime;
		public ActivityInsertionCosts(double additionalCosts, double additionalTime) {
			super();
			this.additionalCosts = additionalCosts;
			this.additionalTime = additionalTime;
		}
		/**
		 * @return the additionalCosts
		 */
		public double getAdditionalCosts() {
			return additionalCosts;
		}
		/**
		 * @return the additionalTime
		 */
		public double getAdditionalTime() {
			return additionalTime;
		}
		
		

	}
	
	public ActivityInsertionCosts getCosts(JobInsertionContext iContext, TourActivity prevAct, TourActivity nextAct, TourActivity newAct, double depTimeAtPrevAct);

}
