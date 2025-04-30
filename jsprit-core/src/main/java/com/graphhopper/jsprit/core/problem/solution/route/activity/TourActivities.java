//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.graphhopper.jsprit.core.problem.solution.route.activity;

import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.*;

public class TourActivities {
    private final ArrayList<TourActivity> tourActivities = new ArrayList<>();
    private final Set<Job> jobs = new HashSet<>();
    private ReverseActivityIterator backward;
    // Cache size to avoid frequent ArrayList.size() calls
    private int cachedSize = 0;

    public static TourActivities copyOf(TourActivities tourActivities) {
        return new TourActivities(tourActivities);
    }

    private TourActivities(TourActivities tour2copy) {
        for (TourActivity tourAct : tour2copy.getActivities()) {
            TourActivity newAct = tourAct.duplicate();
            this.tourActivities.add(newAct);
            this.addJob(newAct);
        }
        this.cachedSize = this.tourActivities.size();
    }

    public TourActivities() {
    }

    public List<TourActivity> getActivities() {
        return Collections.unmodifiableList(this.tourActivities);
    }

    public Iterator<TourActivity> iterator() {
        final Iterator<TourActivity> iterator = this.tourActivities.iterator();
        return new Iterator<TourActivity>() {
            private TourActivity lastReturned = null;

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public TourActivity next() {
                return this.lastReturned = (TourActivity) iterator.next();
            }

            public void remove() {
                if (this.lastReturned instanceof TourActivity.JobActivity) {
                    throw new IllegalStateException("Cannot remove JobActivities via iterator. Use TourActivities.removeActivity(), or alternatively, consider TourActivities.removeJob()");
                } else {
                    iterator.remove();
                    TourActivities.this.cachedSize--; // Update cached size on removal
                }
            }
        };
    }

    public boolean isEmpty() {
        return this.cachedSize == 0;
    }

    public Collection<Job> getJobs() {
        return Collections.unmodifiableSet(this.jobs);
    }

    public boolean servesJob(Job job) {
        return this.jobs.contains(job);
    }

    public String toString() {
        return "[nuOfActivities=" + this.cachedSize + "]";
    }

    public boolean removeJob(Job job) {
        if (!this.jobs.contains(job)) {
            return false;
        } else {
            boolean jobRemoved = this.jobs.remove(job);
            boolean activityRemoved = false;
            Iterator<TourActivity> iterator = this.tourActivities.iterator();

            while (iterator.hasNext()) {
                TourActivity c = (TourActivity) iterator.next();
                if (c instanceof TourActivity.JobActivity) {
                    Job underlyingJob = ((TourActivity.JobActivity) c).getJob();
                    if (job.equals(underlyingJob)) {
                        iterator.remove();
                        this.cachedSize--; // Update cached size on removal
                        activityRemoved = true;
                    }
                }
            }

            assert jobRemoved == activityRemoved : "job removed, but belonging activity not.";

            return activityRemoved;
        }
    }

    public boolean removeActivity(TourActivity activity) {
        if (!(activity instanceof TourActivity.JobActivity)) {
            boolean removed = this.tourActivities.remove(activity);
            if (removed) {
                this.cachedSize--; // Update cached size on removal
            }
            return removed;
        } else {
            Job job = ((TourActivity.JobActivity) activity).getJob();
            boolean jobIsAlsoAssociateToOtherActs = false;
            boolean actRemoved = false;

            for (TourActivity act : new ArrayList<>(this.tourActivities)) {
                if (act == activity) {
                    this.tourActivities.remove(act);
                    this.cachedSize--; // Update cached size on removal
                    if (jobIsAlsoAssociateToOtherActs) {
                        return true;
                    }

                    actRemoved = true;
                } else if (act instanceof TourActivity.JobActivity && ((TourActivity.JobActivity) act).getJob().equals(job)) {
                    if (actRemoved) {
                        return true;
                    }

                    jobIsAlsoAssociateToOtherActs = true;
                }
            }

            if (actRemoved) {
                this.jobs.remove(job);
            }

            return actRemoved;
        }
    }

    public void addActivity(int insertionIndex, TourActivity act) {
        assert insertionIndex >= 0 : "insertionIndex < 0, this cannot be";

        // Update cached size on addition
        if (insertionIndex < this.cachedSize) {
            this.tourActivities.add(insertionIndex, act);
        } else {
            this.tourActivities.add(act);
        }
        this.cachedSize++; // Update cached size on addition

        this.addJob(act);
    }

    public void addActivity(TourActivity act) {
        if (this.tourActivities.contains(act)) {
            throw new IllegalArgumentException("act " + String.valueOf(act) + " already in tour. cannot add act twice.");
        } else {
            this.tourActivities.add(act);
            this.cachedSize++; // Update cached size on addition
            this.addJob(act);
        }
    }

    private void addJob(TourActivity act) {
        if (act instanceof TourActivity.JobActivity) {
            Job job = ((TourActivity.JobActivity) act).getJob();
            this.jobs.add(job);
        }
    }

    public int jobSize() {
        return this.jobs.size();
    }

    // Added method to get size without calling ArrayList.size()
    public int size() {
        return this.cachedSize;
    }

    public Iterator<TourActivity> reverseActivityIterator() {
        if (this.backward == null) {
            this.backward = new ReverseActivityIterator(this.tourActivities);
        } else {
            this.backward.reset();
        }

        return this.backward;
    }

    public static class ReverseActivityIterator implements Iterator<TourActivity> {
        private final List<TourActivity> acts;
        private int currentIndex;

        public ReverseActivityIterator(List<TourActivity> acts) {
            this.acts = acts;
            this.currentIndex = acts.size() - 1;
        }

        public boolean hasNext() {
            return this.currentIndex >= 0;
        }

        public TourActivity next() {
            TourActivity act = (TourActivity) this.acts.get(this.currentIndex);
            --this.currentIndex;
            return act;
        }

        public void reset() {
            this.currentIndex = this.acts.size() - 1;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
