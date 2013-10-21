package basics.route;

import basics.Job;
import basics.Shipment;

public final class DeliverShipment implements DeliveryActivity{

	private Shipment shipment;
	private double endTime;
	private double arrTime;
	
	public DeliverShipment(Shipment shipment) {
		super();
		this.shipment = shipment;
	}

	public DeliverShipment(DeliverShipment deliveryShipmentActivity) {
		this.shipment = (Shipment) deliveryShipmentActivity.getJob();
		this.arrTime = deliveryShipmentActivity.getArrTime();
		this.endTime = deliveryShipmentActivity.getEndTime();
	}

	@Override
	public Job getJob() {
		return shipment;
	}

	@Override
	public int getCapacityDemand() {
		return shipment.getSize()*-1;
	}

	@Override
	public String getName() {
		return "DeliverShipment";
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
}
