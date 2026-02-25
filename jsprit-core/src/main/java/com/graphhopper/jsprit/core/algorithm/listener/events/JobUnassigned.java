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
package com.graphhopper.jsprit.core.algorithm.listener.events;

import com.graphhopper.jsprit.core.problem.job.Job;

import java.util.List;
import java.util.Map;

/**
 * Event emitted when a job cannot be inserted and remains unassigned.
 *
 * @param iteration                The iteration number
 * @param timestamp                When the event occurred
 * @param job                      The job that could not be inserted
 * @param failedConstraintsByRoute Map of route IDs to list of failed constraint names
 * @param reason                   Summary reason why the job could not be inserted
 */
public record JobUnassigned(
        int iteration,
        long timestamp,
        Job job,
        Map<String, List<String>> failedConstraintsByRoute,
        String reason
) implements AlgorithmEvent {
}
