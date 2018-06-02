/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
 * Common base of native-ordered and non-native-ordered {@link WritableBuffer} implementations.
 * Contains methods which are agnostic to the byte order.
 */
abstract class BaseWritableBufferImpl extends WritableBuffer {
  final BaseWritableMemoryImpl originMemory;

  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableBufferImpl ZERO_SIZE_BUFFER;

  static {
    ZERO_SIZE_BUFFER = new WritableBufferImpl(new byte[0], 0L, 0L, 0L, true, null, null, null);
  }

  //called from one of the Endian-sensitive WritableBufferImpls
  BaseWritableBufferImpl(
      final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes, final boolean readOnly, final ByteOrder byteOrder,
      final ByteBuffer byteBuf, final StepBoolean valid, final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly, byteOrder,
        byteBuf, valid);
    this.originMemory = originMemory;
  }

  //DUPLICATES XXX
  @Override
  public Buffer duplicate() {
    return writableDuplicateImpl(true);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    if (isReadOnly()) {
      throw new ReadOnlyException("Writable duplicate of a read-only Buffer is not allowed.");
    }
    return writableDuplicateImpl(false);
  }

  abstract WritableBuffer writableDuplicateImpl(boolean localReadOnly);

  //REGIONS XXX
  @Override
  public Buffer region() {
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), true);
  }

  @Override
  public WritableBuffer writableRegion() {
    if (isReadOnly()) {
      throw new ReadOnlyException("Writable region of a read-only Buffer is not allowed.");
    }
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), false);
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    if (capacityBytes == 0) { return ZERO_SIZE_BUFFER; }
    if (isReadOnly()) {
      throw new ReadOnlyException("Writable region of a read-only Buffer is not allowed.");
    }
    return writableRegionImpl(offsetBytes, capacityBytes, false);
  }

  abstract WritableBuffer writableRegionImpl(long offsetBytes, long capacityBytes,
      boolean localReadOnly);

  //MEMORY XXX
  @Override
  public Memory asMemory() {
    return originMemory;
  }

  @Override
  public WritableMemory asWritableMemory() {
    if (isReadOnly()) {
      throw new ReadOnlyException("Converting a read-only Buffer to a writable Memory is not allowed.");
    }
    return originMemory;
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public final boolean getBoolean() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  @Override
  public final boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  @Override
  public final void getBooleanArray(final boolean[] dstArray, final int dstOffsetBooleans,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBooleans, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset() + pos,
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + dstOffsetBooleans,
            copyBytes);
  }

  @Override
  public final byte getByte() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  @Override
  public final void getByteArray(final byte[] dstArray, final int dstOffsetBytes,
      final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset() + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffsetBytes,
            copyBytes);
  }

  //PRIMITIVE getXXX() Native Endian (used by both endians) XXX
  final char getNativeOrderedChar() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  final char getNativeOrderedChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  final int getNativeOrderedInt() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  final int getNativeOrderedInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  final long getNativeOrderedLong() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  final long getNativeOrderedLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  final short getNativeOrderedShort() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(getUnsafeObject(), getCumulativeOffset() + pos);
  }

  final short getNativeOrderedShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(getUnsafeObject(), getCumulativeOffset() + offsetBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Buffer thatBuf, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(this, thisOffsetBytes, thisLengthBytes,
        thatBuf, thatOffsetBytes, thatLengthBytes);
  }

  /*
   * Develper notes: There is no copyTo for Buffers because of the ambiguity of what to do with
   * the positional values. Switch to Memory view to do copyTo.
   */

  //OTHER READ METHODS XXX
  @Override
  public final long getRegionOffset() {
    return super.getRegOffset();
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public final void putBoolean(final boolean value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  @Override
  public final void putBooleanArray(final boolean[] srcArray, final int srcOffsetBooleans,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetBooleans, lengthBooleans, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BOOLEAN_BASE_OFFSET + srcOffsetBooleans,
            getUnsafeObject(),
            getCumulativeOffset() + pos,
            copyBytes);
  }

  @Override
  public final void putByte(final byte value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  @Override
  public final void putByteArray(final byte[] srcArray, final int srcOffsetBytes,
      final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetBytes, lengthBytes, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + srcOffsetBytes,
            getUnsafeObject(),
            getCumulativeOffset() + pos,
            copyBytes);
  }

  //PRIMITIVE putXXX() Native Endian (used by both endians) XXX
  final void putNativeOrderedChar(final char value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  final void putNativeOrderedChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  final void putNativeOrderedInt(final int value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  final void putNativeOrderedInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  final void putNativeOrderedLong(final long value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  final void putNativeOrderedLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  final void putNativeOrderedShort(final short value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(getUnsafeObject(), getCumulativeOffset() + pos, value);
  }

  final void putNativeOrderedShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(getUnsafeObject(), getCumulativeOffset() + offsetBytes, value);
  }

  //OTHER XXX
  @Override
  public final Object getArray() {
    assertValid();
    return getUnsafeObject();
  }

  @Override
  public final void clear() {
    fill((byte)0);
  }

  @Override
  public final void fill(final byte value) {
    checkValidForWrite();
    long pos = getPosition();
    long len = getEnd() - pos;
    checkInvariants(getStart(), pos + len, getEnd(), getCapacity());
    while (len > 0) {
      final long chunk = Math.min(len, CompareAndCopy.UNSAFE_COPY_THRESHOLD_BYTES);
      unsafe.setMemory(getUnsafeObject(), getCumulativeOffset() + pos, chunk, value);
      pos += chunk;
      len -= chunk;
    }
  }
}
