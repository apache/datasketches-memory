/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

/**
 * @author Lee Rhodes
 */
final class CompareAndCopy {

  /**
   * Don't use {@link sun.misc.Unsafe#copyMemory} to copy blocks of memory larger than this
   * threshold, because internally it doesn't have safepoint polls, that may cause long
   * "Time To Safe Point" pauses in the application. This has been fixed in JDK 9 (see
   * https://bugs.openjdk.java.net/browse/JDK-8149596 and
   * https://bugs.openjdk.java.net/browse/JDK-8141491), but not in JDK 8, so the Memory library
   * should keep having this boilerplate as long as it supports Java 8.
   */
  static final int UNSAFE_COPY_MEMORY_THRESHOLD = 1024 * 1024;

  static int compare(
      final ResourceState state1, final long offsetBytes1, final long lengthBytes1,
      final ResourceState state2, final long offsetBytes2, final long lengthBytes2) {
    state1.checkValid();
    checkBounds(offsetBytes1, lengthBytes1, state1.getCapacity());
    state2.checkValid();
    checkBounds(offsetBytes2, lengthBytes2, state2.getCapacity());
    final long add1 = state1.getCumBaseOffset() + offsetBytes1;
    final long add2 = state2.getCumBaseOffset() + offsetBytes2;
    final Object arr1 = state1.getUnsafeObject();
    final Object arr2 = state2.getUnsafeObject();
    if ((arr1 != arr2) || (add1 != add2)) {
      final long lenBytes = Math.min(lengthBytes1, lengthBytes2);
      for (long i = 0; i < lenBytes; i++) {
        final int byte1 = unsafe.getByte(arr1, add1 + i);
        final int byte2 = unsafe.getByte(arr2, add2 + i);
        if (byte1 < byte2) { return -1; }
        if (byte1 > byte2) { return  1; }
      }
    }
    return Long.compare(lengthBytes1, lengthBytes2);
  }

  static void copy(final ResourceState srcState, final long srcOffsetBytes,
      final ResourceState dstState, final long dstOffsetBytes, final long lengthBytes) {
    srcState.checkValid();
    checkBounds(srcOffsetBytes, lengthBytes, srcState.getCapacity());
    dstState.checkValid();
    checkBounds(dstOffsetBytes, lengthBytes, dstState.getCapacity());
    final long srcAdd = srcState.getCumBaseOffset() + srcOffsetBytes;
    final long dstAdd = dstState.getCumBaseOffset() + dstOffsetBytes;
    copyMemory(srcState.getUnsafeObject(), srcAdd, dstState.getUnsafeObject(), dstAdd,
        lengthBytes);
  }

  //Used by all of the get/put array methods in Buffer and Memory classes
  static final void copyMemoryCheckingDifferentObject(final Object srcUnsafeObj,
      final long srcAdd, final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if (srcUnsafeObj != dstUnsafeObj) {
      copyNonOverlappingMemoryWithChunking(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd,
          lengthBytes);
    } else {
      throw new IllegalArgumentException("Not expecting to copy to/from array which is the "
          + "underlying object of the memory at the same time");
    }
  }

  static boolean equals(final ResourceState state1, final ResourceState state2) {
    final long cap1 = state1.getCapacity();
    final long cap2 = state2.getCapacity();
    return (cap1 == cap2) && equals(state1, 0, state2, 0, cap1);
  }

