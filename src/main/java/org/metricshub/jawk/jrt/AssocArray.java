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

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.metricshub.jawk.intermediate.UninitializedObject;

/**
 * An AWK associative array.
 * <p>
 * The implementation requires the ability to choose,
 * at runtime, whether the keys are to be maintained in
 * sorted order or not. Therefore, the implementation
 * contains a reference to a Map (either TreeMap or
 * HashMap, depending on whether to maintain keys in
 * sorted order or not) and delegates calls to it
 * accordingly.
 *
 * @author Danny Daglas
 */
public class AssocArray implements Comparator<Object> {

	private Map<Object, Object> map;

	/**
	 * <p>Constructor for AssocArray.</p>
	 *
	 * @param sortedArrayKeys Whether keys must be kept sorted
	 */
	public AssocArray(boolean sortedArrayKeys) {
		if (sortedArrayKeys) {
			map = new TreeMap<Object, Object>(this);
		} else {
			map = new HashMap<Object, Object>();
		}
	}

	/**
	 * The parameter to useMapType to convert
	 * this associative array to a HashMap.
	 */
	public static final int MT_HASH = 2;
	/**
	 * The parameter to useMapType to convert
	 * this associative array to a LinkedHashMap.
	 */
	public static final int MT_LINKED = 2 << 1;
	/**
	 * The parameter to useMapType to convert
	 * this associative array to a TreeMap.
	 */
	public static final int MT_TREE = 2 << 2;

	/**
	 * Convert the map which backs this associative array
	 * into one of HashMap, LinkedHashMap, or TreeMap.
	 *
	 * @param mapType Can be one of MT_HASH, MT_LINKED,
	 *   or MT_TREE.
	 */
	public void useMapType(int mapType) {
		assert map.isEmpty();
		switch (mapType) {
			case MT_HASH:
				map = new HashMap<Object, Object>();
				break;
			case MT_LINKED:
				map = new LinkedHashMap<Object, Object>();
				break;
			case MT_TREE:
				map = new TreeMap<Object, Object>(this);
				break;
			default:
				throw new Error("Invalid map type : " + mapType);
		}
	}

	/**
	 * Provide a string representation of the delegated
	 * map object.
	 * It exists to support the _DUMP keyword.
	 *
	 * @return string representing the map/array
	 */
	public String mapString() {
		// was:
		//return map.toString();
		// but since the extensions, assoc arrays can become keys as well
		StringBuilder sb = new StringBuilder().append('{');
		int cnt = 0;
		for (Object o : map.keySet()) {
			if (cnt > 0) {
				sb.append(", ");
			}
			if (o instanceof AssocArray) {
				sb.append(((AssocArray) o).mapString());
			} else {
				sb.append(o.toString());
			}
			//sb.append('=').append(map.get(o));
			sb.append('=');
			Object o2 = map.get(o);
			if (o2 instanceof AssocArray) {
				sb.append(((AssocArray) o2).mapString());
			} else {
				sb.append(o2.toString());
			}
			++cnt;
		}
		return sb.append('}').toString();
	}

	/** a "null" value in Awk */
	private static final UninitializedObject BLANK = new UninitializedObject();

	/**
	 * <p>isIn.</p>
	 *
	 * @param key Key to be checked
	 * @return whether a particular key is
	 * contained within the associative array.
	 * Unlike get(), which adds a blank (null)
	 * reference to the associative array if the
	 * element is not found, isIn will not.
	 * It exists to support the IN keyword.
	 */
	public boolean isIn(Object key) {
		return map.get(key) != null;
	}

	/**
	 * <p>get.</p>
	 *
	 * @param key Key to retrieve in the array
	 * @return the value of an associative array
	 * element given a particular key.
	 * If the key does not exist, a null value
	 * (blank string) is inserted into the array
	 * with this key, and the null value is returned.
	 */
	public Object get(Object key) {
		if (key == null || key instanceof UninitializedObject) {
			key = (long)0;
		}
		Object result = map.get(key);
		if (result != null) {
			return result;
		}

		// Did not find it?
		try {
			// try a integer version key
			key = Long.parseLong(key.toString());
			result = map.get(key);
			if (result != null) {
				return result;
			}
		} catch (Exception e) {}

		// based on the AWK specification:
		// Any reference (except for IN expressions) to a non-existent
		// array element will automatically create it.
		result = BLANK;
		map.put(key, result);

		return result;
	}

	/**
	 * Added to support insertion of primitive key types.
	 *
	 * @param key Key of the entry to put in the array
	 * @param value Value of the key
	 * @return the previous value of the specified key, or null if key didn't exist
	 */
	public Object put(Object key, Object value) {
		if (key == null || key instanceof UninitializedObject) {
			key = (long)0;
		}
		try {
			// Save a primitive version
			long iKey = Long.parseLong(key.toString());
			return map.put(iKey, value);
		} catch (Exception e) {
		}

		return map.put(key, value);
	}

	/**
	 * Added to support insertion of primitive key types.
	 *
	 * @param key Index of the entry to put in the array
	 * @param value Value of the key
	 * @return the previous value of the specified key, or null if key didn't exist
	 */
	public Object put(long key, Object value) {
		return map.put(key, value);
	}

	/**
	 * <p>keySet.</p>
	 *
	 * @return the set of keys
	 */
	public Set<Object> keySet() {
		return map.keySet();
	}

	/**
	 * Clear the array
	 */
	public void clear() {
		map.clear();
	}

	/**
	 * Delete the specified entry
	 *
	 * @param key Key of the entry to remove from the array
	 * @return the value of the entry before it was removed
	 */
	public Object remove(Object key) {
		return map.remove(key);
	}

	/**
	 * {@inheritDoc}
	 *
	 * Do nothing. Should not be called in this state.
	 */
	@Override
	public String toString() {
		throw new AwkRuntimeException("Cannot evaluate an unindexed array.");
	}

	/**
	 * {@inheritDoc}
	 *
	 * Comparator implementation used by the TreeMap
	 * when keys are to be maintained in sorted order.
	 */
	@Override
	public int compare(Object o1, Object o2) {

		if (o1 instanceof String || o2 instanceof String) {
			// use string comparison
			String s1 = o1.toString();
			String s2 = o2.toString();
			return s1.compareTo(s2);
		} else {
			if (o1 instanceof Double || o2 instanceof Double) {
				Double d1 = ((Double) o1);
				Double d2 = ((Double) o2);
				return d1.compareTo(d2);
			} else {
				Integer i1 = ((Integer) o1);
				Integer i2 = ((Integer) o2);
				return i1.compareTo(i2);
			}
		}
	}

	/**
	 * <p>getMapVersion.</p>
	 *
	 * @return the specification version of this class
	 */
	public String getMapVersion() {
		return map.getClass().getPackage().getSpecificationVersion();
	}
}
