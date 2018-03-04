/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

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
 * Implementation of WritableBuffer
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableBufferImpl extends BaseWritableBufferImpl {
  BaseWritableMemoryImpl originMemory = null; //If I came from here ...

  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableBufferImpl ZERO_SIZE_BUFFER;

  static {
    ZERO_SIZE_BUFFER = new WritableBufferImpl(new ResourceState(new byte[0], Prim.BYTE, 0), true);
    ZERO_SIZE_BUFFER.originMemory = WritableMemoryImpl.ZERO_SIZE_MEMORY;
  }

  WritableBufferImpl(final ResourceState state, final boolean localReadOnly) {
    super(state, localReadOnly);
  }

  //DUPLICATES & REGIONS XXX
  @Override
  public Buffer duplicate() {
    return writableDuplicateImpl(false);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    return writableDuplicateImpl(localReadOnly);
  }

  private WritableBuffer writableDuplicateImpl(final boolean localReadOnly) {
    checkValid();
    if (capacity == 0) { return ZERO_SIZE_BUFFER; }
    final WritableBufferImpl wBufImpl = new WritableBufferImpl(state, localReadOnly);
    wBufImpl.setStartPositionEnd(getStart(), getPosition(), getEnd());
    return wBufImpl;
  }

  @Override
  public Buffer region() {
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), true);

  }

  @Override
  public WritableBuffer writableRegion() {
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), false);
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, localReadOnly);
  }

  private WritableBuffer writableRegionImpl(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly) {
    checkValidAndBounds(offsetBytes, capacityBytes);
    if (capacityBytes == 0) { return ZERO_SIZE_BUFFER; }
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    final WritableBufferImpl wBufImpl = new WritableBufferImpl(newState, localReadOnly);
    wBufImpl.setStartPositionEnd(0L, 0L, capacityBytes);
    return wBufImpl;
  }

  //MEMORY XXX
  @Override
  public Memory asMemory() {
    return asWritableMemoryImpl(true);
  }

  @Override
  public WritableMemory asWritableMemory() {
    return asWritableMemoryImpl(localReadOnly);
  }

  private WritableMemory asWritableMemoryImpl(final boolean localReadOnly) {
    checkValid();
    if ((originMemory != null) && (originMemory.localReadOnly == localReadOnly)) {
      return originMemory;
    }
    return new WritableMemoryImpl(state, localReadOnly);
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public char getChar() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public char getChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthChars, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) dstOffset) << CHAR_SHIFT),
            copyBytes);
  }

  @Override
  public double getDouble() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthDoubles, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffset) << DOUBLE_SHIFT),
            copyBytes);
  }

  @Override
  public float getFloat() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthFloats, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffset) << FLOAT_SHIFT),
            copyBytes);
  }

  @Override
  public int getInt() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public int getInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthInts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_INT_BASE_OFFSET + (((long) dstOffset) << INT_SHIFT),
            copyBytes);
  }

  @Override
  public long getLong() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public long getLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthLongs, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_LONG_BASE_OFFSET + (((long) dstOffset) << LONG_SHIFT),
            copyBytes);
  }

  @Override
  public short getShort() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public short getShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthShorts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) dstOffset) << SHORT_SHIFT),
            copyBytes);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putChar(final char value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthChars, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) srcOffset) << CHAR_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthDoubles, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffset) << DOUBLE_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthFloats, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffset) << FLOAT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putInt(final int value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthInts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_INT_BASE_OFFSET + (((long) srcOffset) << INT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putLong(final long value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthLongs, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_LONG_BASE_OFFSET + (((long) srcOffset) << LONG_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putShort(final short value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthShorts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) srcOffset) << SHORT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }
}
