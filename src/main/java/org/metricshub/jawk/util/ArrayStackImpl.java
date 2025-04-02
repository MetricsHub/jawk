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

import java.util.ArrayList;

/**
 * A stack implemented with an ArrayList.
 * Unlike the <code>java.util.Stack</code> which uses a
 * <code>java.util.Vector</code> as a storage mechanism,
 * this implementation is non-synchronized to improve performance.
 * <p>
 * It performs quicker than the <code>LinkedListStackImpl</code> version.
 *
 * <p>
 * There is no maximum capacity which is enforced, nor is there any
 * checks if <code>pop()</code> is executed on an empty stack.
 *
 * @author Danny Daglas
 */
public class ArrayStackImpl<E> extends ArrayList<E> implements MyStack<E> {

	private static final long serialVersionUID = 1L;

	/**
	 * Allocates an ArrayList with a capacity of 100.
	 */
	public ArrayStackImpl() {
		super(100);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Push an item to the stack.
	 */
	@Override
	public void push(E o) {
		add(o);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Pops an item off the stack.
	 * <p>
	 * Warning: no checks are done in terms of size, etc.
	 * If a <code>pop()</code> is called on an empty stack,
	 * an <code>ArrayIndexOutOfBoundException</code> is thrown.
	 */
	@Override
	public E pop() {
		return remove(size() - 1);
	}

	/** {@inheritDoc} */
	@Override
	public E peek() {
		return get(size() - 1);
	}
}
