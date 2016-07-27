/*
 * Licensed to GraphHopper GmbH under one or more contributor
 * license agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * GraphHopper GmbH licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.graphhopper.jsprit.core.util;

import com.graphhopper.jsprit.core.problem.job.Job;
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
