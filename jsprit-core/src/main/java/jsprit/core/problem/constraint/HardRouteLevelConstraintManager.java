package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jsprit.core.problem.misc.JobInsertionContext;


class HardRouteLevelConstraintManager implements HardRouteStateLevelConstraint {

	private Collection<HardRouteStateLevelConstraint> hardConstraints = new ArrayList<HardRouteStateLevelConstraint>();
	
	public void addConstraint(HardRouteStateLevelConstraint constraint){
		hardConstraints.add(constraint);
	}
	
	Collection<HardRouteStateLevelConstraint> getConstraints(){ return Collections.unmodifiableCollection(hardConstraints); }

	@Override
	public boolean fulfilled(JobInsertionContext insertionContext) {
		for(HardRouteStateLevelConstraint constraint : hardConstraints){
			if(!constraint.fulfilled(insertionContext)){
				return false;
			}
		}
		return true;
	}
	
}