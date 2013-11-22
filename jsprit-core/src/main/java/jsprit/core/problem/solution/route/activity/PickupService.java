package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.job.Pickup;
import jsprit.core.problem.job.Service;

public final class PickupService implements PickupActivity{
	
	private Service pickup;
	
	private double arrTime;
	
	private double depTime;
	
	public PickupService(Pickup pickup) {
		super();
		this.pickup = pickup;
	}
	
	public PickupService(Service service){
		this.pickup = service;
	}
	
	private PickupService(PickupService pickupActivity){
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
		return new PickupService(this);
	}

	@Override
	public Service getJob() {
		return pickup;
	}

	@Override
	public int getCapacityDemand() {
		return pickup.getCapacityDemand();
	}

	@Override
	public String toString() {
		return "[act="+getName()+"][capDemand="+getCapacityDemand()+"][loc="+getLocationId()+"]";
	}

}
