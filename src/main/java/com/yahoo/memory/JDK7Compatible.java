/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

final class JDK7Compatible {

  private JDK7Compatible() {}

  public static long getAndAddLong(final Object obj, final long address, final long increment) {
    long retVal;
    do {
      retVal = UnsafeUtil.unsafe.getLongVolatile(obj, address);
    } while (!UnsafeUtil.unsafe.compareAndSwapLong(obj, address, retVal, retVal + increment));

    return retVal;
  }

  public static long getAndSetLong(final Object obj, final long address, final long value) {
    long retVal;
    do {
      retVal = UnsafeUtil.unsafe.getLongVolatile(obj, address);
    } while (!UnsafeUtil.unsafe.compareAndSwapLong(obj, address, retVal, value));

    return retVal;
  }
}
