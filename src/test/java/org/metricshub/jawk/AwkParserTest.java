package org.metricshub.jawk;

import static org.junit.Assert.*;
import static org.metricshub.jawk.AwkTestHelper.evalAwk;
import static org.metricshub.jawk.AwkTestHelper.runAwk;

import org.junit.Test;
import org.metricshub.jawk.frontend.AwkParser;

public class AwkParserTest {

	@Test
	public void testStringParsing() throws Exception {
		assertEquals("'\\\\' must become \\", "\\", evalAwk("\"\\\\\" "));
		assertEquals("'\\a' must become BEL", "\u0007", evalAwk("\"\\a\" "));
		assertEquals("'\\b' must become BS", "\u0008", evalAwk("\"\\b\" "));
		assertEquals("'\\f' must become FF", "\014", evalAwk("\"\\f\" "));
		assertEquals("'\\n' must become LF", "\n", evalAwk("\"\\n\" "));
		assertEquals("'\\r' must become CR", "\r", evalAwk("\"\\r\" "));
		assertEquals("'\\t' must become TAB", "\t", evalAwk("\"\\t\" "));
		assertEquals("'\\v' must become VT", "\u000B", evalAwk("\"\\v\" "));
		assertEquals("'\\33' must become ESC", "\u001B", evalAwk("\"\\33\" "));
		assertEquals("'\\1!' must become {0x01, 0x21}", "\u0001!", evalAwk("\"\\1!\" "));
		assertEquals("'\\19' must become {0x01, 0x39}", "\u00019", evalAwk("\"\\19\" "));
		assertEquals("'\\38' must become {0x03, 0x38}", "\u00038", evalAwk("\"\\38\" "));
		assertEquals("'\\132' must become Z", "Z", evalAwk("\"\\132\" "));
		assertEquals("'\\1320' must become Z0", "Z0", evalAwk("\"\\1320\" "));
		assertEquals("'\\\"' must become \"", "\"", evalAwk("\"\\\"\" "));
		assertEquals("'\\x1B' must become ESC", "\u001B", evalAwk("\"\\x1B\" "));
		assertEquals("'\\x1b' must become ESC", "\u001B", evalAwk("\"\\x1b\" "));
		assertEquals("'\\x1!' must become {0x01, 0x21}", "\u0001!", evalAwk("\"\\x1!\" "));
		assertEquals("'\\x1G' must become {0x01, 0x47}", "\u0001G", evalAwk("\"\\x1G\" "));
		assertEquals("'\\x21A' must become !A", "!A", evalAwk("\"\\x21A\" "));
		assertEquals("'\\x!' must become x!", "x!", evalAwk("\"\\x!\" "));
		assertThrows("Unfinished string by EOF must throw", AwkParser.LexerException.class, () -> runAwk("BEGIN { printf \"unfinished", null));
		assertThrows("Unfinished string by EOL must throw", AwkParser.LexerException.class, () -> evalAwk("\"unfinished\n\""));
		assertThrows("Interrupted octal number in string by EOF must throw", AwkParser.LexerException.class, () -> runAwk("BEGIN { printf \"unfinished\\0", null));
		assertThrows("Interrupted octal number in string by EOL must throw", AwkParser.LexerException.class, () -> evalAwk("\"unfinished\\0\n\""));
		assertThrows("Interrupted hex number in string by EOF must throw", AwkParser.LexerException.class, () -> runAwk("BEGIN { printf \"unfinished\\xF", null));
		assertThrows("Interrupted hex number in string by EOL must throw", AwkParser.LexerException.class, () -> evalAwk("\"unfinished\\xf\n\""));
}

