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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class JawkVersionTest {

	@Test
	public void versionIsEitherBuildMetadataOrTheUnknownFallback() {
		String version = JawkVersion.getVersion();

		assertNotNull("The version report must never be null", version);
		// Unit tests run from target/classes, a plain class directory with no
		// manifest and no Maven descriptor, so "unknown" is the expected answer
		// here; a jar-based run reports the Maven version instead.
		assertTrue(
				"Unexpected version report: " + version,
				"unknown".equals(version) || version.matches("\\d+\\.\\d+\\.\\d+(-\\w+)?"));
	}

	@Test
	public void versionIsStableAcrossCalls() {
		assertEquals(JawkVersion.getVersion(), JawkVersion.getVersion());
	}

	@Test
	public void theJawkOwnedDescriptorWinsOverTheEnclosingManifest() {
		// A Jawk shaded into an application uber-jar sees that application's
		// Implementation-Version through its own package, so the Maven
		// descriptor - which names the artifact it belongs to - decides
		assertEquals("7.2.00", JawkVersion.chooseVersion("7.2.00", "4.1.0-app"));
	}

	@Test
	public void theManifestIsUsedWhenTheDescriptorIsAbsent() {
		// Repackagers that strip META-INF/maven still leave a usable manifest
		assertEquals("7.2.00", JawkVersion.chooseVersion(null, "7.2.00"));
	}

	@Test
	public void unknownIsReportedWhenNeitherSourceHasAVersion() {
		assertEquals("unknown", JawkVersion.chooseVersion(null, null));
	}

	@Test
	public void versionIsNotTheStaleHardCodedStringItReplaced() {
		// The JSR 223 factory used to answer with this literal, frozen at the
		// 3.x line and eleven releases behind (issues #598 and #599)
		assertNotEquals("3.3.06-SNAPSHOT", JawkVersion.getVersion());
	}
}
