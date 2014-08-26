
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

package jsprit.core.util;

import jsprit.core.problem.solution.route.activity.TourActivity;

public class CalculationUtils {
	

	/**
	 * Calculates actEndTime assuming that activity can at earliest start at act.getTheoreticalEarliestOperationStartTime().
	 * 
	 * @param actArrTime
	 * @param act
	 * @return
	 */
	public static double getActivityEndTime(double actArrTime, TourActivity act){
		return Math.max(actArrTime, act.getTheoreticalEarliestOperationStartTime()) + act.getOperationTime();
	}
}
