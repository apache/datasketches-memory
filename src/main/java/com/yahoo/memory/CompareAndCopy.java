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
  static final long UNSAFE_COPY_MEMORY_THRESHOLD = 1024 * 1024;

  static int compare(
      final ResourceState thisState, final long thisOffsetBytes, final long thisLengthBytes,
      final ResourceState thatState, final long thatOffsetBytes, final long thatLengthBytes) {
    thisState.checkValid();
    checkBounds(thisOffsetBytes, thisLengthBytes, thisState.getCapacity());
    thatState.checkValid();
    checkBounds(thatOffsetBytes, thatLengthBytes, thatState.getCapacity());
    final long thisAdd = thisState.getCumBaseOffset() + thisOffsetBytes;
    final long thatAdd = thatState.getCumBaseOffset() + thatOffsetBytes;
    final Object thisObj = thisState.getUnsafeObject();
    final Object thatObj = thatState.getUnsafeObject();
    if ((thisObj != thatObj) || (thisAdd != thatAdd)) {
      final long lenBytes = Math.min(thisLengthBytes, thatLengthBytes);
      for (long i = 0; i < lenBytes; i++) {
        final int thisByte = unsafe.getByte(thisObj, thisAdd + i);
        final int thatByte = unsafe.getByte(thatObj, thatAdd + i);
        if (thisByte < thatByte) { return -1; }
        if (thisByte > thatByte) { return  1; }
      }
    }
    return Long.compare(thisLengthBytes, thatLengthBytes);
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
    if ((((srcAdd + lengthBytes) - dstAdd) <= 0) || (((dstAdd + lengthBytes) - srcAdd) <= 0)) {
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
      final long copy = Math.min(lengthBytes, UNSAFE_COPY_MEMORY_THRESHOLD);
      unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, copy);
      lengthBytes -= copy;
      srcAdd += copy;
      dstAdd += copy;
    }
  }
}
