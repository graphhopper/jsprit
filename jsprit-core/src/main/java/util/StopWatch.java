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

import org.apache.log4j.Logger;

public class StopWatch {
	
	private static Logger log = Logger.getLogger(StopWatch.class);
	
	private double ran;
	
	private double startTime;
	
	public double getCompTimeInSeconds(){
		return (ran)/1000.0;
	}

	public void stop(){
		ran += System.currentTimeMillis() - startTime;
	}
	
	public void start(){
		startTime = System.currentTimeMillis();
	}
	
	public void reset(){
		startTime = 0;
		ran = 0;
	}
	
	@Override
	public String toString() {
		return getCompTimeInSeconds() + " sec";
	}

	public double getCurrTimeInSeconds() {
		return (System.currentTimeMillis()-startTime)/1000.0;
	}
	
}
