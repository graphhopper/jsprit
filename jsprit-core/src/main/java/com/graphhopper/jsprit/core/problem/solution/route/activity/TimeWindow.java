/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
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
    public static TimeWindow newInstance(double hardStart, double hardEnd) {
        return new TimeWindow(hardStart, hardEnd);
    }
    
    public static TimeWindow newInstance(double hardStart, double softStart, double softEnd, double hardEnd) {
    	return new TimeWindow(hardStart, softStart, softEnd, hardEnd);
    }

    private final double hardStart;
    private final double hardEnd;
    private final double softStart;
    private final double softEnd;

    /**
     * Constructs the timeWindow
     *
     * @param start
     * @param end
     * @throw IllegalArgumentException either if start or end < 0.0 or end < start
     */
    public TimeWindow(double hardStart, double hardEnd) {
        super();
        if (hardStart < 0.0 || hardEnd < 0.0)
            throw new IllegalArgumentException("neither time window start nor end must be < 0.0: " + "[start=" + hardStart + "][end=" + hardEnd + "]");
        if (hardEnd < hardStart)
            throw new IllegalArgumentException("time window end cannot be smaller than its start: " + "[start=" + hardStart + "][end=" + hardEnd + "]");
        this.hardStart = hardStart;
        this.hardEnd = hardEnd;
        this.softStart = hardStart;
        this.softEnd = hardEnd;
    }
    
    public TimeWindow(double hardStart, double softStart, double softEnd, double hardEnd) {
        super();
        if (hardStart < 0.0 || hardEnd < 0.0 || softStart < 0.0 || softEnd < 0.0)
            throw new IllegalArgumentException("neither time window start nor end must be < 0.0: " + "[start=" + hardStart + "][softStart=" + softStart + "][softEnd=" + softEnd +"[end=" + hardEnd + "]");
        if (hardEnd < hardStart || softStart < hardStart || softEnd < softStart || hardEnd < softEnd)
            throw new IllegalArgumentException("time window end cannot be smaller than its start: " + "[start=" + hardStart + "][softStart=" + softStart + "][softEnd=" + softEnd +"[end=" + hardEnd + "]");
        this.hardStart = hardStart;
        this.hardEnd = hardEnd;
        this.softStart = softStart;
        this.softEnd = softEnd;
    }

    /**
     * Returns startTime of TimeWindow.
     *
     * @return startTime
     */
    public double getStart() {
        return hardStart;
    }

    /**
     * Returns endTime of TimeWindow.
     *
     * @return endTime
     */
    public double getEnd() {
        return hardEnd;
    }

    public double getHardStart() {
    	return hardStart;
    }

    public double getHardEnd() {
    	return hardEnd;
    }

    public double getSoftStart() {
    	return softStart;
    }

    public double getSoftEnd() {
    	return softEnd;
    }

    @Override
    public String toString() {
    	if(hardStart!= softStart || softEnd != hardEnd)
    		return "[start=" + hardStart + "][softStart=" + softStart + "][softEnd=" + softEnd +"[end=" + hardEnd + "]";
        return "[start=" + hardStart + "][end=" + hardEnd + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(hardEnd);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(hardStart);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(softStart);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(softEnd);
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
        if (Double.doubleToLongBits(hardEnd) != Double.doubleToLongBits(other.hardEnd))
            return false;
        if (Double.doubleToLongBits(hardStart) != Double
            .doubleToLongBits(other.hardStart))
            return false;
        if (Double.doubleToLongBits(softStart) != Double
                .doubleToLongBits(other.softStart))
        	return false;
        if (Double.doubleToLongBits(softEnd) != Double
                .doubleToLongBits(other.softEnd))
        	return false;
        return true;
    }


}
