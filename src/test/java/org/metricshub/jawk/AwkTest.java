package org.metricshub.jawk;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.metricshub.jawk.AwkTestHelper.evalAwk;
import static org.metricshub.jawk.AwkTestHelper.runAwk;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.metricshub.jawk.frontend.AwkParser;

public class AwkTest {

	private static final boolean IS_WINDOWS = (System.getProperty("os.name").contains("Windows"));

	private static final String EOL = System.getProperty("line.separator");

	@SafeVarargs
	static <T> T[] array(T ... vals) {
		return vals;
	}

	static String[] monotoneArray(final String val, final int num) {
		return Collections.nCopies(num, val).toArray(new String[num]);
	}

	static void awk(String ... args) throws ClassNotFoundException, IOException {
		Main.main(args);
	}

	static File classpathFile(final Class<?> c, String path) {
		final URL resource = c.getResource(path);
		try {
			final File relative = resource == null ?  new File(path) :
				Paths.get(resource.toURI()).toFile();
			return relative.getAbsoluteFile();
		} catch (final URISyntaxException e) {
			throw new IllegalStateException("Illegal URL " + resource, e);
		}
	}

	static String pathTo(String name) throws IOException {
		final File file = classpathFile(AwkTest.class, name);
		if (!file.exists()) throw new FileNotFoundException(file.toString());
		return file.getPath();
	}

	@Rule
	public final SystemOutRule systemOutRule = new SystemOutRule().enableLog().muteForSuccessfulTests();


	String[] linesOutput() {
		return systemOutRule.getLog().split(EOL);
	}

	/**
	 * Tests the program <pre>$ awk 1 /dev/null</pre>
	 * @see <a href="http://www.gnu.org/software/gawk/manual/gawk.html#Names>A Rose by Any Other Name</a>
	 */
	@Test
	public void test1() throws Exception {
		final String devnull = IS_WINDOWS ? pathTo("empty.txt") : "/dev/null";
		awk("1", devnull);
	}

	/**
	 * Tests the program <pre>$ awk 'BEGIN { print "Don\47t Panic!" }'</pre>
	 * @see <a href="http://www.gnu.org/software/gawk/manual/gawk.html#Read-Terminal">Running awk Without Input Files</a>
	 */
	@Test
	public void testDontPanic() throws Exception {
		awk("BEGIN { print \"Don\\47t Panic!\" }");
		assertArrayEquals(array("Don't Panic!"), linesOutput());
	}

	/**
	 * Tests the program <pre>$ awk -f advice.awk</pre>
	 * It should output <pre>Don't Panic!</pre>
	 * @see <a href="http://www.gnu.org/software/gawk/manual/gawk.html#Read-Terminal>Running awk Without Input Files</a>
	 */
	@Test
	public void testDontPanicAdvice() throws Exception {
		awk("-f", pathTo("advice.awk"));
		assertArrayEquals(array("Don't Panic!"), linesOutput());
	}

	/**
	 * Tests the program <pre>awk '/li/ { print $0 }' mail-list</pre>
	 * It should output 4 records containing the string "li".
	 * @see <a href="http://www.gnu.org/software/gawk/manual/gawk.html#Very-Simple>Some Simple Examples</a>
	 */
	@Test
	public void testMailListLiList() throws Exception {
		awk("/li/ {print $0}", pathTo("mail-list"));
		assertArrayEquals(array(
				"Amelia       555-5553     amelia.zodiacusque@gmail.com    F",
				"Broderick    555-0542     broderick.aliquotiens@yahoo.com R",
				"Julie        555-6699     julie.perscrutabor@skeeve.com   F",
				"Samuel       555-3430     samuel.lanceolis@shu.edu        A"),
			linesOutput());
	}

	/**
	 * @see <a hef="http://www.gnu.org/software/gawk/manual/gawk.html#Two-Rules">Two Rules</a>
	 */
	@Test
	public void testTwoRules() throws Exception {
		awk("/12/ {print $0} /21/ {print $0}", pathTo("mail-list"), pathTo("inventory-shipped"));
		assertArrayEquals(array(
				"Anthony      555-3412     anthony.asserturo@hotmail.com   A",
				"Camilla      555-2912     camilla.infusarum@skynet.be     R",
				"Fabius       555-1234     fabius.undevicesimus@ucb.edu    F",
				"Jean-Paul    555-2127     jeanpaul.campanorum@nyu.edu     R",
				"Jean-Paul    555-2127     jeanpaul.campanorum@nyu.edu     R",
				"Jan  21  36  64 620",
				"Apr  21  70  74 514"),
			linesOutput());
	}

