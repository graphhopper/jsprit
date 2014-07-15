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
package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.AbstractActivity;
import jsprit.core.problem.job.Delivery;
import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;

public class DefaultTourActivityFactory implements TourActivityFactory{

	@Override
	public AbstractActivity createActivity(Service service) {
		AbstractActivity act;
		if(service instanceof Pickup){
			act = new PickupService((Pickup) service);
		}
		else if(service instanceof Delivery){
			act = new DeliverService((Delivery) service);
		}
		else{
			act = new PickupService(service);
		}
		return act;
	}

}
