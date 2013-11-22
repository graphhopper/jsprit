package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.job.Shipment;

public class DefaultShipmentActivityFactory implements TourShipmentActivityFactory{

	@Override
	public TourActivity createPickup(Shipment shipment) {
		return new PickupShipment(shipment);
	}

	@Override
	public TourActivity createDelivery(Shipment shipment) {
		return new DeliverShipment(shipment);
	}

}
