package io.jawk.util;

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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reports the version of the Jawk that is running.
 * <p>
 * This is the single source of truth behind every version Jawk hands out:
 * {@code jawk -V} on the command line, and
 * {@link javax.script.ScriptEngineFactory#getEngineVersion()} for JSR 223
 * hosts. Embedders can call it directly to stamp their own logs and
 * diagnostics with the Jawk they loaded.
 * </p>
 */
public final class JawkVersion {

	/** Maven descriptor packaged in the Jawk jar, used when the manifest carries no version. */
	private static final String POM_PROPERTIES_RESOURCE = "/META-INF/maven/io.jawk/jawk/pom.properties";

	/** Reported when the build metadata is absent, as in IDE runs and unit tests. */
	private static final String UNKNOWN_VERSION = "unknown";

	/**
	 * Resolved once: the build metadata cannot change while the classes stay
	 * loaded, and the lookup reads a jar resource.
	 */
	private static final String VERSION = resolveVersion();

	/** Not instantiable: the version is a property of the build, not of an object. */
	private JawkVersion() {
		// Utility class
	}

	/**
	 * Returns the version of the running Jawk.
	 *
	 * @return the version string, such as {@code 7.2.00}, or {@code unknown}
	 *         when no build metadata is available
	 */
	public static String getVersion() {
		return VERSION;
	}

	/**
	 * Resolves the Jawk version from the jar metadata.
	 * <p>
	 * The manifest's {@code Implementation-Version} entry is the primary
	 * source; the Maven {@code pom.properties} resource is the fallback for
	 * jars built without that entry. Both are absent when Jawk runs from a
	 * plain class directory (IDE runs, unit tests), where the version is
	 * reported as {@code unknown}.
	 *
	 * @return the version string, or {@code unknown} when no metadata is
	 *         available
	 */
	private static String resolveVersion() {
		Package myPackage = JawkVersion.class.getPackage();
		String version = myPackage != null ? myPackage.getImplementationVersion() : null;
		if (version == null) {
			version = readVersionFromPomProperties();
		}
		return version != null ? version : UNKNOWN_VERSION;
	}

	/**
	 * Reads the Jawk version from the Maven {@code pom.properties} resource
	 * packaged in the jar.
	 *
	 * @return the version string, or {@code null} when the resource is absent
	 *         or unreadable
	 */
	private static String readVersionFromPomProperties() {
		try (InputStream stream = JawkVersion.class.getResourceAsStream(POM_PROPERTIES_RESOURCE)) {
			if (stream == null) {
				return null;
			}
			Properties properties = new Properties();
			properties.load(stream);
			return properties.getProperty("version");
		} catch (IOException ex) {
			// The version is informational: failing to read the metadata must
			// not prevent Jawk from answering the question at all.
			return null;
		}
	}
}
