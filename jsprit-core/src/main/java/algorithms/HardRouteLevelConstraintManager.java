package algorithms;

import java.util.ArrayList;
import java.util.Collection;


class HardRouteLevelConstraintManager implements HardRouteStateLevelConstraint {

	private Collection<HardRouteStateLevelConstraint> hardConstraints = new ArrayList<HardRouteStateLevelConstraint>();
	
	public void addConstraint(HardRouteStateLevelConstraint constraint){
		hardConstraints.add(constraint);
	}

	@Override
	public boolean fulfilled(InsertionContext insertionContext) {
		for(HardRouteStateLevelConstraint constraint : hardConstraints){
			if(!constraint.fulfilled(insertionContext)){
				return false;
			}
		}
		return true;
	}
	
}