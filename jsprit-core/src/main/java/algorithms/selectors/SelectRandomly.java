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
package algorithms.selectors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import util.RandomNumberGeneration;
import basics.VehicleRoutingProblemSolution;



public class SelectRandomly implements SolutionSelector{

	private static SelectRandomly selector = null;
	
	public static SelectRandomly getInstance(){
		if(selector == null){
			selector = new SelectRandomly();
			return selector;
		}
		return selector;
	}
	
	private Random random = RandomNumberGeneration.getRandom();
	
	@Override
	public VehicleRoutingProblemSolution selectSolution(Collection<VehicleRoutingProblemSolution> solutions) {
		if(solutions.isEmpty()) return null;
		List<VehicleRoutingProblemSolution> solList = new ArrayList<VehicleRoutingProblemSolution>(solutions);
		int randomIndex = random.nextInt(solutions.size());
		return solList.get(randomIndex);
	}

	public void setRandom(Random random) {
		this.random = random;
	}

}
