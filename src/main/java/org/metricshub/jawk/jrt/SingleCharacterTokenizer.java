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
 * by a single character.
 *
 * @author Danny Daglas
 */
public class SingleCharacterTokenizer implements Enumeration<Object> {

	private String input;
	private int splitChar;
	private int idx = 0;

	/**
	 * Construct a RegexTokenizer.
	 *
	 * @param input The input string to tokenize.
	 * @param splitChar The character which delineates tokens
	 *   within the input string.
	 */
	public SingleCharacterTokenizer(String input, int splitChar) {
		// input + sentinel
		this.input = input + ((char) splitChar);
		this.splitChar = splitChar;
	}

	/** {@inheritDoc} */
	@Override
	public boolean hasMoreElements() {
		return idx < input.length();
	}

	private StringBuffer sb = new StringBuffer();

	/** {@inheritDoc} */
	@Override
	public Object nextElement() {
		sb.setLength(0);
		while (idx < input.length()) {
			if (input.charAt(idx) == splitChar) {
				++idx;
				break;
			} else {
				sb.append(input.charAt(idx++));
			}
		}

		return sb.toString();
	}
}
