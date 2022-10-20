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

import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Provide linkage to java.nio.Bits.
 *
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
final class NioBits {
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
      isPageAligned = VirtualMachineMemory.getIsPageAligned();
      maxDBBMemory = VirtualMachineMemory.getMaxDBBMemory();

      NIO_BITS_CLASS = Class.forName("java.nio.Bits");

      NIO_BITS_RESERVE_MEMORY_METHOD = NIO_BITS_CLASS
          .getDeclaredMethod("reserveMemory", long.class, int.class); //JD16 requires (long, long)
      NIO_BITS_RESERVE_MEMORY_METHOD.setAccessible(true);

      NIO_BITS_UNRESERVE_MEMORY_METHOD = NIO_BITS_CLASS
          .getDeclaredMethod("unreserveMemory", long.class, int.class); //JD16 requires (long, long)
      NIO_BITS_UNRESERVE_MEMORY_METHOD.setAccessible(true);

      final Field countField = NIO_BITS_CLASS.getDeclaredField(NioBitsFields.COUNT_FIELD_NAME);
      countField.setAccessible(true);
      nioBitsCount = (AtomicLong) (countField.get(null));

      final Field reservedMemoryField = NIO_BITS_CLASS.getDeclaredField(NioBitsFields.RESERVED_MEMORY_FIELD_NAME);
      reservedMemoryField.setAccessible(true);
      nioBitsReservedMemory = (AtomicLong) (reservedMemoryField.get(null));

      final Field totalCapacityField = NIO_BITS_CLASS.getDeclaredField(NioBitsFields.TOTAL_CAPACITY_FIELD_NAME);
      totalCapacityField.setAccessible(true);
      nioBitsTotalCapacity = (AtomicLong) (totalCapacityField.get(null));

    } catch (final ClassNotFoundException | NoSuchMethodException |  IllegalAccessException
        | IllegalArgumentException | SecurityException |  NoSuchFieldException e) {
      throw new RuntimeException("Could not acquire java.nio.Bits class: " + e.getClass());
    }
  }

  private NioBits() { }

  static long getDirectAllocationsCount() { //tested via reflection
    return nioBitsCount.get();
  }

  static long getReservedMemory() { //tested via reflection
    return nioBitsReservedMemory.get();
  }

  static long getTotalCapacity() { //tested via reflection
    return nioBitsTotalCapacity.get();
  }

  static int pageSize() {
    return pageSize;
  }

  static int pageCount(final long bytes) {
    return (int)((bytes + pageSize()) - 1L) / pageSize();
  }

  static long getMaxDirectByteBufferMemory() { //tested via reflection
    return maxDBBMemory;
  }

  static boolean isPageAligned() {
    return isPageAligned;
  }

  //RESERVE & UNRESERVE BITS MEMORY TRACKING COUNTERS
  // Comment from java.nio.Bits.java ~ line 705:
  // -XX:MaxDirectMemorySize limits the total capacity rather than the
  // actual memory usage, which will differ when buffers are page aligned.
  static void reserveMemory(final long allocationSize, final long capacity) {
    reserveUnreserve(allocationSize, capacity, NIO_BITS_RESERVE_MEMORY_METHOD);
  }

  static void unreserveMemory(final long allocationSize, final long capacity) {
    reserveUnreserve(allocationSize, capacity, NIO_BITS_UNRESERVE_MEMORY_METHOD);
  }

  private static void reserveUnreserve(long allocationSize, long capacity, final Method method) {
    Util.zeroCheck(capacity, "capacity");
    // 1GB is a pretty "safe" limit.
    final long chunkSizeLimit = 1L << 30;
    try {
      while (capacity > 0) {
        final long chunk = Math.min(capacity, chunkSizeLimit);
        if (capacity == chunk) {
          method.invoke(null, allocationSize, (int) capacity); //JDK 16 remove cast to int
        } else {
          method.invoke(null, chunk, (int) chunk); //JDK 16 remove cast to int
        }
        capacity -= chunk;
        allocationSize -= chunk;
      }
    } catch (final IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      throw new RuntimeException(
          "Could not invoke java.nio.Bits.unreserveMemory(...) OR java.nio.Bits.reserveMemory(...): "
          + "allocationSize = " + allocationSize + ", capacity = " + capacity, e);
    }
  }
}
