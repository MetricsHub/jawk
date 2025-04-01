package org.metricshub.jawk;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * Jawk
 * ჻჻჻჻჻჻
 * Copyright (C) 2006 - 2025 MetricsHub
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

import java.io.InputStream;
import java.io.PrintStream;

import org.metricshub.jawk.util.AwkParameters;
import org.metricshub.jawk.util.AwkSettings;

/**
 * Entry point into the parsing, analysis, and execution/compilation
 * of a Jawk script.
 * This entry point is used when Jawk is executed as a stand-alone application.
 * If you want to use Jawk as a library, please use {@see Awk}.
 *
 * @author Danny Daglas
 */
public class Main {

	/**
	 * Prohibit the instantiation of this class, other than the
	 * way required by JSR 223.
	 */
	@SuppressWarnings("unused")
	private Main() {}

	/**
	 * Class constructor to support the JSR 223 scripting interface
	 * already provided by Java SE 6.
	 *
	 * @param args String arguments from the command-line.
	 * @param is The input stream to use as stdin.
	 * @param os The output stream to use as stdout.
	 * @param es The output stream to use as stderr.
	 * @throws java.lang.Exception enables exceptions to propagate to the callee.
	 */
	public Main(String[] args, InputStream is, PrintStream os, PrintStream es)
			throws Exception
	{
		System.setIn(is);
		System.setOut(os);
		System.setErr(es);

		AwkSettings settings = AwkParameters.parseCommandLineArguments(args);
		Awk awk = new Awk();
		awk.invoke(settings);
	}

	/**
	 * The entry point to Jawk for the VM.
	 * <p>
	 * The main method is a simple call to the invoke method.
	 * The current implementation is basically as follows:
	 * <blockquote>
	 * <pre>
	 * System.exit(invoke(args));
	 * </pre>
	 * </blockquote>
	 *
	 * @param args Command line arguments to the VM.
	 */
	public static void main(String[] args) {

		try {
			AwkSettings settings = AwkParameters.parseCommandLineArguments(args);
			Awk awk = new Awk();
			awk.invoke(settings);
		} catch (ExitException e) {
			System.exit(e.getCode());
		} catch (Exception e) {
			System.err.printf("%s: %s\n", e.getClass().getSimpleName(), e.getMessage());
			System.exit(1);
		}

	}


}
