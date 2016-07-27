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

package com.graphhopper.jsprit.core.algorithm.termination;

import junit.framework.Assert;
import org.junit.Test;

/**
 * Created by schroeder on 16.12.14.
 */
public class TimeTerminationTest {

    @Test
    public void whenTimeThreshold2000msAndCurrentTime0_itShouldNotBreak() {
        long threshold = 2000;
        TimeTermination tt = new TimeTermination(threshold);
        tt.setTimeGetter(new TimeTermination.TimeGetter() {
            @Override
            public long getCurrentTime() {
                return 0;
            }
        });
        tt.start(0);
        Assert.assertFalse(tt.isPrematureBreak(null));
    }

    @Test
    public void whenTimeThreshold2000msAndCurrentTime2000ms_itShouldBreak() {
        long threshold = 2000;
        TimeTermination tt = new TimeTermination(threshold);
        tt.setTimeGetter(new TimeTermination.TimeGetter() {
            @Override
            public long getCurrentTime() {
                return 2001;
            }
        });
        tt.start(0);
        Assert.assertTrue(tt.isPrematureBreak(null));
    }
}
