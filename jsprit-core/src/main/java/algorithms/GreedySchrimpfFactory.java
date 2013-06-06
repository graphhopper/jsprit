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
package algorithms;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import util.Resource;



import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.io.AlgorithmConfig;
import basics.io.AlgorithmConfigXmlReader;


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
public class GreedySchrimpfFactory {
	
	/**
	 * Creates the {@link VehicleRoutingAlgorithm}.
	 * 
	 * @param vrp
	 * @return algorithm
	 */
	public VehicleRoutingAlgorithm createAlgorithm(VehicleRoutingProblem vrp){
		AlgorithmConfig algorithmConfig = new AlgorithmConfig();
		URL resource = Resource.getAsURL("greedySchrimpf.xml");
		new AlgorithmConfigXmlReader(algorithmConfig).read(resource);
		return VehicleRoutingAlgorithms.createAlgorithm(vrp, algorithmConfig);
	}
	
	

}
