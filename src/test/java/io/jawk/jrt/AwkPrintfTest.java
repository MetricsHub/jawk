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

import static org.junit.Assert.assertEquals;
import static io.jawk.AwkTestSupport.assertSprintf;
import static io.jawk.AwkTestSupport.assertSprintfThrows;
import static io.jawk.AwkTestSupport.assertToAwkString;

import java.util.Locale;
import org.junit.Test;

/**
 * Unit tests for {@link AwkPrintf}, written with the
 * {@code io.jawk.AwkTestSupport} formatter assertion helpers
 * ({@code assertSprintf}, {@code assertSprintfThrows},
 * {@code assertToAwkString}).
 * <p>
 * This suite incorporates the complete unit test suite of the former
 * <a href="https://github.com/metricshub/printf4j">Printf4J</a> project,
 * including the tests that were disabled or commented out there. Where
 * Printf4J (which emulated glibc) and AWK semantics differ, the expected
 * values below were verified against gawk 5 and are annotated accordingly.
 * </p>
 */
public class AwkPrintfTest {

	@Test
	public void testPlus() {
		assertSprintf("+42", "%+d", 42);
		assertSprintf("-42", "%+d", -42);
		assertSprintf("  +42", "%+5d", 42);
		assertSprintf("  -42", "%+5d", -42);
		assertSprintf("            +42", "%+15d", 42);
		assertSprintf("            -42", "%+15d", -42);
		assertSprintf("Hello testing", "%+s", "Hello testing");
		assertSprintf("+1024", "%+d", 1024);
		assertSprintf("-1024", "%+d", -1024);
		assertSprintf("+1024", "%+i", 1024);
		assertSprintf("-1024", "%+i", -1024);
		assertSprintf("1024", "%+u", 1024);
		assertSprintf("4294966272", "%+u", 4294966272L);
		assertSprintf("777", "%+o", 511);
		assertSprintf("37777777001", "%+o", 4294966785L);
		assertSprintf("1234abcd", "%+x", 305441741);
		assertSprintf("edcb5433", "%+x", 3989525555L);
		assertSprintf("1234ABCD", "%+X", 305441741);
		assertSprintf("EDCB5433", "%+X", 3989525555L);
		assertSprintf("x", "%+c", 'x');
		// Was commented out in Printf4J expecting "0": gawk prints nothing for
		// a zero value with an explicit zero precision, even with sign flags.
		assertSprintf("", "%+.0d", 0);
	}

	@Test
	public void testBlank() {
		assertSprintf(" 42", "% d", 42);
		assertSprintf("-42", "% d", -42);
		assertSprintf("   42", "% 5d", 42);
		assertSprintf("  -42", "% 5d", -42);
		assertSprintf("             42", "% 15d", 42);
		assertSprintf("            -42", "% 15d", -42);
		assertSprintf("            -42", "% 15d", -42);
		assertSprintf("        -42.987", "% 15.3f", -42.987);
		assertSprintf("         42.987", "% 15.3f", 42.987);
		assertSprintf("Hello testing", "% s", "Hello testing");
		assertSprintf(" 1024", "% d", 1024);
		assertSprintf("-1024", "% d", -1024);
		assertSprintf(" 1024", "% i", 1024);
		assertSprintf("-1024", "% i", -1024);
		assertSprintf("1024", "% u", 1024);
		assertSprintf("4294966272", "% u", 4294966272L);
		assertSprintf("777", "% o", 511);
		assertSprintf("37777777001", "% o", 4294966785L);
		assertSprintf("1234abcd", "% x", 305441741);
		assertSprintf("edcb5433", "% x", 3989525555L);
		assertSprintf("1234ABCD", "% X", 305441741);
		assertSprintf("EDCB5433", "% X", 3989525555L);
		assertSprintf("x", "% c", 'x');
	}

	@Test
	public void testZero() {
		assertSprintf("42", "%0d", 42);
		assertSprintf("42", "%0ld", 42L);
		assertSprintf("-42", "%0d", -42);
		assertSprintf("00042", "%05d", 42);
		assertSprintf("-0042", "%05d", -42);
		assertSprintf("000000000000042", "%015d", 42);
		assertSprintf("-00000000000042", "%015d", -42);
		assertSprintf("000000000042.12", "%015.2f", 42.1234);
		assertSprintf("00000000042.988", "%015.3f", 42.9876);
		assertSprintf("-00000042.98760", "%015.5f", -42.9876);
	}

	@Test
	public void testMinus() {
		assertSprintf("42", "%-d", 42);
		assertSprintf("-42", "%-d", -42);
		assertSprintf("42   ", "%-5d", 42);
		assertSprintf("-42  ", "%-5d", -42);
		assertSprintf("42             ", "%-15d", 42);
		assertSprintf("-42            ", "%-15d", -42);
		assertSprintf("42", "%-0d", 42);
		assertSprintf("-42", "%-0d", -42);
		assertSprintf("42   ", "%-05d", 42);
		assertSprintf("-42  ", "%-05d", -42);
		assertSprintf("42             ", "%-015d", 42);
		assertSprintf("-42            ", "%-015d", -42);
		assertSprintf("42", "%0-d", 42);
		assertSprintf("-42", "%0-d", -42);
		assertSprintf("42   ", "%0-5d", 42);
		assertSprintf("-42  ", "%0-5d", -42);
		assertSprintf("42             ", "%0-15d", 42);
		assertSprintf("-42            ", "%0-15d", -42);
		assertSprintf("-4.200e+01     ", "%0-15.3e", -42.);
		// Printf4J expected "-42.0 ": AWK's %g removes trailing
		// zeros, so gawk prints "-42 ".
		assertSprintf("-42            ", "%0-15.3g", -42.);
	}

