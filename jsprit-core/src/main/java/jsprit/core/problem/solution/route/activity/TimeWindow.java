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
package jsprit.core.problem.solution.route.activity;

/**
 * TimeWindow consists of a startTime and endTime.
 * 
 * @author stefan schroeder
 * 
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
	public static TimeWindow newInstance(double start, double end){
		return new TimeWindow(start,end);
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
		if(start < 0.0 || end < 0.0) throw new IllegalArgumentException("neither time window start nor end must be < 0.0: " + "[start=" + start + "][end=" + end + "]");
		if(end < start) throw new IllegalArgumentException("time window end cannot be smaller than its start: " + "[start=" + start + "][end=" + end + "]" );
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
