package org.metricshub.jawk.intermediate;

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
 * Marks a position within the tuple list (queue).
 *
 * @author Danny Daglas
 */
public interface Position {

	/**
	 * <p>isEOF.</p>
	 *
	 * @return true whether we are at the end
	 *   of the tuple list, false otherwise
	 */
	boolean isEOF();

	/**
	 * Advances the position to the next tuple,
	 * as ordered within the tuple list (queue).
	 */
	void next();

	/**
	 * <p>opcode.</p>
	 *
	 * @return the opcode for the tuple at this
	 *	position
	 */
	int opcode();

	/**
	 * Get the integer representation for a particular
	 * element within the tuple.
	 *
	 * @param idx The item to retrieve from the tuple.
	 * @return the integer representation of the item.
	 */
	long intArg(int idx);

	/**
	 * Get the boolean representation for a particular
	 * element within the tuple.
	 *
	 * @param idx The item to retrieve from the tuple.
	 * @return the boolean representation of the item.
	 */
	boolean boolArg(int idx);

	/**
	 * Get a reference to a particular element
	 * within the tuple.
	 *
	 * @param idx The item to retrieve from the tuple.
	 * @return a reference to the item.
	 */
	Object arg(int idx);

	/**
	 * Obtain the address argument for this tuple.
	 * <p>
	 * This is a special form in that the tuple
	 * has only the address argument, and nothing else.
	 *
	 * @return a {@link org.metricshub.jawk.intermediate.Address} object
	 */
	Address addressArg();

	/**
	 * Obtain the class argument for this tuple.
	 * <p>
	 * This is a special form in that the tuple
	 * has only the class argument, and nothing else.
	 *
	 * @return a {@link java.lang.Class} object
	 */
	Class<?> classArg();

	/**
	 * Get the source line number for this position.
	 *
	 * @return a int
	 */
	int lineNumber();
}
