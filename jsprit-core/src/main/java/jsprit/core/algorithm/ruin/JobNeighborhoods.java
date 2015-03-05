package jsprit.core.algorithm.ruin;

import jsprit.core.problem.job.Job;

import java.util.Iterator;

/**
* Created by schroeder on 07/01/15.
*/
public interface JobNeighborhoods {

    public Iterator<Job> getNearestNeighborsIterator(int nNeighbors, Job neighborTo);

    public void initialise();

}
