package basics.route;

import basics.Pickup;
import basics.route.TourActivity.JobActivity;

public class PickupActivity implements JobActivity<Pickup>{
 
	private Pickup pickup;
	
	private double arrTime;
	
	private double depTime;
	
	public PickupActivity(Pickup pickup) {
		super();
		this.pickup = pickup;
	}
	
	private PickupActivity(PickupActivity pickupActivity){
		this.pickup=pickupActivity.getJob();
		this.arrTime=pickupActivity.getArrTime();
		this.depTime=pickupActivity.getEndTime();
	}

	@Override
	public String getName() {
		return pickup.getType();
	}

	@Override
	public String getLocationId() {
		return pickup.getLocationId();
	}

	@Override
	public double getTheoreticalEarliestOperationStartTime() {
		return pickup.getTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return pickup.getTimeWindow().getEnd();
	}

	@Override
	public double getOperationTime() {
		return pickup.getServiceDuration();
	}

	@Override
	public double getArrTime() {
		return arrTime;
	}

	@Override
	public double getEndTime() {
		return depTime;
	}

	@Override
	public void setArrTime(double arrTime) {
		this.arrTime=arrTime;
	}

	@Override
	public void setEndTime(double endTime) {
		this.depTime=endTime;
	}

	@Override
	public TourActivity duplicate() {
		return new PickupActivity(this);
	}

	@Override
	public Pickup getJob() {
		return pickup;
	}

	@Override
	public int getCapacityDemand() {
		return pickup.getCapacityDemand();
	}

	
}
