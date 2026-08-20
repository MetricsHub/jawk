package io.jawk.jsr223;

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
import java.util.Arrays;
import java.util.List;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import io.jawk.util.JawkVersion;

/** ScriptEngineFactory for Jawk. */
public class JawkScriptEngineFactory implements ScriptEngineFactory {

	/**
	 * Creates the factory.
	 * <p>
	 * This is the constructor the JSR-223 {@link javax.script.ScriptEngineManager}
	 * uses when it discovers Jawk through the service loader.
	 * </p>
	 */
	public JawkScriptEngineFactory() {
		// Stateless: every engine is created by getScriptEngine().
	}

	@Override
	public String getEngineName() {
		return "Jawk";
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The version of the Jawk that is running, the same one {@code jawk -V}
	 * prints, resolved from the jar metadata by
	 * {@link JawkVersion#getVersion()}. It is {@code unknown} when Jawk runs
	 * from a plain class directory, which carries no version at all.
	 * </p>
	 */
	@Override
	public String getEngineVersion() {
		return JawkVersion.getVersion();
	}

	@Override
	public List<String> getExtensions() {
		return Arrays.asList("awk");
	}

	@Override
	public List<String> getMimeTypes() {
		return Arrays.asList("application/x-awk");
	}

	@Override
	public List<String> getNames() {
		return Arrays.asList("jawk", "Jawk");
	}

	@Override
	public String getLanguageName() {
		return "awk";
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * {@code POSIX}: the language Jawk implements is POSIX awk, which is
	 * specified by revision rather than by a version number of its own. Jawk's
	 * own version, which does move from release to release, is
	 * {@link #getEngineVersion()}.
	 * </p>
	 */
	@Override
	public String getLanguageVersion() {
		return "POSIX";
	}

	@Override
	public Object getParameter(String key) {
		if (ScriptEngine.NAME.equals(key) || ScriptEngine.ENGINE.equals(key)) {
			return getEngineName();
		}
		if (ScriptEngine.ENGINE_VERSION.equals(key)) {
			return getEngineVersion();
		}
		if (ScriptEngine.LANGUAGE.equals(key)) {
			return getLanguageName();
		}
		if (ScriptEngine.LANGUAGE_VERSION.equals(key)) {
			return getLanguageVersion();
		}
		return null;
	}

	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append(obj).append(".").append(m).append("(");
		for (int i = 0; i < args.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(args[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print \"" + toDisplay.replace("\"", "\\\"") + "\"";
	}

	@Override
	public String getProgram(String... statements) {
		StringBuilder sb = new StringBuilder();
		for (String s : statements) {
			sb.append(s).append('\n');
		}
		return sb.toString();
	}

	@Override
	public ScriptEngine getScriptEngine() {
		return new JawkScriptEngine(this);
	}
}
