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

import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;

/**
 * Implementation of {@link WritableBuffer} for native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class NativeWritableBufferImpl extends WritableBufferImpl {

  //Pass-through ctor
  NativeWritableBufferImpl(
      final MemorySegment seg,
      final int typeId,
      final MemoryRequestServer memReqSvr,
      final Arena arena) {
    super(seg, typeId, memReqSvr, arena);
  }

  //PRIMITIVE getX() and getXArray()
  @Override
  public char getChar() {
    final long pos = getPosition();
    setPosition(pos + Character.BYTES);
    return seg.get(JAVA_CHAR_UNALIGNED, pos);
  }

  @Override
  public char getChar(final long offsetBytes) {
    return seg.get(JAVA_CHAR_UNALIGNED, offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_CHAR_UNALIGNED, pos, dstArray, dstOffsetChars, lengthChars);
    setPosition(pos + (lengthChars << CHAR_SHIFT));
  }

  @Override
  public double getDouble() {
    final long pos = getPosition();
    setPosition(pos + Double.BYTES);
    return seg.get(JAVA_DOUBLE_UNALIGNED, pos);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    return seg.get(JAVA_DOUBLE_UNALIGNED, offsetBytes);
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_DOUBLE_UNALIGNED, pos, dstArray, dstOffsetDoubles, lengthDoubles);
    setPosition(pos + (lengthDoubles << DOUBLE_SHIFT));
  }

  @Override
  public float getFloat() {
    final long pos = getPosition();
    setPosition(pos + Float.BYTES);
    return seg.get(JAVA_FLOAT_UNALIGNED, pos);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    return seg.get(JAVA_FLOAT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_FLOAT_UNALIGNED, pos, dstArray, dstOffsetFloats, lengthFloats);
    setPosition(pos + (lengthFloats << FLOAT_SHIFT));
  }

  @Override
  public int getInt() {
    final long pos = getPosition();
    setPosition(pos + Integer.BYTES);
    return seg.get(JAVA_INT_UNALIGNED, pos);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return seg.get(JAVA_INT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_INT_UNALIGNED, pos, dstArray, dstOffsetInts, lengthInts);
    setPosition(pos + (lengthInts << INT_SHIFT));
  }

  @Override
  public long getLong() {
    final long pos = getPosition();
    setPosition(pos + Long.BYTES);
    return seg.get(JAVA_LONG_UNALIGNED, pos);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return seg.get(JAVA_LONG_UNALIGNED, offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_LONG_UNALIGNED, pos, dstArray, dstOffsetLongs, lengthLongs);
    setPosition(pos + (lengthLongs << LONG_SHIFT));
  }

  @Override
  public short getShort() {
    final long pos = getPosition();
    setPosition(pos + Short.BYTES);
    return seg.get(JAVA_SHORT_UNALIGNED, pos);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return seg.get(JAVA_SHORT_UNALIGNED, offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    final long pos = getPosition();
    MemorySegment.copy(seg, JAVA_SHORT_UNALIGNED, pos, dstArray, dstOffsetShorts, lengthShorts);
    setPosition(pos + (lengthShorts << SHORT_SHIFT));
  }

  //PRIMITIVE putX() and putXArray()
  @Override
  public void putChar(final char value) {
    final long pos = getPosition();
    setPosition(pos + Character.BYTES);
    seg.set(JAVA_CHAR_UNALIGNED, pos, value);
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    seg.set(JAVA_CHAR_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetChars, seg, JAVA_CHAR_UNALIGNED, pos, lengthChars);
    setPosition(pos + (lengthChars << CHAR_SHIFT));
  }

  @Override
  public void putDouble(final double value) {
    final long pos = getPosition();
    setPosition(pos + Double.BYTES);
    seg.set(JAVA_DOUBLE_UNALIGNED, pos, value);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    seg.set(JAVA_DOUBLE_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetDoubles, seg, JAVA_DOUBLE_UNALIGNED, pos, lengthDoubles);
    setPosition(pos + (lengthDoubles << DOUBLE_SHIFT));
  }

  @Override
  public void putFloat(final float value) {
    final long pos = getPosition();
    setPosition(pos + Float.BYTES);
    seg.set(JAVA_FLOAT_UNALIGNED, pos, value);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    seg.set(JAVA_FLOAT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetFloats, seg, JAVA_FLOAT_UNALIGNED, pos, lengthFloats);
    setPosition(pos + (lengthFloats << FLOAT_SHIFT));
  }

  @Override
  public void putInt(final int value) {
    final long pos = getPosition();
    setPosition(pos + Integer.BYTES);
    seg.set(JAVA_INT_UNALIGNED, pos, value);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    seg.set(JAVA_INT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetInts, seg, JAVA_INT_UNALIGNED, pos, lengthInts);
    setPosition(pos + (lengthInts << INT_SHIFT));
  }

  @Override
  public void putLong(final long value) {
    final long pos = getPosition();
    setPosition(pos + Long.BYTES);
    seg.set(JAVA_LONG_UNALIGNED, pos, value);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    seg.set(JAVA_LONG_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetLongs, seg, JAVA_LONG_UNALIGNED, pos, lengthLongs);
    setPosition(pos + (lengthLongs << LONG_SHIFT));
  }

  @Override
  public void putShort(final short value) {
    final long pos = getPosition();
    setPosition(pos + Short.BYTES);
    seg.set(JAVA_SHORT_UNALIGNED, pos, value);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    seg.set(JAVA_SHORT_UNALIGNED, offsetBytes, value);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffsetShorts, final int lengthShorts) {
    final long pos = getPosition();
    MemorySegment.copy(srcArray, srcOffsetShorts, seg, JAVA_SHORT_UNALIGNED, pos, lengthShorts);
    setPosition(pos + (lengthShorts << SHORT_SHIFT));
  }

}
