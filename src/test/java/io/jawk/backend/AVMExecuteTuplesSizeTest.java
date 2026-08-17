package io.jawk.backend;

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

/**
 * Guards {@link AVM#executeTuples} against HotSpot's huge-method cliff.
 * <p>
 * HotSpot never JIT-compiles a method whose bytecode is larger than
 * {@code -XX:HugeMethodLimit} (8000, product build, not adjustable without
 * a debug VM). If the interpreter dispatch loop crosses that limit, every
 * AWK script runs ~4x slower, silently: no error, no warning, no JIT warmup.
 * This has happened twice (see issue #562); different compilers sit at
 * different sizes (ECJ output is ~40 bytecodes larger than javac's), so the
 * threshold here keeps a comfortable safety margin below the real limit.
 * </p>
 */
public class AVMExecuteTuplesSizeTest {

	/** Guarded method name. */
	private static final String METHOD_NAME = "executeTuples";

	/**
	 * Maximum allowed bytecode size: well under HotSpot's HugeMethodLimit
	 * (8000) so that neither javac nor ECJ output ever gets close to the
	 * cliff. If this test fails, extract opcode handlers from
	 * {@code executeTuples} into private methods (see the exec* helpers).
	 */
	private static final int MAX_CODE_LENGTH = 7500;

	@Test
	public void testExecuteTuplesStaysUnderHugeMethodLimit() throws IOException {
		Map<String, Integer> codeLengths = readMethodCodeLengths(AVM.class);
		Integer size = codeLengths.get(METHOD_NAME);
		if (size == null) {
			fail("Method " + METHOD_NAME + " not found in AVM.class - update this test if it was renamed");
		}
		assertTrue(
				METHOD_NAME + " is " + size + " bytecodes; it must stay <= " + MAX_CODE_LENGTH
						+ " or HotSpot (HugeMethodLimit=8000) will never JIT-compile the interpreter loop."
						+ " Extract opcode handlers into private exec* methods to shrink it.",
				size <= MAX_CODE_LENGTH);
	}

	@Test
	public void testParserSeesPlausibleMethodSizes() throws IOException {
		// Sanity-check the class-file parser itself: a tiny accessor must
		// exist and be far smaller than the dispatch loop.
		Map<String, Integer> codeLengths = readMethodCodeLengths(AVM.class);
		assertFalse("no methods with Code attributes found", codeLengths.isEmpty());
		Integer size = codeLengths.get(METHOD_NAME);
		assertTrue(
				"executeTuples should be the kind of method this guard exists for (>1000 bytecodes)",
				size != null && size > 1000);
	}

	/**
	 * Parses a class file and returns the {@code Code} attribute length of
	 * each method, keyed by method name. When a name is overloaded, the
	 * largest variant wins: the guard cares about the biggest body.
	 *
	 * @param clazz the class whose bytecode to inspect
	 * @return map of method name to bytecode ({@code code_length}) size
	 * @throws IOException if the class file cannot be read
	 */
	private static Map<String, Integer> readMethodCodeLengths(Class<?> clazz) throws IOException {
		String resource = "/" + clazz.getName().replace('.', '/') + ".class";
		try (InputStream is = clazz.getResourceAsStream(resource)) {
			if (is == null) {
				throw new IOException("Cannot load " + resource);
			}
			DataInputStream in = new DataInputStream(is);
			if (in.readInt() != 0xCAFEBABE) {
				throw new IOException("Not a class file: " + resource);
			}
			in.readUnsignedShort(); // minor
			in.readUnsignedShort(); // major

			// Constant pool: we only need the UTF-8 entries (method and
			// attribute names); everything else is skipped by tag size.
			int cpCount = in.readUnsignedShort();
			String[] utf8 = new String[cpCount];
			for (int i = 1; i < cpCount; i++) {
				int tag = in.readUnsignedByte();
				switch (tag) {
				case 1: // CONSTANT_Utf8
					utf8[i] = in.readUTF();
					break;
				case 7: // Class
				case 8: // String
				case 16: // MethodType
				case 19: // Module
				case 20: // Package
					skipFully(in, 2);
					break;
				case 15: // MethodHandle
					skipFully(in, 3);
					break;
				case 3: // Integer
				case 4: // Float
				case 9: // Fieldref
				case 10: // Methodref
				case 11: // InterfaceMethodref
				case 12: // NameAndType
				case 17: // Dynamic
				case 18: // InvokeDynamic
					skipFully(in, 4);
					break;
				case 5: // Long
				case 6: // Double
					skipFully(in, 8);
					i++; // longs and doubles take two constant pool slots
					break;
				default:
					throw new IOException("Unknown constant pool tag " + tag + " in " + resource);
				}
			}

			skipFully(in, 6); // access_flags, this_class, super_class
			int interfaceCount = in.readUnsignedShort();
			skipFully(in, 2 * interfaceCount);

			skipFieldsOrMethods(in, in.readUnsignedShort(), null, utf8); // fields

			Map<String, Integer> codeLengths = new HashMap<>();
			skipFieldsOrMethods(in, in.readUnsignedShort(), codeLengths, utf8); // methods
			return codeLengths;
		}
	}

	/**
	 * Reads a field_info/method_info table. When {@code codeLengths} is
	 * non-null, records each method's {@code Code} attribute length.
	 *
	 * @param in input positioned at the start of the table
	 * @param count number of entries
	 * @param codeLengths collector for method code lengths, or {@code null}
	 *        to skip entries entirely (fields)
	 * @param utf8 constant pool UTF-8 entries
	 * @throws IOException if the class file cannot be read
	 */
	private static void skipFieldsOrMethods(
			DataInputStream in,
			int count,
			Map<String, Integer> codeLengths,
			String[] utf8)
			throws IOException {
		for (int i = 0; i < count; i++) {
			skipFully(in, 2); // access_flags
			int nameIndex = in.readUnsignedShort();
			skipFully(in, 2); // descriptor_index
			int attributeCount = in.readUnsignedShort();
			for (int a = 0; a < attributeCount; a++) {
				int attrNameIndex = in.readUnsignedShort();
				int attrLength = in.readInt();
				if (codeLengths != null && "Code".equals(utf8[attrNameIndex])) {
					skipFully(in, 4); // max_stack, max_locals
					int codeLength = in.readInt();
					String name = utf8[nameIndex];
					Integer previous = codeLengths.get(name);
					if (previous == null || previous < codeLength) {
						codeLengths.put(name, codeLength);
					}
					// 8 bytes consumed so far: max_stack, max_locals, code_length
					skipFully(in, attrLength - 8L);
				} else {
					skipFully(in, attrLength);
				}
			}
		}
	}

	/**
	 * Skips exactly {@code n} bytes, looping because
	 * {@link java.io.InputStream#skip(long)} may skip fewer.
	 *
	 * @param in the stream to skip in
	 * @param n number of bytes to skip
	 * @throws IOException if the end of the stream is reached first
	 */
	private static void skipFully(DataInputStream in, long n) throws IOException {
		long remaining = n;
		while (remaining > 0) {
			long skipped = in.skip(remaining);
			if (skipped <= 0) {
				if (in.read() < 0) {
					throw new IOException("Unexpected end of class file");
				}
				skipped = 1;
			}
			remaining -= skipped;
		}
	}
}
