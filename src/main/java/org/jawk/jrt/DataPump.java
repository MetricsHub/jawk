package org.jawk.jrt;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * Jawk
 * ჻჻჻჻჻჻
 * Copyleft 2006 - 2023 Danny Daglas, Robin Vobruba, Sentry Software
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

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

/**
 * Relay data from an input stream to an output stream.
 * A thread is created to do the work.
 * <p>
 * Jawk uses data pumps to relay stdin, stdout, and stderr
 * of a spawned process (by, for example, system() or
 * "cmd" | getline) to the stdin, stdout, and/or stderr
 * of the calling process (the interpreter itself).
 *
 * @author Danny Daglas
 */
public class DataPump implements Runnable {

	private InputStream is;
	private PrintStream os;

	/**
	 * Represents a data pump.
	 *
	 * @param in The input stream.
	 * @param out The output stream.
	 */
	public DataPump(InputStream in, PrintStream out) {
		this.is = in;
		this.os = out;
		//setDaemon(true);
	}

	/**
	 * Allocate the data pump and start the thread.
	 *
	 * @param desc A human-readable description of this data pump.
	 *   It is part of the thread name, and, therefore, visible
	 *   upon a VM thread dump.
	 * @param in The input stream.
	 * @param out The output stream.
	 */
	public static void dump(String desc, InputStream in, PrintStream out) {
		new Thread(new DataPump(in, out), desc).start();
	}

	/**
	 * {@inheritDoc}
	 *
	 * VM entry point for the thread. It performs the data
	 * relay.
	 */
	@Override
	public final void run() {
		try {
			byte[] b = new byte[4096];
			int len;
			while ((len = is.read(b, 0, b.length)) >= 0) {
				os.write(b, 0, len);
			}
		} catch (IOException ioe) {
			// ignore
		}
		try {
			is.close();
		} catch (IOException ioe) {}
	}
}
