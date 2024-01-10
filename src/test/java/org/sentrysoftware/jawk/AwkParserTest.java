package org.sentrysoftware.jawk;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
import org.sentrysoftware.jawk.frontend.AwkParser;

public class AwkParserTest {

	@Test
	public void testStringParsing() throws Exception {
		assertEquals("'\\\\' must become \\", "\\", AwkTestHelper.runAwk("BEGIN { printf \"\\\\\" }", null));
		assertEquals("'\\a' must become BEL", "\u0007", AwkTestHelper.runAwk("BEGIN { printf \"\\a\" }", null));
		assertEquals("'\\b' must become BS", "\u0008", AwkTestHelper.runAwk("BEGIN { printf \"\\b\" }", null));
		assertEquals("'\\f' must become FF", "\014", AwkTestHelper.runAwk("BEGIN { printf \"\\f\" }", null));
		assertEquals("'\\n' must become LF", "\n", AwkTestHelper.runAwk("BEGIN { printf \"\\n\" }", null));
		assertEquals("'\\r' must become CR", "\r", AwkTestHelper.runAwk("BEGIN { printf \"\\r\" }", null));
		assertEquals("'\\t' must become TAB", "\t", AwkTestHelper.runAwk("BEGIN { printf \"\\t\" }", null));
		assertEquals("'\\v' must become VT", "\u000B", AwkTestHelper.runAwk("BEGIN { printf \"\\v\" }", null));
		assertEquals("'\\33' must become ESC", "\u001B", AwkTestHelper.runAwk("BEGIN { printf \"\\33\" }", null));
		assertEquals("'\\1!' must become {0x01, 0x21}", "\u0001!", AwkTestHelper.runAwk("BEGIN { printf \"\\1!\" }", null));
		assertEquals("'\\19' must become {0x01, 0x39}", "\u00019", AwkTestHelper.runAwk("BEGIN { printf \"\\19\" }", null));
		assertEquals("'\\38' must become {0x03, 0x38}", "\u00038", AwkTestHelper.runAwk("BEGIN { printf \"\\38\" }", null));
		assertEquals("'\\132' must become Z", "Z", AwkTestHelper.runAwk("BEGIN { printf \"\\132\" }", null));
		assertEquals("'\\1320' must become Z0", "Z0", AwkTestHelper.runAwk("BEGIN { printf \"\\1320\" }", null));
		assertEquals("'\\\"' must become \"", "\"", AwkTestHelper.runAwk("BEGIN { printf \"\\\"\" }", null));
		assertEquals("'\\x1B' must become ESC", "\u001B", AwkTestHelper.runAwk("BEGIN { printf \"\\x1B\" }", null));
		assertEquals("'\\x1b' must become ESC", "\u001B", AwkTestHelper.runAwk("BEGIN { printf \"\\x1b\" }", null));
		assertEquals("'\\x1!' must become {0x01, 0x21}", "\u0001!", AwkTestHelper.runAwk("BEGIN { printf \"\\x1!\" }", null));
		assertEquals("'\\x1G' must become {0x01, 0x47}", "\u0001G", AwkTestHelper.runAwk("BEGIN { printf \"\\x1G\" }", null));
		assertEquals("'\\x21A' must become !A", "!A", AwkTestHelper.runAwk("BEGIN { printf \"\\x21A\" }", null));
		assertEquals("'\\x!' must become x!", "x!", AwkTestHelper.runAwk("BEGIN { printf \"\\x!\" }", null));	
		assertThrows("Unfinished string by EOF must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished", null));
		assertThrows("Unfinished string by EOL must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished\n\"}", null));
		assertThrows("Interrupted octal number in string by EOF must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished\\0", null));
		assertThrows("Interrupted octal number in string by EOL must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished\\0\n\"}", null));
		assertThrows("Interrupted hex number in string by EOF must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished\\xF", null));
		assertThrows("Interrupted hex number in string by EOL must throw", AwkParser.LexerException.class, () -> AwkTestHelper.runAwk("BEGIN { printf \"unfinished\\xf\n\"}", null));
	}
	
	@Test
	public void testMultiLineStatement() throws Exception {
		assertEquals("|| must allow eol", "success", AwkTestHelper.runAwk("BEGIN { if (0 || \n    1) { printf \"success\" } }", null));
		assertEquals("&& must allow eol", "success", AwkTestHelper.runAwk("BEGIN { if (1 && \n    1) { printf \"success\" } }", null));
		assertEquals("? must allow eol", "success", AwkTestHelper.runAwk("BEGIN { printf 1 ?\n\"success\" : \"failed\" }", null));
		assertEquals(": must allow eol", "success", AwkTestHelper.runAwk("BEGIN { printf 1 ? \"success\" :\n\"failed\" }", null));
		assertEquals(", must allow eol", "success", AwkTestHelper.runAwk("BEGIN { printf(\"%s\", \n\"success\") }", null));
		assertEquals("do must allow eol", "success", AwkTestHelper.runAwk("BEGIN { do\n printf \"success\"; while (0) }", null));
		assertEquals("else must allow eol", "success", AwkTestHelper.runAwk("BEGIN { if (0) { printf \"failure\" } else \n printf \"success\" }", null));
	}

	@Test
	public void testUnaryPlus() throws Exception {
		assertEquals("+a must convert a to number", "0", AwkTestHelper.runAwk("BEGIN { printf +a }", null));
	}
	
	@Test
	public void testTernaryExpression() throws Exception {
		assertEquals("Ternary expression must allow string concatenations", "success", AwkTestHelper.runAwk("BEGIN { printf( a \"1\" b ? \"suc\" \"cess\" : \"failure\" ) }", null));
	}
	
	@Test
	public void testParseGron() throws Exception {
		String gron = AwkTestHelper.readResource("/xonixx/gron.awk");
		assertEquals("gron.awk must not trigger any parser exception", "json=[]\n", AwkTestHelper.runAwk(gron, "[]"));
	}
}
