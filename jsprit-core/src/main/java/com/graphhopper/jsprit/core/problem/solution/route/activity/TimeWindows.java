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

import java.util.Collection;

/**
 * Created by schroeder on 20/05/15.
 */
public interface TimeWindows {

    /**
     * A TimeWindows implementation which contains a single time window of
     * eternity.
     */
    public final TimeWindows ANY_TIME = TimeWindowsImpl.INTERNAL_ANY_TIME;

    public Collection<TimeWindow> getTimeWindows();


    public static TimeWindows of(TimeWindow tw) {
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(tw);
        return tws;
    }

    public static TimeWindows of(TimeWindow tw, TimeWindow tw2) {
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(tw);
        tws.add(tw2);
        return tws;
    }

    public static TimeWindows of(TimeWindow tw, TimeWindow tw2, TimeWindow... others) {
        TimeWindowsImpl tws = new TimeWindowsImpl();
        tws.add(tw);
        tws.add(tw2);
        for (TimeWindow otw : others) {
            tws.add(otw);
        }
        return tws;
    }

}
