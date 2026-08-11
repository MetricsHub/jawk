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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormatSymbols;
import java.util.IllegalFormatException;
import java.util.Locale;

/**
 * AWK's {@code printf}/{@code sprintf} formatting engine.
 * <p>
 * This class implements the POSIX AWK formatting semantics (as implemented by
 * gawk), which differ from both C's {@code printf()} and
 * {@link java.lang.String#format(String, Object...)} in several ways:
 * </p>
 * <ul>
 * <li>{@code %s} converts numeric values to strings with AWK's number-to-string
 * rules: integral values are printed without a fractional part, and other
 * values are formatted with {@code CONVFMT};</li>
 * <li>{@code %c} prints the character for a numeric code point, or the first
 * character of a string value;</li>
 * <li>{@code %i} is an alias for {@code %d}, and {@code %u} prints the value
 * as an unsigned 64-bit integer;</li>
 * <li>dynamic field width and precision ({@code *}) consume arguments, and
 * gawk-style positional specifiers ({@code %n$}) are honored;</li>
 * <li>integer conversions of values that exceed the 64-bit range fall back to
 * the full decimal expansion ({@code %d}/{@code %i}) or {@code %g} notation
 * ({@code %u}/{@code %o}/{@code %x}/{@code %X}), like gawk;</li>
 * <li>NaN and infinities print as {@code nan}, {@code inf}, and {@code -inf};</li>
 * <li>{@code %e}, {@code %f}, and {@code %g} round halfway cases to even, like
 * the C library used by gawk;</li>
 * <li>unknown conversion specifiers are printed verbatim without consuming an
 * argument, and a fatal {@link AwkRuntimeException} is raised when there are
 * not enough arguments to satisfy the format string.</li>
 * </ul>
 * <p>
 * This formatting logic was originally externalized in the
 * <a href="https://github.com/metricshub/printf4j">Printf4J</a> project, which
 * emulated glibc's {@code printf()}. It has been reincorporated into Jawk and
 * adapted to AWK's semantics.
 * </p>
 */
public final class AwkPrintf {

	/** Default AWK number-to-string conversion format ({@code CONVFMT}). */
	public static final String DEFAULT_CONVFMT = "%.6g";

	/** Conversion characters recognized as AWK format specifiers. */
	private static final String CONVERSION_CHARS = "diouxXeEfFgGaAcs";

	/** Length modifier characters accepted (and ignored) like gawk. */
	private static final String LENGTH_MODIFIERS = "hjlLtz";

	/** A one-character string holding the NUL character, printed by {@code %c} for empty values. */
	private static final String NUL_STRING = Character.toString((char) 0);

	/** 2^63 as a double, the first value beyond the signed 64-bit range. */
	private static final double TWO_POW_63 = 9.223372036854775808e18;

	/** 2^64 as a {@link BigInteger}, used for unsigned wrapping checks. */
	private static final BigInteger TWO_POW_64 = BigInteger.ONE.shiftLeft(64);

