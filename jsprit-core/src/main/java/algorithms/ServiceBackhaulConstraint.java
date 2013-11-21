package algorithms;

import basics.route.DeliverService;
import basics.route.DeliverShipment;
import basics.route.PickupService;
import basics.route.PickupShipment;
import basics.route.ServiceActivity;
import basics.route.TourActivity;

public class ServiceBackhaulConstraint implements HardActivityStateLevelConstraint {

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(newAct instanceof PickupService && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof ServiceActivity && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof DeliverService && prevAct instanceof PickupService){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof DeliverService && prevAct instanceof ServiceActivity){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		
		if(newAct instanceof DeliverService && prevAct instanceof PickupShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof DeliverService && prevAct instanceof DeliverShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		if(newAct instanceof PickupShipment && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED;}
		if(newAct instanceof DeliverShipment && nextAct instanceof DeliverService){ return ConstraintsStatus.NOT_FULFILLED;}
	
		return ConstraintsStatus.FULFILLED;
	}
		
}