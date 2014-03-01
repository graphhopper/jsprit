package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.Capacity;
import jsprit.core.problem.job.Job;
import jsprit.core.problem.job.Shipment;

public final class PickupShipment implements PickupActivity{

	private Shipment shipment;
	
	private double endTime;
	
	private double arrTime;
	
	public PickupShipment(Shipment shipment) {
		super();
		this.shipment = shipment;
	}

	public PickupShipment(PickupShipment pickupShipmentActivity) {
		this.shipment = (Shipment) pickupShipmentActivity.getJob();
		this.arrTime = pickupShipmentActivity.getArrTime();
		this.endTime = pickupShipmentActivity.getEndTime();
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
		return shipment.getCapacityDemand();
	}

	@Override
	public String getName() {
		return "pickupShipment";
	}

	@Override
	public String getLocationId() {
		return shipment.getPickupLocation();
	}

	@Override
	public double getTheoreticalEarliestOperationStartTime() {
		return shipment.getPickupTimeWindow().getStart();
	}

	@Override
	public double getTheoreticalLatestOperationStartTime() {
		return shipment.getPickupTimeWindow().getEnd();
	}

	@Override
	public double getOperationTime() {
		return shipment.getPickupServiceTime();
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
		return new PickupShipment(this);
	}
	
	@Override
	public String toString() {
		return "[act="+getName()+"][loc="+getLocationId()+"]";
	}

	@Override
	public Capacity getSize() {
		return shipment.getSize();
	}



}
