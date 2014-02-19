package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Delivery;

public final class DeliverService implements DeliveryActivity{
	
	private Delivery delivery;
	
	private Capacity capacity;
	
	private double arrTime;
	
	private double endTime;
	
	public DeliverService(Delivery delivery) {
		super();
		this.delivery = delivery;
		capacity = Capacity.invert(delivery.getCapacity());
	}
	
	private DeliverService(DeliverService deliveryActivity){
		this.delivery=deliveryActivity.getJob();
		this.arrTime=deliveryActivity.getArrTime();
		this.endTime=deliveryActivity.getEndTime();
		capacity = deliveryActivity.getCapacity();
	}

	/**
	 * @deprecated use <code>getCapacity()</code> instead
	 */
	@Deprecated
	@Override
	public int getCapacityDemand() {
		return delivery.getCapacityDemand()*-1;
	}

	@Override
	public String getName() {
		return delivery.getType();
	}

	@Override
	public String getLocationId() {
		return delivery.getLocationId();
	}

	@Override
	public double getTheoreticalEarliestOperationStartTime() {
		return delivery.getTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return delivery.getTimeWindow().getEnd();
	}

	@Override
	public double getOperationTime() {
		return delivery.getServiceDuration();
	}

	@Override
	public double getArrTime() {
		return arrTime;
	}

	@Override
	public double getEndTime() {
		return endTime;
	}

	@Override
	public void setArrTime(double arrTime) {
		this.arrTime=arrTime;
	}

	@Override
	public void setEndTime(double endTime) {
		this.endTime=endTime;
	}

	@Override
	public TourActivity duplicate() {
		return new DeliverService(this);
	}

	@Override
	public Delivery getJob() {
		return delivery;
	}

	@Override
	public String toString() {
		return "[act="+getName()+"][capDemand="+getCapacityDemand()+"][loc="+getLocationId()+"]";
	}

	@Override
	public Capacity getCapacity() {
		return capacity;
	}
}
