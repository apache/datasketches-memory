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

import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_CHAR_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_DOUBLE_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_FLOAT_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_INT_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_LONG_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_SHORT_UNALIGNED_NON_NATIVE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;

/**
 * Implementation of {@link WritableMemory} for non-native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class NonNativeWritableMemoryImpl extends WritableMemoryImpl {

  //Pass-through ctor
  NonNativeWritableMemoryImpl(
      final Arena arena,
      final MemorySegment seg,
      final int typeId,
      final MemoryRequestServer memReqSvr) {
    super(arena, seg, typeId, memReqSvr);
  }

  ///PRIMITIVE getX() and getXArray()
  @Override
  public char getChar(final long offsetBytes) {
    return seg.get(JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetChars << CHAR_SHIFT, copyBytes);
    for (int index = 0; index < lengthChars; index++) {
      final char aChar = srcSlice.getAtIndex(JAVA_CHAR_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_CHAR_UNALIGNED, index, aChar);
    }
  }

  @Override
  public double getDouble(final long offsetBytes) {
    return seg.get(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetDoubles << DOUBLE_SHIFT, copyBytes);
    for (int index = 0; index < lengthDoubles; index++) {
      final double dbl = srcSlice.getAtIndex(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_DOUBLE_UNALIGNED, index, dbl);
    }
  }

  @Override
  public float getFloat(final long offsetBytes) {
    return seg.get(JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetFloats << FLOAT_SHIFT, copyBytes);
    for (int index = 0; index < lengthFloats; index++) {
      final float flt = srcSlice.getAtIndex(JAVA_FLOAT_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_FLOAT_UNALIGNED, index, flt);
    }
  }

  @Override
  public int getInt(final long offsetBytes) {
    return seg.get(JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetInts << INT_SHIFT, copyBytes);
    for (int index = 0; index < lengthInts; index++) {
      final int anInt = srcSlice.getAtIndex(JAVA_INT_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index, anInt);
    }
  }

  @Override
  public long getLong(final long offsetBytes) {
    return seg.get(JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetLongs << LONG_SHIFT, copyBytes);
    for (int index = 0; index < lengthLongs; index++) {
      final long aLong = srcSlice.getAtIndex(JAVA_LONG_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index, aLong);
    }
  }

  @Override
  public short getShort(final long offsetBytes) {
    return seg.get(JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetShorts << SHORT_SHIFT, copyBytes);
    for (int index = 0; index < lengthShorts; index++) {
      final short aShort = srcSlice.getAtIndex(JAVA_SHORT_UNALIGNED_NON_NATIVE, index);
      dstSlice.setAtIndex(ValueLayout.JAVA_SHORT_UNALIGNED, index, aShort);
    }
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public void putChar(final long offsetBytes, final char value) {
    seg.set(JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetChars << CHAR_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthChars; index++) {
      final char aChar = srcSlice.getAtIndex(ValueLayout.JAVA_CHAR_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_CHAR_UNALIGNED_NON_NATIVE, index, aChar);
    }
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    seg.set(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetDoubles << DOUBLE_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthDoubles; index++) {
      final double dbl = srcSlice.getAtIndex(ValueLayout.JAVA_DOUBLE_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, index, dbl);
    }
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    seg.set(JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetFloats << FLOAT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthFloats; index++) {
      final float flt = srcSlice.getAtIndex(ValueLayout.JAVA_FLOAT_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_FLOAT_UNALIGNED_NON_NATIVE, index, flt);
    }
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    seg.set(JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetInts << INT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthInts; index++) {
      final int anInt = srcSlice.getAtIndex(ValueLayout.JAVA_INT_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_INT_UNALIGNED_NON_NATIVE, index, anInt);
    }
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    seg.set(JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetLongs << LONG_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthLongs; index++) {
      final long aLong = srcSlice.getAtIndex(ValueLayout.JAVA_LONG_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_LONG_UNALIGNED_NON_NATIVE, index, aLong);
    }
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    seg.set(JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffsetShorts, final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetShorts << SHORT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, copyBytes);
    for (int index = 0; index < lengthShorts; index++) {
      final short aShort = srcSlice.getAtIndex(ValueLayout.JAVA_SHORT_UNALIGNED, index);
      dstSlice.setAtIndex(JAVA_SHORT_UNALIGNED_NON_NATIVE, index, aShort);
    }
  }

}