	@Test
	public void testEmptyPattern() throws Exception {
		awk("//", pathTo("inventory-shipped"));
		assertArrayEquals(
				array(
					"Jan  13  25  15 115",
					"Feb  15  32  24 226",
					"Mar  15  24  34 228",
					"Apr  31  52  63 420",
					"May  16  34  29 208",
					"Jun  31  42  75 492",
					"Jul  24  34  67 436",
					"Aug  15  34  47 316",
					"Sep  13  55  37 277",
					"Oct  29  54  68 525",
					"Nov  20  87  82 577",
					"Dec  17  35  61 401",
					"",
					"Jan  21  36  64 620",
					"Feb  26  58  80 652",
					"Mar  24  75  70 495",
					"Apr  21  70  74 514"),
				linesOutput());
	}

	@Test
	public void testUninitializedVarible() throws Exception {
		awk("//{ if (v == 0) {print \"uninitialize variable\"} else {print}}",
				pathTo("inventory-shipped"));
		assertArrayEquals(monotoneArray("uninitialize variable", 17),
				linesOutput());
	}

	@Test
	public void testUninitializedVarible2() throws Exception {
		awk("//{ v = 1; if (v == 0) {print \"uninitialize variable\"} else {print}}",
				pathTo("inventory-shipped"));
		assertArrayEquals(
				array(
					"Jan  13  25  15 115",
					"Feb  15  32  24 226",
					"Mar  15  24  34 228",
					"Apr  31  52  63 420",
					"May  16  34  29 208",
					"Jun  31  42  75 492",
					"Jul  24  34  67 436",
					"Aug  15  34  47 316",
					"Sep  13  55  37 277",
					"Oct  29  54  68 525",
					"Nov  20  87  82 577",
					"Dec  17  35  61 401",
					"",
					"Jan  21  36  64 620",
					"Feb  26  58  80 652",
					"Mar  24  75  70 495",
					"Apr  21  70  74 514"),
				linesOutput());
	}

	@Test
	public void testArrayStringKey() throws Exception {
		awk("//{i=1; j=\"1\"; v[i] = 100; print v[i] v[j];}",
				pathTo("inventory-shipped"));
		assertArrayEquals(monotoneArray("100100", 17), linesOutput());
	}

	@Test
	public void testArrayStringKey2() throws Exception {
		awk("//{i=1; j=\"1\"; v[j] = 100; print v[i] v[j];}",
				pathTo("inventory-shipped"));
		assertArrayEquals(monotoneArray("100100", 17), linesOutput());
	}
	
	@Test
	public void testNot() throws Exception {
		assertEquals("!0 must return 1", "1", evalAwk("!0"));
		assertEquals("!1 must return 0", "0", evalAwk("!1"));
		assertEquals("!0.0 must return 1", "1", evalAwk("!0.0"));
		assertEquals("!0.1 must return 0", "0", evalAwk("!0.1"));
		assertEquals("!2^31 must return 0", "0", evalAwk("!2^31"));
		assertEquals("!2^33 must return 0", "0", evalAwk("!2^33"));
		assertEquals("!\"\" must return 1", "1", evalAwk("!\"\""));
		assertEquals("!\"a\" must return 0", "0", evalAwk("!\"a\""));
		assertEquals("!uninitialized must return true", "1", evalAwk("!uninitialized"));
	}
	
