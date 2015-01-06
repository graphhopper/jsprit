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
package jsprit.core.algorithm.state;

import jsprit.core.problem.solution.route.RouteVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;

public class UpdateEndLocationIfRouteIsOpen implements StateUpdater, RouteVisitor{

	@Override
	public void visit(VehicleRoute route) {
		if(route.getVehicle() != null){
			if(!route.getVehicle().isReturnToDepot()){
				setRouteEndToLastActivity(route);
			}
		}
	}

	private void setRouteEndToLastActivity(VehicleRoute route) {
		if(!route.getActivities().isEmpty()){
			TourActivity lastAct = route.getActivities().get(route.getActivities().size()-1);
			route.getEnd().setLocation(lastAct.getLocation());
		}
	}



	

}
