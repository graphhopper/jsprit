package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Simple activity list implementation.
 * <p>
 * The inserted activities will define an sequential order.
 * </p>
 *
 * @author balage
 */
public abstract class AbstractListBackedJobActivityList extends JobActivityList {

    /**
     * The primary container.
     */
    protected List<JobActivity> _activities = new ArrayList<>();
    /**
     * A read only container backed by the primary one. This will be returned.
     */
    protected List<JobActivity> unmodifiableActivities = Collections.unmodifiableList(_activities);

    public AbstractListBackedJobActivityList(AbstractJob job) {
        super(job);
    }

    @Override
    public void addActivity(JobActivity activity) {
        validateActivity(activity);
        if (!_activities.contains(activity)) {
            _activities.add(activity);
            activity.setOrderNumber(_activities.size());
        }
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
     * @param activity The activity to be found.
     * @return The index of the activity in the sequential row.
     * @throws IllegalArgumentException When the activity is not in the queue.
     */
    protected int indexOf(JobActivity activity) {
        int idx = _activities.indexOf(activity);
        if (idx == -1) {
            throw new IllegalArgumentException("Activity " + activity.getName() + " is not in the list.");
        }
        return idx;
    }

}
