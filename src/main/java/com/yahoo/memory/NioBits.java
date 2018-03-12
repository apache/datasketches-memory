/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provide linkage to java.nio.Bits.
 *
 * @author Lee Rhodes
 */
class NioBits {
  static final Class<?> VM_CLASS;
  static final Method VM_MAX_DIRECT_MEMORY_METHOD;
  static final Method VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD;

  static final Class<?> NIO_BITS_CLASS;
  static final Method NIO_BITS_RESERVE_MEMORY_METHOD;
  static final Method NIO_BITS_UNRESERVE_MEMORY_METHOD;
  static final Field NIO_BITS_COUNT_FIELD;
  static final Field NIO_BITS_RESERVED_MEMORY_FIELD;
  static final Field NIO_BITS_TOTAL_CAPACITY_FIELD;


  private static int pageSize = -1;
  private static final long maxDBBMemory;
  private static final boolean isPageAligned;

  static {
    try {
      VM_CLASS = Class.forName("sun.misc.VM");
      VM_MAX_DIRECT_MEMORY_METHOD =
          VM_CLASS.getDeclaredMethod("maxDirectMemory");
      VM_MAX_DIRECT_MEMORY_METHOD.setAccessible(true);
      maxDBBMemory = (long)VM_MAX_DIRECT_MEMORY_METHOD
          .invoke(null); //static method

      VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD =
          VM_CLASS.getDeclaredMethod("isDirectMemoryPageAligned");
      VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD.setAccessible(true);
      isPageAligned = (boolean)VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD
          .invoke(null); //static method

      NIO_BITS_CLASS = Class.forName("java.nio.Bits");

      NIO_BITS_RESERVE_MEMORY_METHOD = NIO_BITS_CLASS
          .getDeclaredMethod("reserveMemory", long.class, int.class);
      NIO_BITS_RESERVE_MEMORY_METHOD.setAccessible(true);

      NIO_BITS_UNRESERVE_MEMORY_METHOD = NIO_BITS_CLASS
          .getDeclaredMethod("unreserveMemory", long.class, int.class);
      NIO_BITS_UNRESERVE_MEMORY_METHOD.setAccessible(true);

      NIO_BITS_COUNT_FIELD = NIO_BITS_CLASS.getDeclaredField("count");
      NIO_BITS_COUNT_FIELD.setAccessible(true);

      NIO_BITS_RESERVED_MEMORY_FIELD = NIO_BITS_CLASS.getDeclaredField("reservedMemory");
      NIO_BITS_RESERVED_MEMORY_FIELD.setAccessible(true);

      NIO_BITS_TOTAL_CAPACITY_FIELD = NIO_BITS_CLASS.getDeclaredField("totalCapacity");
      NIO_BITS_TOTAL_CAPACITY_FIELD.setAccessible(true);

    } catch (final Exception e) {
      throw new RuntimeException("Could not acquire java.nio.Bits class: " + e.getClass()
      + UnsafeUtil.tryIllegalAccessPermit);
    }
  }

  static long getCount() {
    try {
      final long count = ((AtomicLong)(NIO_BITS_COUNT_FIELD.get(null))).get();
      return count;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read Bits.count " + e);
    }
  }

  static long getReservedMemory() {
    try {
      final long resMem = ((AtomicLong)(NIO_BITS_RESERVED_MEMORY_FIELD.get(null))).get();
      return resMem;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read Bits.reservedMemory " + e);
    }
  }

  static long getTotalCapacity() {
    try {
      final long resMem = ((AtomicLong)(NIO_BITS_TOTAL_CAPACITY_FIELD.get(null))).get();
      return resMem;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read Bits.totalCapacity " + e);
    }
  }

  static int pageSize() {
    if (pageSize == -1) {
      pageSize = unsafe.pageSize();
    }
    return pageSize;
  }

  static int pageCount(final long bytes) {
    return (int)((bytes + pageSize()) - 1L) / pageSize();
  }

  static long getMaxDirectByteBufferMemory() {
    return maxDBBMemory;
  }

  static boolean isPageAligned() {
    return isPageAligned;
  }

  //RESERVE & UNRESERVE BITS MEMORY TRACKING COUNTERS
  static void reserveMemory(long capacity) {
    final long pageAdj = isPageAligned ? pageSize : 0;
    try {
      //if pageAligned, and if capacity > 2GB, this inflates the reserved size by 1 page per 2GB
      // or about 2ppm.
      while (capacity > Integer.MAX_VALUE) {
        final int chunk = (int) Math.min(capacity, Integer.MAX_VALUE); //(2GB-1) chunks
        final long size = Math.max(1L,  chunk + pageAdj);
        NIO_BITS_RESERVE_MEMORY_METHOD.invoke(null, size, chunk);
        capacity -= chunk;
      }
      if (capacity > 0) {
        final long size = Math.max(1L, capacity + pageAdj);
        NIO_BITS_RESERVE_MEMORY_METHOD.invoke(null, size, (int) capacity);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Could not invoke java.nio.Bits.reserveMemory(...)" + e);
    }
  }

  static void unreserveMemory(long capacity) {
    final long pageAdj = isPageAligned ? pageSize : 0;
    try {
      while (capacity > Integer.MAX_VALUE) {
        final int chunk = (int) Math.min(capacity, Integer.MAX_VALUE); //(2GB-1) chunks
        final long size = Math.max(1L,  chunk + pageAdj);
        NIO_BITS_UNRESERVE_MEMORY_METHOD.invoke(null, size, chunk);
        capacity -= chunk;
      }
      if (capacity > 0) {
        final long size = Math.max(1L, capacity + pageAdj);
        NIO_BITS_UNRESERVE_MEMORY_METHOD.invoke(null, size, (int) capacity);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Could not invoke java.nio.Bits.unreserveMemory(...)" + e);
    }
  }

}
