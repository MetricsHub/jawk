package org.metricshub.jawk.jrt;

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

/**
 * A list of keys into an associative array.
 * <p>
 * KeyList is provided to differentiate between associative
 * array key lists and other types of lists on the operand stack
 * or as contained by variables. However, this is the only
 * List in used in this manner within Jawk at the time of
 * this writing.
 *
 * @see KeyListImpl
 * @author Danny Daglas
 */
//public interface KeyList extends java.util.List<Object>
public interface KeyList {

	/**
	 * Retrieve the number of elements in the KeyList.
	 *
	 * @return a int
	 */
	int size();

	/**
	 * <p>getFirstAndRemove.</p>
	 *
	 * @return a {@link java.lang.Object} object
	 */
	Object getFirstAndRemove();
}
