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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Implement the KeyList interface with an ArrayList.
 *
 * @author Danny Daglas
 */
//public class KeyListImpl extends ArrayList<Object> implements KeyList
public class KeyListImpl implements KeyList {

	private List<Object> list;

	/**
	 * Convert the set to a KeyList.
	 * We could have used an ArrayList directly. However, tagging
	 * the implementation with a KeyList interface improves type
	 * checking within the parsing / semantic analysis phase.
	 *
	 * @param set a {@link java.util.Set} object
	 */
	public KeyListImpl(Set<Object> set) {
		//super(set);
		list = new ArrayList<Object>(set);
	}

	/** {@inheritDoc} */
	@Override
	public final Object getFirstAndRemove() {
		return list.remove(0);
	}

	/** {@inheritDoc} */
	@Override
	public int size() {
		return list.size();
	}
}
