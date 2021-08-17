/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sun.misc.Unsafe;

/**
 * Provides access to the sun.misc.Unsafe class and its key static fields.
 *
 * @author Lee Rhodes
 */
@SuppressWarnings({"restriction", "javadoc"})
public final class UnsafeUtil {
  public static final Unsafe unsafe;
  public static final String JDK; //must be at least "1.8"
  public static final int JDK_MAJOR; //8, 9, 10, 11, 12, etc

  //not an indicator of whether compressed references are used.
  public static final int ADDRESS_SIZE;

  //For 64-bit JVMs: these offsets vary depending on coop: 16 for JVM <= 32GB; 24 for JVM > 32GB.
  // Making this constant long-typed, rather than int, to exclude possibility of accidental overflow
  // in expressions like arrayLength * ARRAY_BYTE_BASE_OFFSET, where arrayLength is int-typed.
  // The same consideration for constants below: ARRAY_*_INDEX_SCALE, ARRAY_*_INDEX_SHIFT.
  public static final long ARRAY_BOOLEAN_BASE_OFFSET;
  public static final long ARRAY_BYTE_BASE_OFFSET;
  public static final long ARRAY_SHORT_BASE_OFFSET;
  public static final long ARRAY_CHAR_BASE_OFFSET;
  public static final long ARRAY_INT_BASE_OFFSET;
  public static final long ARRAY_LONG_BASE_OFFSET;
  public static final long ARRAY_FLOAT_BASE_OFFSET;
  public static final long ARRAY_DOUBLE_BASE_OFFSET;
  public static final long ARRAY_OBJECT_BASE_OFFSET;

  //@formatter:off

  // Setting those values directly instead of using unsafe.arrayIndexScale(), because it may be
  // beneficial for runtime execution, those values are backed into generated machine code as
  // constants. E. g. see https://shipilev.net/jvm-anatomy-park/14-constant-variables/
  public static final int ARRAY_BOOLEAN_INDEX_SCALE = 1;
  public static final int ARRAY_BYTE_INDEX_SCALE    = 1;
  public static final long ARRAY_SHORT_INDEX_SCALE  = 2;
  public static final long ARRAY_CHAR_INDEX_SCALE   = 2;
  public static final long ARRAY_INT_INDEX_SCALE    = 4;
  public static final long ARRAY_LONG_INDEX_SCALE   = 8;
  public static final long ARRAY_FLOAT_INDEX_SCALE  = 4;
  public static final long ARRAY_DOUBLE_INDEX_SCALE = 8;
  public static final long ARRAY_OBJECT_INDEX_SCALE;  // varies, 4 or 8 depending on coop

  //Used to convert "type" to bytes:  bytes = longs << LONG_SHIFT
  public static final int BOOLEAN_SHIFT    = 0;
  public static final int BYTE_SHIFT       = 0;
  public static final long SHORT_SHIFT     = 1;
  public static final long CHAR_SHIFT      = 1;
  public static final long INT_SHIFT       = 2;
  public static final long LONG_SHIFT      = 3;
  public static final long FLOAT_SHIFT     = 2;
  public static final long DOUBLE_SHIFT    = 3;
  public static final long OBJECT_SHIFT;     // varies, 2 or 3 depending on coop

  public static final String LS = System.getProperty("line.separator");

  //@formatter:on

  static {
    try {
      final Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
      unsafeConstructor.setAccessible(true);
      unsafe = unsafeConstructor.newInstance();

      // Alternative, but may not work across different JVMs.
      //      Field field = Unsafe.class.getDeclaredField("theUnsafe");
      //      field.setAccessible(true);
      //      unsafe = (Unsafe) field.get(null);

    } catch (final InstantiationException | IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException e) {
      e.printStackTrace();
      throw new RuntimeException("Unable to acquire Unsafe. " + e);
    }

    //4 on 32-bit systems. 4 on 64-bit systems < 32GB, otherwise 8.
    //This alone is not an indicator of compressed ref (coop)
    ADDRESS_SIZE = unsafe.addressSize();

    ARRAY_BOOLEAN_BASE_OFFSET = unsafe.arrayBaseOffset(boolean[].class);
    ARRAY_BYTE_BASE_OFFSET = unsafe.arrayBaseOffset(byte[].class);
    ARRAY_SHORT_BASE_OFFSET = unsafe.arrayBaseOffset(short[].class);
    ARRAY_CHAR_BASE_OFFSET = unsafe.arrayBaseOffset(char[].class);
    ARRAY_INT_BASE_OFFSET = unsafe.arrayBaseOffset(int[].class);
    ARRAY_LONG_BASE_OFFSET = unsafe.arrayBaseOffset(long[].class);
    ARRAY_FLOAT_BASE_OFFSET = unsafe.arrayBaseOffset(float[].class);
    ARRAY_DOUBLE_BASE_OFFSET = unsafe.arrayBaseOffset(double[].class);
    ARRAY_OBJECT_BASE_OFFSET = unsafe.arrayBaseOffset(Object[].class);

    ARRAY_OBJECT_INDEX_SCALE = unsafe.arrayIndexScale(Object[].class);
    OBJECT_SHIFT = ARRAY_OBJECT_INDEX_SCALE == 4 ? 2 : 3;

    final String jdkVer = System.getProperty("java.version");
    final int[] p = parseJavaVersion(jdkVer);
    JDK = p[0] + "." + p[1];
    JDK_MAJOR = (p[0] == 1) ? p[1] : p[0];
  }