	@Test
	public void testExit() throws Exception {
		assertThrows("exit NN must throw ExitException", ExitException.class, () -> runAwk("BEGIN { exit 17 }", null));
		assertEquals("exit in BEGIN prevents any rules execution", "", runAwk("BEGIN { exit 0 }\n{ print $0 }", "failure"));
		assertEquals("exit in BEGIN jumps to END", "success", runAwk("BEGIN { exit 0 ; printf \"failure\" }\nEND { printf \"success\" }", ""));
		assertEquals("exit in END stops immediately", "success", runAwk("END { printf \"success\" ; exit 0 ; printf \"failure\" }", ""));
		assertEquals("exit without code works", "", runAwk("BEGIN { exit }\n{ print $0 }", "failure"));
		int code = 0;
		try {
			runAwk("BEGIN { exit 2 }\nEND { exit }", "");
		} catch (ExitException e) {
			code = e.getCode();
		}
		assertEquals("exit without code must not alter previous exit with code", 2, code);
	}
	
	@Test
	public void testRanges() throws Exception {
		String input = "aa\nbb\ncc\ndd\nee\naa\nbb\ncc\ndd\nee";
		assertEquals("Range of regexp must work", "bb\ncc\ndd\nbb\ncc\ndd\n", runAwk("/b/, /d/", input));
		assertEquals("Range of conditions must work", "bb\ncc\ndd\n", runAwk("NR == 2, NR == 4", input));
		assertEquals("Non-matching start condition in range must return nothing", "", runAwk("/zz/, /cc/", input));
		assertEquals("Non-matching end condition in range must return all remaining", "cc\ndd\nee\naa\nbb\ncc\ndd\nee\n", runAwk("/cc/, /zz/", input));
		assertEquals("Range of mixed conditions must work", "bb\ncc\ndd\n", runAwk("NR == 2, /d/", input));
		assertThrows("Invalid range (3 args) must throw", AwkParser.ParserException.class, () -> runAwk("/a/, /b/, NR == 4", input));
		assertEquals("Entering and leaving the range matches 1 record", "bb\nbb\n", runAwk("/b/, /b/", input));
		assertEquals("Range comma is lowest precedence", "bb\ncc\nbb\ncc\n", runAwk("/b/, /d/ || /c/", input));
	}
	
	@Test
	public void testDavideBrini() throws Exception {
		assertEquals("Davide Brini's signature", "dave_br@gmx.com\n", runAwk(
				"BEGIN{O=\"~\"~\"~\";o=\"==\"==\"==\";o+=+o;x=O\"\"O;while(X++<=x+o+o){c=c\"%c\";}"
				+ "printf c,(x-O)*(x-O),x*(x-o)-o,x*(x-O)+x-O-o,+x*(x-O)-x+o,X*(o*o+O)+x-O,"
				+ "X*(X-x)-o*o,(x+X)*o*o+o,x*(X-x)-O-O,x-O+(O+o+X+x)*(o+O),X*X-X*(x-O)-x+O,"
				+ "O+X*(o*(o+O)+O),+x+O+X*o,x*(x-o),(o+X+x)*o*o-(x-O-O),O+(X-x)*(X+O),x-O}", null));
	}
	
	@Test
	public void testIncDec() throws Exception {
		assertEquals("2", runAwk("BEGIN { a = 1; printf ++a }", null));
		assertEquals("12", runAwk("BEGIN { a = 1; printf a++ ; printf a++; }", null));
		assertEquals("0", runAwk("BEGIN { a = 1; printf --a }", null));
		assertEquals("10", runAwk("BEGIN { a = 1; printf a-- ; printf a--; }", null));
		assertEquals("1", runAwk("BEGIN { printf ++a }", null));
		assertEquals("01", runAwk("BEGIN { printf a++ ; printf a++; }", null));
	}
	
	@Test
	public void testPrintfC() throws Exception {
		assertEquals("A", evalAwk("sprintf(\"%c\", 65)"));
	}
	
	@Test
	public void testConcatenationLeftAssociativity() throws Exception {
		assertEquals("Concatenated elements must be eval'ed from left to right", "0123", evalAwk("a++ a++ a++ a++"));
	}

	@Test
	public void testFunctionArgumentsLeftAssociativity() throws Exception {
		assertEquals("Function arguments must be eval'ed from left to right", "0 1 2 3\n",
				runAwk("BEGIN { print a++, a++, a++, a++ }", null));
	}

	@Test
	public void testAtan2ArgumentsLeftAssociativity() throws Exception {
		assertEquals("atan2 arguments must be eval'ed from left to right", "0", evalAwk("atan2(a++, a++)"));
	}

