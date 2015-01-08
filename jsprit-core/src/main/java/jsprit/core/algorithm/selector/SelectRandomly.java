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
package jsprit.core.algorithm.selector;

import jsprit.core.problem.solution.VehicleRoutingProblemSolution;
import jsprit.core.util.RandomNumberGeneration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;




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
