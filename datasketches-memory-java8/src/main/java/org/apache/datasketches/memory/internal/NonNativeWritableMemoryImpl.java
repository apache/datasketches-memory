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

import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.CHAR_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.DOUBLE_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.FLOAT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.INT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.LONG_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.SHORT_SHIFT;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import org.apache.datasketches.memory.WritableMemory;

/*
 * Developer notes: The heavier methods, such as put/get arrays, duplicate, region, clear, fill,
 * compareTo, etc., use hard checks (checkValid*() and checkBounds()), which execute at runtime and
 * throw exceptions if violated. The cost of the runtime checks are minor compared to the rest of
 * the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assertValid*()), which only
 * execute when asserts are enabled and JIT will remove them entirely from production runtime code.
 * The light weight methods will simplify to a single unsafe call, which is further simplified by
 * JIT to an intrinsic that is often a single CPU instruction.
 */

/**
 * Implementation of {@link WritableMemory} for non-native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
abstract class NonNativeWritableMemoryImpl extends BaseWritableMemoryImpl {

  //Pass-through ctor
  NonNativeWritableMemoryImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  ///PRIMITIVE getX() and getXArray()
  @Override
  public char getChar(final long offsetBytes) {
    return Character.reverseBytes(getNativeOrderedChar(offsetBytes));
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeChars(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        copyBytes, dstArray, dstOffsetChars, lengthChars);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return Double.longBitsToDouble(
        Long.reverseBytes(unsafe.getLong(getUnsafeObject(), getCumulativeOffset(offsetBytes))));
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray,
      final int dstOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeDoubles(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        copyBytes, dstArray, dstOffsetDoubles, lengthDoubles);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return Float.intBitsToFloat(
        Integer.reverseBytes(unsafe.getInt(getUnsafeObject(), getCumulativeOffset(offsetBytes))));
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray,
      final int dstOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeFloats(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        copyBytes, dstArray, dstOffsetFloats, lengthFloats);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return Integer.reverseBytes(getNativeOrderedInt(offsetBytes));
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeInts(getUnsafeObject(), getCumulativeOffset(offsetBytes), copyBytes,
        dstArray, dstOffsetInts, lengthInts);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return Long.reverseBytes(getNativeOrderedLong(offsetBytes));
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray,
      final int dstOffsetLongs, final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeLongs(getUnsafeObject(), getCumulativeOffset(offsetBytes), copyBytes,
        dstArray, dstOffsetLongs, lengthLongs);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return Short.reverseBytes(getNativeOrderedShort(offsetBytes));
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray,
      final int dstOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    CompareAndCopy.getNonNativeShorts(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        copyBytes, dstArray, dstOffsetShorts, lengthShorts);
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public void putChar(final long offsetBytes, final char value) {
    putNativeOrderedChar(offsetBytes, Character.reverseBytes(value));
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffsetChars,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeChars(srcArray, srcOffsetChars, lengthChars, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        Long.reverseBytes(Double.doubleToRawLongBits(value)));
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray,
      final int srcOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeDoubles(srcArray, srcOffsetDoubles, lengthDoubles, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        Integer.reverseBytes(Float.floatToRawIntBits(value)));
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray,
      final int srcOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeFloats(srcArray, srcOffsetFloats, lengthFloats, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    putNativeOrderedInt(offsetBytes, Integer.reverseBytes(value));
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeInts(srcArray, srcOffsetInts, lengthInts, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    putNativeOrderedLong(offsetBytes, Long.reverseBytes(value));
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeLongs(srcArray, srcOffsetLongs, lengthLongs, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    putNativeOrderedShort(offsetBytes, Short.reverseBytes(value));
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray,
      final int srcOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    CompareAndCopy.putNonNativeShorts(srcArray, srcOffsetShorts, lengthShorts, copyBytes,
        getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  //Atomic Write Methods
  @Override
  public long getAndAddLong(final long offsetBytes, final long delta) { //JDK 8+
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long addr = getCumulativeOffset(offsetBytes);
    long oldValReverseBytes, oldVal, newValReverseBytes;
    final Object unsafeObj = getUnsafeObject();
    do {
      oldValReverseBytes = unsafe.getLongVolatile(unsafeObj, addr);
      oldVal = Long.reverseBytes(oldValReverseBytes);
      newValReverseBytes = Long.reverseBytes(oldVal + delta);
    } while (!unsafe.compareAndSwapLong(unsafeObj, addr, oldValReverseBytes, newValReverseBytes));

    return oldVal;
  }

  @Override
  public long getAndSetLong(final long offsetBytes, final long newValue) { //JDK 8+
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long addr = getCumulativeOffset(offsetBytes);
    final long newValueReverseBytes = Long.reverseBytes(newValue);
    return Long.reverseBytes(unsafe.getAndSetLong(getUnsafeObject(), addr, newValueReverseBytes));
  }

  @Override
  public boolean compareAndSwapLong(final long offsetBytes, final long expect, final long update) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.compareAndSwapLong(getUnsafeObject(), getCumulativeOffset(offsetBytes),
        Long.reverseBytes(expect), Long.reverseBytes(update));
  }
}
