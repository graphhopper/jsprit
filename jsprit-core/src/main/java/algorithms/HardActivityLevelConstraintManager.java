package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import basics.route.TourActivity;

class HardActivityLevelConstraintManager implements HardActivityLevelConstraint {

	private Collection<HardActivityLevelConstraint> hardConstraints = new ArrayList<HardActivityLevelConstraint>();
	
	public void addConstraint(HardActivityLevelConstraint constraint){
		hardConstraints.add(constraint);
	}
	
	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		for(HardActivityLevelConstraint constraint : hardConstraints){
			ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)){
				return status;
			}
		}
		return ConstraintsStatus.FULFILLED;
	}
	
}