package com.graphhopper.jsprit.core.problem.job;

import com.graphhopper.jsprit.core.problem.solution.route.activity.InternalActivityMarker;
import com.graphhopper.jsprit.core.problem.solution.route.activity.JobActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Abstract ancestor of the container implementations for activities of an {@linkplain AbstractJob}.
 * <p>
 * <p>
 * It provides functions to query all, the preceding or subsequent activities.
 * </p>
 *
 * @author balage
 */
public abstract class JobActivityList {

    //    TODO getRElation of two activities: PRIOR, SUBSEQUENT or UNRELATED

    private AbstractJob job;

    public JobActivityList(AbstractJob job) {
        super();
        this.job = job;
    }

    /**
     * @return The job the activity list is associated to.
     */
    public AbstractJob getJob() {
        return job;
    }

    /**
     * Adds an activity to the list.
     *
     * @param activity The activity to insert.
     */
    public abstract void addActivity(JobActivity activity);


    /**
     * Validates that an activity could be inserted to the queue.
     *
     * @param activity The activity to insert.
     */
    protected void validateActivity(JobActivity activity) {
        // Internal activities can be inserted only when the job itself is internal
        if (activity instanceof InternalActivityMarker && !(job instanceof InternalJobMarker)) {
            throw new IllegalArgumentException("Can't add an internal activity to a non-internal job: " + activity.getClass().getCanonicalName());
        }
        // The job of the activity should be the same as the job of the list
        if (!activity.getJob().equals(job)) {
            throw new IllegalArgumentException("The activity " + activity.getName() + " is not associated with this job.");
        }
    }

    /**
     * @return The number of activities.
     */
    public abstract int size();

    /**
     * @return All activities.
     */
    public abstract List<JobActivity> getAll();

    /**
     * Returns all the activities to be done before the <code>activity</code>.
     *
     * @param activity The activity to compare to.
     * @return The list of the preceding activities.
     */
    public abstract Set<JobActivity> getPreceding(JobActivity activity);

    /**
     * Returns all the activities to be done after the <code>activity</code>.
     *
     * @param activity The activity to compare to.
     * @return The list of the subsequent activities.
     */
    public abstract Set<JobActivity> getSubsequent(JobActivity activity);

    /**
     * @return Returns the duplicated copy of the activities.
     */
    public List<JobActivity> getAllDuplicated() {
        List<JobActivity> acts = new ArrayList<>();
        for (JobActivity act : getAll()) {
            acts.add((JobActivity) act.duplicate());
        }
        return acts;
    }

    /**
     * @return Returns all possible orderings.
     */
    public abstract Set<List<JobActivity>> getPossibleOrderings();

    public Optional<JobActivity> findByType(String type) {
        return getAll().stream().filter(a -> a.getType().equals(type)).findFirst();
    }

    public Optional<JobActivity> findByName(String name) {
        return getAll().stream().filter(a -> a.getName().equals(name)).findFirst();
    }

}
