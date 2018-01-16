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
package com.graphhopper.jsprit.core.problem.job;


import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.HasId;
import com.graphhopper.jsprit.core.problem.HasIndex;
import com.graphhopper.jsprit.core.problem.Skills;

/**
 * Basic interface for all jobs.
 *
 * @author schroeder
 */
public interface Job extends HasId, HasIndex {


    /**
     * Returns the unique identifier (id) of a job.
     *
     * @return id
     */
    public String getId();

    /**
     * Returns size, i.e. capacity-demand, of this job which can consist of an arbitrary number of capacity dimensions.
     *
     * @return Capacity
     */
    public Capacity getSize();

    public Skills getRequiredSkills();

    /**
     * Returns name.
     *
     * @return name
     */
    public String getName();

    /**
     * Get priority of job. Only 1 = high priority, 2 = medium and 3 = low are allowed.
     * <p>
     * Default is 2 = medium.
     *
     * @return priority
     */
    public int getPriority();

    public double getMaxTimeInVehicle();

}
