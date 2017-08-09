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

import java.util.Collection;

import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.SizeDimension;
import com.graphhopper.jsprit.core.problem.job.AbstractJob;
import com.graphhopper.jsprit.core.problem.job.Break;
import com.graphhopper.jsprit.core.problem.job.Break.Builder;

/**
 * An {@linkplain InternalJobActivity} marking the break time of the vehicle.
 *
 * @author Balage
 *
 */
public class BreakActivity extends InternalJobActivity {

    /**
     * Creates a new Break activity instance.
     *
     * @param aBreak
     *            The {@linkplain Break} job instance to associate the activity
     *            with.
     * @param builder
     *            The Break job builder.
     * @return The new break instance.
     */
    public static BreakActivity newInstance(Break aBreak, Builder builder) {
        return new BreakActivity(aBreak, "break", builder.getLocation(), builder.getServiceTime(),
                builder.getCapacity(), builder.getTimeWindows().getTimeWindows());
    }

    /**
     * Copy constructor.
     * <p>
     * Makes a shallow copy.
     * </p>
     *
     * @param breakActivity
     *            The activity to copy.
     */
    public BreakActivity(BreakActivity breakActivity) {
        super(breakActivity);
    }

    private BreakActivity(AbstractJob job, String name, Location location, double operationTime,
            SizeDimension capacity, Collection<TimeWindow> timeWindows) {
        super(job, name, location, operationTime, capacity, timeWindows);
    }

    @Override
    public Break getJob() {
        return (Break) super.getJob();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getJob() == null) ? 0 : getJob().hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BreakActivity other = (BreakActivity) obj;
        if (getJob() == null) {
            if (other.getJob() != null)
                return false;
        } else if (!getJob().equals(other.getJob()))
            return false;
        return true;
    }


    /**
     * Sets the location of the break.
     *
     * @param location
     *            The location.
     */
    public void setLocation(Location breakLocation) {
        location = breakLocation;
    }

    /**
     * @return The time window of the break.
     */
    public TimeWindow getBreakTimeWindow() {
        // Break has always a single time window
        return getTimeWindows().iterator().next();
    }

}
