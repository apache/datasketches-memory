/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;
import static com.yahoo.memory.Util.nativeOrder;

import java.nio.ByteBuffer;

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
 * Implementation of {@link WritableBuffer} for native endian byte order. Non-native variant is
 * {@link NonNativeWritableBufferImpl}.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class WritableBufferImpl extends BaseWritableBufferImpl {

  //ctor for all parameters
  WritableBufferImpl(
      final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes, final boolean readOnly, final ByteBuffer byteBuf,
      final StepBoolean valid, final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly, nativeOrder,
        byteBuf, valid, originMemory);
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public char getChar() {
    return getNativeOrderedChar();
  }

  @Override
  public char getChar(final long offsetBytes) {
    return getNativeOrderedChar(offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetChars, lengthChars, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) dstOffsetChars) << CHAR_SHIFT),
            copyBytes);
  }

  @Override
  public double getDouble() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(getUnsafeObject(), getCumulativeOffset(pos));
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffsetDoubles,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetDoubles, lengthDoubles, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffsetDoubles) << DOUBLE_SHIFT),
            copyBytes);
  }

  @Override
  public float getFloat() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(getUnsafeObject(), getCumulativeOffset(pos));
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffsetFloats,
      final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetFloats, lengthFloats, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffsetFloats) << FLOAT_SHIFT),
            copyBytes);
  }

  @Override
  public int getInt() {
    return getNativeOrderedInt();
  }

  @Override
  public int getInt(final long offsetBytes) {
    return getNativeOrderedInt(offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetInts, lengthInts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_INT_BASE_OFFSET + (((long) dstOffsetInts) << INT_SHIFT),
            copyBytes);
  }

  @Override
  public long getLong() {
    return getNativeOrderedLong();
  }

  @Override
  public long getLong(final long offsetBytes) {
    return getNativeOrderedLong(offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetLongs, lengthLongs, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_LONG_BASE_OFFSET + (((long) dstOffsetLongs) << LONG_SHIFT),
            copyBytes);
  }

  @Override
  public short getShort() {
    return getNativeOrderedShort();
  }

  @Override
  public short getShort(final long offsetBytes) {
    return getNativeOrderedShort(offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffsetShorts,
      final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetShorts, lengthShorts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) dstOffsetShorts) << SHORT_SHIFT),
            copyBytes);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putChar(final char value) {
    putNativeOrderedChar(value);
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    putNativeOrderedChar(offsetBytes, value);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetChars, lengthChars, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) srcOffsetChars) << CHAR_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffsetDoubles,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetDoubles, lengthDoubles, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffsetDoubles) << DOUBLE_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffsetFloats,
      final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetFloats, lengthFloats, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffsetFloats) << FLOAT_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public void putInt(final int value) {
    putNativeOrderedInt(value);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    putNativeOrderedInt(offsetBytes, value);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetInts, lengthInts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_INT_BASE_OFFSET + (((long) srcOffsetInts) << INT_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public void putLong(final long value) {
    putNativeOrderedLong(value);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    putNativeOrderedLong(offsetBytes, value);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetLongs, lengthLongs, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_LONG_BASE_OFFSET + (((long) srcOffsetLongs) << LONG_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public void putShort(final short value) {
    putNativeOrderedShort(value);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    putNativeOrderedShort(offsetBytes, value);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetShorts, lengthShorts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) srcOffsetShorts) << SHORT_SHIFT),
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }
}
