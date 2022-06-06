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

import org.apache.datasketches.memory.WritableBuffer;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

/*
 * Developer notes: The heavier methods, such as put/get arrays, duplicate, region, clear, fill,
 * compareTo, etc., use hard checks (check*() and incrementAndCheck*() methods), which execute at
 * runtime and throw exceptions if violated. The cost of the runtime checks are minor compared to
 * the rest of the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assert*() and
 * incrementAndAssert*() methods), which only execute when asserts are enabled and JIT will remove
 * them entirely from production runtime code. The offset versions of the light weight methods will
 * simplify to a single unsafe call, which is further simplified by JIT to an intrinsic that is
 * often a single CPU instruction.
 */

/**
 * Implementation of {@link WritableBuffer} for native endian byte order.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
@SuppressWarnings("restriction")
abstract class NativeWritableBufferImpl extends BaseWritableBufferImpl {

  //Pass-through ctor
  NativeWritableBufferImpl(
      final MemorySegment seg,
      final int typeId) {
    super(seg, typeId);
  }

  //PRIMITIVE getX() and getXArray()
  @Override
  public char getChar() {
    final long pos = getPosition();
    setPosition(pos + Character.BYTES);
    return MemoryAccess.getCharAtOffset(seg, pos);
  }

  @Override
  public char getChar(final long offsetBytes) {
    return MemoryAccess.getCharAtOffset(seg, offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetChars << CHAR_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public double getDouble() {
    final long pos = getPosition();
    setPosition(pos + Double.BYTES);
    return MemoryAccess.getDoubleAtOffset(seg, pos);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    return MemoryAccess.getDoubleAtOffset(seg, offsetBytes);
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffsetDoubles, final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetDoubles << DOUBLE_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public float getFloat() {
    final long pos = getPosition();
    setPosition(pos + Float.BYTES);
    return MemoryAccess.getFloatAtOffset(seg, pos);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    return MemoryAccess.getFloatAtOffset(seg, offsetBytes);
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffsetFloats, final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetFloats << FLOAT_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public int getInt() {
    final long pos = getPosition();
    setPosition(pos + Integer.BYTES);
    return MemoryAccess.getIntAtOffset(seg, pos);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return MemoryAccess.getIntAtOffset(seg, offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetInts << INT_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public long getLong() {
    final long pos = getPosition();
    setPosition(pos + Long.BYTES);
    return MemoryAccess.getLongAtOffset(seg, pos);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return MemoryAccess.getLongAtOffset(seg, offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetLongs << LONG_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public short getShort() {
    final long pos = getPosition();
    setPosition(pos + Short.BYTES);
    return MemoryAccess.getShortAtOffset(seg, pos);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return MemoryAccess.getShortAtOffset(seg, offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffsetShorts, final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    final MemorySegment srcSlice = seg.asSlice(pos, copyBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetShorts << SHORT_SHIFT, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  //PRIMITIVE putX() and putXArray()
  @Override
  public void putChar(final char value) {
    final long pos = getPosition();
    setPosition(pos + Character.BYTES);
    MemoryAccess.setCharAtOffset(seg, pos, value);
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    MemoryAccess.setCharAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetChars << CHAR_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    final long pos = getPosition();
    setPosition(pos + Double.BYTES);
    MemoryAccess.setDoubleAtOffset(seg, pos, value);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    MemoryAccess.setDoubleAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffsetDoubles, final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetDoubles << DOUBLE_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    final long pos = getPosition();
    setPosition(pos + Float.BYTES);
    MemoryAccess.setFloatAtOffset(seg, pos, value);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    MemoryAccess.setFloatAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffsetFloats, final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetFloats << FLOAT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public void putInt(final int value) {
    final long pos = getPosition();
    setPosition(pos + Integer.BYTES);
    MemoryAccess.setIntAtOffset(seg, pos, value);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    MemoryAccess.setIntAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetInts << INT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public void putLong(final long value) {
    final long pos = getPosition();
    setPosition(pos + Long.BYTES);
    MemoryAccess.setLongAtOffset(seg, pos, value);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    MemoryAccess.setLongAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetLongs << LONG_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }

  @Override
  public void putShort(final short value) {
    final long pos = getPosition();
    setPosition(pos + Short.BYTES);
    MemoryAccess.setShortAtOffset(seg, pos, value);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    MemoryAccess.setShortAtOffset(seg, offsetBytes, value);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetShorts << SHORT_SHIFT, copyBytes);
    final MemorySegment dstSlice = seg.asSlice(pos, copyBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + copyBytes);
  }
}
