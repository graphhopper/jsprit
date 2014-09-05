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
package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;

/**
 * Hard constraint that evaluates whether a new job (insertionContext.getJob()) can be inserted
 * existing route (insertionContext.getRoute()).
 */
public interface HardRouteConstraint extends HardConstraint{

    /**
     * Returns whether a job can be inserted in route.
     *
     * @param insertionContext provides context information about inserting a new job, i.e. the new job (<code>insertionContext.getJob()</code>),
     *                         the route where the new job should be inserted (<code>insertionContext.getRoute()</code>), the new vehicle that
     *                         should operate the route plus the new job (<code>insertionContext.getNewVehicle()</code>) and the new departure
     *                         time at this vehicle's start location (<code>insertionContext.getNewDepartureTime()</code>).
     * @return true if constraint is met, false otherwise
     */
	public boolean fulfilled(JobInsertionContext insertionContext);
	
}
