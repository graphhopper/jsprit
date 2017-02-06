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

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobUnassignedListener;
import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTracker implements JobUnassignedListener {

    Map<String, Multiset<String>> reasons = new HashMap<>();

    Map<Integer, String> codesToReason = new HashMap<>();

    Map<String, Integer> failedConstraintNamesToCode = new HashMap<>();

    public UnassignedJobReasonTracker() {
        codesToReason.put(1, "cannot serve required skill");
        codesToReason.put(2, "cannot be visited within time window");
        codesToReason.put(3, "does not fit into any vehicle due to capacity");
        codesToReason.put(4, "cannot be assigned due to max distance constraint of vehicle");

        failedConstraintNamesToCode.put("HardSkillConstraint", 1);
        failedConstraintNamesToCode.put("VehicleDependentTimeWindowConstraints", 2);
        failedConstraintNamesToCode.put("ServiceLoadRouteLevelConstraint", 3);
        failedConstraintNamesToCode.put("PickupAndDeliverShipmentLoadActivityLevelConstraint", 3);
        failedConstraintNamesToCode.put("ServiceLoadActivityLevelConstraint", 3);
        failedConstraintNamesToCode.put("MaxDistanceConstraint", 4);
    }

    @Override
    public void informJobUnassigned(Job unassigned, Collection<String> failedConstraintNames) {
        if (!this.reasons.containsKey(unassigned.getId())) {
            Multiset<String> ms = HashMultiset.create();
            this.reasons.put(unassigned.getId(), ms);
        }
        for (String r : failedConstraintNames) {
            this.reasons.get(unassigned.getId()).add(r);
        }
    }

    public void put(String simpleNameOfFailedConstraint, int code, String reason) {
        if (code <= 20)
            throw new IllegalArgumentException("first 20 codes are reserved internally. choose a code > 20");
        codesToReason.put(code, reason);
        if (failedConstraintNamesToCode.containsKey(simpleNameOfFailedConstraint)) {
            throw new IllegalArgumentException(simpleNameOfFailedConstraint + " already assigned to code and reason");
        } else failedConstraintNamesToCode.put(simpleNameOfFailedConstraint, code);
    }

    public int getCode(Job job) {
        Multiset<String> reasons = this.reasons.get(job.getId());
        String mostLikelyReason = getMostLikely(reasons);
        return toCode(mostLikelyReason);
    }

    public String getReason(Job job) {
        Multiset<String> reasons = this.reasons.get(job.getId());
        String mostLikelyReason = getMostLikely(reasons);
        int code = toCode(mostLikelyReason);
        if (code == -1) return mostLikelyReason;
        else return codesToReason.get(code);
    }

    private int toCode(String mostLikelyReason) {
        if (failedConstraintNamesToCode.containsKey(mostLikelyReason))
            return failedConstraintNamesToCode.get(mostLikelyReason);
        else return -1;
    }

    private String getMostLikely(Multiset<String> reasons) {
        Set<String> set = reasons.elementSet();
        int maxCount = 0;
        String mostLikely = null;
        for (String r : set) {
            int count = reasons.count(r);
            if (count > maxCount) {
                mostLikely = r;
                maxCount = count;
            }
        }
        return mostLikely;
    }


}
