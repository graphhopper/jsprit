package com.graphhopper.jsprit.core.problem.job;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

/**
 * Simple activity list implementation.
 * <p>
 * The inserted activities will define an sequential order.
 * </p>
 *
 * @author balage
 *
 */
public class SequentialJobActivityList extends AbstractListBackedJobActivityList {

    public SequentialJobActivityList(AbstractJob job) {
        super(job);
    }

    @Override
    public Set<JobActivity> getPreceding(JobActivity activity) {
        return new HashSet<>(unmodifiableActivities.subList(0, indexOf(activity)));
    }

    @Override
    public Set<JobActivity> getSubsequent(JobActivity activity) {
        return new HashSet<>(unmodifiableActivities.subList(indexOf(activity), unmodifiableActivities.size()));
    }

    @Override
    public Set<List<JobActivity>> getPossibleOrderings() {
        Set<List<JobActivity>> res = new HashSet<List<JobActivity>>();
        res.add(unmodifiableActivities);
        return res;
    }


}
