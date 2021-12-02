package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.util.RandomNumberGeneration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class JobsInsertionSorter {
    private static Logger logger = LoggerFactory.getLogger(JobsInsertionSorter.class);
    protected static Random random = RandomNumberGeneration.getRandom();
    final double ratioToSort;

    public JobsInsertionSorter(double ratioToSort) {
        this.ratioToSort = ratioToSort;
    }

    public List<Job> getJobsSortedInInsertionOrder(List<Job> jobsToInsert, final Map<String, Integer> nearestUnassigned) {
        Comparator<Job> withMostNeighborsComparator = new Comparator<Job>() {
            @Override
            public int compare(Job job1, Job job2) {
                return Double.compare(nearestUnassigned.get(job2.getId()), nearestUnassigned.get(job1.getId()));
            }
        };
        try {
            Collections.shuffle(jobsToInsert);
            if (random.nextDouble() <= ratioToSort) {
                Collections.sort(jobsToInsert, withMostNeighborsComparator);
            }
        } catch (Exception e) {
            logger.error("failed to sort", e);
        }
        return jobsToInsert;
    }

    public List<Job> getSortedJobNeighborsForInsertionInSameRoute(Job job, List<Job> jobsToInsert) {
        return jobsToInsert;
    }
}