	@Test
	public void testComparisonArgumentsLeftAssociativity() throws Exception {
		assertEquals("Comparison arguments must be eval'ed from left to right", "1",
				runAwk("BEGIN { r = (a++ < a++); printf r }", null));
	}

	@Test
	public void testAssignmentRightToLeft() throws Exception {
		assertEquals("Assignment is eval'ed right first, and then left", "0",
				runAwk("BEGIN { arr[a++] = a++; printf arr[1] }", null));
	}

	@Test
	public void testBinaryExpressionLeftAssociativity() throws Exception {
		assertEquals("Binary expression is eval'ed from left to right", "0.5",
				runAwk("BEGIN { a = 1; printf a++ / a++ }", null));
	}

	@Test
	public void testChainedAdditionsAndSubtractionsLeftAssociativity() throws Exception {
		assertEquals("Chained additions and subtractions must be eval'ed from left to right", "6",
				evalAwk("10 - 3 - 2 + 1"));
	}

	@Test
	public void testChainedMultiplicationsAndDivisionsLeftAssociativity() throws Exception {
		assertEquals("Chained multiplies and divides must be eval'ed from left to right", "5",
				evalAwk("12 / 3 / 4 * 5"));
	}

	@Test
	public void testChainedExponentiationRightAssociativity() throws Exception {
		assertEquals("Chained powers must be eval'ed from right to left", "4", evalAwk("256 ^ 0.5 ^ 4 ^ 0.5"));
	}

	// Additional tests to further cover left associativity:

	@Test
	public void testChainedLogicalAndLeftAssociativity() throws Exception {
		assertEquals("Chained logical AND must be eval'ed from left to right", "0",
				runAwk("BEGIN { a = 0; r = (a++ && a++ && a++); printf r }", null));
	}

	@Test
	public void testChainedLogicalOrLeftAssociativity() throws Exception {
		assertEquals("Chained logical OR must be eval'ed from left to right", "1",
				runAwk("BEGIN { a = 1; r = (a++ || a++ || a++); printf r }", null));
	}

	@Test
	public void testChainedComparisonLeftAssociativity() throws Exception {
		assertEquals("Chained comparisons must be eval'ed from left to right", "0",
				runAwk("BEGIN { a = 1; r = (a++ < a++ < a++); printf r }", null));
	}

	@Test
	public void testChainedStringConcatenationLeftAssociativity() throws Exception {
		assertEquals("Chained string concatenation must be eval'ed from left to right", "abcde",
				evalAwk("\"a\" \"b\" \"c\" \"d\" \"e\""));
	}

	@Test
	public void testComplexExpressionLeftAssociativity() throws Exception {
		assertEquals("Complex expression with mixed operators must be eval'ed from left to right", "8",
				evalAwk("10 + 12 / 3 * 2 - 6 / 3 * 5"));
	}
	
	@Test
	public void testSubstr() throws Exception {
		assertEquals("234", evalAwk("substr(\"12345\", 2, 3)"));
		assertEquals("2345", evalAwk("substr(\"12345\", 2, 10)"));
		assertEquals("123", evalAwk("substr(\"12345\", 0, 3)"));
		assertEquals("123", evalAwk("substr(\"12345\", -1, 3)"));
		assertEquals("", evalAwk("substr(\"12345\", 2, 0)"));
		assertEquals("", evalAwk("substr(\"12345\", 2, -1)"));
		assertEquals("", evalAwk("substr(\"12345\", -1, -1)"));
		assertEquals("", evalAwk("substr(\"12345\", 10, 3)"));
		assertEquals("2345", evalAwk("substr(\"12345\", 2)"));
		assertEquals("12345", evalAwk("substr(\"12345\", 0)"));
		assertEquals("12345", evalAwk("substr(\"12345\", -1)"));
	}
	
