package algorithms.constraints;

import java.util.ArrayList;
import java.util.Collection;

import algorithms.InsertionContext;
import basics.route.TourActivity;

class HardActivityLevelConstraintManager implements HardActivityLevelConstraint {

	private Collection<HardActivityLevelConstraint> hardConstraints = new ArrayList<HardActivityLevelConstraint>();
	
	public void addConstraint(HardActivityLevelConstraint constraint){
		hardConstraints.add(constraint);
	}
	
	@Override
	public boolean fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		for(HardActivityLevelConstraint constraint : hardConstraints){
			if(!constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime)){
				return false;
			}
		}
		return true;
	}
	
}