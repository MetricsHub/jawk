package org.metricshub.jawk;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test Suite based on unit and non-regression tests from bwk. Each AWK script
 * in the src/test/resources/bwk.t/t.scripts directory will be executed against
 * the corresponding *.in input, and its output will be compared to the
 * corresponding *.ok file.
 * 
 * @see <a href="https://github.com/onetrueawk/awk">One True Awk</a>
 */
@RunWith(Parameterized.class)
public class BwkTTest {

	private static final String BWK_T_PATH = "/bwk/t";
	private static File bwkTDirectory;
	private static File scriptsDirectory;

	
	/**
	 * Initialization of the tests
	 * 
	 * @throws Exception
	 */
	@BeforeClass
	public static void beforeAll() throws Exception {
	}

	/**
	 * @return the list of awk scripts in /src/test/resources/gawk
	 * @throws Exception
	 */
	@Parameters(name = "BWK.t {0}")
	public static Iterable<String> awkList() throws Exception {

		// Get the /bwk resource directory
		URL bwkTUrl = BwkTTest.class.getResource(BWK_T_PATH);
		if (bwkTUrl == null) {
			throw new IOException("Couldn't find resource " + BWK_T_PATH);
		}
		bwkTDirectory = new File(bwkTUrl.toURI());
		if (!bwkTDirectory.isDirectory()) {
			throw new IOException(BWK_T_PATH + " is not a directory");
		}
		scriptsDirectory = new File(bwkTDirectory, "scripts");
		if (!scriptsDirectory.isDirectory()) {
			throw new IOException("scripts is not a directory");
		}
		
		return Arrays.stream(scriptsDirectory.listFiles())
				.filter(sf -> sf.getName().startsWith("t."))
				.map(File::getName)
				.collect(Collectors.toList());

	}

	/** Path to the AWK test script to execute */
	@Parameter
	public String awkName;

	/**
	 * Execute the AWK script stored in {@link #awkName}
	 * 
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception {

		// Get the AWK script file
		File awkFile = new File(scriptsDirectory, awkName);

		// Get the file with the expected result
		File okFile = new File(bwkTDirectory, "results/" + awkName + ".ok");

		// Get the input file (always the same)
		File inputFile = new File(bwkTDirectory, "inputs/test.data");

		String result = AwkTestHelper.runAwk(awkFile, inputFile);
		String expectedResult = AwkTestHelper.readTextFile(okFile);
		assertEquals(expectedResult, result);

	}

	/**
	 * Initialization of the tests (create a temporary directory for some of the
	 * scripts)
	 * 
	 * @throws Exception
	 */
	@AfterClass
	public static void afterAll() throws Exception {
	}
}
