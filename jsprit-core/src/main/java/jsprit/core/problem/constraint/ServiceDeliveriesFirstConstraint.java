package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.DeliverService;
import jsprit.core.problem.solution.route.activity.DeliverShipment;
import jsprit.core.problem.solution.route.activity.PickupService;
import jsprit.core.problem.solution.route.activity.PickupShipment;
import jsprit.core.problem.solution.route.activity.ServiceActivity;
import jsprit.core.problem.solution.route.activity.TourActivity;

public class ServiceDeliveriesFirstConstraint implements HardActivityStateLevelConstraint {

	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
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