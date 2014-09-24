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
package jsprit.core.problem.solution.route;

import jsprit.core.problem.solution.route.activity.ActivityVisitor;
import jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;


public class RouteActivityVisitor implements RouteVisitor{

	private Collection<ActivityVisitor> visitors = new ArrayList<ActivityVisitor>();
	
	@Override
	public void visit(VehicleRoute route) {
		if(visitors.isEmpty()) return;
		begin(route);
		for(TourActivity act : route.getTourActivities().getActivities()){
			visit(act);
		}
		end(route);
	}

	private void end(VehicleRoute route) {
		for(ActivityVisitor visitor : visitors){
			visitor.finish();
		}
		
	}

	private void visit(TourActivity act) {
		for(ActivityVisitor visitor : visitors){
			visitor.visit(act);
		}
	}

	private void begin(VehicleRoute route) {
		for(ActivityVisitor visitor : visitors){
			visitor.begin(route);
		}
		
	}

	public RouteActivityVisitor addActivityVisitor(ActivityVisitor activityVisitor){
		if(!visitors.contains(activityVisitor)){
			visitors.add(activityVisitor);
		}
		return this;
	}
}
