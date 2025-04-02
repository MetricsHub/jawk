package org.metricshub.jawk.util;

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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import org.slf4j.Logger;

/**
 * Represents one AWK-script file content source.
 *
 * @author Danny Daglas
 */
public class ScriptFileSource extends ScriptSource {

	private static final Logger LOG = AwkLogger.getLogger(ScriptFileSource.class);

	private String filePath;
	private Reader fileReader;
	private InputStream fileInputStream;

	/**
	 * <p>Constructor for ScriptFileSource.</p>
	 *
	 * @param filePath a {@link java.lang.String} object
	 */
	public ScriptFileSource(String filePath) {
		super(filePath, null, filePath.endsWith(".ai"));

		this.filePath = filePath;
		this.fileReader = null;
		this.fileInputStream = null;
	}

	/**
	 * <p>Getter for the field <code>filePath</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getFilePath() {
		return filePath;
	}

	/** {@inheritDoc} */
	@Override
	public Reader getReader() {

		if ((fileReader == null) && !isIntermediate()) {
			try {
				fileReader = new FileReader(filePath);
			} catch (FileNotFoundException ex) {
				LOG.error("Failed to open script source for reading: " + filePath, ex);
			}
		}

		return fileReader;
	}

	/** {@inheritDoc} */
	@Override
	public InputStream getInputStream() {

		if ((fileInputStream == null) && isIntermediate()) {
			try {
				fileInputStream = new FileInputStream(filePath);
			} catch (FileNotFoundException ex) {
				LOG.error("Failed to open script source for reading: " + filePath, ex);
			}
		}

		return fileInputStream;
	}
}
