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
  private static final Class<?> VM_CLASS;
  private static final Method VM_MAX_DIRECT_MEMORY_METHOD;
  private static final Method VM_IS_DIRECT_MEMORY_PAGE_ALIGNED_METHOD;

  private static final Class<?> NIO_BITS_CLASS;
  private static final Method NIO_BITS_RESERVE_MEMORY_METHOD;
  private static final Method NIO_BITS_UNRESERVE_MEMORY_METHOD;

  private static final AtomicLong nioBitsCount;
  private static final AtomicLong nioBitsReservedMemory;
  private static final AtomicLong nioBitsTotalCapacity;

  private static int pageSize = unsafe.pageSize();
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

      final Field countField = NIO_BITS_CLASS.getDeclaredField("count");
      countField.setAccessible(true);
      nioBitsCount = (AtomicLong) (countField.get(null));

      final Field reservedMemoryField = NIO_BITS_CLASS.getDeclaredField("reservedMemory");
      reservedMemoryField.setAccessible(true);
      nioBitsReservedMemory = (AtomicLong) (reservedMemoryField.get(null));

      final Field totalCapacityField = NIO_BITS_CLASS.getDeclaredField("totalCapacity");
      totalCapacityField.setAccessible(true);
      nioBitsTotalCapacity = (AtomicLong) (totalCapacityField.get(null));

    } catch (final Exception e) {
      throw new RuntimeException("Could not acquire java.nio.Bits class: " + e.getClass()
      + UnsafeUtil.tryIllegalAccessPermit);
    }
  }

  static long getDirectAllocationsCount() {
    try {
      final long count = nioBitsCount.get();
      return count;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read Bits.count " + e);
    }
  }

  static long getReservedMemory() {
    try {
      final long resMem = nioBitsReservedMemory.get();
      return resMem;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read Bits.reservedMemory " + e);
    }
  }

  static long getTotalCapacity() {
    try {
      final long resMem = nioBitsTotalCapacity.get();
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
    Util.zeroCheck(capacity, "capacity");
    try {
      while (capacity > (1L << 30)) {
        final long chunk = Math.min(capacity, (1L << 30)); // 1GB chunks
        NIO_BITS_RESERVE_MEMORY_METHOD.invoke(null, chunk, (int) chunk);
        capacity -= chunk;
      }

      if (capacity > 0) {
        NIO_BITS_RESERVE_MEMORY_METHOD.invoke(null, capacity, (int) capacity);
      }

      if (isPageAligned) {
        NIO_BITS_RESERVE_MEMORY_METHOD.invoke(null, pageSize, 0);
      }
    } catch (final Exception e) {
      throw new RuntimeException("Could not invoke java.nio.Bits.reserveMemory(...)" + e);
    }
  }

  static void unreserveMemory(long capacity) {
    Util.zeroCheck(capacity, "capacity");
    try {
      while (capacity > (1L << 30)) {
        final long chunk = Math.min(capacity, (1L << 30)); // 1GB chunks
        NIO_BITS_UNRESERVE_MEMORY_METHOD.invoke(null, chunk, (int) chunk);
        capacity -= chunk;
      }
      if (capacity > 0) {
        NIO_BITS_UNRESERVE_MEMORY_METHOD.invoke(null, capacity, (int) capacity);
      }

      if (isPageAligned) {
        NIO_BITS_UNRESERVE_MEMORY_METHOD.invoke(null, pageSize, 0);
      }

    } catch (final Exception e) {
      throw new RuntimeException("Could not invoke java.nio.Bits.unreserveMemory(...)" + e);
    }
  }

}
