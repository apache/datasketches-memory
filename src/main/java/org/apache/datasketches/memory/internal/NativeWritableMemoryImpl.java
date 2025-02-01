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

import static java.lang.foreign.ValueLayout.JAVA_CHAR_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_INT_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_LONG_UNALIGNED;
import static java.lang.foreign.ValueLayout.JAVA_SHORT_UNALIGNED;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableMemory;

/**
 * Implementation of {@link WritableMemory} for native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class NativeWritableMemoryImpl extends WritableMemoryImpl {

  //Pass-through constructor
  NativeWritableMemoryImpl(
      final MemorySegment seg,
      final int typeId,
      final MemoryRequestServer memReqSvr,
      final Arena arena) {
    super(seg, typeId, memReqSvr, arena);
  }

  //PRIMITIVE getX() and getXArray()
  @Override
  public char getChar(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_CHAR_UNALIGNED, offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    MemorySegment.copy(seg, JAVA_CHAR_UNALIGNED, offsetBytes, dstArray, dstOffsetChars, lengthChars);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_DOUBLE_UNALIGNED, offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    MemorySegment.copy(seg, JAVA_DOUBLE_UNALIGNED, offsetBytes, dstArray, dstOffsetDoubles, lengthDoubles);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_FLOAT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    MemorySegment.copy(seg, JAVA_FLOAT_UNALIGNED, offsetBytes, dstArray, dstOffsetFloats, lengthFloats);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_INT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    MemorySegment.copy(seg, JAVA_INT_UNALIGNED, offsetBytes, dstArray, dstOffsetInts, lengthInts);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_LONG_UNALIGNED, offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    MemorySegment.copy(seg, JAVA_LONG_UNALIGNED, offsetBytes, dstArray, dstOffsetLongs, lengthLongs);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return seg.get(ValueLayout.JAVA_SHORT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    MemorySegment.copy(seg, JAVA_SHORT_UNALIGNED, offsetBytes, dstArray, dstOffsetShorts, lengthShorts);
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public void putChar(final long offsetBytes, final char value) {
    seg.set(ValueLayout.JAVA_CHAR_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    MemorySegment.copy(srcArray, srcOffsetChars, seg, JAVA_CHAR_UNALIGNED, offsetBytes, lengthChars);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    seg.set(ValueLayout.JAVA_DOUBLE_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    MemorySegment.copy(srcArray, srcOffsetDoubles, seg, JAVA_DOUBLE_UNALIGNED, offsetBytes, lengthDoubles);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    seg.set(ValueLayout.JAVA_FLOAT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    MemorySegment.copy(srcArray, srcOffsetFloats, seg, JAVA_FLOAT_UNALIGNED, offsetBytes, lengthFloats);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    seg.set(ValueLayout.JAVA_INT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    MemorySegment.copy(srcArray, srcOffsetInts, seg, JAVA_INT_UNALIGNED, offsetBytes, lengthInts);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    seg.set(ValueLayout.JAVA_LONG_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    MemorySegment.copy(srcArray, srcOffsetLongs, seg, JAVA_LONG_UNALIGNED, offsetBytes, lengthLongs);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    seg.set(ValueLayout.JAVA_SHORT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffsetShorts, final int lengthShorts) {
    MemorySegment.copy(srcArray, srcOffsetShorts, seg, JAVA_SHORT_UNALIGNED, offsetBytes, lengthShorts);
  }

}
