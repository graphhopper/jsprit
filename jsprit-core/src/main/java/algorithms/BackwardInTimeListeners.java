package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.TourActivity;
import basics.route.VehicleRoute;

class BackwardInTimeListeners {
	
	interface BackwardInTimeListener{
		
		public void start(VehicleRoute route);

		public void prevActivity(TourActivity act, double latestDepartureTime, double latestOperationStartTime);

		public void finnish();
		
	}
	
	private Collection<BackwardInTimeListener> listeners = new ArrayList<BackwardInTimeListener>();
	
	public void addListener(BackwardInTimeListener l){
		listeners.add(l);
	}
	
	public void start(VehicleRoute route){
		for(BackwardInTimeListener l : listeners){ l.start(route); }
	}
	
	/**
	 * Informs listener about nextActivity.
	 * 
	 * <p>LatestDepartureTime is the theoretical latest departureTime to meet the latestOperationStartTimeWindow at the nextActivity (forward in time), i.e.
	 * assume act_i and act_j are two successive activities and the latestDepTime of act_j is 10pm. With a travelTime from act_i to act_j of 1h the latestDepartureTime at act_i is 9pm.
	 * However, the latestOperationStartTime of act_i is 8pm, then (with a serviceTime of 0) the latestOperationStartTime at act_i amounts to 8pm.
	 * 
	 * @param act
	 * @param latestDepartureTime
	 * @param latestOperationStartTime
	 */
	public void prevActivity(TourActivity act, double latestDepartureTime, double latestOperationStartTime){
		for(BackwardInTimeListener l : listeners){ l.prevActivity(act,latestDepartureTime,latestOperationStartTime); }
	}
	
	public void finnish(){
		for(BackwardInTimeListener l : listeners){ l.finnish(); }
	}

}
