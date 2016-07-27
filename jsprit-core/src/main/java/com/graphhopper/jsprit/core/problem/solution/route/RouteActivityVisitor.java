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
package com.graphhopper.jsprit.core.problem.solution.route;

import com.graphhopper.jsprit.core.problem.solution.route.activity.ActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;


public class RouteActivityVisitor implements RouteVisitor {

    private Collection<ActivityVisitor> visitors = new ArrayList<ActivityVisitor>();

    @Override
    public void visit(VehicleRoute route) {
        if (visitors.isEmpty()) return;
        begin(route);
        for (TourActivity act : route.getTourActivities().getActivities()) {
            visit(act);
        }
        end(route);
    }

    private void end(VehicleRoute route) {
        for (ActivityVisitor visitor : visitors) {
            visitor.finish();
        }

    }

    private void visit(TourActivity act) {
        for (ActivityVisitor visitor : visitors) {
            visitor.visit(act);
        }
    }

    private void begin(VehicleRoute route) {
        for (ActivityVisitor visitor : visitors) {
            visitor.begin(route);
        }

    }

    public RouteActivityVisitor addActivityVisitor(ActivityVisitor activityVisitor) {
        if (!visitors.contains(activityVisitor)) {
            visitors.add(activityVisitor);
        }
        return this;
    }
}
