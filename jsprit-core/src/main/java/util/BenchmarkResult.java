/*******************************************************************************
 * Copyright (C) 2013  Stefan Schroeder
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;

import org.apache.commons.math.stat.descriptive.DescriptiveStatistics;



public class BenchmarkResult {
	private double[] results;
	private double[] vehicles;
	private double[] times;
	
	private DescriptiveStatistics statsResults;
	private DescriptiveStatistics statsVehicles;
	private DescriptiveStatistics statsTimes;
	
	public final BenchmarkInstance instance;
	
	public final int runs;
	
	public BenchmarkResult(BenchmarkInstance instance, int runs, double[] results, double[] compTimes, double[] vehicles) {
		super();
		this.results = results;
		this.runs = runs;
		this.times = compTimes;
		this.instance = instance;
		this.vehicles = vehicles;
		this.statsResults = new DescriptiveStatistics(results);
		this.statsTimes = new DescriptiveStatistics(times);
		this.statsVehicles = new DescriptiveStatistics(vehicles);
	}
	
	public double[] getResults(){
		return results;
	}
	
	public double[] getVehicles(){
		return vehicles;
	}
	
	public double[] getCompTimes(){
		return times;
	}
	
	public DescriptiveStatistics getResultStats(){
		return statsResults;
	}
	
	public DescriptiveStatistics getVehicleStats(){
		return statsVehicles;
	}
	
	public DescriptiveStatistics getTimesStats(){
		return statsTimes;
	}
	
}
