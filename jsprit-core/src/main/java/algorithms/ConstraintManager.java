package algorithms;

import basics.route.TourActivity;

class ConstraintManager implements HardActivityLevelConstraint, HardRouteLevelConstraint{

	private HardActivityLevelConstraintManager actLevelConstraintManager = new HardActivityLevelConstraintManager();
	
	private HardRouteLevelConstraintManager routeLevelConstraintManager = new HardRouteLevelConstraintManager();
	
	public void addConstraint(HardActivityLevelConstraint actLevelConstraint){
		actLevelConstraintManager.addConstraint(actLevelConstraint);
	}
	
	public void addConstraint(HardRouteLevelConstraint routeLevelConstraint){
		routeLevelConstraintManager.addConstraint(routeLevelConstraint);
	}
	
	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		return routeLevelConstraintManager.fulfilled(insertionContext);
	}

	@Override
	public ConstraintsStatus fulfilled(InsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		return actLevelConstraintManager.fulfilled(iFacts, prevAct, newAct, nextAct, prevActDepTime);
	}
	
}