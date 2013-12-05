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
			route.getEnd().setLocationId(lastAct.getLocationId());
		}
	}



	

}
