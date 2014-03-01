package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;

public final class DeliverShipment implements DeliveryActivity{

	private Shipment shipment;
	
	private double endTime;
	
	private double arrTime;
	
	private Capacity capacity;
	
	public DeliverShipment(Shipment shipment) {
		super();
		this.shipment = shipment;
		this.capacity = Capacity.invert(shipment.getSize());
	}

	public DeliverShipment(DeliverShipment deliveryShipmentActivity) {
		this.shipment = (Shipment) deliveryShipmentActivity.getJob();
		this.arrTime = deliveryShipmentActivity.getArrTime();
		this.endTime = deliveryShipmentActivity.getEndTime();
		this.capacity = deliveryShipmentActivity.getSize();
	}

	@Override
	public Job getJob() {
		return shipment;
	}

	/**
	 * @deprecated use <code>getCapacity()</code> instead
	 */
	@Deprecated
	@Override
	public int getCapacityDemand() {
		return shipment.getCapacityDemand()*-1;
	}

	@Override
	public String getName() {
		return "deliverShipment";
	}

	@Override
	public String getLocationId() {
		return shipment.getDeliveryLocation();
	}

	@Override
	public double getTheoreticalEarliestOperationStartTime() {
		return shipment.getDeliveryTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return shipment.getDeliveryTimeWindow().getEnd();
	}

	@Override
	public double getOperationTime() {
		return shipment.getDeliveryServiceTime();
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
		return new DeliverShipment(this);
	}

	@Override
	public String toString() {
		return "[act="+getName()+"][loc="+getLocationId()+"]";
	}

	@Override
	public Capacity getSize() {
		return capacity;
	}
}
