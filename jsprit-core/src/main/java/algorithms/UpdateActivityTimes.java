package algorithms;

import org.apache.log4j.Logger;

import basics.route.TourActivity;
import basics.route.VehicleRoute;
import algorithms.ForwardInTimeListeners.ForwardInTimeListener;

class UpdateActivityTimes implements ForwardInTimeListener{

	private Logger log = Logger.getLogger(UpdateActivityTimes.class);
	
	@Override
	public void start(VehicleRoute route) {}

	@Override
	public void nextActivity(TourActivity act, double arrTime, double endTime) {
//		log.debug(act.toString() + " arrTime="+ arrTime + " endTime=" + endTime);
		act.setArrTime(arrTime);
		act.setEndTime(endTime);
	}

	@Override
	public void finnish() {}

}