	@Test
	public void testPrintComparison() throws Exception {
		assertEquals(
				"Comparison operators must be allowed in a print statement", 
				"1\n", 
				runAwk("BEGIN { print 1 < \"2\" }", null)
		);
		assertEquals(
				"Comparison operators must be allowed in a print statement", 
				"0\n", 
				runAwk("BEGIN { print 1 >= \"2\" }", null)
		);
		assertEquals(
				"> in a print statement must not output to stdout", 
				"", 
				runAwk("BEGIN { print 1 > TEMPDIR\"/printRedirect\" }", null, true)
		);
		assertTrue("> in a print statement must write to the specified file",
				Files.exists(Paths.get(AwkTestHelper.getTempDirectory(), "printRedirect"))
		);
		assertEquals(
				"> surrounded with parenthesis in a print statement doesn't redirect",
				"1\n",
				runAwk("BEGIN { print(1 > 0) }", null)
		);
		assertEquals(
				"> surrounded with parenthesis in a print statement doesn't redirect",
				"test1test\n",
				runAwk("BEGIN { print \"test\" (1 > 0) \"test\" }", null)
		);
	}
	
	@Test
	public void testSubArray() throws Exception {
		assertEquals(
				"sub on an array element must change the value of the element",
				"abc:d\n",
				runAwk("BEGIN { a[1] = \"ab:c:d\"; sub(/:/, \"\", a[1]); print a[1]; }", null)
		);

		assertEquals(
				"gsub on an array element must change the value of the element",
				"abcd\n",
				runAwk("BEGIN { a[1] = \"ab:c:d\"; gsub(/:/, \"\", a[1]); print a[1]; }", null)
		);
	}
	
	@Test
	public void testSubDollarReference() throws Exception {
		assertEquals(
				"sub on $4 must change the value of the 4th field",
				"aa bb cc Zd\n",
				runAwk("{ sub(/d/, \"Z\", $4); print $1, $2, $3, $4; }", "aa bb cc dd")
		);		

		assertEquals(
				"gsub on $4 must change the value of the 4th field",
				"aa bb cc ZZ\n",
				runAwk("{ gsub(/d/, \"Z\", $4); print $1, $2, $3, $4; }", "aa bb cc dd")
		);		
	}
	
	@Test
	public void testSubDollarZero() throws Exception {
		assertEquals(
				"sub on $0 must change the value of the entire line",
				"aa bb cc Zd\nZd\n",
				runAwk("{ sub(/d/, \"Z\"); print $0; print $4; }", "aa bb cc dd")
		);		

		assertEquals(
				"gsub on $0 must change the value of the entire line",
				"aa bb cc ZZ\nZZ\n",
				runAwk("{ gsub(/d/, \"Z\"); print $0; print $4; }", "aa bb cc dd")
		);		

		assertEquals(
				"sub on $0 must change the value of the entire line",
				"aa bb cc Zd\nZd\n",
				runAwk("{ sub(/d/, \"Z\", $0); print $0; print $4; }", "aa bb cc dd")
		);		

		assertEquals(
				"gsub on $0 must change the value of the entire line",
				"aa bb cc ZZ\nZZ\n",
				runAwk("{ gsub(/d/, \"Z\", $0); print $0; print $4; }", "aa bb cc dd")
		);		
	}
	
	@Test
	public void testSubVariable() throws Exception {
		assertEquals(
				"sub on variable must change the value of the variable",
				"aa bb cc Zd\n",
				runAwk("BEGIN { v = \"aa bb cc dd\"; sub(/d/, \"Z\", v); print v; }", null)
		);		
		assertEquals(
				"gsub on variable must change the value of the variable",
				"aa bb cc ZZ\n",
				runAwk("BEGIN { v = \"aa bb cc dd\"; gsub(/d/, \"Z\", v); print v; }", null)
		);		
	}
	
	@Test
	public void testSubReplaceWithMatchReference() throws Exception {
		assertEquals(
				"gsub must replace '&' with actual match",
				"a _b_ _c_ d e\n",
				runAwk("{ gsub(/[b-c]/, \"_&_\"); print $0; }", "a b c d e")
		);
		
		assertEquals(
				"gsub must replace '\\&' with '\\'",
				"a _&_ _&_ d e\n",
				runAwk("{ gsub(/[b-c]/, \"_\\\\&_\"); print $0; }", "a b c d e")
		);

		assertEquals(
				"gsub must replace '$0' with '$0'",
				"a _$0_ _$0_ d e\n",
				runAwk("{ gsub(/[b-c]/, \"_$0_\"); print $0; }", "a b c d e")
		);

	}
	
}
