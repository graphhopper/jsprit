package jsprit.core.problem;

import jsprit.core.problem.job.Job;

/**
 * Created by schroeder on 14.07.14.
 */
public abstract class AbstractJob implements Job {

    private int index;

    public int getIndex(){ return index; }

    protected void setIndex(int index){ this.index = index; }

}
