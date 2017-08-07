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
package com.graphhopper.jsprit.core.problem.solution.route.activity;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.DeliveryJob;

public class DeliveryActivityTest extends JobActivityTest {

    @Before
    public void doBefore() {
        service = new DeliveryJob.Builder("service").setLocation(Location.newInstance("loc"))
                        .setTimeWindow(TimeWindow.newInstance(1., 2.)).
                        setServiceTime(20d).
                        addSizeDimension(0, 10).addSizeDimension(1, 100).addSizeDimension(2, 1000).build();
        createActivity(service);
    }

    @Override
    @Test
    public void whenCallingCapacity_itShouldReturnCorrectCapacity() {
        assertEquals(-10, activity.getLoadChange().get(0));
        assertEquals(-100, activity.getLoadChange().get(1));
        assertEquals(-1000, activity.getLoadChange().get(2));
    }

}
