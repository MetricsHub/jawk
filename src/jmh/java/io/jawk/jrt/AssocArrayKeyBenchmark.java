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

import java.util.concurrent.TimeUnit;
import io.jawk.Awk;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

/**
 * Microbenchmarks for associative-array key normalization
 * ({@link AssocArray#toLongKey(Object)}) and the {@link HashAssocArray}
 * operations that call it on every access.
 * <p>
 * The {@code baselineParseLong*} methods measure the former exception-driven
 * implementation ({@code Long.parseLong} in a try/catch) so a single run
 * quantifies the cost of the {@link NumberFormatException} stack-trace fill
 * that the non-throwing scan avoids.
 * </p>
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 3, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
@State(Scope.Thread)
public class AssocArrayKeyBenchmark {

	private Object numericKey;
	private Object shortStringKey;
	private Object wordStringKey;
	private Object subsepKey;
	private Object digitPrefixKey;
	private Object overflowDigitsKey;
	private AssocArray hashArray;

	/**
	 * Initializes benchmark operands as mutable state fields so benchmark bodies
	 * do not feed compile-time constants directly to the JIT.
	 */
	@Setup(Level.Trial)
	public void setup() {
		this.numericKey = "8675309";
		this.shortStringKey = "ab";
		this.wordStringKey = "elephant";
		this.subsepKey = "one" + Awk.DEFAULT_SUBSEP + "two";
		this.digitPrefixKey = "123abc";
		this.overflowDigitsKey = "12345678901234567890123";
		this.hashArray = AssocArray.createHash();
		this.hashArray.put(this.numericKey, "numeric value");
		this.hashArray.put(this.wordStringKey, "word value");
		this.hashArray.put(this.subsepKey, "subsep value");
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for an integer string.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeyNumeric() {
		return AssocArray.toLongKey(this.numericKey);
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for a short non-numeric
	 * string.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeyShortString() {
		return AssocArray.toLongKey(this.shortStringKey);
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for a typical word key.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeyWordString() {
		return AssocArray.toLongKey(this.wordStringKey);
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for a SUBSEP-joined
	 * multidimensional key.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeySubsep() {
		return AssocArray.toLongKey(this.subsepKey);
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for a string that starts
	 * with digits but is not an integer.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeyDigitPrefix() {
		return AssocArray.toLongKey(this.digitPrefixKey);
	}

	/**
	 * Measures {@link AssocArray#toLongKey(Object)} for an all-digits string
	 * that overflows the {@code long} range (worst case for the scan).
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long toLongKeyOverflowDigits() {
		return AssocArray.toLongKey(this.overflowDigitsKey);
	}

	/**
	 * Measures the former exception-driven normalization for an integer string.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long baselineParseLongNumeric() {
		return parseLongOrNull(this.numericKey);
	}

	/**
	 * Measures the former exception-driven normalization for a typical word
	 * key, paying the {@link NumberFormatException} stack-trace fill.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long baselineParseLongWordString() {
		return parseLongOrNull(this.wordStringKey);
	}

	/**
	 * Measures the former exception-driven normalization for a SUBSEP-joined
	 * multidimensional key.
	 *
	 * @return normalized key
	 */
	@Benchmark
	public Long baselineParseLongSubsep() {
		return parseLongOrNull(this.subsepKey);
	}

	/**
	 * Measures {@link HashAssocArray#get(Object)} for an existing word key.
	 *
	 * @return stored value
	 */
	@Benchmark
	public Object hashGetWordString() {
		return this.hashArray.get(this.wordStringKey);
	}

	/**
	 * Measures {@link HashAssocArray#get(Object)} for an existing SUBSEP-joined
	 * key.
	 *
	 * @return stored value
	 */
	@Benchmark
	public Object hashGetSubsep() {
		return this.hashArray.get(this.subsepKey);
	}

	/**
	 * Measures {@link HashAssocArray#put(Object, Object)} for a word key.
	 *
	 * @return previous value
	 */
	@Benchmark
	public Object hashPutWordString() {
		return this.hashArray.put(this.wordStringKey, "word value");
	}

	/**
	 * Measures {@link HashAssocArray#put(Object, Object)} for a SUBSEP-joined
	 * key.
	 *
	 * @return previous value
	 */
	@Benchmark
	public Object hashPutSubsep() {
		return this.hashArray.put(this.subsepKey, "subsep value");
	}

	/**
	 * The former {@link AssocArray#toLongKey(Object)} implementation, kept here
	 * as a baseline: {@code Long.parseLong} allocates a
	 * {@link NumberFormatException} (with stack-trace fill) for every
	 * non-integer key.
	 *
	 * @param key the key to parse
	 * @return the {@code Long} value, or {@code null} if not a long integer
	 */
	private static Long parseLongOrNull(Object key) {
		try {
			return Long.parseLong(key.toString());
		} catch (Exception e) { // NOPMD - EmptyCatchBlock: intentionally ignored
			return null;
		}
	}
}
