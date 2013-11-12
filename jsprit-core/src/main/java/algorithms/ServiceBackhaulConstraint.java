package algorithms;

import basics.route.DeliveryActivity;
import basics.route.PickupActivity;
import basics.route.ServiceActivity;
import basics.route.Start;
import basics.route.TourActivity;

public class ServiceBackhaulConstraint implements HardActivityStateLevelConstraint {

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(newAct instanceof PickupActivity && nextAct instanceof DeliveryActivity){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof ServiceActivity && nextAct instanceof DeliveryActivity){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof DeliveryActivity && prevAct instanceof PickupActivity){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof DeliveryActivity && prevAct instanceof ServiceActivity){ return ConstraintsStatus.NOT_FULFILLED; }
		return ConstraintsStatus.FULFILLED;
	}
		
}