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
package basics.algo;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.junit.Test;

import algorithms.acceptors.SolutionAcceptor;
import algorithms.selectors.SolutionSelector;
import basics.VehicleRoutingProblem;
import basics.VehicleRoutingProblemSolution;
import basics.algo.SearchStrategy;
import basics.algo.SearchStrategyModule;
import basics.algo.SearchStrategyModuleListener;



public class SearchStrategyTest {
	
	@Test(expected=IllegalStateException.class)
	public void whenANullModule_IsAdded_throwException(){
		SolutionSelector select = mock(SolutionSelector.class);
		SolutionAcceptor accept = mock(SolutionAcceptor.class);
		
		SearchStrategy strat = new SearchStrategy(select, accept);
		strat.addModule(null);
		
	}
	
	@Test
	public void whenStratRunsWithOneModule_runItOnes(){
		SolutionSelector select = mock(SolutionSelector.class);
		SolutionAcceptor accept = mock(SolutionAcceptor.class);
		
		final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
		final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
		
		when(select.selectSolution(null)).thenReturn(newSol);
		
		final Collection<Integer> runs = new ArrayList<Integer>();
		
		SearchStrategy strat = new SearchStrategy(select, accept);
		SearchStrategyModule mod = new SearchStrategyModule() {
			
			@Override
			public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
				runs.add(1);
				return vrpSolution;
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
		};
		strat.addModule(mod);
		strat.run(vrp, null);
		
		assertEquals(runs.size(), 1);
	}
	
	@Test
	public void whenStratRunsWithTwoModule_runItTwice(){
		SolutionSelector select = mock(SolutionSelector.class);
		SolutionAcceptor accept = mock(SolutionAcceptor.class);
		
		final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
		final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
		
		when(select.selectSolution(null)).thenReturn(newSol);
		
		final Collection<Integer> runs = new ArrayList<Integer>();
		
		SearchStrategy strat = new SearchStrategy(select, accept);
		
		SearchStrategyModule mod = new SearchStrategyModule() {
			
			@Override
			public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
				runs.add(1);
				return vrpSolution;
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
		};
		SearchStrategyModule mod2 = new SearchStrategyModule() {
			
			@Override
			public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
				runs.add(1);
				return vrpSolution;
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
		};
		strat.addModule(mod);
		strat.addModule(mod2);
		strat.run(vrp, null);
		
		assertEquals(runs.size(), 2);
	}
	
	@Test
	public void whenStratRunsWithNModule_runItNTimes(){
		SolutionSelector select = mock(SolutionSelector.class);
		SolutionAcceptor accept = mock(SolutionAcceptor.class);
		
		final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
		final VehicleRoutingProblemSolution newSol = mock(VehicleRoutingProblemSolution.class);
		
		when(select.selectSolution(null)).thenReturn(newSol);
		
		int N = new Random().nextInt(1000);
		
		final Collection<Integer> runs = new ArrayList<Integer>();
		
		SearchStrategy strat = new SearchStrategy(select, accept);
		
		for(int i=0;i<N;i++){
			SearchStrategyModule mod = new SearchStrategyModule() {

				@Override
				public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
					runs.add(1);
					return vrpSolution;
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
			};
			strat.addModule(mod);
		}
		strat.run(vrp, null);
		assertEquals(runs.size(), N);
	}
	
	@Test(expected=IllegalStateException.class) 
	public void whenSelectorDeliversNullSolution_throwException(){
		SolutionSelector select = mock(SolutionSelector.class);
		SolutionAcceptor accept = mock(SolutionAcceptor.class);
		
		final VehicleRoutingProblem vrp = mock(VehicleRoutingProblem.class);
		
		when(select.selectSolution(null)).thenReturn(null);
		
		int N = new Random().nextInt(1000);
		
		final Collection<Integer> runs = new ArrayList<Integer>();
		
		SearchStrategy strat = new SearchStrategy(select, accept);
		
		for(int i=0;i<N;i++){
			SearchStrategyModule mod = new SearchStrategyModule() {

				@Override
				public VehicleRoutingProblemSolution runAndGetSolution(VehicleRoutingProblemSolution vrpSolution) {
					runs.add(1);
					return vrpSolution;
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
			};
			strat.addModule(mod);
		}
		strat.run(vrp, null);
		assertEquals(runs.size(), N);
	}
	

}
