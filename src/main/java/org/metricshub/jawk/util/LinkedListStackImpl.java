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

import java.util.LinkedList;

/**
 * A simple delegate to a LinkedList.
 * Unlike <code>java.util.Stack</code>,
 * this implementation is non-synchronized to improve performance.
 * <p>
 * It performs slower than the ArrayStackImpl version.
 *
 * <p>
 * There is no maximum capacity which is enforced, nor is there any
 * checks if pop() is executed on an empty stack.
 *
 * @author Danny Daglas
 */
public class LinkedListStackImpl<E> extends LinkedList<E> implements MyStack<E> {

	private static final long serialVersionUID = 1L;

	/** {@inheritDoc} */
	@Override
	public void push(E o) {
		addFirst(o);
	}

	/** {@inheritDoc} */
	@Override
	public E pop() {
		return removeFirst();
	}
}
