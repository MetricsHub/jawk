package org.metricshub.jawk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.metricshub.jawk.intermediate.UninitializedObject;
import org.metricshub.jawk.jrt.JRT;

public class JRTTest {

	@Test
	public void testToDouble() {
		assertEquals(65.0, JRT.toDouble('A'), 0);
		assertEquals(65.0, JRT.toDouble(65), 0);
		assertEquals(65.0, JRT.toDouble(65L), 0);
		assertEquals(65.0, JRT.toDouble(65.0), 0);
		assertEquals(65.1, JRT.toDouble(65.1), 0);
		assertEquals(65.9, JRT.toDouble(65.9), 0);
		assertEquals(65.0, JRT.toDouble(Integer.valueOf(65)), 0);
		assertEquals(65.0, JRT.toDouble(Long.valueOf(65)), 0);
		assertEquals(65.0, JRT.toDouble(Float.valueOf(65)), 0);
		assertEquals(65.0, JRT.toDouble(Double.valueOf(65)), 0);
		assertEquals(65.0, JRT.toDouble("65"), 0);
		assertEquals(65.0, JRT.toDouble("65A"), 0);
		assertEquals(65.0, JRT.toDouble("65A6666666666666666666666666600000000033333333333999999999999"), 0);
		assertEquals(65.0, JRT.toDouble("6.5E+1"), 0);
		assertEquals(0.0, JRT.toDouble(""), 0);
		Object nothing = null;
		assertEquals(0.0, JRT.toDouble(nothing), 0);
	}

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
	
	@Test
	public void testCompare2Uninitialized() {
		
		// Uninitialized ==
		assertTrue(JRT.compare2(new UninitializedObject(), new UninitializedObject(), 0));
		assertTrue(JRT.compare2(new UninitializedObject(), "0", 0));
		assertTrue(JRT.compare2(new UninitializedObject(), 0, 0));
		assertTrue(JRT.compare2("0", new UninitializedObject(), 0));
		assertTrue(JRT.compare2(0, new UninitializedObject(), 0));
		assertFalse(JRT.compare2(new UninitializedObject(), "1", 0));
		assertFalse(JRT.compare2(new UninitializedObject(), 1, 0));
		assertFalse(JRT.compare2("1", new UninitializedObject(), 0));
		assertFalse(JRT.compare2(1, new UninitializedObject(), 0));
		
		// Uninitialized <
		assertFalse(JRT.compare2(new UninitializedObject(), new UninitializedObject(), -1));
		assertFalse(JRT.compare2(new UninitializedObject(), "0", -1));
		assertFalse(JRT.compare2(new UninitializedObject(), 0, -1));
		assertFalse(JRT.compare2("0", new UninitializedObject(), -1));
		assertFalse(JRT.compare2(0, new UninitializedObject(), -1));
		assertTrue(JRT.compare2(new UninitializedObject(), "1", -1));
		assertTrue(JRT.compare2(new UninitializedObject(), 1, -1));
		assertFalse(JRT.compare2("1", new UninitializedObject(), -1));
		assertFalse(JRT.compare2(1, new UninitializedObject(), -1));
		
		// Uninitialized >
		assertFalse(JRT.compare2(new UninitializedObject(), new UninitializedObject(), 1));
		assertFalse(JRT.compare2(new UninitializedObject(), "0", 1));
		assertFalse(JRT.compare2(new UninitializedObject(), 0, 1));
		assertFalse(JRT.compare2("0", new UninitializedObject(), 1));
		assertFalse(JRT.compare2(0, new UninitializedObject(), 1));
		assertFalse(JRT.compare2(new UninitializedObject(), "1", 1));
		assertFalse(JRT.compare2(new UninitializedObject(), 1, 1));
		assertTrue(JRT.compare2("1", new UninitializedObject(), 1));
		assertTrue(JRT.compare2(1, new UninitializedObject(), 1));
	}
	
	@Test
	public void testPrepareReplacement() throws Exception {
		assertEquals("don't change", JRT.prepareReplacement("don't change"));
		assertEquals("a$0a", JRT.prepareReplacement("a&a"));
		assertEquals("1$01", JRT.prepareReplacement("1&1"));
		assertEquals("a$0b$0c", JRT.prepareReplacement("a&b&c"));
		assertEquals("a\\b", JRT.prepareReplacement("a\\b"));
		assertEquals("a&b", JRT.prepareReplacement("a\\&b"));
		assertEquals("a\\", JRT.prepareReplacement("a\\"));
		assertEquals("a\\$", JRT.prepareReplacement("a$"));
		assertEquals("a\\\\$", JRT.prepareReplacement("a\\$"));
		assertEquals("a\\\\\\$", JRT.prepareReplacement("a\\\\$"));
		assertEquals("a\\\\$0", JRT.prepareReplacement("a\\\\&"));
		assertEquals("a\\\\&", JRT.prepareReplacement("a\\\\\\&"));
		assertEquals("", JRT.prepareReplacement(""));
		assertEquals("", JRT.prepareReplacement(null));
	}

}
