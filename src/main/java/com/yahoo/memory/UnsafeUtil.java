/*
 * Copyright 2015-16, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import sun.misc.Unsafe;

/**
 * Provides access to the sun.misc.Unsafe class and its key static fields.
 *
 * <p>The internal static initializer also detects whether the methods unique to the Unsafe class
 * in JDK8 are present; if not, methods that are compatible with JDK7 are substituted using an
 * internal interface.  In order for this to work with jdk7, this library must be compiled using
 * jdk8 and it must be done with both source and target versions of jdk7 specified in pom.xml.
 * The resultant jar will work on jdk7 and jdk8.</p>
 *
 * <p>This may work with jdk9 but might require the JVM arg <i>-permit-illegal-access</i>,
 * <i>–illegal-access=permit</i> or equivalent. Proper operation with jdk9 or above is not
 * guaranteed and has not been tested.
 *
 * @author Lee Rhodes
 */
public final class UnsafeUtil {
  public static final Unsafe unsafe;
  public static final double JDK;
  static final JDKCompatibility compatibilityMethods;

  //not an indicator of whether compressed references are used.
  public static final int ADDRESS_SIZE;

  //For 64-bit JVMs: varies depending on coop: 16 for JVM <= 32GB; 24 for JVM > 32GB
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

  static String tryIllegalAccessPermit = " Try setting JVM arg -permit-illegal-access, "
      + "–illegal-access=permit or equivalent.";

  static {
    try {
      //should work across JVMs, e.g., with Android:
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
      throw new RuntimeException("Unable to acquire Unsafe. " + tryIllegalAccessPermit, e);
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

    JDK = majorJavaVersion(System.getProperty("java.version"));
    if (JDK == 1.7) {
      compatibilityMethods = new JDK7Compatible(unsafe);
    } else {
      compatibilityMethods = new JDK8Compatible(unsafe);
    }
  }

  static double majorJavaVersion(final String jdkVer) { //avail for test
    //double version = 0;
    try {
      final String[] parts = jdkVer.trim().split("[^0-9\\.]")[0].split("\\.");
      final String num = parts[0] + "." + ((parts.length > 1) ? parts[1] : "0");
      final double ver = Double.parseDouble(num);
      if (ver < 1.7) {
        throw new ExceptionInInitializerError("JDK Major Version must be >= 1.7");
      }
      return ver;
    } catch (final Exception e) {
      throw new ExceptionInInitializerError("Improper Java -version string: "
          + jdkVer + "\n" + e);
    }
  }

  private UnsafeUtil() {}

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

  interface JDKCompatibility {

    long getAndAddLong(Object obj, long address, long increment);

    long getAndSetLong(Object obj, long address, long value);
  }

  private static class JDK8Compatible implements JDKCompatibility {
    private final Unsafe myUnsafe;

    JDK8Compatible(final Unsafe unsafe) {
      myUnsafe = unsafe;
    }

    @Override
    public long getAndAddLong(final Object obj, final long address, final long increment) {
      return myUnsafe.getAndAddLong(obj, address, increment);
    }

    @Override
    public long getAndSetLong(final Object obj, final long address, final long value) {
      return myUnsafe.getAndSetLong(obj, address, value);
    }
  }

  private static class JDK7Compatible implements JDKCompatibility {
    private final Unsafe myUnsafe;

    JDK7Compatible(final Unsafe unsafe) {
      myUnsafe = unsafe;
    }

    @Override
    public synchronized long getAndAddLong(final Object obj, final long address, final long increment) {
      long retVal;
      do {
        retVal = myUnsafe.getLongVolatile(obj, address);
      } while (!myUnsafe.compareAndSwapLong(obj, address, retVal, retVal + increment));

      return myUnsafe.getLongVolatile(obj, address);
    }

    @Override
    public synchronized long getAndSetLong(final Object obj, final long address, final long value) {
      long retVal;
      do {
        retVal = myUnsafe.getLongVolatile(obj, address);
      } while (!myUnsafe.compareAndSwapLong(obj, address, retVal, value));

      return myUnsafe.getLongVolatile(obj, address);
    }
  }

}
