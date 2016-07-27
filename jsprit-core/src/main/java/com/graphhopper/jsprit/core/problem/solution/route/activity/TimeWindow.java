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

/**
 * TimeWindow consists of a startTime and endTime.
 *
 * @author stefan schroeder
 */

public class TimeWindow {

    /**
     * Returns new instance of TimeWindow.
     *
     * @param start
     * @param end
     * @return TimeWindow
     * @throw IllegalArgumentException either if start or end < 0.0 or end < start
     */
    public static TimeWindow newInstance(double start, double end) {
        return new TimeWindow(start, end);
    }

    private final double start;
    private final double end;

    /**
     * Constructs the timeWindow
     *
     * @param start
     * @param end
     * @throw IllegalArgumentException either if start or end < 0.0 or end < start
     */
    public TimeWindow(double start, double end) {
        super();
        if (start < 0.0 || end < 0.0)
            throw new IllegalArgumentException("neither time window start nor end must be < 0.0: " + "[start=" + start + "][end=" + end + "]");
        if (end < start)
            throw new IllegalArgumentException("time window end cannot be smaller than its start: " + "[start=" + start + "][end=" + end + "]");
        this.start = start;
        this.end = end;
    }

    /**
     * Returns startTime of TimeWindow.
     *
     * @return startTime
     */
    public double getStart() {
        return start;
    }

    /**
     * Returns endTime of TimeWindow.
     *
     * @return endTime
     */
    public double getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return "[start=" + start + "][end=" + end + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(end);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(start);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    /**
     * Two timeWindows are equal if they have the same start AND endTime.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TimeWindow other = (TimeWindow) obj;
        if (Double.doubleToLongBits(end) != Double.doubleToLongBits(other.end))
            return false;
        if (Double.doubleToLongBits(start) != Double
            .doubleToLongBits(other.start))
            return false;
        return true;
    }


}
