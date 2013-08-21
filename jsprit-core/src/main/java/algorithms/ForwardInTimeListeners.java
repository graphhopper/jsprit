package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

class ForwardInTimeListeners {
	
	interface ForwardInTimeListener{
		
		public void start(VehicleRoute route);

		public void nextActivity(TourActivity act, double arrTime,double endTime);

		public void finnish();
		
	}
	
	private Collection<ForwardInTimeListener> listeners = new ArrayList<ForwardInTimeListeners.ForwardInTimeListener>();
	
	public void addListener(ForwardInTimeListener l){
		listeners.add(l);
	}
	
	public void start(VehicleRoute route){
		for(ForwardInTimeListener l : listeners){ l.start(route); }
	}
	
	public void nextActivity(TourActivity act, double arrTime, double endTime){
		for(ForwardInTimeListener l : listeners){ l.nextActivity(act,arrTime,endTime); }
	}
	
	public void finnish(){
		for(ForwardInTimeListener l : listeners){ l.finnish(); }
	}

}
