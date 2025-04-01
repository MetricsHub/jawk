package org.metricshub.jawk.jrt;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * Jawk
 * ჻჻჻჻჻჻
 * Copyright (C) 2006 - 2025 MetricsHub
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

import java.util.Enumeration;

/**
 * Similar to StringTokenizer, except that tokens are delimited
 * by a regular expression.
 *
 * @author Danny Daglas
 */
public class RegexTokenizer implements Enumeration<Object> {

	private String[] array;
	private int idx = 0;

	/**
	 * Construct a RegexTokenizer.
	 *
	 * @param input The input string to tokenize.
	 * @param delimitterRegexPattern The regular expression delineating tokens
	 *   within the input string.
	 */
	public RegexTokenizer(String input, String delimitterRegexPattern) {
		array = input.split(delimitterRegexPattern, -2);
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasMoreElements() {
		return idx < array.length;
	}

	/** {@inheritDoc} */
	@Override
	public Object nextElement() {
		return array[idx++];
	}
}
