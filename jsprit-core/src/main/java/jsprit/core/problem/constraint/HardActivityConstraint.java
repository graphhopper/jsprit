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
import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Hard constraint that evaluates whether a new activity can be inserted between an activity segment (prevAct,nextAct).
 */
public interface HardActivityConstraint extends HardConstraint{

    /**
     * Indicates whether a hard activity constraint is fulfilled or not
     */
	static enum ConstraintsStatus {

		NOT_FULFILLED_BREAK, NOT_FULFILLED, FULFILLED

	}

    /**
     * Returns whether newAct can be inserted in between prevAct and nextAct.
     *
     * <p>
     * When you check activities, you need to understand the following:
     *
     * Let us assume an existing route;
     *
     * start, ..., i-1, i, j, j+1, ..., end
     *
     * When inserting a shipment, two activities will be inserted, pickupShipment k_pick and deliverShipment k_deliver,
     * i.e. jsprit loops through this route (activity sequence) and checks hard and soft constraints and calculates (marginal) insertion costs. For
     * the activity sequence above, it means:
     *<p>
     * start, k_pick, start+1 (prevAct, newAct, nextAct)<br>
     * ...<br>
     * i-1, k_pick, i<br>
     * i, k_pick, j<br>
     * ...<br>
     *<p>
     * accordingly:<br>
     * start, k_pick, k_delivery (prevAct, newAct, nextAct)<br>
     * ...<br>
     * i-1, k_delivery, i<br>
     * i, k_delivery, j<br>
     * ...<br>
     *<p>
     * You specify a hard activity constraint, you to check whether for example k_pick can be inserted between prevActivity and nextActivity at all.
     * If so, your hard constraint should return ConstraintsStatus.FULFILLED.<br>
     * If not, you can return ConstraintsStatus.NOT_FULFILLED or ConstraintsStatus.NOT_FULFILLED_BREAK.<br>
     *
     * Latter should be used, if your constraint can never be fulfilled anymore when looping further through your route.
     *<p>
     * Since constraint checking at activity level is rather time consuming (you need to do this thousand/millions times),
     * you can memorize states behind activities to avoid additional loopings through your activity sequence and thus to
     * check your constraint locally (only by looking at prevAct, newAct, nextAct) in constant time.
     *
     * @param iFacts JobInsertionContext provides additional information that might be important when evaluating the insertion of newAct
     * @param prevAct the previous activity, i.e. the activity before the new activity
     * @param newAct the new activity to be inserted in between prevAct and nextAct
     * @param nextAct the next activity, i.e. the activity after the new activity
     * @param prevActDepTime the departure time at previous activity (prevAct) with the new vehicle (iFacts.getNewVehicle())
     * @return fulfilled if hard constraint is met, other not fulfilled.
     */
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

}
