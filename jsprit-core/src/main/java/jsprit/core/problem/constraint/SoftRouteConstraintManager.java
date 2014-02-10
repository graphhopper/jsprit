package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jsprit.core.problem.misc.JobInsertionContext;

class SoftRouteConstraintManager implements SoftRouteConstraint{

	private Collection<SoftRouteConstraint> softConstraints = new ArrayList<SoftRouteConstraint>();
	
	public void addConstraint(SoftRouteConstraint constraint){
		softConstraints.add(constraint);
	}
	
	Collection<SoftRouteConstraint> getConstraints(){ return Collections.unmodifiableCollection(softConstraints); }
	
	@Override
	public double getCosts(JobInsertionContext insertionContext) {
		double sumCosts = 0.0;
		for(SoftRouteConstraint c : softConstraints){
			sumCosts += c.getCosts(insertionContext);
		}
		return sumCosts;
	}

}
