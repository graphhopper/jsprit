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

import jsprit.core.problem.cost.VehicleRoutingTransportCosts;
import jsprit.core.problem.solution.route.RouteVisitor;
import jsprit.core.problem.solution.route.VehicleRoute;
import jsprit.core.problem.solution.route.activity.TourActivity;

public class UpdateDepartureTime implements StateUpdater, RouteVisitor{

	private VehicleRoutingTransportCosts routingCosts;

	public UpdateDepartureTime(VehicleRoutingTransportCosts routingCosts) {
		this.routingCosts = routingCosts;
	}

	@Override
	public void visit(VehicleRoute route) {
		if(route.getVehicle() != null){
			if(route.getVehicle().hasVariableDepartureTime()) {
				if (!route.isEmpty()) {
					TourActivity first = route.getActivities().get(0);
					double earliestStart = first.getTheoreticalEarliestOperationStartTime();
					double backwardTravelTime = routingCosts.getBackwardTransportTime(first.getLocation(), route.getStart().getLocation(),
							earliestStart, route.getDriver(), route.getVehicle());
					double newDepartureTime = Math.max(route.getStart().getEndTime(), earliestStart - backwardTravelTime);
					route.setVehicleAndDepartureTime(route.getVehicle(), newDepartureTime);
				}
			}
		}
	}





	

}
