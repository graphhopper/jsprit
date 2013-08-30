package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class ForwardInTimeListeners {
	
	interface ForwardInTimeListener{
		
		public void start(VehicleRoute route, Start start, double departureTime);

		public void nextActivity(TourActivity act, double arrTime,double endTime);

		public void end(End end, double arrivalTime);
		
	}
	
	private Collection<ForwardInTimeListener> listeners = new ArrayList<ForwardInTimeListeners.ForwardInTimeListener>();
	
	public void addListener(ForwardInTimeListener l){
		listeners.add(l);
	}
	
	public void start(VehicleRoute route, Start start, double departureTime){
		for(ForwardInTimeListener l : listeners){ l.start(route, start, departureTime); }
	}
	
	public void nextActivity(TourActivity act, double arrTime, double endTime){
		for(ForwardInTimeListener l : listeners){ l.nextActivity(act,arrTime,endTime); }
	}
	
	public void end(End end, double arrivalTime){
		for(ForwardInTimeListener l : listeners){ l.end(end, arrivalTime); }
	}

	public boolean isEmpty() {
		return listeners.isEmpty();
	}

}
