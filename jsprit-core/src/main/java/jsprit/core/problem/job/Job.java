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
package jsprit.core.problem.job;


import jsprit.core.problem.Capacity;
import jsprit.core.problem.HasId;
import jsprit.core.problem.HasIndex;
import jsprit.core.problem.Skills;

/**
 * Basic interface for all jobs.
 * 
 * @author schroeder
 *
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
}
