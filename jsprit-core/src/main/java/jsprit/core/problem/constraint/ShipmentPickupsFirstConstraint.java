package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.activity.TourActivity;

public class ShipmentPickupsFirstConstraint implements HardActivityStateLevelConstraint {

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		if(newAct instanceof DeliverShipment && nextAct instanceof PickupShipment){ return ConstraintsStatus.NOT_FULFILLED; }
		if(newAct instanceof PickupShipment && prevAct instanceof DeliverShipment){ return ConstraintsStatus.NOT_FULFILLED_BREAK; }
		return ConstraintsStatus.FULFILLED;
	}
		
}