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
package jsprit.core.algorithm.box;

import jsprit.core.algorithm.VehicleRoutingAlgorithm;
import jsprit.core.algorithm.io.AlgorithmConfig;
import jsprit.core.algorithm.io.AlgorithmConfigXmlReader;
import jsprit.core.algorithm.io.VehicleRoutingAlgorithms;
import jsprit.core.problem.VehicleRoutingProblem;
import jsprit.core.util.Resource;

import java.net.URL;



/**
 * Factory that creates the {@link VehicleRoutingAlgorithm} as proposed by Schrimpf et al., 2000 with the following parameters:
 * 
 * <p>
 * R&R_random (prob=0.5, F=0.5);
 * R&R_radial (prob=0.5, F=0.3);
 * threshold-accepting with exponentialDecayFunction (alpha=0.1, warmup-iterations=100);
 * nuOfIterations=2000
 * 
 * <p>Gerhard Schrimpf, Johannes Schneider, Hermann Stamm- Wilbrandt, and Gunter Dueck. 
 * Record breaking optimization results using the ruin and recreate principle. 
 * Journal of Computational Physics, 159(2):139 – 171, 2000. ISSN 0021-9991. doi: 10.1006/jcph.1999. 6413. 
 * URL http://www.sciencedirect.com/science/article/ pii/S0021999199964136
 * 
 * <p>algorithm-xml-config is available at src/main/resources/schrimpf.xml. 
 * 
 * @author stefan schroeder
 *
 */
public class SchrimpfFactory {
	
	/**
	 * Creates the {@link VehicleRoutingAlgorithm}.
	 * 
	 * @param vrp the underlying vehicle routing problem
	 * @return algorithm
	 */
	public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		URL resource = Resource.getAsURL("schrimpf.xml");
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		return VehicleRoutingAlgorithms.createAlgorithm(vrp, algorithmConfig);
	}
	
	

}