	@Test
	public void testHash() {
		// Printf4J expected "" here, but gawk prints "0" for a zero value
		// with '#' and a zero precision on %x.
		assertSprintf("0", "%#.0x", 0);
		// Printf4J had this assertion commented out as "the real expected
		// behavior, which is wrong IMO" (it returned "0x0" instead): C and
		// gawk agree on "0", which is what AwkPrintf now produces.
		assertSprintf("0", "%#.1x", 0);
		// "%#.0llx" is invalid in gawk: doubled length modifiers make the
		// whole specifier print verbatim, without consuming an argument.
		assertSprintf("%#.0llx", "%#.0llx", 0);
		assertSprintf("0x0000614e", "%#.8x", 0x614e);
		// Was commented out in Printf4J ("binary is not supported for now"):
		// %b is not an AWK conversion, so gawk prints the specifier verbatim.
		assertSprintf("%#b", "%#b", 6);
		// gawk-verified: the '#' prefix depends on the original value, so a
		// nonzero fraction that truncates to zero keeps the prefix.
		assertSprintf("0x0", "%#.0x", 0.1);
		assertSprintf("0x0", "%#x", 0.5);
		// gawk-verified: '#' with %o always adds its leading zero on nonzero
		// values, in addition to any precision padding.
		assertSprintf("00", "%#o", 0.5);
		assertSprintf("00", "%#.0o", 0.2);
		assertSprintf("000001", "%#.5o", 1);
		assertSprintf("0010", "%#.3o", 8);
		assertSprintf("010", "%#o", 8);
	}

	@Test
	public void testSpecifier() {
		assertSprintf("Hello testing", "Hello testing");
		assertSprintf("Hello testing", "%s", "Hello testing");
		assertSprintf("1024", "%d", 1024);
		assertSprintf("-1024", "%d", -1024);
		assertSprintf("1024", "%i", 1024);
		assertSprintf("-1024", "%i", -1024);
		assertSprintf("1024", "%u", 1024);
		assertSprintf("4294966272", "%u", 4294966272L);
		assertSprintf("777", "%o", 511);
		assertSprintf("37777777001", "%o", 4294966785L);
		assertSprintf("1234abcd", "%x", 305441741);
		assertSprintf("edcb5433", "%x", 3989525555L);
		assertSprintf("1234ABCD", "%X", 305441741);
		assertSprintf("EDCB5433", "%X", 3989525555L);
		assertSprintf("%", "%%");
	}

	@Test
	public void testWidth() {
		assertSprintf("Hello testing", "%1s", "Hello testing");
		assertSprintf("1024", "%1d", 1024);
		assertSprintf("-1024", "%1d", -1024);
		assertSprintf("1024", "%1i", 1024);
		assertSprintf("-1024", "%1i", -1024);
		assertSprintf("1024", "%1u", 1024);
		assertSprintf("4294966272", "%1u", 4294966272L);
		assertSprintf("777", "%1o", 511);
		assertSprintf("37777777001", "%1o", 4294966785L);
		assertSprintf("1234abcd", "%1x", 305441741);
		assertSprintf("edcb5433", "%1x", 3989525555L);
		assertSprintf("1234ABCD", "%1X", 305441741);
		assertSprintf("EDCB5433", "%1X", 3989525555L);
		assertSprintf("x", "%1c", 'x');
	}

	@Test
	public void testWidth20() {
		assertSprintf("               Hello", "%20s", "Hello");
		assertSprintf("                1024", "%20d", 1024);
		assertSprintf("               -1024", "%20d", -1024);
		assertSprintf("                1024", "%20i", 1024);
		assertSprintf("               -1024", "%20i", -1024);
		assertSprintf("                1024", "%20u", 1024);
		assertSprintf("          4294966272", "%20u", 4294966272L);
		assertSprintf("                 777", "%20o", 511);
		assertSprintf("         37777777001", "%20o", 4294966785L);
		assertSprintf("            1234abcd", "%20x", 305441741);
		assertSprintf("            edcb5433", "%20x", 3989525555L);
		assertSprintf("            1234ABCD", "%20X", 305441741);
		assertSprintf("            EDCB5433", "%20X", 3989525555L);
		assertSprintf("                   x", "%20c", 'x');
	}

	@Test
	public void testWidthStar20() {
		assertSprintf("               Hello", "%*s", 20, "Hello");
		assertSprintf("                1024", "%*d", 20, 1024);
		assertSprintf("               -1024", "%*d", 20, -1024);
		assertSprintf("                1024", "%*i", 20, 1024);
		assertSprintf("               -1024", "%*i", 20, -1024);
		assertSprintf("                1024", "%*u", 20, 1024);
		assertSprintf("          4294966272", "%*u", 20, 4294966272L);
		assertSprintf("                 777", "%*o", 20, 511);
		assertSprintf("         37777777001", "%*o", 20, 4294966785L);
		assertSprintf("            1234abcd", "%*x", 20, 305441741);
		assertSprintf("            edcb5433", "%*x", 20, 3989525555L);
		assertSprintf("            1234ABCD", "%*X", 20, 305441741);
		assertSprintf("            EDCB5433", "%*X", 20, 3989525555L);
		assertSprintf("                   x", "%*c", 20, 'x');
	}

