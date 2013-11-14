package algorithms;

import basics.route.DeliverShipment;
import basics.route.PickupShipment;
import basics.route.TourActivity;

public class ShipmentPickupsFirstConstraint implements HardActivityStateLevelConstraint {

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(newAct instanceof DeliverShipment && nextAct instanceof PickupShipment){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof PickupShipment && prevAct instanceof DeliverShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		return ConstraintsStatus.FULFILLED;
	}
		
}