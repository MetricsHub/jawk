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
	 *
	 * @return the version string, or {@code unknown} when no metadata is
	 *         available
	 */
	private static String resolveVersion() {
		Package myPackage = JawkVersion.class.getPackage();
		return chooseVersion(
				readVersionFromPomProperties(),
				myPackage != null ? myPackage.getImplementationVersion() : null);
	}

	/**
	 * Picks the version to report, preferring the one that is known to describe
	 * Jawk itself.
	 * <p>
	 * The Maven {@code pom.properties} resource comes first because it names its
	 * artifact: it can only be Jawk's. The manifest's
	 * {@code Implementation-Version} does not, and
	 * {@link Package#getImplementationVersion()} serves the main attributes of
	 * whichever jar encloses the classes, so a Jawk shaded into an application
	 * uber-jar would otherwise report the application's version. It stays as the
	 * fallback for jars repackaged without the Maven descriptor. Neither is
	 * present when Jawk runs from a plain class directory (IDE runs, unit
	 * tests).
	 *
	 * @param pomPropertiesVersion version read from the Maven descriptor, or
	 *        {@code null} when it is absent
	 * @param manifestVersion version the enclosing manifest declares, or
	 *        {@code null} when it declares none
	 * @return the version to report, or {@code unknown} when neither source has
	 *         one
	 */
	static String chooseVersion(String pomPropertiesVersion, String manifestVersion) {
		if (pomPropertiesVersion != null) {
			return pomPropertiesVersion;
		}
		return manifestVersion != null ? manifestVersion : UNKNOWN_VERSION;
	}

	/**
	 * Reads the Jawk version from the Maven {@code pom.properties} resource
	 * packaged in the jar, the descriptor of the {@code io.jawk:jawk} artifact.
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
