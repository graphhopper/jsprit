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

import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobUnassignedListener;
import com.graphhopper.jsprit.core.problem.job.Job;
import org.apache.commons.math3.stat.Frequency;

import java.util.*;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTracker implements JobUnassignedListener {

    Map<String, Frequency> reasons = new HashMap<>();

    Map<Integer, String> codesToReason = new HashMap<>();

    Map<String, Integer> failedConstraintNamesToCode = new HashMap<>();

    Set<String> constraintsToBeIgnored = new HashSet<>();

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

    public void ignore(String simpleNameOfConstraint) {
        constraintsToBeIgnored.add(simpleNameOfConstraint);
    }

    @Override
    public void informJobUnassigned(Job unassigned, Collection<String> failedConstraintNames) {
        if (!this.reasons.containsKey(unassigned.getId())) {
            this.reasons.put(unassigned.getId(), new Frequency());
        }
        for (String r : failedConstraintNames) {
            if (constraintsToBeIgnored.contains(r)) continue;
            this.reasons.get(unassigned.getId()).addValue(r);
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

    /**
     * For each job id, it returns frequency distribution of failed constraints (simple name of constraint) in an unmodifiable map.
     *
     * @return
     */
    public Map<String, Frequency> getReasons() {
        return Collections.unmodifiableMap(reasons);
    }

    /**
     * Returns an unmodifiable map of codes and reason pairs.
     *
     * @return
     */
    public Map<Integer, String> getCodesToReason() {
        return Collections.unmodifiableMap(codesToReason);
    }

    /**
     * Returns an unmodifiable map of constraint names (simple name of constraint) and reason code pairs.
     *
     * @return
     */
    public Map<String, Integer> getFailedConstraintNamesToCode() {
        return Collections.unmodifiableMap(failedConstraintNamesToCode);
    }

    /**
     * Returns the most likely reason code i.e. the reason (failed constraint) being observed most often.
     *
     * 1 --> "cannot serve required skill
     * 2 --> "cannot be visited within time window"
     * 3 --> "does not fit into any vehicle due to capacity"
     * 4 --> "cannot be assigned due to max distance constraint of vehicle"
     *
     * @param jobId
     * @return
     */
    public int getMostLikelyReasonCode(String jobId) {
        Frequency reasons = this.reasons.get(jobId);
        String mostLikelyReason = getMostLikely(reasons);
        return toCode(mostLikelyReason);
    }

    /**
     * Returns the most likely reason i.e. the reason (failed constraint) being observed most often.
     *
     * @param jobId
     * @return
     */
    public String getMostLikelyReason(String jobId) {
        Frequency reasons = this.reasons.get(jobId);
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

    private String getMostLikely(Frequency reasons) {
        Iterator<Map.Entry<Comparable<?>, Long>> entryIterator = reasons.entrySetIterator();
        int maxCount = 0;
        String mostLikely = null;
        while (entryIterator.hasNext()) {
            Map.Entry<Comparable<?>, Long> entry = entryIterator.next();
            if (entry.getValue() > maxCount) {
                Comparable<?> key = entry.getKey();
                mostLikely = key.toString();
            }
        }
        return mostLikely;
    }


}
