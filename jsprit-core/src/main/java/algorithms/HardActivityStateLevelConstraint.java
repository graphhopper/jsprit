package algorithms;

import basics.route.TourActivity;

public interface HardActivityStateLevelConstraint {
	
	static enum ConstraintsStatus {
		
		NOT_FULFILLED_BREAK, NOT_FULFILLED, FULFILLED;

	}
	
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct, TourActivity newAct, TourActivity nextAct, double prevActDepTime);

}