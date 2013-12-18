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
package jsprit.core.util;


public class StopWatch {
	
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
