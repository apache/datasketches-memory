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

import static org.apache.datasketches.memory.internal.UnsafeUtil.CHAR_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.DOUBLE_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.FLOAT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.INT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.LONG_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.SHORT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.checkBounds;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;
import static org.apache.datasketches.memory.internal.Util.UNSAFE_COPY_THRESHOLD_BYTES;

/**
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
final class CompareAndCopy {

  private CompareAndCopy() { }

  static int compare(
      final BaseStateImpl state1, final long offsetBytes1, final long lengthBytes1,
      final BaseStateImpl state2, final long offsetBytes2, final long lengthBytes2) {
    state1.checkValid();
    checkBounds(offsetBytes1, lengthBytes1, state1.getCapacity());
    state2.checkValid();
    checkBounds(offsetBytes2, lengthBytes2, state2.getCapacity());
    final long cumOff1 = state1.getCumulativeOffset(offsetBytes1);
    final long cumOff2 = state2.getCumulativeOffset(offsetBytes2);
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

  static boolean equals(final BaseStateImpl state1, final BaseStateImpl state2) {
    final long cap1 = state1.getCapacity();
    final long cap2 = state2.getCapacity();
    return (cap1 == cap2) && equals(state1, 0, state2, 0, cap1);
  }

  //Developer notes: this is subtlely different from (campare == 0) in that this has an early
  // stop if the arrays and offsets are the same as there is only one length.  Also this can take
  // advantage of chunking with longs, while compare cannot.
  static boolean equals(
      final BaseStateImpl state1, final long offsetBytes1,
      final BaseStateImpl state2, final long offsetBytes2, long lengthBytes) {
    state1.checkValid();
    checkBounds(offsetBytes1, lengthBytes, state1.getCapacity());
    state2.checkValid();
    checkBounds(offsetBytes2, lengthBytes, state2.getCapacity());
    long cumOff1 = state1.getCumulativeOffset(offsetBytes1);
    long cumOff2 = state2.getCumulativeOffset(offsetBytes2);
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

  static void copy(final BaseStateImpl srcState, final long srcOffsetBytes,
      final BaseStateImpl dstState, final long dstOffsetBytes, final long lengthBytes) {
    srcState.checkValid();
    checkBounds(srcOffsetBytes, lengthBytes, srcState.getCapacity());
    dstState.checkValid();
    checkBounds(dstOffsetBytes, lengthBytes, dstState.getCapacity());
    final long srcAdd = srcState.getCumulativeOffset(srcOffsetBytes);
    final long dstAdd = dstState.getCumulativeOffset(dstOffsetBytes);
    copyMemory(srcState.getUnsafeObject(), srcAdd, dstState.getUnsafeObject(), dstAdd,
        lengthBytes);
  }

  //Used by all of the get/put array methods in BufferImpl and MemoryImpl classes
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

  static void getNonNativeChars(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final char[] dstArray, int dstOffsetChars,
      int lengthChars) {
    checkBounds(dstOffsetChars, lengthChars, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkChars = (int) (chunkBytes >> CHAR_SHIFT);
      getCharArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetChars, chunkChars);
      cumOffsetBytes += chunkBytes;
      dstOffsetChars += chunkChars;
      copyBytes -= chunkBytes;
      lengthChars -= chunkChars;
    }
    getCharArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetChars, lengthChars);
  }

  private static void getCharArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthChars; i++) {
      dstArray[dstOffsetChars + i] = Character.reverseBytes(
          unsafe.getChar(unsafeObj, cumOffsetBytes + (((long) i) << CHAR_SHIFT)));
    }
  }

  static void getNonNativeDoubles(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final double[] dstArray, int dstOffsetDoubles,
      int lengthDoubles) {
    checkBounds(dstOffsetDoubles, lengthDoubles, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkDoubles = (int) (chunkBytes >> DOUBLE_SHIFT);
      getDoubleArrayChunk(unsafeObj, cumOffsetBytes,
          dstArray, dstOffsetDoubles, chunkDoubles);
      cumOffsetBytes += chunkBytes;
      dstOffsetDoubles += chunkDoubles;
      copyBytes -= chunkBytes;
      lengthDoubles -= chunkDoubles;
    }
    getDoubleArrayChunk(unsafeObj, cumOffsetBytes,
        dstArray, dstOffsetDoubles, lengthDoubles);
  }

  private static void getDoubleArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthDoubles; i++) {
      dstArray[dstOffsetDoubles + i] = Double.longBitsToDouble(Long.reverseBytes(
          unsafe.getLong(unsafeObj, cumOffsetBytes + (((long) i) << DOUBLE_SHIFT))));
    }
  }

  static void getNonNativeFloats(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final float[] dstArray, int dstOffsetFloats,
      int lengthFloats) {
    checkBounds(dstOffsetFloats, lengthFloats, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkFloats = (int) (chunkBytes >> FLOAT_SHIFT);
      getFloatArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetFloats, chunkFloats);
      cumOffsetBytes += chunkBytes;
      dstOffsetFloats += chunkFloats;
      copyBytes -= chunkBytes;
      lengthFloats -= chunkFloats;
    }
    getFloatArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetFloats, lengthFloats);
  }

  private static void getFloatArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthFloats; i++) {
      dstArray[dstOffsetFloats + i] = Float.intBitsToFloat(Integer.reverseBytes(
          unsafe.getInt(unsafeObj, cumOffsetBytes + (((long) i) << FLOAT_SHIFT))));
    }
  }

  static void getNonNativeInts(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final int[] dstArray, int dstOffsetInts,
      int lengthInts) {
    checkBounds(dstOffsetInts, lengthInts, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkInts = (int) (chunkBytes >> INT_SHIFT);
      getIntArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetInts, chunkInts);
      cumOffsetBytes += chunkBytes;
      dstOffsetInts += chunkInts;
      copyBytes -= chunkBytes;
      lengthInts -= chunkInts;
    }
    getIntArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetInts, lengthInts);
  }

  private static void getIntArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthInts; i++) {
      dstArray[dstOffsetInts + i] = Integer.reverseBytes(
          unsafe.getInt(unsafeObj, cumOffsetBytes + (((long) i) << INT_SHIFT)));
    }
  }

  static void getNonNativeLongs(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final long[] dstArray, int dstOffsetLongs,
      int lengthLongs) {
    checkBounds(dstOffsetLongs, lengthLongs, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkLongs = (int) (chunkBytes >> LONG_SHIFT);
      getLongArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetLongs, chunkLongs);
      cumOffsetBytes += chunkBytes;
      dstOffsetLongs += chunkLongs;
      copyBytes -= chunkBytes;
      lengthLongs -= chunkLongs;
    }
    getLongArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetLongs, lengthLongs);
  }

  private static void getLongArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthLongs; i++) {
      dstArray[dstOffsetLongs + i] = Long.reverseBytes(
          unsafe.getLong(unsafeObj, cumOffsetBytes + (((long) i) << LONG_SHIFT)));
    }
  }

  static void getNonNativeShorts(final Object unsafeObj, long cumOffsetBytes,
      long copyBytes, final short[] dstArray, int dstOffsetShorts,
      int lengthShorts) {
    checkBounds(dstOffsetShorts, lengthShorts, dstArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkShorts = (int) (chunkBytes >> SHORT_SHIFT);
      getShortArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetShorts, chunkShorts);
      cumOffsetBytes += chunkBytes;
      dstOffsetShorts += chunkShorts;
      copyBytes -= chunkBytes;
      lengthShorts -= chunkShorts;
    }
    getShortArrayChunk(unsafeObj, cumOffsetBytes, dstArray, dstOffsetShorts, lengthShorts);
  }

  private static void getShortArrayChunk(final Object unsafeObj, final long cumOffsetBytes,
      final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthShorts; i++) {
      dstArray[dstOffsetShorts + i] = Short.reverseBytes(
          unsafe.getShort(unsafeObj, cumOffsetBytes + (((long) i) << SHORT_SHIFT)));
    }
  }

  static void putNonNativeChars(final char[] srcArray, int srcOffsetChars, int lengthChars,
      long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetChars, lengthChars, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkChars = (int) (chunkBytes >> CHAR_SHIFT);
      putCharArrayChunk(srcArray, srcOffsetChars, chunkChars, unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetChars += chunkChars;
      copyBytes -= chunkBytes;
      lengthChars -= chunkChars;
    }
    putCharArrayChunk(srcArray, srcOffsetChars, lengthChars, unsafeObj, cumOffsetBytes);
  }

  private static void putCharArrayChunk(final char[] srcArray, final int srcOffsetChars,
      final int lengthChars, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthChars; i++) {
      unsafe.putChar(unsafeObj, cumOffsetBytes + (((long) i) << CHAR_SHIFT),
          Character.reverseBytes(srcArray[srcOffsetChars + i]));
    }
  }

  static void putNonNativeDoubles(final double[] srcArray, int srcOffsetDoubles,
      int lengthDoubles, long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetDoubles, lengthDoubles, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkDoubles = (int) (chunkBytes >> DOUBLE_SHIFT);
      putDoubleArrayChunk(srcArray, srcOffsetDoubles, chunkDoubles,
          unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetDoubles += chunkDoubles;
      copyBytes -= chunkBytes;
      lengthDoubles -= chunkDoubles;
    }
    putDoubleArrayChunk(srcArray, srcOffsetDoubles, lengthDoubles,
        unsafeObj, cumOffsetBytes);
  }

  private static void putDoubleArrayChunk(final double[] srcArray, final int srcOffsetDoubles,
      final int lengthDoubles, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthDoubles; i++) {
      unsafe.putLong(unsafeObj, cumOffsetBytes + (((long) i) << DOUBLE_SHIFT),
          Long.reverseBytes(Double.doubleToRawLongBits(srcArray[srcOffsetDoubles + i])));
    }
  }

  static void putNonNativeFloats(final float[] srcArray, int srcOffsetFloats,
      int lengthFloats, long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetFloats, lengthFloats, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkFloats = (int) (chunkBytes >> FLOAT_SHIFT);
      putFloatArrayChunk(srcArray, srcOffsetFloats, chunkFloats, unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetFloats += chunkFloats;
      copyBytes -= chunkBytes;
      lengthFloats -= chunkFloats;
    }
    putFloatArrayChunk(srcArray, srcOffsetFloats, lengthFloats, unsafeObj, cumOffsetBytes);
  }

  private static void putFloatArrayChunk(final float[] srcArray, final int srcOffsetFloats,
      final int lengthFloats, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthFloats; i++) {
      unsafe.putInt(unsafeObj, cumOffsetBytes + (((long) i) << FLOAT_SHIFT),
          Integer.reverseBytes(Float.floatToRawIntBits(srcArray[srcOffsetFloats + i])));
    }
  }

  static void putNonNativeInts(final int[] srcArray, int srcOffsetInts, int lengthInts,
      long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetInts, lengthInts, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkInts = (int) (chunkBytes >> INT_SHIFT);
      putIntArrayChunk(srcArray, srcOffsetInts, chunkInts, unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetInts += chunkInts;
      copyBytes -= chunkBytes;
      lengthInts -= chunkInts;
    }
    putIntArrayChunk(srcArray, srcOffsetInts, lengthInts, unsafeObj, cumOffsetBytes);
  }

  private static void putIntArrayChunk(final int[] srcArray, final int srcOffsetInts,
      final int lengthInts, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthInts; i++) {
      unsafe.putInt(unsafeObj, cumOffsetBytes + (((long) i) << INT_SHIFT),
          Integer.reverseBytes(srcArray[srcOffsetInts + i]));
    }
  }

  static void putNonNativeLongs(final long[] srcArray, int srcOffsetLongs, int lengthLongs,
      long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetLongs, lengthLongs, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkLongs = (int) (chunkBytes >> LONG_SHIFT);
      putLongArrayChunk(srcArray, srcOffsetLongs, chunkLongs, unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetLongs += chunkLongs;
      copyBytes -= chunkBytes;
      lengthLongs -= chunkLongs;
    }
    putLongArrayChunk(srcArray, srcOffsetLongs, lengthLongs, unsafeObj, cumOffsetBytes);
  }

  private static void putLongArrayChunk(final long[] srcArray, final int srcOffsetLongs,
      final int lengthLongs, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthLongs; i++) {
      unsafe.putLong(unsafeObj, cumOffsetBytes + (((long) i) << LONG_SHIFT),
          Long.reverseBytes(srcArray[srcOffsetLongs + i]));
    }
  }

  static void putNonNativeShorts(final short[] srcArray, int srcOffsetShorts,
      int lengthShorts, long copyBytes, final Object unsafeObj, long cumOffsetBytes) {
    checkBounds(srcOffsetShorts, lengthShorts, srcArray.length);
    while (copyBytes > UNSAFE_COPY_THRESHOLD_BYTES) {
      final long chunkBytes = Math.min(copyBytes, UNSAFE_COPY_THRESHOLD_BYTES);
      final int chunkShorts = (int) (chunkBytes >> SHORT_SHIFT);
      putShortArrayChunk(srcArray, srcOffsetShorts, chunkShorts, unsafeObj, cumOffsetBytes);
      cumOffsetBytes += chunkBytes;
      srcOffsetShorts += chunkShorts;
      copyBytes -= chunkBytes;
      lengthShorts -= chunkShorts;
    }
    putShortArrayChunk(srcArray, srcOffsetShorts, lengthShorts, unsafeObj, cumOffsetBytes);
  }

  private static void putShortArrayChunk(final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts, final Object unsafeObj, final long cumOffsetBytes) {
    // JDK 9 adds native intrinsics for such bulk non-native ordered primitive memory copy.
    // TODO-JDK9 use them when the library adds support for JDK 9
    // int-counted loop to avoid safepoint polls
    for (int i = 0; i < lengthShorts; i++) {
      unsafe.putShort(unsafeObj, cumOffsetBytes + (((long) i) << SHORT_SHIFT),
          Short.reverseBytes(srcArray[srcOffsetShorts + i]));
    }
  }
}