  private UnsafeUtil() {}

  /**
   * Returns first two number groups of the java version string.
   * @param jdkVer the java version string from System.getProperty("java.version").
   * @return first two number groups of the java version string.
   */
  public static int[] parseJavaVersion(final String jdkVer) {
    final int p0, p1;
    try {
      String[] parts = jdkVer.trim().split("[^0-9\\.]");//grab only number groups and "."
      parts = parts[0].split("\\."); //split out the number groups
      p0 = Integer.parseInt(parts[0]); //the first number group
      p1 = (parts.length > 1) ? Integer.parseInt(parts[1]) : 0; //2nd number group, or 0
    } catch (final NumberFormatException | ArrayIndexOutOfBoundsException  e) {
      throw new IllegalArgumentException("Improper Java -version string: " + jdkVer + "\n" + e);
    }
    //checkJavaVersion(jdkVer, p0, p1); //TODO Optional to omit this.
    return new int[] {p0, p1};
  }

  public static void checkJavaVersion(final String jdkVer, final int p0, final int p1) {
    if ( (p0 < 1) || ((p0 == 1) && (p1 < 8)) || (p0 > 13)  ) {
      throw new IllegalArgumentException(
          "Unsupported JDK Major Version, must be one of 1.8, 8, 9, 10, 11, 12, 13: " + jdkVer);
    }
  }

  public static long getFieldOffset(final Class<?> c, final String fieldName) {
    try {
      return unsafe.objectFieldOffset(c.getDeclaredField(fieldName));
    } catch (final NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Like {@link Unsafe#arrayBaseOffset(Class)}, but caches return values for common array types.
   * Useful because calling {@link Unsafe#arrayBaseOffset(Class)} directly incurs more overhead.
   * @param c The given Class&lt;?&gt;.
   * @return the base-offset
   */
  public static long getArrayBaseOffset(final Class<?> c) {
    // Ordering here is roughly in order of what we expect to be most popular.
    if (c == byte[].class) {
      return ARRAY_BYTE_BASE_OFFSET;
    } else if (c == int[].class) {
      return ARRAY_INT_BASE_OFFSET;
    } else if (c == long[].class) {
      return ARRAY_LONG_BASE_OFFSET;
    } else if (c == float[].class) {
      return ARRAY_FLOAT_BASE_OFFSET;
    } else if (c == double[].class) {
      return ARRAY_DOUBLE_BASE_OFFSET;
    } else if (c == boolean[].class) {
      return ARRAY_BOOLEAN_BASE_OFFSET;
    } else if (c == short[].class) {
      return ARRAY_SHORT_BASE_OFFSET;
    } else if (c == char[].class) {
      return ARRAY_CHAR_BASE_OFFSET;
    } else if (c == Object[].class) {
      return ARRAY_OBJECT_BASE_OFFSET;
    } else {
      return unsafe.arrayBaseOffset(c);
    }
  }

  /**
   * Assert the requested offset and length against the allocated size.
   * The invariants equation is: {@code 0 <= reqOff <= reqLen <= reqOff + reqLen <= allocSize}.
   * If this equation is violated and assertions are enabled, an {@link AssertionError} will
   * be thrown.
   * @param reqOff the requested offset
   * @param reqLen the requested length
   * @param allocSize the allocated size.
   */
  public static void assertBounds(final long reqOff, final long reqLen, final long allocSize) {
    assert ((reqOff | reqLen | (reqOff + reqLen) | (allocSize - (reqOff + reqLen))) >= 0) :
      "reqOffset: " + reqOff + ", reqLength: " + reqLen
      + ", (reqOff + reqLen): " + (reqOff + reqLen) + ", allocSize: " + allocSize;
  }

  /**
   * Check the requested offset and length against the allocated size.
   * The invariants equation is: {@code 0 <= reqOff <= reqLen <= reqOff + reqLen <= allocSize}.
   * If this equation is violated an {@link IllegalArgumentException} will be thrown.
   * @param reqOff the requested offset
   * @param reqLen the requested length
   * @param allocSize the allocated size.
   */
  public static void checkBounds(final long reqOff, final long reqLen, final long allocSize) {
    if ((reqOff | reqLen | (reqOff + reqLen) | (allocSize - (reqOff + reqLen))) < 0) {
      throw new IllegalArgumentException(
          "reqOffset: " + reqOff + ", reqLength: " + reqLen
              + ", (reqOff + reqLen): " + (reqOff + reqLen) + ", allocSize: " + allocSize);
    }
  }
}
