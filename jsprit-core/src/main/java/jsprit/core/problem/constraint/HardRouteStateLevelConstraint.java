package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;


public interface HardRouteStateLevelConstraint {

	public boolean fulfilled(JobInsertionContext insertionContext);
	
}