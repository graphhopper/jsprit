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

import com.graphhopper.jsprit.core.problem.solution.route.activity.ReverseActivityVisitor;
import com.graphhopper.jsprit.core.problem.solution.route.activity.TourActivity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;


public class ReverseRouteActivityVisitor implements RouteVisitor {

    private Collection<ReverseActivityVisitor> visitors = new ArrayList<ReverseActivityVisitor>();

    @Override
    public void visit(VehicleRoute route) {
        if (visitors.isEmpty()) return;
        if (route.isEmpty()) return;
        begin(route);
        Iterator<TourActivity> revIterator = route.getTourActivities().reverseActivityIterator();
        while (revIterator.hasNext()) {
            TourActivity act = revIterator.next();
            visit(act);
        }
        finish(route);
    }

    private void finish(VehicleRoute route) {
        for (ReverseActivityVisitor visitor : visitors) {
            visitor.finish();
        }

    }

    private void visit(TourActivity act) {
        for (ReverseActivityVisitor visitor : visitors) {
            visitor.visit(act);
        }
    }

    private void begin(VehicleRoute route) {
        for (ReverseActivityVisitor visitor : visitors) {
            visitor.begin(route);
        }

    }

    public void addActivityVisitor(ReverseActivityVisitor activityVisitor) {
        if (!visitors.contains(activityVisitor)) {
            visitors.add(activityVisitor);
        }
    }
}