	@Test
	public void testMinus20() {
		assertSprintf("Hello               ", "%-20s", "Hello");
		assertSprintf("1024                ", "%-20d", 1024);
		assertSprintf("-1024               ", "%-20d", -1024);
		assertSprintf("1024                ", "%-20i", 1024);
		assertSprintf("-1024               ", "%-20i", -1024);
		assertSprintf("1024                ", "%-20u", 1024);
		assertSprintf("1024.1234           ", "%-20.4f", 1024.1234);
		assertSprintf("4294966272          ", "%-20u", 4294966272L);
		assertSprintf("777                 ", "%-20o", 511);
		assertSprintf("37777777001         ", "%-20o", 4294966785L);
		assertSprintf("1234abcd            ", "%-20x", 305441741);
		assertSprintf("edcb5433            ", "%-20x", 3989525555L);
		assertSprintf("1234ABCD            ", "%-20X", 305441741);
		assertSprintf("EDCB5433            ", "%-20X", 3989525555L);
		assertSprintf("x                   ", "%-20c", 'x');
		assertSprintf("|    9| |9 | |    9|", "|%5d| |%-2d| |%5d|", 9, 9, 9);
		assertSprintf("|   10| |10| |   10|", "|%5d| |%-2d| |%5d|", 10, 10, 10);
		assertSprintf("|    9| |9           | |    9|", "|%5d| |%-12d| |%5d|", 9, 9, 9);
		assertSprintf("|   10| |10          | |   10|", "|%5d| |%-12d| |%5d|", 10, 10, 10);
	}

	@Test
	public void testZeroMinus20() {
		assertSprintf("Hello               ", "%0-20s", "Hello");
		assertSprintf("1024                ", "%0-20d", 1024);
		assertSprintf("-1024               ", "%0-20d", -1024);
		assertSprintf("1024                ", "%0-20i", 1024);
		assertSprintf("-1024               ", "%0-20i", -1024);
		assertSprintf("1024                ", "%0-20u", 1024);
		assertSprintf("4294966272          ", "%0-20u", 4294966272L);
		assertSprintf("777                 ", "%0-20o", 511);
		assertSprintf("37777777001         ", "%0-20o", 4294966785L);
		assertSprintf("1234abcd            ", "%0-20x", 305441741);
		assertSprintf("edcb5433            ", "%0-20x", 3989525555L);
		assertSprintf("1234ABCD            ", "%0-20X", 305441741);
		assertSprintf("EDCB5433            ", "%0-20X", 3989525555L);
		assertSprintf("x                   ", "%0-20c", 'x');
	}

	@Test
	public void testPadding20() {
		assertSprintf("00000000000000001024", "%020d", 1024);
		assertSprintf("-0000000000000001024", "%020d", -1024);
		assertSprintf("00000000000000001024", "%020i", 1024);
		assertSprintf("-0000000000000001024", "%020i", -1024);
		assertSprintf("00000000000000001024", "%020u", 1024);
		assertSprintf("00000000004294966272", "%020u", 4294966272L);
		assertSprintf("00000000000000000777", "%020o", 511);
		assertSprintf("00000000037777777001", "%020o", 4294966785L);
		assertSprintf("0000000000001234abcd", "%020x", 305441741);
		assertSprintf("000000000000edcb5433", "%020x", 3989525555L);
		assertSprintf("0000000000001234ABCD", "%020X", 305441741);
		assertSprintf("000000000000EDCB5433", "%020X", 3989525555L);
	}

	@Test
	public void testPaddingPrecision20() {
		assertSprintf("00000000000000001024", "%.20d", 1024);
		assertSprintf("-00000000000000001024", "%.20d", -1024);
		assertSprintf("00000000000000001024", "%.20i", 1024);
		assertSprintf("-00000000000000001024", "%.20i", -1024);
		assertSprintf("00000000000000001024", "%.20u", 1024);
		assertSprintf("00000000004294966272", "%.20u", 4294966272L);
		assertSprintf("00000000000000000777", "%.20o", 511);
		assertSprintf("00000000037777777001", "%.20o", 4294966785L);
		assertSprintf("0000000000001234abcd", "%.20x", 305441741);
		assertSprintf("000000000000edcb5433", "%.20x", 3989525555L);
		assertSprintf("0000000000001234ABCD", "%.20X", 305441741);
		assertSprintf("000000000000EDCB5433", "%.20X", 3989525555L);
	}

	@Test
	public void testPaddingHashZero20() {
		assertSprintf("00000000000000001024", "%#020d", 1024);
		assertSprintf("-0000000000000001024", "%#020d", -1024);
		assertSprintf("00000000000000001024", "%#020i", 1024);
		assertSprintf("-0000000000000001024", "%#020i", -1024);
		assertSprintf("00000000000000001024", "%#020u", 1024);
		assertSprintf("00000000004294966272", "%#020u", 4294966272L);
		assertSprintf("00000000000000000777", "%#020o", 511);
		assertSprintf("00000000037777777001", "%#020o", 4294966785L);
		assertSprintf("0x00000000001234abcd", "%#020x", 305441741);
		assertSprintf("0x0000000000edcb5433", "%#020x", 3989525555L);
		assertSprintf("0X00000000001234ABCD", "%#020X", 305441741);
		assertSprintf("0X0000000000EDCB5433", "%#020X", 3989525555L);
	}

