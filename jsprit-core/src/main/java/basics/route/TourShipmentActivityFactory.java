package basics.route;

import basics.Shipment;

public interface TourShipmentActivityFactory {
	
	public TourActivity createPickup(Shipment shipment);

	public TourActivity createDelivery(Shipment shipment);
	
}
