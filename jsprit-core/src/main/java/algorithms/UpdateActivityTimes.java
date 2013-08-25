package algorithms;

import org.apache.log4j.Logger;

import algorithms.ForwardInTimeListeners.ForwardInTimeListener;
import basics.route.End;
import basics.route.Start;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

class UpdateActivityTimes implements ForwardInTimeListener{

	private Logger log = Logger.getLogger(UpdateActivityTimes.class);
	
	@Override
	public void start(VehicleRoute route, Start start, double departureTime) {
		start.setEndTime(departureTime);
	}

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
		act.setArrTime(arrTime);
		act.setEndTime(endTime);
	}

	@Override
	public void end(End end, double arrivalTime) {
		end.setArrTime(arrivalTime);
	}

}
