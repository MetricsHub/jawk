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

/**
 * A stack-like interface.
 * <p>
 * Unfortunately, <code>java.util.Stack</code> uses a Vector, and is,
 * therefore, needlessly synchronized in a non-multi-threaded
 * environment. As a result, it was necessary to re-implement the
 * stack in this manner by using a non-synchronized list.
 *
 * @author Danny Daglas
 */
public interface MyStack<E> {

	/**
	 * Push an item onto the stack.
	 *
	 * @param o The item to push onto the stack.
	 */
	void push(E o);

	/**
	 * Pop an item off the stack and return that item
	 * to the callee.
	 *
	 * @return The top of the stack, which is subsequently
	 *   removed from the stack.
	 */
	E pop();

	/**
	 * <p>size.</p>
	 *
	 * @return The number of elements within the stack.
	 */
	int size();

	/**
	 * Eliminate all items from the stack.
	 */
	void clear();

	/**
	 * Inspect the top-most element without affecting the stack.
	 *
	 * @return the top of the stack, without removing it from the stack
	 */
	E peek();
}