	private AwkPrintf() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Formats the given arguments with AWK's {@code sprintf()} semantics, using
	 * {@link Locale#US} and the default {@code CONVFMT} ({@code "%.6g"}).
	 *
	 * @param format AWK format string
	 * @param args arguments supplied after the format string
	 * @return the formatted text
	 * @throws AwkRuntimeException when there are not enough arguments to
	 *         satisfy the format string
	 */
	public static String sprintf(final String format, final Object... args) {
		return sprintf(Locale.US, DEFAULT_CONVFMT, format, args);
	}

	/**
	 * Formats the given arguments with AWK's {@code sprintf()} semantics.
	 *
	 * @param locale locale used for numeric formatting (decimal separator,
	 *        grouping separator for the {@code '} flag)
	 * @param convfmt number-to-string conversion format ({@code CONVFMT}) used
	 *        by {@code %s} for non-integral numeric values
	 * @param format AWK format string
	 * @param args arguments supplied after the format string
	 * @return the formatted text
	 * @throws AwkRuntimeException when there are not enough arguments to
	 *         satisfy the format string
	 */
	public static String sprintf(final Locale locale, final String convfmt, final String format, final Object... args) {
		Locale actualLocale = locale == null ? Locale.US : locale;
		// An explicitly empty CONVFMT stays empty, like gawk; only a null
		// (absent) format selects the default.
		String actualConvfmt = convfmt == null ? DEFAULT_CONVFMT : convfmt;
		Object[] actualArgs = args == null ? new Object[0] : args;
		return new AwkPrintfFormatter(actualLocale, actualConvfmt, format, actualArgs).format();
	}

	/**
	 * Converts a value to a string using AWK's number-to-string rules.
	 * <p>
	 * Non-numeric values are converted with {@code toString()}. Numeric values
	 * holding an integral value are printed without a fractional part (using
	 * the full decimal expansion when the value exceeds the 64-bit range), NaN
	 * and infinities print as {@code nan}, {@code inf}, and {@code -inf}, and
	 * all other numeric values are formatted with the supplied conversion
	 * format ({@code CONVFMT} or {@code OFMT}).
	 * </p>
	 *
	 * @param value value to convert
	 * @param conversionFormat number-to-string conversion format
	 * @param locale locale used for numeric formatting
	 * @return the AWK string value of {@code value}
	 */
	public static String toAwkString(final Object value, final String conversionFormat, final Locale locale) {
		if (value == null) {
			return "";
		}
		if (value instanceof Long || value instanceof Integer || value instanceof Short || value instanceof Byte) {
			// Preserve exact 64-bit values that a double round-trip would corrupt.
			return Long.toString(((Number) value).longValue());
		}
		if (!(value instanceof Number)) {
			return value.toString();
		}
		return numberToAwkString(((Number) value).doubleValue(), conversionFormat, locale);
	}

	private static String numberToAwkString(final double number, final String conversionFormat, final Locale locale) {
		if (Double.isNaN(number)) {
			return "nan";
		}
		if (Double.isInfinite(number)) {
			return number > 0 ? "inf" : "-inf";
		}
		if (JRT.isActuallyLong(number)) {
			double rounded = Math.rint(number);
			if (rounded >= -TWO_POW_63 && rounded < TWO_POW_63) {
				return Long.toString((long) rounded);
			}
			// The exact binary value of the double is intended: it makes rounding match gawk's C library.
			return new BigDecimal(rounded).toBigInteger().toString(); // NOPMD
		}
		// An explicitly empty CONVFMT/OFMT stays empty, like gawk; only a
		// null (absent) format selects the default.
		String fmt = conversionFormat == null ? DEFAULT_CONVFMT : conversionFormat;
		return sprintf(locale, DEFAULT_CONVFMT, fmt, Double.valueOf(number));
	}

	/**
	 * Immutable set of conversion flags parsed from one format specifier.
	 */
	private static final class Flags {

		private final boolean leftJustify;
		private final boolean plusSign;
		private final boolean spaceSign;
		private final boolean zeroPad;
		private final boolean alternate;
		private final boolean grouping;

		Flags(boolean leftJustify, boolean plusSign, boolean spaceSign, boolean zeroPad, boolean alternate,
				boolean grouping) {
			this.leftJustify = leftJustify;
			this.plusSign = plusSign;
			this.spaceSign = spaceSign;
			this.zeroPad = zeroPad;
			this.alternate = alternate;
			this.grouping = grouping;
		}

		/**
		 * Returns these flags with the {@code '} grouping flag cleared, for
		 * conversions that gawk never groups (octal and hexadecimal).
		 *
		 * @return an equivalent flag set without grouping
		 */
		Flags withoutGrouping() {
			return grouping ? new Flags(leftJustify, plusSign, spaceSign, zeroPad, alternate, false) : this;
		}
	}

	/**
	 * Stateful single-pass formatter for one {@code sprintf()} call.
	 */
	private static final class AwkPrintfFormatter {

		private final Locale locale;
		private final String convfmt;
		private final String format;
		private final Object[] args;
		private final StringBuilder out;

		/** Index of the next sequential argument to consume. */
		private int argIndex;

		/** Whether a positional ({@code n$}) argument reference was seen. */
		private boolean sawPositional;

		/** Whether a sequential argument reference was seen. */
		private boolean sawSequential;

		AwkPrintfFormatter(Locale locale, String convfmt, String format, Object[] args) {
			this.locale = locale;
			this.convfmt = convfmt;
			this.format = format;
			this.args = args;
			this.out = new StringBuilder(format.length() + 16);
		}

		String format() {
			int length = format.length();
			int i = 0;
			while (i < length) {
				char c = format.charAt(i);
				if (c != '%') {
					out.append(c);
					i++;
					continue;
				}
				if (i + 1 >= length) {
					// Dangling '%' at the end of the format: print it verbatim.
					out.append('%');
					break;
				}
				if (format.charAt(i + 1) == '%') {
					out.append('%');
					i += 2;
					continue;
				}
				i = formatSpecifier(i);
			}
			return out.toString();
		}

		/**
		 * Parses and renders one format specifier starting at {@code start}
		 * (which points at the {@code '%'}), and returns the index of the
		 * first character after the specifier.
		 */
		private int formatSpecifier(int start) {
			int length = format.length();
			int i = start + 1;

			// gawk-style positional specifier: %n$...
			int argPosition = 0;
			int digitsEnd = i;
			while (digitsEnd < length && isAsciiDigit(format.charAt(digitsEnd))) {
				digitsEnd++;
			}
			if (digitsEnd > i && digitsEnd < length && format.charAt(digitsEnd) == '$') {
				argPosition = parseInt(format, i, digitsEnd);
				if (argPosition <= 0) {
					throw new AwkRuntimeException("argument index with `$' must be > 0 in `" + format + "'");
				}
				i = digitsEnd + 1;
			}

			// Flags, in any order and possibly repeated.
			boolean leftJustify = false;
			boolean plusSign = false;
			boolean spaceSign = false;
			boolean zeroPad = false;
			boolean alternate = false;
			boolean grouping = false;
			flagLoop: while (i < length) {
				switch (format.charAt(i)) {
				case '-':
					leftJustify = true;
					break;
				case '+':
					plusSign = true;
					break;
				case ' ':
					spaceSign = true;
					break;
				case '0':
					zeroPad = true;
					break;
				case '#':
					alternate = true;
					break;
				case '\'':
					grouping = true;
					break;
				default:
					break flagLoop;
				}
				i++;
			}

			// Field width: digits, or '*' (optionally '*n$').
			int width = -1;
			if (i < length && format.charAt(i) == '*') {
				i++;
				int starArgEnd = starPositionEnd(i);
				long dynamicWidth;
				if (starArgEnd > i) {
					int starPosition = parseInt(format, i, starArgEnd - 1);
					// gawk treats a zero-indexed star operand ("%*0$d") as
					// the value zero, without consuming an argument.
					dynamicWidth = starPosition == 0 ? 0 : (long) JRT.toDouble(argAt(starPosition));
					i = starArgEnd;
				} else {
					// A sequential star operand pins the format to sequential
					// mode; an explicitly positioned one is neutral.
					recordArgumentMode(false);
					dynamicWidth = (long) JRT.toDouble(nextArg());
				}
				if (dynamicWidth < 0) {
					leftJustify = true;
					dynamicWidth = -dynamicWidth;
				}
				width = (int) Math.min(dynamicWidth, Integer.MAX_VALUE);
			} else {
				int widthEnd = i;
				while (widthEnd < length && isAsciiDigit(format.charAt(widthEnd))) {
					widthEnd++;
				}
				if (widthEnd > i) {
					width = parseInt(format, i, widthEnd);
					i = widthEnd;
				}
			}

			// Precision: '.' followed by digits (empty means 0), or '.*'.
			int precision = -1;
			if (i < length && format.charAt(i) == '.') {
				i++;
				if (i < length && format.charAt(i) == '*') {
					i++;
					int starArgEnd = starPositionEnd(i);
					long dynamicPrecision;
					if (starArgEnd > i) {
						int starPosition = parseInt(format, i, starArgEnd - 1);
						// Same zero-index rule as the width operand.
						dynamicPrecision = starPosition == 0 ? 0 : (long) JRT.toDouble(argAt(starPosition));
						i = starArgEnd;
					} else {
						// Same sequential-mode tracking as the width operand.
						recordArgumentMode(false);
						dynamicPrecision = (long) JRT.toDouble(nextArg());
					}
					// A negative dynamic precision means "no precision" in C.
					if (dynamicPrecision >= 0) {
						precision = (int) Math.min(dynamicPrecision, Integer.MAX_VALUE);
					}
				} else {
					int precisionEnd = i;
					while (precisionEnd < length && isAsciiDigit(format.charAt(precisionEnd))) {
						precisionEnd++;
					}
					precision = precisionEnd == i ? 0 : parseInt(format, i, precisionEnd);
					i = precisionEnd;
				}
			}

			// Length modifiers (h, j, l, L, t, z) are each accepted at most
			// once and ignored, like gawk. A repeated modifier such as "ll"
			// or "hh" makes the whole specifier invalid, also like gawk.
			int modifierMask = 0;
			while (i < length) {
				int modifierIndex = LENGTH_MODIFIERS.indexOf(format.charAt(i));
				if (modifierIndex < 0 || (modifierMask & 1 << modifierIndex) != 0) {
					break;
				}
				modifierMask |= 1 << modifierIndex;
				i++;
			}

			if (i < length && format.charAt(i) == '%') {
				// A percent conversion reached through flags, width, or
				// precision prints a plain '%' and ignores them all, like
				// gawk ("%5%" prints "%"). An explicit position still pins
				// the format to positional mode, also like gawk.
				if (argPosition > 0) {
					recordArgumentMode(true);
					requireArgumentIndex(argPosition);
				}
				out.append('%');
				return i + 1;
			}

			if (i >= length || CONVERSION_CHARS.indexOf(format.charAt(i)) < 0) {
				// Unknown or unterminated conversion: print the specifier
				// verbatim (including the offending character) without
				// consuming an argument, like gawk. An explicit position
				// still pins the format to positional mode, also like gawk.
				if (argPosition > 0) {
					recordArgumentMode(true);
					requireArgumentIndex(argPosition);
				}
				int end = i < length ? i + 1 : length;
				out.append(format, start, end);
				return end;
			}

			char conversion = format.charAt(i);
			i++;

			Flags flags = new Flags(leftJustify, plusSign, spaceSign, zeroPad, alternate, grouping);
			recordArgumentMode(argPosition > 0);
			Object arg = argPosition > 0 ? argAt(argPosition) : nextArg();
			render(conversion, flags, width, precision, arg);
			return i;
		}

		/**
		 * Returns the index right after a {@code n$} sequence starting at
		 * {@code i}, or {@code i} when there is no such sequence.
		 */
		private int starPositionEnd(int i) {
			int length = format.length();
			int digitsEnd = i;
			while (digitsEnd < length && isAsciiDigit(format.charAt(digitsEnd))) {
				digitsEnd++;
			}
			if (digitsEnd > i && digitsEnd < length && format.charAt(digitsEnd) == '$') {
				return digitsEnd + 1;
			}
			return i;
		}

		private Object nextArg() {
			if (argIndex >= args.length) {
				throw new AwkRuntimeException("not enough arguments to satisfy format string `" + format + "'");
			}
			return args[argIndex++];
		}

		private Object argAt(int position) {
			requireArgumentIndex(position);
			return args[position - 1];
		}

		/**
		 * Validates a positional ({@code n$}) argument index against the
		 * supplied arguments, like gawk, which checks the index even for
		 * conversions that do not consume the referenced value.
		 */
		private void requireArgumentIndex(int position) {
			if (position <= 0) {
				throw new AwkRuntimeException("argument index with `$' must be > 0 in `" + format + "'");
			}
			if (position > args.length) {
				throw new AwkRuntimeException(
						"argument index " + position + " greater than total number of supplied arguments in `"
								+ format + "'");
			}
		}

		/**
		 * Records how one conversion selects its value argument and rejects
		 * format strings that mix positional ({@code n$}) and sequential
		 * conversions, like gawk. Star width and precision operands are not
		 * tracked: gawk allows an explicitly positioned star operand
		 * ({@code %*2$s}) alongside sequential conversions.
		 *
		 * @param positional whether the conversion used an {@code n$} index
		 */
		private void recordArgumentMode(boolean positional) {
			if (positional) {
				sawPositional = true;
			} else {
				sawSequential = true;
			}
			if (sawPositional && sawSequential) {
				throw new AwkRuntimeException("must use `count$' on all formats or none in `" + format + "'");
			}
		}

		private void render(char conversion, Flags flags, int width, int precision, Object arg) {
			switch (conversion) {
			case 'c':
				appendPadded(characterOf(arg), flags.leftJustify, false, width);
				break;
			case 's':
				String s = toAwkString(arg, convfmt, locale);
				if (precision >= 0 && s.codePointCount(0, s.length()) > precision) {
					// The precision counts characters (code points), so it
					// can never split a surrogate pair.
					s = s.substring(0, s.offsetByCodePoints(0, precision));
				}
				appendPadded(s, flags.leftJustify, false, width);
				break;
			case 'd':
			case 'i':
				renderSignedInteger(flags, width, precision, arg);
				break;
			case 'u':
			case 'o':
			case 'x':
			case 'X':
				renderUnsignedInteger(conversion, flags, width, precision, arg);
				break;
			case 'e':
			case 'E':
			case 'f':
			case 'F':
			case 'g':
			case 'G':
			case 'a':
			case 'A':
				renderFloat(conversion, flags, width, precision, arg);
				break;
			default:
				// Unreachable: the caller only passes known conversions.
				break;
			}
		}

		/** Renders the {@code %c} character for the given argument. */
		private String characterOf(Object arg) {
			if (arg == null) {
				return NUL_STRING;
			}
			boolean numeric = arg instanceof Number || (arg instanceof StrNum && ((StrNum) arg).isNumber());
			if (numeric) {
				long code = (long) JRT.toDouble(arg);
				StringBuilder sb = new StringBuilder(2);
				if (code >= 0 && code <= Character.MAX_CODE_POINT) {
					sb.appendCodePoint((int) code);
				} else {
					sb.append((char) code);
				}
				return sb.toString();
			}
			String s = arg.toString();
			if (s.isEmpty()) {
				return NUL_STRING;
			}
			StringBuilder sb = new StringBuilder(2);
			sb.appendCodePoint(s.codePointAt(0));
			return sb.toString();
		}

		private void renderSignedInteger(Flags flags, int width, int precision, Object arg) {
			double d = JRT.toDouble(arg);
			if (renderNonFinite('d', flags, width, d)) {
				return;
			}

			boolean negative;
			String magnitude;
			if (arg instanceof Long || arg instanceof Integer || arg instanceof Short || arg instanceof Byte) {
				long v = ((Number) arg).longValue();
				negative = v < 0;
				magnitude = negative ? Long.toUnsignedString(-v) : Long.toString(v);
			} else if (d >= -TWO_POW_63 && d < TWO_POW_63) {
				long v = (long) d;
				negative = v < 0;
				magnitude = negative ? Long.toUnsignedString(-v) : Long.toString(v);
			} else {
				// Out of 64-bit range: print the full decimal expansion of the
				// (integral) double, like gawk.
				// The exact binary value of the double is intended: it makes rounding match gawk's C library.
				BigInteger bi = new BigDecimal(d).toBigInteger(); // NOPMD
				negative = bi.signum() < 0;
				magnitude = bi.abs().toString();
			}

			String sign = negative ? "-" : flags.plusSign ? "+" : flags.spaceSign ? " " : "";
			appendInteger(sign, "", magnitude, flags, width, precision, isZeroMagnitude(magnitude));
		}

		private void renderUnsignedInteger(char conversion, Flags flags, int width, int precision, Object arg) {
			double d = JRT.toDouble(arg);
			if (renderNonFinite(conversion, flags, width, d)) {
				return;
			}

			int radix = conversion == 'o' ? 8 : conversion == 'u' ? 10 : 16;
			String magnitude;
			if (arg instanceof Long || arg instanceof Integer || arg instanceof Short || arg instanceof Byte) {
				magnitude = Long.toUnsignedString(((Number) arg).longValue(), radix);
			} else if (d >= -TWO_POW_63 && d < TWO_POW_63) {
				magnitude = Long.toUnsignedString((long) d, radix);
			} else {
				// The exact binary value of the double is intended: it makes rounding match gawk's C library.
				BigInteger bi = new BigDecimal(d).toBigInteger(); // NOPMD
				if (bi.signum() >= 0 && bi.compareTo(TWO_POW_64) < 0) {
					magnitude = bi.toString(radix);
				} else {
					// Out of the unsigned 64-bit range: fall back to %g
					// notation with the original sign, flags, precision, and
					// width, like gawk.
					renderFloat('g', flags, width, precision, Double.valueOf(d));
					return;
				}
			}
			if (conversion == 'X') {
				magnitude = magnitude.toUpperCase(Locale.ROOT);
			}

			boolean zeroValue = d == 0;
			boolean zeroMagnitude = isZeroMagnitude(magnitude);
			int actualPrecision = precision;
			if (zeroMagnitude && precision == 0 && (flags.alternate || !zeroValue)) {
				// gawk prints "0" rather than nothing for a zero magnitude
				// with an explicit zero precision when the '#' flag is given,
				// or when the original value is nonzero and merely truncates
				// to zero.
				actualPrecision = 1;
			}
			// The '#' prefix depends on the original value, not the truncated
			// magnitude: gawk prints "0x0" for %#.0x with 0.1. For %o, gawk
			// always adds the alternate leading zero in addition to any
			// precision padding: %#.5o of 1 prints "000001".
			String prefix = "";
			if (flags.alternate && !zeroValue) {
				if (conversion == 'x') {
					prefix = "0x";
				} else if (conversion == 'X') {
					prefix = "0X";
				} else if (conversion == 'o') {
					prefix = "0";
				}
			}
			// gawk's ' flag groups decimal output only, never octal or
			// hexadecimal.
			Flags integerFlags = conversion == 'u' ? flags : flags.withoutGrouping();
			appendInteger("", prefix, magnitude, integerFlags, width, actualPrecision, zeroMagnitude);
		}

		/**
		 * Applies precision, grouping, and width to an integer body and
		 * appends it to the output.
		 */
		private void appendInteger(
				String sign,
				String prefix,
				String magnitude,
				Flags flags,
				int width,
				int precision,
				boolean zeroMagnitude) {
			String digits = magnitude;
			if (precision == 0 && zeroMagnitude) {
				// C: a zero value with an explicit zero precision prints no
				// characters. gawk drops the sign flags as well.
				appendPadded("", flags.leftJustify, false, width);
				return;
			}
			if (precision > digits.length()) {
				digits = zeros(precision - digits.length()) + digits;
			}
			if (flags.grouping) {
				digits = groupDigits(digits);
			}
			String body = sign + prefix + digits;
			if (width > body.length() && flags.zeroPad && !flags.leftJustify && precision < 0) {
				// Zero padding goes between the sign/prefix and the digits.
				out.append(sign).append(prefix);
				out.append(zeros(width - body.length()));
				out.append(digits);
				return;
			}
			appendPadded(body, flags.leftJustify, false, width);
		}

		private void renderFloat(char conversion, Flags flags, int width, int precision, Object arg) {
			double d = JRT.toDouble(arg);
			if (renderNonFinite(conversion, flags, width, d)) {
				return;
			}
			String magnitude = floatBody(conversion, flags, precision, d);
			if (magnitude == null) {
				return;
			}
			boolean negative = d < 0 || (d == 0 && Double.doubleToRawLongBits(d) != 0L);
			String sign = negative ? "-" : flags.plusSign ? "+" : flags.spaceSign ? " " : "";
			// The hexadecimal prefix of %a/%A stays ahead of any zero padding,
			// like an integer prefix.
			String prefix = "";
			if (magnitude.startsWith("0x") || magnitude.startsWith("0X")) {
				prefix = magnitude.substring(0, 2);
				magnitude = magnitude.substring(2);
			}
			String full = sign + prefix + magnitude;
			if (width > full.length() && flags.zeroPad && !flags.leftJustify) {
				out.append(sign).append(prefix);
				out.append(zeros(width - full.length()));
				out.append(magnitude);
				return;
			}
			appendPadded(full, flags.leftJustify, false, width);
		}

		/**
		 * Renders the digits of a finite double for a floating-point
		 * conversion, without sign and without width padding: the absolute
		 * value is formatted and the caller applies the sign.
		 */
		private String floatBody(char conversion, Flags flags, int precision, double d) {
			double abs = Math.abs(d);
			switch (conversion) {
			case 'f':
			case 'F': {
				int p = precision < 0 ? 6 : precision;
				// The exact binary value of the double is intended: it makes rounding match gawk's C library.
				String s = decimalString(new BigDecimal(abs).setScale(p, RoundingMode.HALF_EVEN)); // NOPMD
				if (flags.alternate && p == 0) {
					s = forceDecimalSeparator(s);
				}
				if (flags.grouping) {
					s = groupDigits(s);
				}
				return s;
			}
			case 'e':
			case 'E': {
				int p = precision < 0 ? 6 : precision;
				String s = scientific(abs, p);
				if (flags.alternate && p == 0) {
					int exponentStart = s.indexOf('e');
					s = forceDecimalSeparator(s.substring(0, exponentStart)) + s.substring(exponentStart);
				}
				return conversion == 'E' ? s.toUpperCase(Locale.ROOT) : s;
			}
			case 'g':
			case 'G': {
				int p = precision < 0 ? 6 : precision == 0 ? 1 : precision;
				String s = generalFloat(abs, p, flags.alternate, flags.grouping);
				return conversion == 'G' ? s.toUpperCase(Locale.ROOT) : s;
			}
			case 'a':
			case 'A':
			default: {
				// %a is C-library dependent in gawk; delegate to Java's
				// hexadecimal float notation.
				StringBuilder spec = new StringBuilder("%");
				if (precision >= 0) {
					spec.append('.').append(precision);
				}
				spec.append(conversion);
				try {
					String s = String.format(locale, spec.toString(), Double.valueOf(abs));
					return s;
				} catch (IllegalFormatException e) {
					out.append(spec);
					return null;
				}
			}
			}
		}

		/** Formats {@code abs >= 0} in C's {@code %e} notation. */
		private String scientific(double abs, int precision) {
			BigDecimal mantissa;
			int exponent;
			if (abs == 0) {
				mantissa = BigDecimal.ZERO.setScale(precision);
				exponent = 0;
			} else {
				// The exact binary value of the double is intended: this is what makes
				// rounding match the C library used by gawk.
				BigDecimal rounded = new BigDecimal(abs) // NOPMD
						.round(new MathContext(precision + 1, RoundingMode.HALF_EVEN));
				exponent = rounded.precision() - rounded.scale() - 1;
				mantissa = rounded.movePointLeft(exponent).setScale(precision, RoundingMode.UNNECESSARY);
			}
			return decimalString(mantissa) + "e" + (exponent < 0 ? "-" : "+") + exponentDigits(Math.abs(exponent));
		}

		/** Formats {@code abs >= 0} in C's {@code %g} notation. */
		private String generalFloat(double abs, int precision, boolean alternate, boolean grouping) {
			if (abs == 0) {
				return alternate ? forceDecimalSeparator("0") + zeros(precision - 1) : "0";
			}
			// The exact binary value of the double is intended: it makes rounding match gawk's C library.
			BigDecimal rounded = new BigDecimal(abs).round(new MathContext(precision, RoundingMode.HALF_EVEN)); // NOPMD
			int exponent = rounded.precision() - rounded.scale() - 1;
			if (exponent >= -4 && exponent < precision) {
				String s = decimalString(rounded.setScale(precision - 1 - exponent, RoundingMode.UNNECESSARY));
				s = alternate ? forceDecimalSeparator(s) : stripTrailingFractionZeros(s);
				// gawk groups %g in fixed notation, like %f, but never in
				// exponential notation.
				return grouping ? groupDigits(s) : s;
			}
			String mantissa = decimalString(
					rounded.movePointLeft(exponent).setScale(precision - 1, RoundingMode.UNNECESSARY));
			mantissa = alternate ? forceDecimalSeparator(mantissa) : stripTrailingFractionZeros(mantissa);
			return mantissa + "e" + (exponent < 0 ? "-" : "+") + exponentDigits(Math.abs(exponent));
		}

		/**
		 * Renders NaN and infinities for any numeric conversion, honoring the
		 * sign flags and field width, and returns {@code true} when the value
		 * was such a special value.
		 */
		private boolean renderNonFinite(char conversion, Flags flags, int width, double d) {
			if (!Double.isNaN(d) && !Double.isInfinite(d)) {
				return false;
			}
			String body;
			if (Double.isNaN(d)) {
				body = flags.plusSign ? "+nan" : flags.spaceSign ? " nan" : "nan";
			} else if (d > 0) {
				body = flags.plusSign ? "+inf" : flags.spaceSign ? " inf" : "inf";
			} else {
				body = "-inf";
			}
			if (isUpperCaseConversion(conversion)) {
				body = body.toUpperCase(Locale.ROOT);
			}
			// The zero flag is ignored for non-finite values, like C.
			appendPadded(body, flags.leftJustify, false, width);
			return true;
		}

		/** Renders a {@link BigDecimal} using the locale's decimal separator. */
		private String decimalString(BigDecimal value) {
			String s = value.toPlainString();
			char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			return decimalSeparator == '.' ? s : s.replace('.', decimalSeparator);
		}

		/** Inserts locale grouping separators into the integer part of {@code s}. */
		private String groupDigits(String s) {
			char groupingSeparator = DecimalFormatSymbols.getInstance(locale).getGroupingSeparator();
			char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			int end = s.indexOf(decimalSeparator);
			if (end < 0) {
				end = s.length();
			}
			StringBuilder sb = new StringBuilder(s.length() + 8);
			for (int i = 0; i < end; i++) {
				sb.append(s.charAt(i));
				int remaining = end - 1 - i;
				if (remaining > 0 && remaining % 3 == 0 && isAsciiDigit(s.charAt(i))) {
					sb.append(groupingSeparator);
				}
			}
			sb.append(s, end, s.length());
			return sb.toString();
		}

		/**
		 * Appends the locale decimal separator when {@code s} has none, as
		 * the '#' flag requires for {@code %g} results without fractional
		 * digits.
		 */
		private String forceDecimalSeparator(String s) {
			char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			return s.indexOf(decimalSeparator) < 0 ? s + decimalSeparator : s;
		}

		private String stripTrailingFractionZeros(String s) {
			char decimalSeparator = DecimalFormatSymbols.getInstance(locale).getDecimalSeparator();
			if (s.indexOf(decimalSeparator) < 0) {
				return s;
			}
			int end = s.length();
			while (end > 0 && s.charAt(end - 1) == '0') {
				end--;
			}
			if (end > 0 && s.charAt(end - 1) == decimalSeparator) {
				end--;
			}
			return s.substring(0, end);
		}

		private void appendPadded(String body, boolean leftJustify, boolean zeroPad, int width) {
			// The field width counts characters (code points), so that a
			// supplementary character fills one column, not two.
			int bodyLength = body.codePointCount(0, body.length());
			if (width <= bodyLength) {
				out.append(body);
				return;
			}
			int padLength = width - bodyLength;
			if (leftJustify) {
				out.append(body);
				appendSpaces(padLength);
			} else if (zeroPad) {
				out.append(zeros(padLength)).append(body);
			} else {
				appendSpaces(padLength);
				out.append(body);
			}
		}

		private void appendSpaces(int count) {
			for (int i = 0; i < count; i++) {
				out.append(' ');
			}
		}
	}

	/** Renders an exponent value with at least two digits, like C. */
	private static String exponentDigits(int exponent) {
		String digits = Integer.toString(exponent);
		return digits.length() < 2 ? "0" + digits : digits;
	}

	private static boolean isUpperCaseConversion(char conversion) {
		return conversion == 'X' || conversion == 'E' || conversion == 'F' || conversion == 'G' || conversion == 'A';
	}

	private static boolean isZeroMagnitude(String magnitude) {
		for (int i = 0; i < magnitude.length(); i++) {
			if (magnitude.charAt(i) != '0') {
				return false;
			}
		}
		return true;
	}

	private static boolean isAsciiDigit(char c) {
		return c >= '0' && c <= '9';
	}

	private static int parseInt(String s, int from, int to) {
		long value = 0;
		for (int i = from; i < to; i++) {
			value = value * 10 + s.charAt(i) - '0';
			if (value > Integer.MAX_VALUE) {
				return Integer.MAX_VALUE;
			}
		}
		return (int) value;
	}

	private static String zeros(int count) {
		StringBuilder sb = new StringBuilder(Math.max(count, 0));
		for (int i = 0; i < count; i++) {
			sb.append('0');
		}
		return sb.toString();
	}
}
