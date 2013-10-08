package algorithms.states;

import org.apache.log4j.Logger;

import algorithms.ActivityVisitor;
import basics.costs.ForwardTransportTime;
import basics.route.TourActivity;
import basics.route.VehicleRoute;

/**
 * Updates arrival and end times of activities. 
 * 
 * <p>Note that this modifies arrTime and endTime of each activity in a route.
 * 
 * @author stefan
 *
 */
public class UpdateActivityTimes implements ActivityVisitor{

	private Logger log = Logger.getLogger(UpdateActivityTimes.class);
	
	private ActivityTimeTracker timeTracker;
	
	private VehicleRoute route;
	
	/**
	 * Updates arrival and end times of activities. 
	 * 
	 * <p>Note that this modifies arrTime and endTime of each activity in a route.
	 * 
	 * <p>ArrTimes and EndTimes can be retrieved by <br>
	 * <code>activity.getArrTime()</code> and
	 * <code>activity.getEndTime()</code>
	 * 
	 * @author stefan
	 *
	 */
	public UpdateActivityTimes(ForwardTransportTime transportTime) {
		super();
		timeTracker = new ActivityTimeTracker(transportTime);
	}

	@Override
	public void begin(VehicleRoute route) {
		timeTracker.begin(route);
		this.route = route;
		route.getStart().setEndTime(timeTracker.getActEndTime());
	}

	@Override
	public void visit(TourActivity activity) {
		timeTracker.visit(activity);
		activity.setArrTime(timeTracker.getActArrTime());
		activity.setEndTime(timeTracker.getActEndTime());
	}

	@Override
	public void finish() {
		timeTracker.finish();
		route.getEnd().setArrTime(timeTracker.getActArrTime());
	}

}