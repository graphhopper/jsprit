/*******************************************************************************
 * Copyright (c) 2011 Stefan Schroeder.
 * eMail: stefan.schroeder@kit.edu
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html
 * 
 * Contributors:
 *     Stefan Schroeder - initial API and implementation
 ******************************************************************************/
package util;

import java.util.Random;

public class RandomNumberGeneration {

	private static long DEFAULT_SEED = 4711L;

	private static Random random = new Random(DEFAULT_SEED);

	public static Random getRandom() {
		return random;
	}

	public static void setSeed(long seed) {
		random.setSeed(seed);
	}

	public static void reset() {
		random.setSeed(DEFAULT_SEED);
	}

}
