/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
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
   *
   * <p>A reference to this can be found in {@link java.nio.Bits}.</p>
   */
  static final int UNSAFE_COPY_THRESHOLD_BYTES = 1024 * 1024;

  private CompareAndCopy() { }

  static int compare(
      final ResourceState state1, final long offsetBytes1, final long lengthBytes1,
      final ResourceState state2, final long offsetBytes2, final long lengthBytes2) {
    state1.checkValid();
    checkBounds(offsetBytes1, lengthBytes1, state1.getCapacity());
    state2.checkValid();
    checkBounds(offsetBytes2, lengthBytes2, state2.getCapacity());
    final long cumOff1 = state1.getCumulativeOffset() + offsetBytes1;
    final long cumOff2 = state2.getCumulativeOffset() + offsetBytes2;
    final Object arr1 = state1.getUnsafeObject();
    final Object arr2 = state2.getUnsafeObject();
    if ((arr1 != arr2) || (cumOff1 != cumOff2)) {
      final long lenBytes = Math.min(lengthBytes1, lengthBytes2);
      for (long i = 0; i < lenBytes; i++) {
        final int byte1 = unsafe.getByte(arr1, cumOff1 + i);
        final int byte2 = unsafe.getByte(arr2, cumOff2 + i);
        if (byte1 < byte2) { return -1; }
        if (byte1 > byte2) { return  1; }
      }
    }
    return Long.compare(lengthBytes1, lengthBytes2);
  }

  static boolean equals(final ResourceState state1, final ResourceState state2) {
    final long cap1 = state1.getCapacity();
    final long cap2 = state2.getCapacity();
    return (cap1 == cap2) && equals(state1, 0, state2, 0, cap1);
  }

  //Developer notes: this is subtlely different from (campare == 0) in that this has an early
  // stop if the arrays and offsets are the same as there is only one length.  Also this can take
  // advantage of chunking with longs, while compare cannot.
  static boolean equals(
      final ResourceState state1, final long offsetBytes1,
      final ResourceState state2, final long offsetBytes2, long lengthBytes) {
    state1.checkValid();
    checkBounds(offsetBytes1, lengthBytes, state1.getCapacity());
    state2.checkValid();
    checkBounds(offsetBytes2, lengthBytes, state2.getCapacity());
    long cumOff1 = state1.getCumulativeOffset() + offsetBytes1;
    long cumOff2 = state2.getCumulativeOffset() + offsetBytes2;
    final Object arr1 = state1.getUnsafeObject(); //could be null
    final Object arr2 = state2.getUnsafeObject(); //could be null
    if ((arr1 == arr2) && (cumOff1 == cumOff2)) { return true; }

    while (lengthBytes >= Long.BYTES) {
      final int chunk = (int) Math.min(lengthBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      // int-counted loop to avoid safepoint polls (otherwise why we chunk by
      // UNSAFE_COPY_MEMORY_THRESHOLD)
      int i = 0;
      for (; i <= (chunk - Long.BYTES); i += Long.BYTES) {
        final long v1 = unsafe.getLong(arr1, cumOff1 + i);
        final long v2 = unsafe.getLong(arr2, cumOff2 + i);
        if (v1 != v2) { return false; }
      }
      lengthBytes -= i;
      cumOff1 += i;
      cumOff2 += i;
    }
    //check the remainder bytes, if any
    return (lengthBytes == 0) || equalsByBytes(arr1, cumOff1, arr2, cumOff2, (int) lengthBytes);
  }

  //use only for short runs
  private static boolean equalsByBytes(final Object arr1, final long cumOff1, final Object arr2,
      final long cumOff2, final int lenBytes) {
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lenBytes; i++) {
      final int v1 = unsafe.getByte(arr1, cumOff1 + i);
      final int v2 = unsafe.getByte(arr2, cumOff2 + i);
      if (v1 != v2) { return false; }
    }
    return true;
  }

  static int hashCode(final ResourceState state) {
    state.checkValid();
    long lenBytes = state.getCapacity();
    long cumOff = state.getCumulativeOffset();
    final Object arr = state.getUnsafeObject(); //could be null
    int result = 1;
    while (lenBytes >= Long.BYTES) {
      final int chunk = (int) Math.min(lenBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      // int-counted loop to avoid safepoint polls (otherwise why we chunk by
      // UNSAFE_COPY_MEMORY_THRESHOLD)
      int i = 0;

      for (; i <= (chunk - Long.BYTES); i += Long.BYTES) {
        final long v = unsafe.getLong(arr, cumOff + i);
        final int vHash = (int) (v ^ (v >>> 32));
        result = (31 * result) + vHash;
      }
      lenBytes -= i;
      cumOff += i;
    }
    //hash the remainder bytes, if any, as a long
    if (lenBytes == 0) { return result; }
    long v = 0;
    for (int i = 0; i < lenBytes; i++) {
      v |= (unsafe.getByte(arr, cumOff + i) & 0XFFL) << (i << 3);
    }
    final int vHash = (int) (v ^ (v >>> 32));
    return (31 * result) + vHash;
  }

  static void copy(final ResourceState srcState, final long srcOffsetBytes,
      final ResourceState dstState, final long dstOffsetBytes, final long lengthBytes) {
    srcState.checkValid();
    checkBounds(srcOffsetBytes, lengthBytes, srcState.getCapacity());
    dstState.checkValid();
    checkBounds(dstOffsetBytes, lengthBytes, dstState.getCapacity());
    final long srcAdd = srcState.getCumulativeOffset() + srcOffsetBytes;
    final long dstAdd = dstState.getCumulativeOffset() + dstOffsetBytes;
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
   * @see #UNSAFE_COPY_THRESHOLD_BYTES
   */
  private static void copyNonOverlappingMemoryWithChunking(final Object srcUnsafeObj,
      long srcAdd, final Object dstUnsafeObj, long dstAdd, long lengthBytes) {
    while (lengthBytes > 0) {
      final long chunk = Math.min(lengthBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, chunk);
      lengthBytes -= chunk;
      srcAdd += chunk;
      dstAdd += chunk;
    }
  }

  static void getNonNativeChars(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final char[] dstArray, int dstOffsetChars,
      int lengthChars) {
    checkBounds(dstOffsetChars, lengthChars, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkChars = (int) (chunkBytes >> CHAR_SHIFT);
      getCharArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetChars, chunkChars);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetChars += chunkChars;
      copyBytes -= chunkBytes;
      lengthChars -= chunkChars;
    }
    getCharArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetChars, lengthChars);
  }

  private static void getCharArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthChars; i++) {
      dstArray[dstOffsetChars + i] = Character.reverseBytes(
          unsafe.getChar(unsafeObj, cumulativeOffsetBytes + (((long) i) << CHAR_SHIFT)));
    }
  }

  static void getNonNativeDoubles(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final double[] dstArray, int dstOffsetDoubles,
      int lengthDoubles) {
    checkBounds(dstOffsetDoubles, lengthDoubles, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkDoubles = (int) (chunkBytes >> DOUBLE_SHIFT);
      getDoubleArrayChunk(unsafeObj, cumulativeOffsetBytes,
          dstArray, dstOffsetDoubles, chunkDoubles);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetDoubles += chunkDoubles;
      copyBytes -= chunkBytes;
      lengthDoubles -= chunkDoubles;
    }
    getDoubleArrayChunk(unsafeObj, cumulativeOffsetBytes,
        dstArray, dstOffsetDoubles, lengthDoubles);
  }

  private static void getDoubleArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthDoubles; i++) {
      dstArray[dstOffsetDoubles + i] = Double.longBitsToDouble(Long.reverseBytes(
          unsafe.getLong(unsafeObj, cumulativeOffsetBytes + (((long) i) << DOUBLE_SHIFT))));
    }
  }

  static void getNonNativeFloats(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final float[] dstArray, int dstOffsetFloats,
      int lengthFloats) {
    checkBounds(dstOffsetFloats, lengthFloats, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkFloats = (int) (chunkBytes >> FLOAT_SHIFT);
      getFloatArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetFloats, chunkFloats);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetFloats += chunkFloats;
      copyBytes -= chunkBytes;
      lengthFloats -= chunkFloats;
    }
    getFloatArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetFloats, lengthFloats);
  }

  private static void getFloatArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthFloats; i++) {
      dstArray[dstOffsetFloats + i] = Float.intBitsToFloat(Integer.reverseBytes(
          unsafe.getInt(unsafeObj, cumulativeOffsetBytes + (((long) i) << FLOAT_SHIFT))));
    }
  }

  static void getNonNativeInts(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final int[] dstArray, int dstOffsetInts,
      int lengthInts) {
    checkBounds(dstOffsetInts, lengthInts, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkInts = (int) (chunkBytes >> INT_SHIFT);
      getIntArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetInts, chunkInts);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetInts += chunkInts;
      copyBytes -= chunkBytes;
      lengthInts -= chunkInts;
    }
    getIntArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetInts, lengthInts);
  }

  private static void getIntArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthInts; i++) {
      dstArray[dstOffsetInts + i] = Integer.reverseBytes(
          unsafe.getInt(unsafeObj, cumulativeOffsetBytes + (((long) i) << INT_SHIFT)));
    }
  }

  static void getNonNativeLongs(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final long[] dstArray, int dstOffsetLongs,
      int lengthLongs) {
    checkBounds(dstOffsetLongs, lengthLongs, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkLongs = (int) (chunkBytes >> LONG_SHIFT);
      getLongArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetLongs, chunkLongs);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetLongs += chunkLongs;
      copyBytes -= chunkBytes;
      lengthLongs -= chunkLongs;
    }
    getLongArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetLongs, lengthLongs);
  }

  private static void getLongArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthLongs; i++) {
      dstArray[dstOffsetLongs + i] = Long.reverseBytes(
          unsafe.getLong(unsafeObj, cumulativeOffsetBytes + (((long) i) << LONG_SHIFT)));
    }
  }

  static void getNonNativeShorts(final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes, long copyBytes, final short[] dstArray, int dstOffsetShorts,
      int lengthShorts) {
    checkBounds(dstOffsetShorts, lengthShorts, dstArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkShorts = (int) (chunkBytes >> SHORT_SHIFT);
      getShortArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetShorts, chunkShorts);
      cumulativeOffsetBytes += chunkBytes;
      dstOffsetShorts += chunkShorts;
      copyBytes -= chunkBytes;
      lengthShorts -= chunkShorts;
    }
    getShortArrayChunk(unsafeObj, cumulativeOffsetBytes, dstArray, dstOffsetShorts, lengthShorts);
  }

  private static void getShortArrayChunk(final Object unsafeObj, final long cumulativeOffsetBytes,
      final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthShorts; i++) {
      dstArray[dstOffsetShorts + i] = Short.reverseBytes(
          unsafe.getShort(unsafeObj, cumulativeOffsetBytes + (((long) i) << SHORT_SHIFT)));
    }
  }

  static void putNonNativeChars(final char[] srcArray, int srcOffsetChars, int lengthChars,
      long copyBytes, final Object unsafeObj, final long cumBaseOffset, final long offsetBytes) {
    checkBounds(srcOffsetChars, lengthChars, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkChars = (int) (chunkBytes >> CHAR_SHIFT);
      putCharArrayChunk(srcArray, srcOffsetChars, chunkChars, unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetChars += chunkChars;
      copyBytes -= chunkBytes;
      lengthChars -= chunkChars;
    }
    putCharArrayChunk(srcArray, srcOffsetChars, lengthChars, unsafeObj, cumulativeOffsetBytes);
  }

  private static void putCharArrayChunk(final char[] srcArray, final int srcOffsetChars,
      final int lengthChars, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthChars; i++) {
      unsafe.putChar(unsafeObj, cumulativeOffsetBytes + (((long) i) << CHAR_SHIFT),
          Character.reverseBytes(srcArray[srcOffsetChars + i]));
    }
  }

  static void putNonNativeDoubles(final double[] srcArray, int srcOffsetDoubles,
      int lengthDoubles, long copyBytes, final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes) {
    checkBounds(srcOffsetDoubles, lengthDoubles, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkDoubles = (int) (chunkBytes >> DOUBLE_SHIFT);
      putDoubleArrayChunk(srcArray, srcOffsetDoubles, chunkDoubles,
          unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetDoubles += chunkDoubles;
      copyBytes -= chunkBytes;
      lengthDoubles -= chunkDoubles;
    }
    putDoubleArrayChunk(srcArray, srcOffsetDoubles, lengthDoubles,
        unsafeObj, cumulativeOffsetBytes);
  }

  private static void putDoubleArrayChunk(final double[] srcArray, final int srcOffsetDoubles,
      final int lengthDoubles, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthDoubles; i++) {
      unsafe.putLong(unsafeObj, cumulativeOffsetBytes + (((long) i) << DOUBLE_SHIFT),
          Long.reverseBytes(Double.doubleToRawLongBits(srcArray[srcOffsetDoubles + i])));
    }
  }

  static void putNonNativeFloats(final float[] srcArray, int srcOffsetFloats,
      int lengthFloats, long copyBytes, final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes) {
    checkBounds(srcOffsetFloats, lengthFloats, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkFloats = (int) (chunkBytes >> FLOAT_SHIFT);
      putFloatArrayChunk(srcArray, srcOffsetFloats, chunkFloats, unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetFloats += chunkFloats;
      copyBytes -= chunkBytes;
      lengthFloats -= chunkFloats;
    }
    putFloatArrayChunk(srcArray, srcOffsetFloats, lengthFloats, unsafeObj, cumulativeOffsetBytes);
  }

  private static void putFloatArrayChunk(final float[] srcArray, final int srcOffsetFloats,
      final int lengthFloats, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthFloats; i++) {
      unsafe.putInt(unsafeObj, cumulativeOffsetBytes + (((long) i) << FLOAT_SHIFT),
          Integer.reverseBytes(Float.floatToRawIntBits(srcArray[srcOffsetFloats + i])));
    }
  }

  static void putNonNativeInts(final int[] srcArray, int srcOffsetInts, int lengthInts,
      long copyBytes, final Object unsafeObj, final long cumBaseOffset, final long offsetBytes) {
    checkBounds(srcOffsetInts, lengthInts, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkInts = (int) (chunkBytes >> INT_SHIFT);
      putIntArrayChunk(srcArray, srcOffsetInts, chunkInts, unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetInts += chunkInts;
      copyBytes -= chunkBytes;
      lengthInts -= chunkInts;
    }
    putIntArrayChunk(srcArray, srcOffsetInts, lengthInts, unsafeObj, cumulativeOffsetBytes);
  }

  private static void putIntArrayChunk(final int[] srcArray, final int srcOffsetInts,
      final int lengthInts, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthInts; i++) {
      unsafe.putInt(unsafeObj, cumulativeOffsetBytes + (((long) i) << INT_SHIFT),
          Integer.reverseBytes(srcArray[srcOffsetInts + i]));
    }
  }

  static void putNonNativeLongs(final long[] srcArray, int srcOffsetLongs, int lengthLongs,
      long copyBytes, final Object unsafeObj, final long cumBaseOffset, final long offsetBytes) {
    checkBounds(srcOffsetLongs, lengthLongs, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkLongs = (int) (chunkBytes >> LONG_SHIFT);
      putLongArrayChunk(srcArray, srcOffsetLongs, chunkLongs, unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetLongs += chunkLongs;
      copyBytes -= chunkBytes;
      lengthLongs -= chunkLongs;
    }
    putLongArrayChunk(srcArray, srcOffsetLongs, lengthLongs, unsafeObj, cumulativeOffsetBytes);
  }

  private static void putLongArrayChunk(final long[] srcArray, final int srcOffsetLongs,
      final int lengthLongs, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthLongs; i++) {
      unsafe.putLong(unsafeObj, cumulativeOffsetBytes + (((long) i) << LONG_SHIFT),
          Long.reverseBytes(srcArray[srcOffsetLongs + i]));
    }
  }

  static void putNonNativeShorts(final short[] srcArray, int srcOffsetShorts,
      int lengthShorts, long copyBytes, final Object unsafeObj, final long cumBaseOffset,
      final long offsetBytes) {
    checkBounds(srcOffsetShorts, lengthShorts, srcArray.length);
    long cumulativeOffsetBytes = cumBaseOffset + offsetBytes;
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkShorts = (int) (chunkBytes >> SHORT_SHIFT);
      putShortArrayChunk(srcArray, srcOffsetShorts, chunkShorts, unsafeObj, cumulativeOffsetBytes);
      cumulativeOffsetBytes += chunkBytes;
      srcOffsetShorts += chunkShorts;
      copyBytes -= chunkBytes;
      lengthShorts -= chunkShorts;
    }
    putShortArrayChunk(srcArray, srcOffsetShorts, lengthShorts, unsafeObj, cumulativeOffsetBytes);
  }

  private static void putShortArrayChunk(final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts, final Object unsafeObj, final long cumulativeOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthShorts; i++) {
      unsafe.putShort(unsafeObj, cumulativeOffsetBytes + (((long) i) << SHORT_SHIFT),
          Short.reverseBytes(srcArray[srcOffsetShorts + i]));
    }
  }
}
