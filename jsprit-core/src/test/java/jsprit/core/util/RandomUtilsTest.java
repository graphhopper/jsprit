package jsprit.core.util;

import jsprit.core.problem.job.Job;
import junit.framework.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by schroeder on 14/01/15.
 */
public class RandomUtilsTest {

    @Test
    public void shouldReturnSingleJob() {
        Job job = mock(Job.class);
        Collection<Job> jobs = Arrays.asList(job);
        Assert.assertEquals(job, RandomUtils.nextItem(jobs, RandomNumberGeneration.getRandom()));
    }

    @Test
    public void shouldReturnSingleJob_() {
        Job job = mock(Job.class);
        Collection<Job> jobs = Arrays.asList(job);
        Assert.assertEquals(job, RandomUtils.nextJob(jobs, RandomNumberGeneration.getRandom()));
    }

    @Test
    public void shouldReturnJob3() {
        Job job3 = mock(Job.class);
        List<Job> jobs = Arrays.asList(mock(Job.class), mock(Job.class), job3);
        Random random = mock(Random.class);
        when(random.nextInt(jobs.size())).thenReturn(2);
        Assert.assertEquals(job3, RandomUtils.nextJob(jobs, random));
    }


}
