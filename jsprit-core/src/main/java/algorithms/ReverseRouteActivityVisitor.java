package algorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

class ReverseRouteActivityVisitor implements RouteVisitor{

	private Collection<ReverseActivityVisitor> visitors = new ArrayList<ReverseActivityVisitor>();
	
	@Override
	public void visit(VehicleRoute route) {
		if(visitors.isEmpty()) return;
		if(route.isEmpty()) return;
		begin(route);
		Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator();
		while(revIterator.hasNext()){
			TourActivity act = revIterator.next();
			visit(act);
		}
		finish(route);
	}

	private void finish(VehicleRoute route) {
		for(ReverseActivityVisitor visitor : visitors){
			visitor.finish();
		}
		
	}

	private void visit(TourActivity act) {
		for(ReverseActivityVisitor visitor : visitors){
			visitor.visit(act);
		}
	}

	private void begin(VehicleRoute route) {
		for(ReverseActivityVisitor visitor : visitors){
			visitor.begin(route);
		}
		
	}

	public void addActivityVisitor(ReverseActivityVisitor activityVisitor){
		visitors.add(activityVisitor);
	}
}
