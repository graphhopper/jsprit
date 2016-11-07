package com.graphhopper.jsprit.core.problem.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public class SequentialJobActivityList extends JobActivityList {

    /**
     * The primary container.
     */
    private List<JobActivity> _activities = new ArrayList<>();
    /**
     * A read only container backed by the primary one. This will be returned.
     */
    private List<JobActivity> unmodifiableActivities = Collections.unmodifiableList(_activities);

    public SequentialJobActivityList(AbstractJob job) {
        super(job);
    }

    @Override
    public void addActivity(JobActivity activity) {
        validateActivity(activity);
        _activities.add(activity);
    }

    @Override
    public int size() {
        return _activities.size();
    }

    @Override
    public List<JobActivity> getAll() {
        return unmodifiableActivities;
    }

    /**
     * @param activity
     *            The activity to be found.
     * @return The index of the activity in the sequential row.
     * @throws IllegalArgumentException
     *             When the activity is not in the queue.
     */
    private int indexOf(JobActivity activity) {
        int idx = _activities.indexOf(activity);
        if (idx == -1) {
            throw new IllegalArgumentException("Activity " + activity.getName() + " is not in the list.");
        }
        return idx;
    }

    @Override
    public List<JobActivity> getPreceding(JobActivity activity) {
        return unmodifiableActivities.subList(0, indexOf(activity));
    }

    @Override
    public List<JobActivity> getSubsequent(JobActivity activity) {
        return unmodifiableActivities.subList(indexOf(activity), unmodifiableActivities.size());
    }




}
