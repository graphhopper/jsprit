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

import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.Location;

public final class Start extends InternalActivity {

    public static Start newInstance(String locationId, double theoreticalStart, double theoreticalEnd) {
        Location loc = null;
        if (locationId != null) {
            loc = Location.Builder.newInstance().setId(locationId).build();
        }
        return new Start(loc, theoreticalStart, theoreticalEnd);
    }

    public static Start copyOf(Start start) {
        return new Start(start);
    }

    public Start(Location location, double theoreticalStart, double theoreticalEnd) {
        super("start", location, SizeDimension.EMPTY);
        setTheoreticalEarliestOperationStartTime(theoreticalStart);
        setTheoreticalLatestOperationStartTime(theoreticalEnd);
        endTime = theoreticalStart;
        setIndex(-1);
    }

    private Start(Start start) {
        super(start);
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public double getOperationTime() {
        return 0.0;
    }

    @Override
    public String toString() {
        return "[type=" + getName() + "][location=" + location
            + "][twStart=" + Activities.round(getTheoreticalEarliestOperationStartTime())
            + "][twEnd=" + Activities.round(getTheoreticalLatestOperationStartTime()) + "]";
    }

}
