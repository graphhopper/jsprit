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

import com.graphhopper.jsprit.core.algorithm.listener.IterationStartsListener;
import com.graphhopper.jsprit.core.algorithm.recreate.InsertionData;
import com.graphhopper.jsprit.core.algorithm.recreate.listener.JobUnassignedListener;
import com.graphhopper.jsprit.core.problem.VehicleRoutingProblem;
import com.graphhopper.jsprit.core.problem.job.Job;
import com.graphhopper.jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import org.apache.commons.math3.stat.Frequency;

import java.util.*;

/**
 * Created by schroeder on 06/02/17.
 */
public class UnassignedJobReasonTracker implements JobUnassignedListener, IterationStartsListener {

    public static String getMostLikelyFailedConstraintName(Frequency failedConstraintNamesFrequency) {
        if (failedConstraintNamesFrequency == null) return "no reason found";
        Iterator<Map.Entry<Comparable<?>, Long>> entryIterator = failedConstraintNamesFrequency.entrySetIterator();
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

    private int iterationNumber;

    // iterationNumber -> jobId -> list<insertion data>
    Map<Integer, Map<String, List<InsertionData>>> failedInsertions = new HashMap<>();

    Map<Integer, String> codesToHumanReadableReason = new HashMap<>();

    Map<String, Integer> failedConstraintNamesToCode = new HashMap<>();

    Set<String> failedConstraintNamesToBeIgnored = new HashSet<>();

    public UnassignedJobReasonTracker() {
        codesToHumanReadableReason.put(1, "cannot serve required skill");
        codesToHumanReadableReason.put(2, "cannot be visited within time window");
        codesToHumanReadableReason.put(3, "does not fit into any vehicle due to capacity");
        codesToHumanReadableReason.put(4, "cannot be assigned due to max distance constraint of vehicle");

        failedConstraintNamesToCode.put("HardSkillConstraint", 1);
        failedConstraintNamesToCode.put("VehicleDependentTimeWindowConstraints", 2);
        failedConstraintNamesToCode.put("ServiceLoadRouteLevelConstraint", 3);
        failedConstraintNamesToCode.put("PickupAndDeliverShipmentLoadActivityLevelConstraint", 3);
        failedConstraintNamesToCode.put("ServiceLoadActivityLevelConstraint", 3);
        failedConstraintNamesToCode.put("MaxDistanceConstraint", 4);

        failedInsertions.put(iterationNumber, new HashMap<String, List<InsertionData>>());
    }

    public void ignore(String simpleNameOfConstraint) {
        failedConstraintNamesToBeIgnored.add(simpleNameOfConstraint);
    }

    @Override
    public void informJobUnassigned(Job unassigned, InsertionData insertionData) {
        Map<String, List<InsertionData>> failedInsertionsInIteration = failedInsertions.get(iterationNumber);
        if (!failedInsertionsInIteration.containsKey(unassigned.getId())) {
            failedInsertionsInIteration.put(unassigned.getId(), new ArrayList<InsertionData>());
        }
        failedInsertionsInIteration.get(unassigned.getId()).add(insertionData);
    }

    public void put(String simpleNameOfFailedConstraint, int code, String reason) {
        if (code <= 20)
            throw new IllegalArgumentException("first 20 codes are reserved internally. choose a code > 20");
        codesToHumanReadableReason.put(code, reason);
        if (failedConstraintNamesToCode.containsKey(simpleNameOfFailedConstraint)) {
            throw new IllegalArgumentException(simpleNameOfFailedConstraint + " already assigned to code and reason");
        } else failedConstraintNamesToCode.put(simpleNameOfFailedConstraint, code);
    }

    /**
     * For each job id, it returns frequency distribution of failed constraints (simple name of constraint) in an unmodifiable map.
     *
     * @return
     */
    @Deprecated
    public Map<String, Frequency> getReasons() {
        return Collections.unmodifiableMap(aggregateFailedConstraintNamesFrequencyMapping());
    }

    /**
     * For each job id, it returns frequency distribution of failed constraints (simple name of constraint) in an unmodifiable map.
     *
     * @return
     */
    public Map<String, Frequency> getFailedConstraintNamesFrequencyMapping() {
        return Collections.unmodifiableMap(aggregateFailedConstraintNamesFrequencyMapping());
    }

    /**
     * Aggregates the failed constraints from all insertion data from iterations.
     *
     * @return
     */
    protected Map<String, Frequency> aggregateFailedConstraintNamesFrequencyMapping() {
        Map<String, Frequency> aggregatedMap = new HashMap<>();
        for (Map<String, List<InsertionData>> jobInsertionsMap : failedInsertions.values()) {
            for (Map.Entry<String, List<InsertionData>> jobInsertionEntry : jobInsertionsMap.entrySet()) {
                String jobId = jobInsertionEntry.getKey();
                if (!aggregatedMap.containsKey(jobId)) {
                    aggregatedMap.put(jobId, new Frequency());
                }
                for (InsertionData failedInsertion: jobInsertionEntry.getValue()) {
                    for (String failedConstraint: failedInsertion.getFailedConstraintNames()) {
                        if (!failedConstraintNamesToBeIgnored.contains(failedConstraint)) {
                            aggregatedMap.get(jobId).addValue(failedConstraint);
                        }
                    }
                }
            }
        }
        return aggregatedMap;
    }

    public Collection<InsertionData> getFailedInsertionsForJob(String jobId) {
        Collection<InsertionData> result = new ArrayList<>();
        for (Map<String, List<InsertionData>> jobInsertionMap: failedInsertions.values()) {
            if (jobInsertionMap.containsKey(jobId)) {
                result.addAll(jobInsertionMap.get(jobId));
            }
        }
        return result;
    }

    public Collection<InsertionData> getFailedInsertionsInIterationForJob(int iterationNumber, String jobId) {
        if (failedInsertions.containsKey(iterationNumber) && failedInsertions.get(iterationNumber).containsKey(jobId)) {
            return Collections.unmodifiableList(failedInsertions.get(iterationNumber).get(jobId));
        }
        return Collections.unmodifiableList(new ArrayList<InsertionData>());
    }

    /**
     * Returns an unmodifiable map of codes and reason pairs.
     *
     * @return
     */
    public Map<Integer, String> getCodesToReason() {
        return Collections.unmodifiableMap(codesToHumanReadableReason);
    }

    /**
     * Returns an unmodifiable map of constraint names (simple name of constraint) and reason code pairs.
     *
     * @return
     */
    public Map<String, Integer> getFailedConstraintNamesToCode() {
        return Collections.unmodifiableMap(failedConstraintNamesToCode);
    }

    public int getCode(String failedConstraintName) {
        return toCode(failedConstraintName);
    }

    public String getHumanReadableReason(int code) {
        return getCodesToReason().get(code);
    }

    public String getHumanReadableReason(String failedConstraintName) {
        return getCodesToReason().get(getCode(failedConstraintName));
    }

    /**
     * Returns the most likely reason code i.e. the reason (failed constraint) being observed most often.
     * <p>
     * 1 --> "cannot serve required skill
     * 2 --> "cannot be visited within time window"
     * 3 --> "does not fit into any vehicle due to capacity"
     * 4 --> "cannot be assigned due to max distance constraint of vehicle"
     *
     * @param jobId
     * @return
     */
    public int getMostLikelyReasonCode(String jobId) {
        Map<String, Frequency> frequencyMap = aggregateFailedConstraintNamesFrequencyMapping();
        Frequency reasons = frequencyMap.get(jobId);
        String mostLikelyReason = getMostLikelyFailedConstraintName(reasons);
        return toCode(mostLikelyReason);
    }

    /**
     * Returns the most likely reason i.e. the reason (failed constraint) being observed most often.
     *
     * @param jobId
     * @return
     */
    public String getMostLikelyReason(String jobId) {
        Map<String, Frequency> frequencyMap = aggregateFailedConstraintNamesFrequencyMapping();
        if (!frequencyMap.containsKey(jobId)) return "no reason found";
        Frequency reasons = frequencyMap.get(jobId);
        String mostLikelyReason = getMostLikelyFailedConstraintName(reasons);
        int code = toCode(mostLikelyReason);
        if (code == -1) return mostLikelyReason;
        else return codesToHumanReadableReason.get(code);
    }

    private int toCode(String mostLikelyReason) {
        if (failedConstraintNamesToCode.containsKey(mostLikelyReason))
            return failedConstraintNamesToCode.get(mostLikelyReason);
        else return -1;
    }

    @Override
    public void informIterationStarts(int i, VehicleRoutingProblem problem,
                                      Collection<VehicleRoutingProblemSolution> solutions) {
        iterationNumber = i;
        failedInsertions.put(iterationNumber, new HashMap<String, List<InsertionData>>());
    }

}
