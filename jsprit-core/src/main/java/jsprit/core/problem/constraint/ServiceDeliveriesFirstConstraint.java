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
import jsprit.core.problem.solution.route.activity.*;

public class ServiceDeliveriesFirstConstraint implements HardActivityConstraint {

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(newAct instanceof PickupService && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof ServiceActivity && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof DeliverService && prevAct instanceof PickupService){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof DeliverService && prevAct instanceof ServiceActivity){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		
		if(newAct instanceof DeliverService && prevAct instanceof PickupShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof DeliverService && prevAct instanceof DeliverShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof PickupShipment && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED;}
		if(newAct instanceof DeliverShipment && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED;}
	
		return ConstraintsStatus.FULFILLED;
	}
		
}
