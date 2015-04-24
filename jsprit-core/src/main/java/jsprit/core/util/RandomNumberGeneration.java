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

import java.util.Random;

public class RandomNumberGeneration {

	private static long DEFAULT_SEED = 4711L;

	private static Random random = new Random(DEFAULT_SEED);

	public static Random newInstance(){
		return new Random(DEFAULT_SEED);
	}

	public static Random getRandom() {
		return random;
	}

	public static void setSeed(long seed) {
		random.setSeed(seed);
	}

	public static void reset() {
		reset(random);
	}

	public static void reset(Random random){
		random.setSeed(DEFAULT_SEED);
	}

}
