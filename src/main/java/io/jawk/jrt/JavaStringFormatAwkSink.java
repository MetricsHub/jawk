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

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;
import io.jawk.intermediate.UninitializedObject;

/**
 * Text {@link AwkSink} whose {@code printf} and {@code sprintf} use Java's
 * standard {@link String#format(Locale, String, Object...)} instead of AWK's
 * formatting rules.
 * <p>
 * This gives AWK scripts access to Java's formatting capabilities, such as
 * {@code %,d} grouping, {@code %(d} negative parentheses, date/time
 * conversions ({@code %tY}), and generally faster formatting. In exchange,
 * the format string follows {@link java.util.Formatter} semantics, not POSIX
 * AWK: conversions must match the value's Java type. AWK numbers holding an
 * integral value reach {@code String.format} as {@link Long}, other numbers
 * as {@link Double}, and text as {@link String}, so {@code %d} requires an
 * integral value and {@code %f} a floating-point one; a mismatch raises Java's
 * {@link java.util.IllegalFormatException}. Input-derived values are passed
 * as plain strings, and uninitialized variables as Java {@code null} (which
 * {@link java.util.Formatter} renders as {@code "null"}).
 * </p>
 */
public class JavaStringFormatAwkSink extends OutputStreamAwkSink {

	/**
	 * Creates a sink backed by an {@link OutputStream}.
	 *
	 * @param outputStream stream that should receive AWK output
	 */
	public JavaStringFormatAwkSink(OutputStream outputStream) {
		super(outputStream);
	}

	/**
	 * Creates a sink backed by an {@link OutputStream}.
	 *
	 * @param outputStream stream that should receive AWK output
	 * @param locale locale used for formatting
	 */
	public JavaStringFormatAwkSink(OutputStream outputStream, Locale locale) {
		super(outputStream, locale);
	}

	/**
	 * Creates a sink backed directly by a {@link PrintStream}.
	 *
	 * @param printStream stream that should receive AWK output
	 */
	public JavaStringFormatAwkSink(PrintStream printStream) {
		super(printStream);
	}

	/**
	 * Creates a sink backed directly by a {@link PrintStream}.
	 *
	 * @param printStream stream that should receive AWK output
	 * @param locale locale used for formatting
	 */
	public JavaStringFormatAwkSink(PrintStream printStream, Locale locale) {
		super(printStream, locale);
	}

	/**
	 * Formats with Java's standard {@link String#format(Locale, String, Object...)}
	 * instead of AWK's rules. {@code convfmt} is not used: number-to-string
	 * conversion follows the Java conversion in the format string.
	 *
	 * @param convfmt number-to-string conversion format ({@code CONVFMT}), unused
	 * @param format {@link java.util.Formatter}-style format string
	 * @param values arguments supplied after the format string
	 * @return formatted text
	 */
	@Override
	public String sprintf(String convfmt, String format, Object... values) {
		Object[] safeValues = values == null ? new Object[0] : values;
		Object[] javaValues = new Object[safeValues.length];
		for (int i = 0; i < safeValues.length; i++) {
			Object value = safeValues[i];
			// Jawk-internal scalar types are mapped to their natural Java
			// counterparts: input-derived text becomes a plain String, an
			// uninitialized variable becomes null, and numbers become Long
			// when they hold an integral value (runtime arithmetic yields
			// doubles even for integral results).
			if (value instanceof UninitializedObject) {
				value = null;
			} else {
				value = JRT.toJavaScalar(value);
			}
			javaValues[i] = value;
		}
		return String.format(getLocale(), format, javaValues);
	}
}
