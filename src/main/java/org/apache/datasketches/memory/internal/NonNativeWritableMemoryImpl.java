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

import static java.lang.foreign.ValueLayout.JAVA_CHAR;
import static java.lang.foreign.ValueLayout.JAVA_DOUBLE;
import static java.lang.foreign.ValueLayout.JAVA_FLOAT;
import static java.lang.foreign.ValueLayout.JAVA_INT;
import static java.lang.foreign.ValueLayout.JAVA_LONG;
import static java.lang.foreign.ValueLayout.JAVA_SHORT;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_CHAR_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_DOUBLE_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_FLOAT_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_INT_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_LONG_UNALIGNED_NON_NATIVE;
import static org.apache.datasketches.memory.internal.NonNativeValueLayouts.JAVA_SHORT_UNALIGNED_NON_NATIVE;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

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
      final MemorySegment seg,
      final int typeId,
      final MemoryRequestServer memReqSvr,
      final Arena arena) {
    super(seg, typeId, memReqSvr, arena);
  }

  ///PRIMITIVE getX() and getXArray()
  @Override
  public char getChar(final long offsetBytes) {
    return seg.get(JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    getCharArr(seg, offsetBytes, dstArray, dstOffsetChars, lengthChars);
  }

  static void getCharArr(
      final MemorySegment seg, final long offsetBytes, final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetChars) << CHAR_SHIFT;
    MemorySegment.copy(seg, JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_CHAR, dstOffsetBytes, lengthChars);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    return seg.get(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    getDoubleArr(seg, offsetBytes, dstArray, dstOffsetDoubles, lengthDoubles);
  }

  static void getDoubleArr(
      final MemorySegment seg, final long offsetBytes, final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetDoubles) << DOUBLE_SHIFT;
    MemorySegment.copy(seg, JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_DOUBLE, dstOffsetBytes, lengthDoubles);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    return seg.get(JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    getFloatArr(seg, offsetBytes, dstArray, dstOffsetFloats, lengthFloats);
  }

  static void getFloatArr(
      final MemorySegment seg, final long offsetBytes, final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetFloats) << FLOAT_SHIFT;
    MemorySegment.copy(seg, JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_FLOAT, dstOffsetBytes, lengthFloats);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return seg.get(JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    getIntArr(seg, offsetBytes, dstArray, dstOffsetInts, lengthInts);
  }

  static void getIntArr(
      final MemorySegment seg, final long offsetBytes, final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetInts) << INT_SHIFT;
    MemorySegment.copy(seg, JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_INT, dstOffsetBytes, lengthInts);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return seg.get(JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    getLongArr(seg, offsetBytes, dstArray, dstOffsetLongs, lengthLongs);
  }

  static void getLongArr(
      final MemorySegment seg, final long offsetBytes, final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetLongs) << LONG_SHIFT;
    MemorySegment.copy(seg, JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_LONG, dstOffsetBytes, lengthLongs);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return seg.get(JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    getShortArr(seg, offsetBytes, dstArray, dstOffsetShorts, lengthShorts);
  }

  static void getShortArr(
      final MemorySegment seg, final long offsetBytes, final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    final MemorySegment dstSeg = MemorySegment.ofArray(dstArray);
    final long dstOffsetBytes = ((long) dstOffsetShorts) << SHORT_SHIFT;
    MemorySegment.copy(seg, JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes, dstSeg, JAVA_SHORT, dstOffsetBytes, lengthShorts);
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public void putChar(final long offsetBytes, final char value) {
    seg.set(JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    putCharArr(seg, offsetBytes, srcArray, srcOffsetChars, lengthChars);
  }

  static void putCharArr(
      final MemorySegment seg, final long offsetBytes, final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetChars) << CHAR_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_CHAR, srcOffsetBytes, seg, JAVA_CHAR_UNALIGNED_NON_NATIVE, offsetBytes, lengthChars);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    seg.set(JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    putDoubleArr(seg, offsetBytes, srcArray, srcOffsetDoubles, lengthDoubles);
  }

  static void putDoubleArr(
      final MemorySegment seg, final long offsetBytes, final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetDoubles) << DOUBLE_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_DOUBLE, srcOffsetBytes, seg, JAVA_DOUBLE_UNALIGNED_NON_NATIVE, offsetBytes, lengthDoubles);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    seg.set(JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    putFloatArr(seg, offsetBytes, srcArray, srcOffsetFloats, lengthFloats);
  }

  static void putFloatArr(
      final MemorySegment seg, final long offsetBytes, final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetFloats) << FLOAT_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_FLOAT, srcOffsetBytes, seg, JAVA_FLOAT_UNALIGNED_NON_NATIVE, offsetBytes, lengthFloats);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    seg.set(JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    putIntArr(seg, offsetBytes, srcArray, srcOffsetInts, lengthInts);
  }

  static void putIntArr(
      final MemorySegment seg, final long offsetBytes, final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetInts) << INT_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_INT, srcOffsetBytes, seg, JAVA_INT_UNALIGNED_NON_NATIVE, offsetBytes, lengthInts);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    seg.set(JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    putLongArr(seg, offsetBytes, srcArray, srcOffsetLongs, lengthLongs);
  }

  static void putLongArr(
      final MemorySegment seg, final long offsetBytes, final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetLongs) << LONG_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_LONG, srcOffsetBytes, seg, JAVA_LONG_UNALIGNED_NON_NATIVE, offsetBytes, lengthLongs);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    seg.set(JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffsetShorts, final int lengthShorts) {
    putShortArr(seg, offsetBytes, srcArray, srcOffsetShorts, lengthShorts);
  }

  static void putShortArr(
      final MemorySegment seg, final long offsetBytes, final short[] srcArray, final int srcOffsetShorts, final int lengthShorts) {
    final MemorySegment srcSeg = MemorySegment.ofArray(srcArray);
    final long srcOffsetBytes = ((long) srcOffsetShorts) << SHORT_SHIFT;
    MemorySegment.copy(srcSeg, JAVA_SHORT, srcOffsetBytes, seg, JAVA_SHORT_UNALIGNED_NON_NATIVE, offsetBytes, lengthShorts);
  }

}
