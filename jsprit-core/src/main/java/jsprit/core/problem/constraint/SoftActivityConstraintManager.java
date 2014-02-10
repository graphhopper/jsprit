package jsprit.core.problem.constraint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import jsprit.core.problem.misc.JobInsertionContext;
import jsprit.core.problem.solution.route.activity.TourActivity;

class SoftActivityConstraintManager implements SoftActivityConstraint{

	private Collection<SoftActivityConstraint> softConstraints = new ArrayList<SoftActivityConstraint>();
	
	public void addConstraint(SoftActivityConstraint constraint){
		softConstraints.add(constraint);
	}
	
	Collection<SoftActivityConstraint> getConstraints(){ return Collections.unmodifiableCollection(softConstraints); }
	
	@Override
	public double getCosts(JobInsertionContext iFacts, TourActivity prevAct,TourActivity newAct, TourActivity nextAct, double prevActDepTime) {
		double sumCosts = 0.0;
		for(SoftActivityConstraint c : softConstraints){
			sumCosts += c.getCosts(iFacts, prevAct, newAct, nextAct, prevActDepTime);
		}
		return sumCosts;
	}

}
