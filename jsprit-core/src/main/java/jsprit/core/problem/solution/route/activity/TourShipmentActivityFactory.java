package jsprit.core.problem.solution.route.activity;

import jsprit.core.problem.job.Shipment;

public interface TourShipmentActivityFactory {
	
	public TourActivity createPickup(Shipment shipment);

	public TourActivity createDelivery(Shipment shipment);
	
}
