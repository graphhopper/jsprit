package algorithms;

import java.util.ArrayList;
import java.util.Collection;

import algorithms.ConstraintManager.Priority;
import basics.route.TourActivity;

class HardActivityLevelConstraintManager implements HardActivityStateLevelConstraint {

	private Collection<HardActivityStateLevelConstraint> criticalConstraints = new ArrayList<HardActivityStateLevelConstraint>();
	
	private Collection<HardActivityStateLevelConstraint> highPrioConstraints = new ArrayList<HardActivityStateLevelConstraint>();
	
	private Collection<HardActivityStateLevelConstraint> lowPrioConstraints = new ArrayList<HardActivityStateLevelConstraint>();
	
	public void addConstraint(HardActivityStateLevelConstraint constraint, Priority priority){
		if(priority.equals(Priority.CRITICAL)){
			criticalConstraints.add(constraint);
		}
		else if(priority.equals(Priority.HIGH)){
			highPrioConstraints.add(constraint);
		}
		else{
			lowPrioConstraints.add(constraint);
		}
	}
	
	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		ConstraintsStatus notFulfilled = null;
		for(HardActivityStateLevelConstraint c : criticalConstraints){
			ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				return status;
			}
			else{
				if(status.equals(ConstraintsStatus.NOT_FULFILLED)){
					notFulfilled = status;
				}
			}
		}
		if(notFulfilled != null) return notFulfilled;
		
		for(HardActivityStateLevelConstraint c : highPrioConstraints){
			ConstraintsStatus status = c.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK)){
				return status;
			}
			else{
				if(status.equals(ConstraintsStatus.NOT_FULFILLED)){
					notFulfilled = status;
				}
			}
		}
		if(notFulfilled != null) return notFulfilled;
		
		for(HardActivityStateLevelConstraint constraint : lowPrioConstraints){
			ConstraintsStatus status = constraint.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
			if(status.equals(ConstraintsStatus.NOT_FULFILLED_BREAK) || status.equals(ConstraintsStatus.NOT_FULFILLED)){
				return status;
			}
		}
		
		return ConstraintsStatus.FULFILLED;
	}
	
}