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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.junit.Before;
import org.junit.Test;

import algorithms.VehicleRoutingAlgorithms.ModKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.AcceptorKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.RuinStrategyKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.SelectorKey;
import algorithms.VehicleRoutingAlgorithms.TypedMap.StrategyModuleKey;
import algorithms.acceptors.AcceptNewIfBetterThanWorst;
import algorithms.acceptors.SolutionAcceptor;
import algorithms.selectors.SelectBest;
import algorithms.selectors.SolutionSelector;
import basics.Job;
import basics.VehicleRoutingAlgorithm;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyModule;
import basics.algo.SearchStrategyModuleListener;
import basics.io.VrpXMLReader;
import basics.route.VehicleRoute;

public class TestAlgorithmReader {
	
	XMLConfiguration config;
	
	VehicleRoutingProblem vrp;
	
	Collection<VehicleRoutingProblemSolution> solutions;
	
	@Before
	public void doBefore() throws ConfigurationException{
		config = new XMLConfiguration("src/test/resources/testConfig.xml");
		VehicleRoutingProblem.Builder vrpBuilder = VehicleRoutingProblem.Builder.newInstance();
		solutions = new ArrayList<VehicleRoutingProblemSolution>();
		new VrpXMLReader(vrpBuilder,solutions).read("src/test/resources/finiteVrpForReaderV2Test.xml");
		vrp = vrpBuilder.build();
	}
	
	@Test
	public void testTypedMap(){
		algorithms.VehicleRoutingAlgorithms.TypedMap typedMap = new algorithms.VehicleRoutingAlgorithms.TypedMap();
		
		String acceptorName = "acceptor";
		String acceptorId = "acceptorId";
		
		ModKey key = new ModKey(acceptorName,acceptorId);
		AcceptorKey accKey = new AcceptorKey(key);
		
		SolutionAcceptor acceptor = new AcceptNewIfBetterThanWorst(1);
		
		typedMap.put(accKey, acceptor);
		
		assertEquals(acceptor,typedMap.get(accKey));
		
	}
	
	@Test
	public void testTypedMap2(){
		algorithms.VehicleRoutingAlgorithms.TypedMap typedMap = new algorithms.VehicleRoutingAlgorithms.TypedMap();
		
		String acceptorName = "acceptor";
		String acceptorId = "acceptorId";
		
		String selectorName = "selector";
		String selectorId = "selectorId";
		
		ModKey key = new ModKey(acceptorName,acceptorId);
		AcceptorKey accKey = new AcceptorKey(key);
		SolutionAcceptor acceptor =  new AcceptNewIfBetterThanWorst(1);
		
		SelectorKey selKey = new SelectorKey(new ModKey(selectorName,selectorId));
		SolutionSelector selector = new SelectBest();
		
		typedMap.put(accKey, acceptor);
		typedMap.put(selKey, selector);
		
		assertEquals(acceptor,typedMap.get(accKey));
		assertEquals(selector, typedMap.get(selKey));
	}
	
	@Test
	public void testTypedMap3(){
		algorithms.VehicleRoutingAlgorithms.TypedMap typedMap = new algorithms.VehicleRoutingAlgorithms.TypedMap();
		
		String acceptorName = "acceptor";
		String acceptorId = "acceptorId";
		
		String acceptorName2 = "acceptor2";
		String acceptorId2 = "acceptorId2";
		
		String selectorName = "selector";
		String selectorId = "selectorId";
		
		ModKey key = new ModKey(acceptorName,acceptorId);
		AcceptorKey accKey = new AcceptorKey(key);
		SolutionAcceptor acceptor =  new AcceptNewIfBetterThanWorst(1);
		
		SelectorKey selKey = new SelectorKey(new ModKey(selectorName,selectorId));
		SolutionSelector selector = new SelectBest();
		
		AcceptorKey accKey2 = new AcceptorKey(new ModKey(acceptorName2,acceptorId2));
		SolutionAcceptor acceptor2 =  new AcceptNewIfBetterThanWorst(1);
		
		typedMap.put(accKey, acceptor);
		typedMap.put(selKey, selector);
		typedMap.put(accKey2, acceptor2);
		
		assertEquals(acceptor,typedMap.get(accKey));
		assertEquals(selector, typedMap.get(selKey));
		assertEquals(acceptor2,typedMap.get(accKey2));
	}
	
	@Test
	public void testTypedMap4(){
		algorithms.VehicleRoutingAlgorithms.TypedMap typedMap = new algorithms.VehicleRoutingAlgorithms.TypedMap();
		
		String acceptorName = "acceptor";
		String acceptorId = "acceptorId";
		
		String moduleName = "acceptor";
		String moduleId = "acceptorId";
		
		ModKey key = new ModKey(acceptorName,acceptorId);
		RuinStrategyKey accKey = new RuinStrategyKey(key);
		RuinStrategy acceptor = new RuinStrategy(){

			@Override
			public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public Collection<Job> ruin(Collection<VehicleRoute> vehicleRoutes,
					Job targetJob, int nOfJobs2BeRemoved) {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addListener(RuinListener ruinListener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void removeListener(RuinListener ruinListener) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public Collection<RuinListener> getListeners() {
				// TODO Auto-generated method stub
				return null;
			}
			
		};
		
		StrategyModuleKey moduleKey = new StrategyModuleKey(key);
		SearchStrategyModule stratModule = new SearchStrategyModule() {
			
			@Override
			public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
				return null;
			}

			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public void addModuleListener(
					SearchStrategyModuleListener moduleListener) {
				// TODO Auto-generated method stub
				
			}
		};;;
		
		typedMap.put(accKey, acceptor);
		typedMap.put(moduleKey, stratModule);
		typedMap.put(moduleKey, stratModule);
		
		assertEquals(acceptor,typedMap.get(accKey));
		assertEquals(stratModule, typedMap.get(moduleKey));
		
	}
	

	
	@Test
	public void initialiseConstructionAlgoCorrectly(){
		VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, config);
		assertTrue(true);
	}
	
	@Test
	public void whenCreatingAlgorithm_nOfStrategiesIsCorrect(){
		VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, config);
		assertEquals(3, algo.getSearchStrategyManager().getStrategies().size());
	}

	@Test
	public void whenCreatingAlgorithm_nOfIterationsIsReadCorrectly(){
		VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, config);
		assertEquals(10, algo.getNuOfIterations());
	}
	
	@Test
	public void whenCreatingAlgorithm_nOfStrategyModulesIsCorrect(){
		VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, config);
		int nOfModules = 0;
		for(SearchStrategy strat : algo.getSearchStrategyManager().getStrategies()){
			nOfModules += strat.getSearchStrategyModules().size();
		}
		assertEquals(3, nOfModules);
	}

//	@Test
//	public void whenCreatingAlgorithm_regretInsertionIsReadCorrectly(){
//		VehicleRoutingAlgorithm algo = VehicleRoutingAlgorithms.readAndCreateAlgorithm(vrp, "src/test/resources/configWithRegretInsertion.xml");
//		int nOfModules = 0;
//		for(SearchStrategy strat : algo.getSearchStrategyManager().getStrategies()){
//			for(SearchStrategyModule module : strat.getSearchStrategyModules()){
//				if(module.getName().contains("ruin_and_recreate")){
//					nOfModules++;
//				}
//			}
//			
//		}
//		assertEquals(3, nOfModules);
//		
//	}
//	
	
}
