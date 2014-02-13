package jsprit.core.problem.constraint;

import jsprit.core.problem.misc.JobInsertionContext;

public interface SoftRouteConstraint extends SoftConstraint{
	
	public double getCosts(JobInsertionContext insertionContext);

}
