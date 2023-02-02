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

import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import org.apache.datasketches.memory.WritableMemory;

/**
 * Implementation of {@link WritableMemory} for native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
abstract class NativeWritableMemoryImpl extends BaseWritableMemoryImpl {

  //Pass-through constructor
  NativeWritableMemoryImpl() { }

  ///PRIMITIVE getX() and getXArray()
  @Override
  public char getChar(final long offsetBytes) {
    return getNativeOrderedChar(offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetChars, lengthChars, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) dstOffsetChars) << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    checkValidAndBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray,
      final int dstOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetDoubles, lengthDoubles, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffsetDoubles) << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    checkValidAndBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray,
      final int dstOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetFloats, lengthFloats, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffsetFloats) << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return getNativeOrderedInt(offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetInts, lengthInts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_INT_BASE_OFFSET + (((long) dstOffsetInts) << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return getNativeOrderedLong(offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray,
      final int dstOffsetLongs, final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetLongs, lengthLongs, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (((long) dstOffsetLongs) << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return getNativeOrderedShort(offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray,
      final int dstOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(dstOffsetShorts, lengthShorts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) dstOffsetShorts) << SHORT_SHIFT),
        copyBytes);
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public void putChar(final long offsetBytes, final char value) {
    putNativeOrderedChar(offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray,
      final int srcOffsetChars, final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetChars, lengthChars, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) srcOffsetChars) << CHAR_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    checkValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray,
      final int srcOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetDoubles, lengthDoubles, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffsetDoubles) << DOUBLE_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    checkValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray,
      final int srcOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetFloats, lengthFloats, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffsetFloats) << FLOAT_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    putNativeOrderedInt(offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetInts, lengthInts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (((long) srcOffsetInts) << INT_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    putNativeOrderedLong(offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetLongs, lengthLongs, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (((long) srcOffsetLongs) << LONG_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    putNativeOrderedShort(offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray,
      final int srcOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    ResourceImpl.checkBounds(srcOffsetShorts, lengthShorts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) srcOffsetShorts) << SHORT_SHIFT),
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

}
