package algorithms;

import basics.costs.ForwardTransportTime;
import basics.route.Driver;
import basics.route.TourActivity;
import basics.route.Vehicle;
import basics.route.VehicleRoute;

class ActivityTimeTracker implements ActivityVisitor{

	private ForwardTransportTime transportTime;
	
	private TourActivity prevAct = null;
	
	private double startAtPrevAct;
	
	private VehicleRoute route;
	
	private boolean beginFirst = false;
	
	private double actArrTime;
	
	private double actEndTime;
	
	public ActivityTimeTracker(ForwardTransportTime transportTime) {
		super();
		this.transportTime = transportTime;
	}

	public double getActArrTime(){
		return actArrTime;
	}
	
	public double getActEndTime(){
		return actEndTime;
	}
	
	@Override
	public void begin(VehicleRoute route) {
		prevAct = route.getStart(); 
		startAtPrevAct = prevAct.getEndTime();
		
		actEndTime = startAtPrevAct;
		
		this.route = route;
		
		beginFirst = true;
	}

	@Override
	public void visit(TourActivity activity) {
		if(!beginFirst) throw new IllegalStateException("never called begin. this however is essential here");
		double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), activity.getLocationId(), startAtPrevAct, route.getDriver(), route.getVehicle());
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
		
		actArrTime = arrivalTimeAtCurrAct;
		
		double operationStartTime = Math.max(activity.getTheoreticalEarliestOperationStartTime(), arrivalTimeAtCurrAct);
		double operationEndTime = operationStartTime + activity.getOperationTime();
		
		actEndTime = operationEndTime;
		
		prevAct = activity;
		startAtPrevAct = operationEndTime;

	}

	@Override
	public void finish() {
		double transportTime = this.transportTime.getTransportTime(prevAct.getLocationId(), route.getEnd().getLocationId(), startAtPrevAct, route.getDriver(), route.getVehicle());
		double arrivalTimeAtCurrAct = startAtPrevAct + transportTime; 
		
		actArrTime = arrivalTimeAtCurrAct;
		actEndTime = arrivalTimeAtCurrAct;
		
		beginFirst = false;
	}
	
	
	

}
