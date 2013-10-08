package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

class RouteActivityVisitor implements RouteVisitor{

	private Collection<ActivityVisitor> visitors = new ArrayList<ActivityVisitor>();
	
	@Override
	public void visit(VehicleRoute route) {
		if(visitors.isEmpty()) return;
		if(route.isEmpty()) return;
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

	public void addActivityVisitor(ActivityVisitor activityVisitor){
		visitors.add(activityVisitor);
	}
}