	@Test
	public void testMultiLineStatement() throws Exception {
		assertEquals("|| must allow eol", "success", runAwk("BEGIN { if (0 || \n    1) { printf \"success\" } }", null));
		assertEquals("&& must allow eol", "success", runAwk("BEGIN { if (1 && \n    1) { printf \"success\" } }", null));
		assertEquals("? must allow eol", "success", evalAwk("1 ?\n\"success\" : \"failed\" "));
		assertEquals(": must allow eol", "success", evalAwk("1 ? \"success\" :\n\"failed\" "));
		assertEquals(", must allow eol", "success", runAwk("BEGIN { printf(\"%s\", \n\"success\") }", null));
		assertEquals("do must allow eol", "success", runAwk("BEGIN { do\n printf \"success\"; while (0) }", null));
		assertEquals("else must allow eol", "success", runAwk("BEGIN { if (0) { printf \"failure\" } else \n printf \"success\" }", null));
	}

	@Test
	public void testUnaryPlus() throws Exception {
		assertEquals("+a must convert a to number", "0", evalAwk("+a "));
	}

	@Test
	public void testTernaryExpression() throws Exception {
		assertEquals("Ternary expression must allow string concatenations", "success", runAwk("BEGIN { printf( a \"1\" b ? \"suc\" \"cess\" : \"failure\" ) }", null));
	}

	@Test
	public void testGron() throws Exception {
		String gron = AwkTestHelper.readResource("/xonixx/gron.awk");
		assertEquals("gron.awk must not trigger any parser exception", "json=[]\n", runAwk(gron, "[]"));
		assertEquals("gron.awk must work", "json=[]\njson[0]={}\njson[0].a=1\njson[1]={}\njson[1].b=\"2\"\n", runAwk(gron, "[{\"a\": 1},\n{\"b\": \"2\"}]"));
	}

	@Test
	public void testPow() throws Exception {
		assertEquals("^ (pow) operator must be supported", "256", evalAwk("2^8 "));
		assertEquals("** (pow) operator must be supported", "256", evalAwk("2**8 "));
	}
	
	@Test
	public void testOperatorPrecedence() throws Exception {
		assertEquals("$a precedes a++", "1122", runAwk("{ a = 1; printf $a++ ; printf a ; printf $(a++) ; printf a }", "1 2 3"));
		assertEquals("$a precedes ++a", "2233", runAwk("{ a = 1; printf $++a ; printf a ; printf $(++a) ; printf a }", "1 2 3"));
		assertEquals("$a precedes a--", "3322", runAwk("{ a = 3; printf $a-- ; printf a ; printf $(a--) ; printf a }", "1 2 3"));
		assertEquals("$a precedes --a", "2211", runAwk("{ a = 3; printf $--a ; printf a ; printf $(--a) ; printf a }", "1 2 3"));
		assertEquals("++ precedes ^", "22", runAwk("BEGIN { a = 1; printf(2^a++); printf a }", null));
		assertEquals("^ precedes unary -", "-1", evalAwk("-1^2"));
		assertEquals("^ precedes unary !", "1", evalAwk("!0^2"));
		assertEquals("Unary - precedes *", "-2", evalAwk("0 + -1 * 2"));
		assertEquals("* precedes +", "5", evalAwk("1 + 2 * 2"));
		assertEquals("+ precedes string concat", "33", evalAwk("1 + 2 3"));
	}
	
	@Test
	public void testRegExpConstant() throws Exception {
		assertEquals("/\\\\/ must be supported", "success", runAwk("/\\\\/ { printf \"success\" }", "a\\b"));
		assertEquals("/\\// must be supported", "success", runAwk("/\\// { printf \"success\" }", "a/b"));
		assertEquals("/=1/ must be supported", "success", runAwk("/=1/ { printf \"success\" }", "a=1\n1\n="));
		assertEquals("/\\057/ must be supported", "success", runAwk("/\\057/ { printf \"success\" }", "a/b"));
		assertThrows("Unfinished regexp by EOF must throw", AwkParser.LexerException.class, () -> runAwk("/unfinished { print $0 }", null));
		assertThrows("Unfinished regexp by EOL must throw", AwkParser.LexerException.class, () -> evalAwk("/unfinished\n/"));
	}
}
