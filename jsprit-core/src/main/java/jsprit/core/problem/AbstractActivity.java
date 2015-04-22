package jsprit.core.problem;

import jsprit.core.problem.solution.route.activity.TourActivity;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractActivity implements TourActivity {

    private int index;
	private double operationTime = -1;

    public int getIndex(){ return index; }

    protected void setIndex(int index){ this.index = index; }

	@Override
	public double getOperationTime() {
		return operationTime;
	}

	public void setOperationTime(double operationTime) {
		this.operationTime = operationTime;
	}
}
