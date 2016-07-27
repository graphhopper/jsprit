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
import com.graphhopper.jsprit.core.problem.solution.route.VehicleRoute;

import java.util.Collection;
import java.util.List;
import java.util.Random;

/**
 * Created by schroeder on 14/01/15.
 */
public class RandomUtils {

    public static VehicleRoute nextRoute(Collection<VehicleRoute> routes, Random random) {
        return nextItem(routes, random);
    }

    public static Job nextJob(Collection<Job> jobs, Random random) {
        return nextItem(jobs, random);
    }

    public static Job nextJob(List<Job> jobs, Random random) {
        return nextItem(jobs, random);
    }

    public static <T> T nextItem(Collection<T> items, Random random) {
        int randomIndex = random.nextInt(items.size());
        int count = 0;
        for (T item : items) {
            if (count == randomIndex) return item;
            count++;
        }
        return null;
    }

    public static <T> T nextItem(List<T> items, Random random) {
        int randomIndex = random.nextInt(items.size());
        return items.get(randomIndex);
    }

}
