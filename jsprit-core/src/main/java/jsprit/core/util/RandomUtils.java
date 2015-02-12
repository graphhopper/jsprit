package jsprit.core.util;

import jsprit.core.problem.job.Job;
import jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by schroeder on 14/01/15.
 */
public class RandomUtils {

    public static VehicleRoute nextRoute(Collection<VehicleRoute> routes, Random random){
        return nextItem(routes,random);
    }

    public static Job nextJob(Collection<Job> jobs, Random random){
        return nextItem(jobs,random);
    }

    public static Job nextJob(List<Job> jobs, Random random){
        return nextItem(jobs,random);
    }

    public static <T> T nextItem(Collection<T> items, Random random){
        int randomIndex = random.nextInt(items.size());
        int count = 0;
        for(T item : items){
            if(count == randomIndex) return item;
            count++;
        }
        return null;
    }

    public static <T> T nextItem(List<T> items, Random random){
        int randomIndex = random.nextInt(items.size());
        return items.get(randomIndex);
    }

}
