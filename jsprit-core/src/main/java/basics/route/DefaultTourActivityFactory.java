package basics.route;

import basics.Delivery;
import basics.Pickup;
import basics.Service;

public class DefaultTourActivityFactory implements TourActivityFactory{

	@Override
	public TourActivity createActivity(Service service) {
		TourActivity act;
		if(service instanceof Pickup){
			act = new PickupActivity((Pickup) service);
		}
		else if(service instanceof Delivery){
			act = new DeliveryActivity((Delivery) service);
		}
		else{
			act = ServiceActivity.newInstance(service);
		}
		return act;
	}

}
