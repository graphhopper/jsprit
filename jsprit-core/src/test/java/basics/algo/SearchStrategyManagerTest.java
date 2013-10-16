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
package basics.algo;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.mockito.Mockito.when;

import java.util.Random;

import org.junit.Test;



public class SearchStrategyManagerTest {

	@Test
	public void StrategyManagerInAction_addingStrategy_IsSuccessful(){
		SearchStrategyManager manager = new SearchStrategyManager();
		SearchStrategy strat1 = mock(SearchStrategy.class);
		SearchStrategy strat2 = mock(SearchStrategy.class);
		manager.addStrategy(strat1, 0.5);
		manager.addStrategy(strat2, 0.5);
		assertTrue(true);
	}
	
	@Test(expected=IllegalStateException.class)
	public void StrategyManagerInAction_strategyIsNull_throwsException(){
		SearchStrategyManager manager = new SearchStrategyManager();
		manager.addStrategy(null, 1.0);
		assertTrue(false);
	}
	
	@Test(expected=IllegalStateException.class)
	public void StrategyManagerInAction_probabilityIsHigherThanOne_throwsException(){
		SearchStrategyManager manager = new SearchStrategyManager();
		SearchStrategy strat = mock(SearchStrategy.class);
		manager.addStrategy(strat, 1.5);
		assertTrue(false);
	}
	
	@Test(expected=IllegalStateException.class)
	public void StrategyManagerInAction_probabilityIsLowerThanZero_throwsException(){
		SearchStrategyManager manager = new SearchStrategyManager();
		SearchStrategy strat = mock(SearchStrategy.class);
		manager.addStrategy(strat, -1.0);
		assertTrue(false);
	}
	
	@Test(expected = IllegalStateException.class)
	public void StrategyManagerInAction_addingSeveralStratsLeadsToAProbHigherThanOne_throwsException(){
		SearchStrategyManager manager = new SearchStrategyManager();
		SearchStrategy mockedStrat1 = mock(SearchStrategy.class);
		SearchStrategy mockedStrat2 = mock(SearchStrategy.class);
		
		manager.addStrategy(mockedStrat1, 0.5);
		manager.addStrategy(mockedStrat2, 0.6);
	}
	
	@Test
	public void whenRandomDices_0point1_returnsStrategy1(){
		SearchStrategyManager managerUnderTest = new SearchStrategyManager();
		SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
		SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
		managerUnderTest.addStrategy(mockedStrategy1, 0.2);
		managerUnderTest.addStrategy(mockedStrategy2, 0.8);
		
		Random mockedRandom = mock(Random.class);
		managerUnderTest.setRandom(mockedRandom);
		stub(mockedRandom.nextDouble()).toReturn(0.1);
		
		assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy1));
		
	}
	
	@Test
	public void whenRandomDices_0point5_returnsStrategy2(){
		SearchStrategyManager managerUnderTest = new SearchStrategyManager();
		SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
		SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
		managerUnderTest.addStrategy(mockedStrategy1, 0.2);
		managerUnderTest.addStrategy(mockedStrategy2, 0.8);
		
		Random mockedRandom = mock(Random.class);
		managerUnderTest.setRandom(mockedRandom);
		when(mockedRandom.nextDouble()).thenReturn(0.5);
		
		assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy2));
		
	}
	
	@Test
	public void whenRandomDices_0point0_returnsStrategy1(){
		SearchStrategyManager managerUnderTest = new SearchStrategyManager();
		SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
		SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
		managerUnderTest.addStrategy(mockedStrategy1, 0.2);
		managerUnderTest.addStrategy(mockedStrategy2, 0.8);
		
		Random mockedRandom = mock(Random.class);
		managerUnderTest.setRandom(mockedRandom);
		when(mockedRandom.nextDouble()).thenReturn(0.0);
		
		assertThat(managerUnderTest.getRandomStrategy(), is(mockedStrategy1));
		
	}
	
	@Test(expected=IllegalStateException.class)
	public void whenRandomIsNull_throwException(){
		SearchStrategyManager managerUnderTest = new SearchStrategyManager();
		SearchStrategy mockedStrategy1 = mock(SearchStrategy.class);
		SearchStrategy mockedStrategy2 = mock(SearchStrategy.class);
		managerUnderTest.addStrategy(mockedStrategy1, 0.2);
		managerUnderTest.addStrategy(mockedStrategy2, 0.8);
		
		Random mockedRandom = null;
		managerUnderTest.setRandom(mockedRandom);
		managerUnderTest.getRandomStrategy();
		
	}
}
