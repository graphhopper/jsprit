/*******************************************************************************
 * Copyright (C) 2014  Stefan Schroeder
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

}
