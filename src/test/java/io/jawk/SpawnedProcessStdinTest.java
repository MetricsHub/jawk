package io.jawk;

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
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.concurrent.TimeUnit;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Verifies that the children of {@code system()} and of a command input pipe
 * ({@code "cmd" | getline}) inherit Jawk's standard input, as POSIX requires,
 * when Jawk reads the standard input of the JVM — and that embedded executions
 * bound to a custom stream keep the child's standard input closed.
 * <p>
 * The inheritance can only be observed across a real process boundary, which
 * the in-process builders of {@link AwkTestSupport} cannot provide, so the
 * inheriting cases spawn the CLI in a fresh JVM whose standard input is
 * redirected from a file.
 */
public class SpawnedProcessStdinTest {

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	public void commandInputPipeChildReadsJawkStandardInput() throws Exception {
		String output = runCliJvmWithStdin(
				"BEGIN { \"sort\" | getline line; print \"[\" line \"]\" }",
				"hello\n");
		assertEquals("[hello]\n", output);
	}

	@Test
	public void systemChildReadsJawkStandardInput() throws Exception {
		String output = runCliJvmWithStdin(
				"BEGIN { system(\"sort\") }",
				"zulu\nalpha\n");
		assertEquals("alpha\nzulu\n", output);
	}

	@Test
	public void embeddedExecutionKeepsChildStandardInputClosed() throws Exception {
		// A custom input stream cannot be lent to another OS process, so the
		// child sees end of input at once and getline returns 0
		AwkTestSupport
				.cliTest("embedded cmd|getline child gets no standard input")
				.script("BEGIN { n = (\"sort\" | getline line); print n \"[\" line \"]\" }")
				.stdin("never seen by the child\n")
				.expectLines("0[]")
				.runAndAssert();
	}

	/**
	 * Runs the CLI in a fresh JVM with its standard input redirected from a
	 * file holding the given content, and returns the standard output with
	 * platform line separators normalized to {@code \n}. The script is passed
	 * through a file: an inline argument would not survive the Windows
	 * command-line round trip, which mangles embedded double quotes.
	 */
	private String runCliJvmWithStdin(String script, String stdinContent) throws Exception {
		File stdinFile = tempFolder.newFile("stdin.txt");
		Files.write(stdinFile.toPath(), stdinContent.getBytes(StandardCharsets.UTF_8));
		File scriptFile = tempFolder.newFile("script.awk");
		Files.write(scriptFile.toPath(), script.getBytes(StandardCharsets.UTF_8));

		String javaBinary = new File(new File(System.getProperty("java.home"), "bin"), "java").getAbsolutePath();
		File classes = new File(Cli.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		ProcessBuilder pb = new ProcessBuilder(
				javaBinary,
				"-cp",
				classes.getAbsolutePath(),
				Cli.class.getName(),
				"-f",
				scriptFile.getAbsolutePath());
		pb.redirectInput(stdinFile);

		Process process = pb.start();
		ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		ByteArrayOutputStream stderr = new ByteArrayOutputStream();
		try (InputStream out = process.getInputStream(); InputStream err = process.getErrorStream()) {
			copy(out, stdout);
			copy(err, stderr);
		}
		assertTrue("CLI JVM did not terminate", process.waitFor(30, TimeUnit.SECONDS));
		assertEquals(
				"CLI JVM failed: " + stderr.toString("UTF-8"),
				0,
				process.exitValue());
		return stdout.toString("UTF-8").replace("\r\n", "\n");
	}

	private static void copy(InputStream in, ByteArrayOutputStream sink) throws Exception {
		byte[] buffer = new byte[8192];
		int n;
		while ((n = in.read(buffer)) >= 0) {
			sink.write(buffer, 0, n);
		}
	}
}
