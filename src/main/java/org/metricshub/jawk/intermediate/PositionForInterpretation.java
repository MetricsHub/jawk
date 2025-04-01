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
 * An interface to a tuple position for interpretation.
 * <p>
 * This is differentiated from a position interface for
 * compilation because compilation requires linear
 * access (i.e., non-jumps) to the tuple list, while
 * interpretation requires this as well as jump capability.
 *
 * @author Danny Daglas
 */
public interface PositionForInterpretation extends Position {

	/**
	 * Reposition to the tuple located at a particular address.
	 * This is usually done in a response to an if condition.
	 * However, this is also done to perform loops, etc.
	 *
	 * @param address The target address for the jump.
	 */
	void jump(Address address);

	/**
	 * <p>current.</p>
	 *
	 * @return The current index into the tuple list (queue)
	 *	of the tuple located at the current position.
	 */
	int current();

	/**
	 * Reposition to the tuple located at a particular index
	 * into the tuple list (queue)..
	 *
	 * @param idx The target index for the jump.
	 */
	void jump(int idx);
}
