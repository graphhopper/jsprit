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

    /**
     * Handshake class for C++ like friend visibility behavior emulation.
     *
     * <p>
     * This is not a class for the end-users. (To be frank, this class can't be
     * instantiate outside the parent task.
     * </p>
     *
     * <p>
     * Based on
     * {@link https://stackoverflow.com/questions/182278/is-there-a-way-to-simulate-the-c-friend-concept-in-java}
     * </p>
     *
     * @author Balage
     */
    // C++ like friend behavior simulation
    public final static class FriendlyHandshake {
        private FriendlyHandshake() {
        }
    }

    private static final FriendlyHandshake FRIENDLY_HANDSHAKE = new FriendlyHandshake();

    @Override
    public void addActivity(JobActivity activity) {
        validateActivity(activity);
        if (!_activities.contains(activity)) {
            _activities.add(activity);
            activity.impl_setOrderNumber(FRIENDLY_HANDSHAKE, _activities.size());
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
        if (idx == -1)
            throw new IllegalArgumentException("Activity " + activity.getName() + " is not in the list.");
        return idx;
    }

}
