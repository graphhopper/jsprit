package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import jsprit.core.problem.constraint.ConstraintManager.Priority;
import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;


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
	
	Collection<HardActivityStateLevelConstraint> getCriticalConstraints(){ return Collections.unmodifiableCollection(criticalConstraints); }
	
	Collection<HardActivityStateLevelConstraint> getHighPrioConstraints(){ return Collections.unmodifiableCollection(highPrioConstraints); }
	
	Collection<HardActivityStateLevelConstraint> getLowPrioConstraints(){ return Collections.unmodifiableCollection(lowPrioConstraints); }
	
	Collection<HardActivityStateLevelConstraint> getAllConstraints(){
		List<HardActivityStateLevelConstraint> c = new ArrayList<HardActivityStateLevelConstraint>();
		c.addAll(criticalConstraints);
		c.addAll(highPrioConstraints);
		c.addAll(lowPrioConstraints);
		return Collections.unmodifiableCollection(c); 
	}
	
	@Override
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
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