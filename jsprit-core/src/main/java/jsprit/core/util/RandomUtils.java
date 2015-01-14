package jsprit.core.util;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;
import java.util.Random;

/**
 * Created by schroeder on 14/01/15.
 */
public class RandomUtils {

    public static VehicleRoute nextRoute(Collection<VehicleRoute> routes, Random random){
        int randomIndex = random.nextInt(routes.size());
        int count = 0;
        for(VehicleRoute route : routes){
            if(count <= randomIndex) return route;
            count++;
        }
        return null;
    }

    public static Job nextJob(Collection<Job> jobs, Random random){
        int randomIndex = random.nextInt(jobs.size());
        int count = 0;
        for(Job job : jobs){
            if(count <= randomIndex) return job;
            count++;
        }
        return null;
    }

}
