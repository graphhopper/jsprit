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

import com.graphhopper.jsprit.core.problem.Capacity;
import com.graphhopper.jsprit.core.problem.Location;
import com.graphhopper.jsprit.core.problem.job.Break;

public class BreakActivity extends InternalJobActivity {

// REMARK - Balage1551 - What is it is used for?
//    public static int counter = 0;

// REMARK - Balage1551 - Never used and there is a duplicate implemented in JobActivity, so that could be used.
//    public static BreakActivity copyOf(BreakActivity breakActivity) {
//        return new BreakActivity(breakActivity);
//    }

    public static BreakActivity newInstance(Break aBreak) {
        return new BreakActivity(aBreak);
    }

    protected BreakActivity(Break aBreak) {
        super(aBreak, "Break", aBreak.getLocation(), aBreak.getServiceDuration(), Capacity.createNullCapacity(aBreak.getSize()));
//        counter++;
    }

    public BreakActivity(BreakActivity breakActivity) {
        super(breakActivity);
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        BreakActivity other = (BreakActivity) obj;
        if (getJob() == null) {
            if (other.getJob() != null) {
                return false;
            }
        } else if (!getJob().equals(other.getJob())) {
            return false;
        }
        return true;
    }


    public void setLocation(Location breakLocation) {
        location = breakLocation;
    }

}
