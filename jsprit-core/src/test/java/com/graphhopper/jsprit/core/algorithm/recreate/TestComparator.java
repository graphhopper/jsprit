package com.graphhopper.jsprit.core.algorithm.recreate;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.job.Service;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by schroeder on 24/05/16.
 */
public class TestComparator {

    @Test
    public void test(){
        Service s = Service.Builder.newInstance("1").setLocation(Location.newInstance("loc"))
            .setPriority(1).build();
        Service s2 = Service.Builder.newInstance("2").setLocation(Location.newInstance("loc"))
            .setPriority(2).build();
        Service s3 = Service.Builder.newInstance("3").setLocation(Location.newInstance("loc"))
            .setPriority(3).build();
        Service s4 = Service.Builder.newInstance("4").setLocation(Location.newInstance("loc"))
            .setPriority(1).build();
        List<Job> jobs = new ArrayList<Job>();
        jobs.add(s2);
        jobs.add(s3);
        jobs.add(s4);
        jobs.add(s);
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return o1.getPriority() - o2.getPriority();
            }
        });

        for(Job j : jobs){
            System.out.println(j.getId());
        }
    }
}