	@Test
	public void testPaddingHash20() {
		assertSprintf("                1024", "%#20d", 1024);
		assertSprintf("               -1024", "%#20d", -1024);
		assertSprintf("                1024", "%#20i", 1024);
		assertSprintf("               -1024", "%#20i", -1024);
		assertSprintf("                1024", "%#20u", 1024);
		assertSprintf("          4294966272", "%#20u", 4294966272L);
		// The following assertions were commented out in Printf4J; they match
		// C and gawk, and now pass.
		assertSprintf("                0777", "%#20o", 511);
		assertSprintf("        037777777001", "%#20o", 4294966785L);
		assertSprintf("          0x1234abcd", "%#20x", 305441741);
		assertSprintf("          0xedcb5433", "%#20x", 3989525555L);
		assertSprintf("          0X1234ABCD", "%#20X", 305441741);
		assertSprintf("          0XEDCB5433", "%#20X", 3989525555L);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testPadding20Dot5() {
		assertSprintf("               01024", "%20.5d", 1024);
		assertSprintf("              -01024", "%20.5d", -1024);
		assertSprintf("               01024", "%20.5i", 1024);
		assertSprintf("              -01024", "%20.5i", -1024);
		assertSprintf("               01024", "%20.5u", 1024);
		assertSprintf("          4294966272", "%20.5u", 4294966272L);
		assertSprintf("               00777", "%20.5o", 511);
		assertSprintf("         37777777001", "%20.5o", 4294966785L);
		assertSprintf("            1234abcd", "%20.5x", 305441741);
		assertSprintf("          00edcb5433", "%20.10x", 3989525555L);
		assertSprintf("            1234ABCD", "%20.5X", 305441741);
		assertSprintf("          00EDCB5433", "%20.10X", 3989525555L);
	}

	// Was @Disabled in Printf4J; matches C and gawk.
	@Test
	public void testPaddingNegativeNumbers() {
		// space padding
		assertSprintf("-5", "% 1d", -5);
		assertSprintf("-5", "% 2d", -5);
		assertSprintf(" -5", "% 3d", -5);
		assertSprintf("  -5", "% 4d", -5);
		// zero padding
		assertSprintf("-5", "%01d", -5);
		assertSprintf("-5", "%02d", -5);
		assertSprintf("-05", "%03d", -5);
		assertSprintf("-005", "%04d", -5);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testPaddingNegativeFloat() {
		// space padding
		assertSprintf("-5.0", "% 3.1f", -5.);
		assertSprintf("-5.0", "% 4.1f", -5.);
		assertSprintf(" -5.0", "% 5.1f", -5.);
		assertSprintf("    -5", "% 6.1g", -5.);
		assertSprintf("-5.0e+00", "% 6.1e", -5.);
		assertSprintf("  -5.0e+00", "% 10.1e", -5.);
		// zero padding
		assertSprintf("-5.0", "%03.1f", -5.);
		assertSprintf("-5.0", "%04.1f", -5.);
		assertSprintf("-05.0", "%05.1f", -5.);
		// zero padding no decimal point
		assertSprintf("-5", "%01.0f", -5.);
		assertSprintf("-5", "%02.0f", -5.);
		assertSprintf("-05", "%03.0f", -5.);
		assertSprintf("-005.0e+00", "%010.1e", -5.);
		assertSprintf("-05E+00", "%07.0E", -5.);
		assertSprintf("-05", "%03.0g", -5.);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testLength() {
		assertSprintf("", "%.0s", "Hello testing");
		assertSprintf("                    ", "%20.0s", "Hello testing");
		assertSprintf("", "%.s", "Hello testing");
		assertSprintf("                    ", "%20.s", "Hello testing");
		assertSprintf("                1024", "%20.0d", 1024);
		assertSprintf("               -1024", "%20.0d", -1024);
		assertSprintf("                    ", "%20.d", 0);
		assertSprintf("                1024", "%20.0i", 1024);
		assertSprintf("               -1024", "%20.i", -1024);
		assertSprintf("                    ", "%20.i", 0);
		assertSprintf("                1024", "%20.u", 1024);
		assertSprintf("          4294966272", "%20.0u", 4294966272L);
		assertSprintf("                    ", "%20.u", 0L);
		assertSprintf("                 777", "%20.o", 511);
		assertSprintf("         37777777001", "%20.0o", 4294966785L);
		assertSprintf("                    ", "%20.o", 0L);
		assertSprintf("            1234abcd", "%20.x", 305441741);
		assertSprintf("                                          1234abcd", "%50.x", 305441741);
		assertSprintf("                                          1234abcd     12345", "%50.x%10.u", 305441741, 12345);
		assertSprintf("            edcb5433", "%20.0x", 3989525555L);
		assertSprintf("                    ", "%20.x", 0L);
		assertSprintf("            1234ABCD", "%20.X", 305441741);
		assertSprintf("            EDCB5433", "%20.0X", 3989525555L);
		assertSprintf("                    ", "%20.X", 0L);
		assertSprintf("  ", "%02.0u", 0L);
		assertSprintf("  ", "%02.0d", 0);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testFloat() {
		// test special-case floats
		assertSprintf("     nan", "%8f", Float.NaN);
		assertSprintf("     inf", "%8f", Float.POSITIVE_INFINITY);
		assertSprintf("-inf    ", "%-8f", Float.NEGATIVE_INFINITY);
		assertSprintf("    +inf", "%+8e", Float.POSITIVE_INFINITY);
		assertSprintf("3.1415", "%.4f", 3.1415354);
		assertSprintf("30343.142", "%.3f", 30343.1415354);
		assertSprintf("34", "%.0f", 34.1415354);
		assertSprintf("1", "%.0f", 1.3);
		assertSprintf("2", "%.0f", 1.55);
		assertSprintf("1.6", "%.1f", 1.64);
		assertSprintf("42.90", "%.2f", 42.8952);
		assertSprintf("42.895200000", "%.9f", 42.8952);
		assertSprintf("42.8952230000", "%.10f", 42.895223);
		// Printf4J expected "42.895223123000" and "42.895223877000" here
		// because its reference implementation truncated to 9 significant
		// fraction digits; gawk prints the correctly rounded values.
		assertSprintf("42.895223123457", "%.12f", 42.89522312345678);
		assertSprintf("42.895223876543", "%.12f", 42.89522387654321);
		assertSprintf(" 42.90", "%6.2f", 42.8952);
		assertSprintf("+42.90", "%+6.2f", 42.8952);
		assertSprintf("+42.9", "%+5.1f", 42.9252);
		assertSprintf("42.500000", "%f", 42.5);
		assertSprintf("42.5", "%.1f", 42.5);
		assertSprintf("42167.000000", "%f", 42167.0);
		assertSprintf("-12345.987654321", "%.9f", -12345.987654321);
		assertSprintf("4.0", "%.1f", 3.999);
		assertSprintf("4", "%.0f", 3.5);
		assertSprintf("4", "%.0f", 4.5);
		assertSprintf("3", "%.0f", 3.49);
		assertSprintf("3.5", "%.1f", 3.49);
		assertSprintf("a0.5  ", "a%-5.1f", 0.5);
		assertSprintf("a0.5  end", "a%-5.1fend", 0.5);
		assertSprintf("12345.7", "%G", 12345.678);
		assertSprintf("12345.68", "%.7G", 12345.678);
		assertSprintf("1.2346E+08", "%.5G", 123456789.);
		// Printf4J expected "12345.0": AWK's %G removes trailing zeros.
		assertSprintf("12345", "%.6G", 12345.);
		assertSprintf("  +1.235e+08", "%+12.4g", 123456789.);
		assertSprintf("0.0012", "%.2G", 0.001234);
		assertSprintf(" +0.001234", "%+10.4G", 0.001234);
		assertSprintf("+001.234e-05", "%+012.4g", 0.00001234);
		assertSprintf("-1.23e-308", "%.3g", -1.2345e-308);
		assertSprintf("+1.230E+308", "%+.3E", 1.23e+308);
		// Printf4J expected "1.0e+20" (its reference implementation switched
		// to exponential notation out of range); gawk prints the full value.
		assertSprintf("100000000000000000000.0", "%.1f", 1E20);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5,
	// which only accepts a single 'h', 'l', or 'L' length modifier and
	// prints any other modifier combination verbatim.
	@Test
	public void testTypes() {
		assertSprintf("0", "%i", 0);
		assertSprintf("1234", "%i", 1234);
		assertSprintf("32767", "%i", 32767);
		assertSprintf("-32767", "%i", -32767);
		assertSprintf("30", "%li", 30L);
		assertSprintf("-2147483647", "%li", -2147483647L);
		assertSprintf("2147483647", "%li", 2147483647L);
		// Doubled modifiers ("ll", "hh") and the "q", "j", "z", and "t"
		// modifiers are not valid in gawk: the specifier prints verbatim and
		// consumes no argument.
		assertSprintf("%lli", "%lli", 30L);
		assertSprintf("%lli", "%lli", -9223372036854775807L);
		assertSprintf("%lli", "%lli", 9223372036854775807L);
		assertSprintf("100000", "%lu", 100000L);
		assertSprintf("4294967295", "%lu", 0xFFFFFFFFL);
		assertSprintf("%llu", "%llu", 281474976710656L);
		assertSprintf("%llu", "%llu", Long.parseUnsignedLong("18446744073709551615"));
		assertSprintf("%zu", "%zu", 2147483647L);
		assertSprintf("%zd", "%zd", 2147483647L);
		assertSprintf("%zi", "%zi", -2147483647L);
		// %b is not an AWK conversion: printed verbatim, like gawk.
		assertSprintf("%b", "%b", 60000);
		assertSprintf("%lb", "%lb", 12345678L);
		assertSprintf("165140", "%o", 60000);
		assertSprintf("57060516", "%lo", 12345678L);
		assertSprintf("12345678", "%lx", 0x12345678L);
		assertSprintf("%llx", "%llx", 0x1234567891234567L);
		assertSprintf("abcdefab", "%lx", 0xabcdefabL);
		assertSprintf("ABCDEFAB", "%lX", 0xabcdefabL);
		assertSprintf("v", "%c", 'v');
		assertSprintf("wv", "%cv", 'w');
		assertSprintf("A Test", "%s", "A Test");
		// gawk ignores the single 'h' modifier without truncating the value,
		// and prints the invalid "hh" specifiers verbatim.
		assertSprintf("%hhu", "%hhu", 0xFFFFL);
		assertSprintf("13398", "%hu", 13398);
		assertSprintf("1193046", "%hu", 0x123456L);
		assertSprintf("Test%hhi 10000", "%s%hhi %hu", "Test", 10000, 0xFFFFFFFFL);
	}

	// Was @Disabled in Printf4J, which expected "kmarco": gawk prints the
	// unknown "%k" specifier verbatim.
	@Test
	public void testUnknown() {
		assertSprintf("%kmarco", "%kmarco", 42, 37);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testStringLength() {
		assertSprintf("This", "%.4s", "This is a test");
		assertSprintf("test", "%.4s", "test");
		assertSprintf("123", "%.7s", "123");
		assertSprintf("", "%.7s", "");
		assertSprintf("1234ab", "%.4s%.2s", "123456", "abcdef");
		// Printf4J expected ".2s": gawk prints the whole invalid specifier
		// verbatim.
		assertSprintf("%.4.2s", "%.4.2s", "123456");
		assertSprintf("123", "%.*s", 3, "123456");
		// The precision counts characters, so it never splits a surrogate
		// pair, like gawk in a multibyte locale.
		assertSprintf("😀", "%.1s", "😀x");
		assertSprintf("😀x", "%.2s", "😀x");
		// The field width also counts characters: a supplementary character
		// fills one column (gawk pads %s the same way; its %c padding counts
		// bytes, a C-locale artifact that Jawk does not reproduce).
		assertSprintf("  😀", "%3s", "😀");
		assertSprintf("😀  ", "%-3s", "😀");
		assertSprintf("  😀", "%3c", 0x1F600);
	}

	// Was @Disabled in Printf4J; expected values verified against gawk 5.
	@Test
	public void testMisc() {
		assertSprintf("53000atest-20 bit", "%u%u%ctest%d %s", 5, 3000, 'a', -20, "bit");
		assertSprintf("0.33", "%.*f", 2, 0.33333333);
		assertSprintf("1", "%.*d", -1, 1);
		assertSprintf("foo", "%.3s", "foobar");
		// Printf4J expected " " (glibc behavior): gawk prints nothing at all
		// for a zero value with zero precision, even with the space flag.
		assertSprintf("", "% .0d", 0);
		assertSprintf("     00004", "%10.5d", 4);
		assertSprintf("hi x", "%*sx", -3, "hi");
		assertSprintf("0.33", "%.*g", 2, 0.33333333);
		assertSprintf("3.33e-01", "%.*e", 2, 0.33333333);
	}

	@Test
	public void testChar() {
		assertSprintf("A", "%c", 65);
		assertSprintf("A", "%c", 65L);
		assertSprintf("A", "%c", 65.0);
		assertSprintf("A", "%c", 65.1);
		assertSprintf("A", "%c", Integer.valueOf(65));
		assertSprintf("A", "%c", Long.valueOf(65));
		assertSprintf("A", "%c", Float.valueOf(65));
		assertSprintf("A", "%c", Double.valueOf(65));
		assertSprintf("6", "%c", "65");
		Object nothing = null;
		assertSprintf("\0", "%c", nothing);
	}

	// Ported from Printf4J's testToChar; AwkPrintf converts values for %c
	// internally, so the equivalent assertions go through sprintf().
	@Test
	public void testToChar() {
		assertSprintf("A", "%c", 65);
		assertSprintf("A", "%c", 65L);
		assertSprintf("A", "%c", 65.0);
		assertSprintf("A", "%c", 65.1);
		assertSprintf("A", "%c", 65.9);
		assertSprintf("A", "%c", Integer.valueOf(65));
		assertSprintf("A", "%c", Long.valueOf(65));
		assertSprintf("A", "%c", Float.valueOf(65));
		assertSprintf("A", "%c", Double.valueOf(65));
		assertSprintf("6", "%c", "65");
		assertSprintf("\0", "%c", "");
		Object nothing = null;
		assertSprintf("\0", "%c", nothing);
	}

	// Ported from Printf4J's testToLong: the same conversion now lives in
	// JRT.toLong (they shared the same original implementation).
	@Test
	public void testToLong() {
		assertEquals(65L, JRT.toLong('A'));
		assertEquals(65L, JRT.toLong(65));
		assertEquals(65L, JRT.toLong(65L));
		assertEquals(65L, JRT.toLong(65.0));
		assertEquals(65L, JRT.toLong(65.1));
		assertEquals(65L, JRT.toLong(65.9));
		assertEquals(65L, JRT.toLong(Integer.valueOf(65)));
		assertEquals(65L, JRT.toLong(Long.valueOf(65)));
		assertEquals(65L, JRT.toLong(Float.valueOf(65)));
		assertEquals(65L, JRT.toLong(Double.valueOf(65)));
		assertEquals(65L, JRT.toLong("65"));
		assertEquals(65L, JRT.toLong("65A"));
		assertEquals(65L, JRT.toLong("65A6666666666666666666666666600000000033333333333999999999999"));
		assertEquals(0L, JRT.toLong(""));
		Object nothing = null;
		assertEquals(0L, JRT.toLong(nothing));
	}

	// Ported from Printf4J's testToDouble: the same conversion now lives in
	// JRT.toDouble (they shared the same original implementation).
	@Test
	public void testToDouble() {
		assertEquals(65.0, JRT.toDouble('A'), 0.0);
		assertEquals(65.0, JRT.toDouble(65), 0.0);
		assertEquals(65.0, JRT.toDouble(65L), 0.0);
		assertEquals(65.0, JRT.toDouble(65.0), 0.0);
		assertEquals(65.1, JRT.toDouble(65.1), 0.0);
		assertEquals(65.9, JRT.toDouble(65.9), 0.0);
		assertEquals(65.0, JRT.toDouble(Integer.valueOf(65)), 0.0);
		assertEquals(65.0, JRT.toDouble(Long.valueOf(65)), 0.0);
		assertEquals(65.0, JRT.toDouble(Float.valueOf(65)), 0.0);
		assertEquals(65.0, JRT.toDouble(Double.valueOf(65)), 0.0);
		assertEquals(65.0, JRT.toDouble("65"), 0.0);
		assertEquals(65.0, JRT.toDouble("65A"), 0.0);
		assertEquals(65.0, JRT.toDouble("65A6666666666666666666666666600000000033333333333999999999999"), 0.0);
		assertEquals(65.0, JRT.toDouble("6.5E+1"), 0.0);
		assertEquals(0.0, JRT.toDouble(""), 0.0);
		Object nothing = null;
		assertEquals(0.0, JRT.toDouble(nothing), 0.0);
	}

	// ------------------------------------------------------------------
	// AWK-specific semantics beyond the original Printf4J suite.
	// ------------------------------------------------------------------

	@Test
	public void testStringConversionUsesAwkNumberToStringRules() {
		// The symptom from issue #528: an integral double prints without a
		// fractional part.
		assertSprintf("1", "%s", 1.0);
		assertSprintf("x[1]", "x[%s]", 1.0);
		// Non-integral values use CONVFMT.
		assertSprintf("3.14159", "%s", 3.14159265);
		assertSprintf("3.1", Locale.US, "%.2g", "%s", 3.14159265);
		// CONVFMT that is not a %g-style format is honored verbatim.
		assertSprintf("3.14", Locale.US, "%.2f", "%s", 3.14159265);
		// Integral values beyond the 64-bit range print in full.
		assertSprintf("100000000000000000000", "%s", 1e20);
		// Exact long values are preserved.
		assertSprintf("9223372036854775807", "%s", Long.MAX_VALUE);
	}

	@Test
	public void testCharConversion() {
		// A numeric value selects the corresponding code point.
		assertSprintf("é", "%c", 233);
		// A code point beyond the BMP produces the full character.
		assertSprintf(new String(Character.toChars(0x1F600)), "%c", 0x1F600);
		// A string value uses its first character.
		assertSprintf("X", "%c", "XYZ");
		// Width applies to %c like any other conversion.
		assertSprintf("    A", "%5c", 65);
		assertSprintf("A    ", "%-5c", 65);
	}

	@Test
	public void testDynamicWidthAndPrecision() {
		assertSprintf("    3.14", "%*.*f", 8, 2, 3.14159);
		assertSprintf("  3.14159", "%9s", 3.14159);
		// A negative dynamic width means left justification.
		assertSprintf("42    ", "%*d", -6, 42);
		// Width and precision arguments are converted like AWK numbers.
		assertSprintf("  3.14", "%*.*f", "6", "2", 3.14159);
	}

	@Test
	public void testPositionalSpecifiers() {
		assertSprintf("b a", "%2$s %1$s", "a", "b");
		assertSprintf("a b a", "%1$s %2$s %1$s", "a", "b");
		// Mixing positional and sequential specifiers is fatal, like gawk.
		assertSprintfThrows(AwkRuntimeException.class, "%2$s %s", "a", "b");
		// A zero positional index is fatal, like gawk.
		assertSprintfThrows(AwkRuntimeException.class, "%0$s", "a");
		// gawk-verified: an explicitly positioned star operand may accompany
		// sequential conversions.
		assertSprintf("    a|5", "%*2$s|%s", "a", 5);
		assertSprintf("a      5", "%1$s %2$*3$d", "a", 5, 6);
		// gawk-verified: a sequential star operand with a positional
		// conversion is a mixed-mode fatal error...
		assertSprintfThrows(AwkRuntimeException.class, "%2$*d", 5, 12);
		// ...and an explicitly positioned unknown specifier pins the format
		// to positional mode even though it prints verbatim.
		assertSprintfThrows(AwkRuntimeException.class, "%2$q|%d", 5, 12);
	}

	@Test
	public void testOutOfRangeFallbackKeepsSignFlagsAndPrecision() {
		// gawk-verified: the %g fallback for out-of-range %u/%o/%x/%X keeps
		// the sign, the precision, and the zero and '#' flags.
		assertSprintf("-1.26765e+30", "%u", -Math.pow(2, 100));
		assertSprintf("1.2676506e+30", "%.10x", Math.pow(2, 100));
		assertSprintf("1.27e+30", "%#.3x", Math.pow(2, 100));
		assertSprintf("0000000001.26765e+30", "%020u", Math.pow(2, 100));
	}

	@Test
	public void testAlternateFormKeepsDecimalPoint() {
		// gawk-verified: '#' forces a decimal point even when no fractional
		// digits remain.
		assertSprintf("1.", "%#.1g", 1);
		assertSprintf("1.e+04", "%#.1g", 12345);
		assertSprintf("1.2e+04", "%#.2g", 12345);
		assertSprintf("1.e+04", "%#.0e", 12345);
		assertSprintf("1.00000", "%#g", 1);
	}

	@Test
	public void testZeroPrecisionZeroValue() {
		// gawk-verified: unsigned conversions print "0" when a nonzero value
		// truncates to zero, or when the '#' flag is given; signed %d prints
		// nothing in both zero cases.
		assertSprintf("0", "%.0x", 0.1);
		assertSprintf("0", "%.0u", 0.1);
		assertSprintf("0", "%.0o", 0.1);
		assertSprintf("", "%.0d", 0.1);
		assertSprintf("0", "%#.0u", 0);
		assertSprintf("", "%.0u", 0);
		assertSprintf("", "%.0x", 0);
	}

	@Test
	public void testIntegerTruncationAndConversion() {
		// %d truncates toward zero.
		assertSprintf("42", "%d", 42.7);
		assertSprintf("-42", "%d", -42.7);
		// Strings convert with AWK's number rules (leading/trailing spaces,
		// exponent notation, numeric prefixes).
		assertSprintf("1000", "%d", "1e3");
		assertSprintf("42", "%d", " 42 ");
		assertSprintf("3", "%d", "+3.9");
		assertSprintf("0", "%d", "abc");
		assertSprintf("0", "%x", "abc");
	}

	@Test
	public void testOutOfRangeIntegerConversions() {
		// Negative values wrap to unsigned 64-bit for %u, %o, %x.
		assertSprintf("18446744073709551615", "%u", -1);
		assertSprintf("ffffffffffffffff", "%x", -1);
		assertSprintf("1777777777777777777777", "%o", -1);
		// 2^63 is out of the signed range but fits unsigned.
		assertSprintf("9223372036854775808", "%d", 9.223372036854775808e18);
		// %d beyond 64 bits prints the full decimal expansion, like gawk.
		assertSprintf("1267650600228229401496703205376", "%d", Math.pow(2, 100));
		assertSprintf("-1267650600228229401496703205376", "%d", -Math.pow(2, 100));
		// %u, %o, and %x beyond 64 bits fall back to %g notation, like gawk.
		assertSprintf("1.26765e+30", "%x", Math.pow(2, 100));
		assertSprintf("1.26765e+30", "%u", Math.pow(2, 100));
		assertSprintf("1.26765e+30", "%o", Math.pow(2, 100));
	}

	@Test
	public void testHexFloat() {
		// %a uses Java's hexadecimal float notation (gawk documents %a as
		// C-library dependent); the 0x prefix stays ahead of zero padding.
		assertSprintf("0x1.34ap10", "%a", 1234.5);
		assertSprintf("0x00000000001.34ap10", "%020a", 1234.5);
		assertSprintf("-0x1.34ap10", "%a", -1234.5);
	}

	@Test
	public void testNonFiniteValues() {
		assertSprintf("nan", "%d", Double.NaN);
		assertSprintf("inf", "%d", Double.POSITIVE_INFINITY);
		assertSprintf("-inf", "%f", Double.NEGATIVE_INFINITY);
		assertSprintf("INF", "%E", Double.POSITIVE_INFINITY);
		assertSprintf("NAN", "%G", Double.NaN);
		assertSprintf("nan", "%s", Double.NaN);
		assertSprintf("inf", "%s", Double.POSITIVE_INFINITY);
		assertSprintf("-inf", "%s", Double.NEGATIVE_INFINITY);
	}

	@Test
	public void testUnknownSpecifiersDoNotConsumeArguments() {
		// The unknown %q prints verbatim and its argument feeds %d instead.
		assertSprintf("%q1", "%q%d", 1, 2);
		// %n is not an AWK conversion (Printf4J used to print a newline).
		assertSprintf("a%nb", "a%nb");
		// A dangling % prints verbatim.
		assertSprintf("abc%", "abc%");
	}

	@Test
	public void testNotEnoughArgumentsIsFatal() {
		assertSprintfThrows(AwkRuntimeException.class, "%d %s", 1);
		assertSprintfThrows(AwkRuntimeException.class, "%5s");
		assertSprintfThrows(AwkRuntimeException.class, "%*d", 5);
	}

	@Test
	public void testExtraArgumentsAreIgnored() {
		assertSprintf("a b", "%s %s", "a", "b", "c");
	}

	@Test
	public void testGroupingFlag() {
		assertSprintf("1,234,567", "%'d", 1234567);
		assertSprintf("1,234,567.89", "%'.2f", 1234567.891);
		assertSprintf("1.234.567", Locale.GERMANY, AwkPrintf.DEFAULT_CONVFMT, "%'d", 1234567);
	}

	@Test
	public void testLocaleDecimalSeparator() {
		assertSprintf("3,14", Locale.FRANCE, AwkPrintf.DEFAULT_CONVFMT, "%.2f", 3.14159);
		assertSprintf("3,14159", Locale.FRANCE, AwkPrintf.DEFAULT_CONVFMT, "%g", 3.14159);
	}

	@Test
	public void testToAwkString() {
		assertToAwkString("", null);
		assertToAwkString("text", "text");
		assertToAwkString("1", 1.0);
		assertToAwkString("0.1", 0.1);
		assertToAwkString("3.14159", 3.14159265);
		assertToAwkString("100000000000000000000", 1e20);
		assertToAwkString("9223372036854775807", Long.MAX_VALUE);
		assertToAwkString("nan", Double.NaN);
		assertToAwkString("inf", Double.POSITIVE_INFINITY);
		assertToAwkString("-inf", Double.NEGATIVE_INFINITY);
	}
}
