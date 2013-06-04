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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import basics.route.Start;

public class TestRefs {
	
	@Test
	public void testReferencs(){
		List<Start> starts = new ArrayList<Start>();
		starts.add(Start.newInstance("foo0", 0.0, 0.0));
		starts.add(Start.newInstance("foo1", 1.0, 1.0));
		
		doSmth(starts);
		
		assertTrue(starts.get(0).getLocationId().startsWith("foo"));
		assertTrue(starts.get(1).getLocationId().startsWith("foo"));
	}

	private void doSmth(List<Start> starts) {
		int count = 0;
		for(Start s : starts){
			s = Start.newInstance("yo_"+count,0.0,0.0);
			count++;
		}
		
	}

}
