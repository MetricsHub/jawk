package org.metricshub.jawk;

import static org.junit.Assert.*;

import org.junit.Test;
import org.metricshub.jawk.jrt.ConditionPair;

public class ConditionPairTest {

	@Test
	public void test() {
		ConditionPair cp = new ConditionPair();
		assertFalse("Begin outside", cp.update(false, false));
		assertTrue("Entering", cp.update(true, false));
		assertTrue("Still inside", cp.update(false, false));
		assertTrue("Leaving", cp.update(false, true));
		assertFalse("Outside", cp.update(false, false));
		
		assertTrue("Re-entering", cp.update(true, false));
		assertTrue("Still inside", cp.update(false, false));
		assertTrue("Re-re-entering", cp.update(true, false));
		assertTrue("Leaving", cp.update(false, true));
		assertFalse("Outside", cp.update(false, false));
		assertFalse("Re-leaving", cp.update(false, true));

		assertTrue("Entering and leaving", cp.update(true, true));
		assertFalse("So we're outside", cp.update(false, false));
	}

}
