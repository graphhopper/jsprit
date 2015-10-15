package jsprit.core.algorithm.recreate;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

/**
 * Created by schroeder on 15/10/15.
 */
class ScoredJob {

    static class BadJob extends ScoredJob {

        BadJob(Job job) {
            super(job, 0., null, null, false);
        }
    }

    private Job job;

    private double score;

    private InsertionData insertionData;

    private VehicleRoute route;

    private boolean newRoute;


    ScoredJob(Job job, double score, InsertionData insertionData, VehicleRoute route, boolean isNewRoute) {
        this.job = job;
        this.score = score;
        this.insertionData = insertionData;
        this.route = route;
        this.newRoute = isNewRoute;
    }

    public boolean isNewRoute() {
        return newRoute;
    }

    public Job getJob() {
        return job;
    }

    public double getScore() {
        return score;
    }

    public InsertionData getInsertionData() {
        return insertionData;
    }

    public VehicleRoute getRoute() {
        return route;
    }

}
