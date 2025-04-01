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
 * A placeholder for an object which has a reference to
 * a function address, but which may not be realized yet.
 * This is particularly important for forward-referenced
 * functions. For example:
 * <blockquote>
 * <pre>
 * BEGIN { f(3) }
 * function f(x) { print x*x }
 * </pre>
 * </blockquote>
 * f() is referred to prior to its definition. Therefore,
 * the getFunctionAddress() call within the BEGIN block
 * will not return a meaningful address. However, anytime
 * after f(x) is defined, getFunctionAddress() will return
 * the correct function address.
 *
 * @author Danny Daglas
 */
public interface HasFunctionAddress {

	/**
	 * Get an address to the tuple where this function is
	 * defined.
	 * <p>
	 * If getFunctionAddress() is called prior to defining
	 * the function address (prior to parsing the function
	 * block), the result is undefined. (As of this writing,
	 * a NullPointerException is thrown.)
	 *
	 * @return a {@link org.metricshub.jawk.intermediate.Address} object
	 */
	Address getFunctionAddress();
}
