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

import java.util.Collection;

import basics.VehicleRoutingProblemSolution;

public class Solutions {
	
	public static VehicleRoutingProblemSolution getBest(Collection<VehicleRoutingProblemSolution> solutions){
		VehicleRoutingProblemSolution best = null;
		for(VehicleRoutingProblemSolution s : solutions){
			if(best == null) best = s;
			else if(s.getCost() < best.getCost()) best = s;
		}
		return best;
	}

}
