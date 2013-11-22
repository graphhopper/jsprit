package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;

public interface HardActivityStateLevelConstraint {
	
	static enum ConstraintsStatus {
		
		NOT_FULFILLED_BREAK, NOT_FULFILLED, FULFILLED;

	}
	
	public ConstraintsStatus fulfilled(JobInsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

}