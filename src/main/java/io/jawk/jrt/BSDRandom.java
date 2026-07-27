package io.jawk.jrt;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * Jawk
 * ჻჻჻჻჻჻
 * Copyright (C) 2006 - 2026 MetricsHub
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

/**
 * Simple pseudo-random number generator compatible with the C library
 * {@code random()} function.
 */
public class BSDRandom {

	private static final int RAND_DEG = 31;
	private static final int RAND_SEP = 3;
	private final int[] state = new int[RAND_DEG];
	private int fptr;
	private int rptr;
	private int seed;

	/**
	 * Creates a new generator with the specified seed.
	 *
	 * @param seed Initial pseudo-random seed
	 */
	public BSDRandom(int seed) {
		setSeed(seed);
	}

	/**
	 * Seed the generator. A seed of {@code 0} is transformed to {@code 1}
	 * as in the original implementation.
	 *
	 * @param newSeed New pseudo-random seed
	 * @return Previous pseudo-random seed
	 */
	public final int setSeed(int newSeed) {
		int previousSeed = seed;
		seed = newSeed;
		int effectiveSeed = newSeed;
		if (effectiveSeed == 0) {
			effectiveSeed = 1;
		}
		state[0] = effectiveSeed;
		for (int i = 1; i < RAND_DEG; i++) {
			long val = 16807L * state[i - 1] % 2147483647L;
			state[i] = (int) val;
		}
		fptr = RAND_SEP;
		rptr = 0;
		for (int i = 0; i < 10 * RAND_DEG; i++) {
			nextInt();
		}
		return previousSeed;
	}

	/**
	 * Returns the seed most recently supplied to {@link #setSeed(int)}.
	 *
	 * @return Current pseudo-random seed
	 */
	public int getSeed() {
		return seed;
	}

	private int nextInt() {
		int val = state[fptr] + state[rptr];
		state[fptr] = val;
		if (++fptr >= RAND_DEG) {
			fptr = 0;
		}
		if (++rptr >= RAND_DEG) {
			rptr = 0;
		}
		return (val >>> 1) & 0x7fffffff;
	}

	/**
	 * Return the next pseudo-random number in the range {@code [0.0,1.0)}.
	 *
	 * @return Next pseudo-random floating-point value
	 */
	public double nextDouble() {
		return ((double) nextInt()) / 2147483647.0;
	}
}