  static boolean equals(final ResourceState state1, final long off1, final ResourceState state2,
      final long off2, long lenBytes) {
    state1.checkValid();
    state2.checkValid();
    if (state1 == state2) { return true; }
    final long cap1 = state1.getCapacity();
    final long cap2 = state2.getCapacity();
    checkBounds(off1, lenBytes, cap1);
    checkBounds(off2, lenBytes, cap2);
    long cumOff1 = state1.getCumBaseOffset() + off1;
    long cumOff2 = state2.getCumBaseOffset() + off2;
    final Object arr1 = state1.getUnsafeObject(); //could be null
    final Object arr2 = state2.getUnsafeObject(); //could be null
    if ((arr1 == arr2) && (cumOff1 == cumOff2)) { return true; }

    while (lenBytes >= Long.BYTES) {
      final int chunk = (int) Math.min(lenBytes, UNSAFE_COPY_MEMORY_THRESHOLD);
      // int-counted loop to avoid safepoint polls (otherwise why we chunk by
      // UNSAFE_COPY_MEMORY_THRESHOLD)
      int i = 0;
      for (; i <= (chunk - Long.BYTES); i += Long.BYTES) {
        final long v1 = unsafe.getLong(arr1, cumOff1 + i);
        final long v2 = unsafe.getLong(arr2, cumOff2 + i);
        if (v1 == v2) { continue; }
        else { return false; }
      }
      lenBytes -= i;
      cumOff1 += i;
      cumOff2 += i;
    }
    //check the remainder bytes, if any
    return (lenBytes == 0) ? true : equalsByBytes(arr1, cumOff1, arr2, cumOff2, (int) lenBytes);
  }

  //use only for short runs
  private static boolean equalsByBytes(final Object arr1, final long cumOff1, final Object arr2,
      final long cumOff2, final int lenBytes) {
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lenBytes; i++) {
      final int v1 = unsafe.getByte(arr1, cumOff1 + i);
      final int v2 = unsafe.getByte(arr2, cumOff2 + i);
      if (v1 == v2) { continue; }
      else { return false; }
    }
    return true;
  }

  //only valid and bounds checks have been performed at this point
  private static void copyMemory(final Object srcUnsafeObj, final long srcAdd,
      final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if (srcUnsafeObj != dstUnsafeObj) {
      //either srcArray != dstArray OR one of them is off-heap
      copyNonOverlappingMemoryWithChunking(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd,
          lengthBytes);
    } else { //either srcArray == dstArray OR both src and dst are off-heap
      copyMemoryOverlapAddressCheck(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
    }
  }

  /**
   * At this point either srcArray == dstArray OR both src and dst are off-heap.
   * Performs overlapping address check. If addresses do not overlap, proceed to
   * {@link #copyNonOverlappingMemoryWithChunking(Object, long, Object, long, long)}; otherwise
   * fall back on <i>Unsafe.copyMemory(...)</i> tolerating potentially long
   * Time to Safe Point pauses.
   * If srcAdd == dstAdd an exception will be thrown.
   * @param srcUnsafeObj The source array object, it may be null.
   * @param srcAdd The cumulative source offset
   * @param dstUnsafeObj The destination array object, it may be null
   * @param dstAdd The cumulative destination offset
   * @param lengthBytes The length to be copied in bytes
   */
  private static void copyMemoryOverlapAddressCheck(final Object srcUnsafeObj, final long srcAdd,
      final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if (((srcAdd + lengthBytes) <= dstAdd) || ((dstAdd + lengthBytes) <= srcAdd)) {
      copyNonOverlappingMemoryWithChunking(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd,
          lengthBytes);
      return;
    }
    if (srcAdd == dstAdd) {
      throw new IllegalArgumentException(
          "Attempt to copy a block of memory exactly in-place, should be a bug");
    }
    // If regions do overlap, fall back to unsafe.copyMemory, tolerating potentially long
    // Time to Safe Point pauses.
    unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
  }

  /**
   * This copies only non-overlapping memory in chunks to avoid safepoint delays.
   * Java 9 may not require the chunking.
   * @param srcUnsafeObj The source array object, it may be null.
   * @param srcAdd The cumulative source offset
   * @param dstUnsafeObj The destination array object, it may be null
   * @param dstAdd The cumulative destination offset
   * @param lengthBytes The length to be copied in bytes
   * @see #UNSAFE_COPY_MEMORY_THRESHOLD
   */
  private static void copyNonOverlappingMemoryWithChunking(final Object srcUnsafeObj,
      long srcAdd, final Object dstUnsafeObj, long dstAdd, long lengthBytes) {
    while (lengthBytes > 0) {
      final long chunk = Math.min(lengthBytes, UNSAFE_COPY_MEMORY_THRESHOLD);
      unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, chunk);
      lengthBytes -= chunk;
      srcAdd += chunk;
      dstAdd += chunk;
    }
  }
}
