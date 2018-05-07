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
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
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
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  final boolean localReadOnly;
  final BaseWritableMemoryImpl originMemory;

  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableBufferImpl ZERO_SIZE_BUFFER;

  static {
    final ResourceState state = new ResourceState(new byte[0], Prim.BYTE, 0);
    ZERO_SIZE_BUFFER = new WritableBufferImpl(state, true, BaseWritableMemoryImpl.ZERO_SIZE_MEMORY);
  }

  //called from one of the Endian-sensitive WritableBufferImpls
  BaseWritableBufferImpl(final ResourceState state, final boolean localReadOnly,
      final BaseWritableMemoryImpl originMemory) {
    super(state.getCapacity());
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    cumBaseOffset = state.getCumBaseOffset();
    this.localReadOnly = localReadOnly;
    this.originMemory = originMemory;
  }

  //DUPLICATES XXX
  @Override
  public Buffer duplicate() {
    return writableDuplicateImpl(true);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    if (localReadOnly) {
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
    if (localReadOnly) {
      throw new ReadOnlyException("Writable region of a read-only Buffer is not allowed.");
    }
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), false);
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    if (localReadOnly) {
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
    if (localReadOnly) {
      throw new ReadOnlyException("This Buffer is Read-Only.");
    }
    return originMemory;
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public final boolean getBoolean() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public final boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public final void getBooleanArray(final boolean[] dstArray, final int dstOffsetBooleans,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBooleans, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + dstOffsetBooleans,
            copyBytes);
  }

  @Override
  public final byte getByte() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public final void getByteArray(final byte[] dstArray, final int dstOffsetBytes,
      final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffsetBytes,
            copyBytes);
  }

  //PRIMITIVE getXXX() Native Endian (used by both endians) XXX
  final char getNativeOrderedChar() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + pos);
  }

  final char getNativeOrderedChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final int getNativeOrderedInt() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + pos);
  }

  final int getNativeOrderedInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final long getNativeOrderedLong() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + pos);
  }

  final long getNativeOrderedLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final short getNativeOrderedShort() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + pos);
  }

  final short getNativeOrderedShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Buffer thatBuf, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(state, thisOffsetBytes, thisLengthBytes,
        thatBuf.getResourceState(), thatOffsetBytes, thatLengthBytes);
  }

  /*
   * Develper notes: There is no copyTo for Buffers because of the ambiguity of what to do with
   * the positional values. Switch to Memory view to do copyTo.
   */

  //OTHER READ METHODS XXX
  @Override
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public final long getCapacity() {
    assertValid();
    return capacity;
  }

  @Override
  public final long getCumulativeOffset() {
    assertValid();
    return cumBaseOffset;
  }

  @Override
  public final long getRegionOffset() {
    assertValid();
    return state.getRegionOffset();
  }

  @Override
  public final boolean hasArray() {
    assertValid();
    return unsafeObj != null;
  }

  @Override
  public final boolean hasByteBuffer() {
    assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public final boolean isDirect() {
    assertValid();
    return state.isDirect();
  }

  @Override
  public final boolean isReadOnly() {
    assertValid();
    return state.isResourceReadOnly() || localReadOnly;
  }

  @Override
  public final boolean isSameResource(final Buffer that) {
    if (that == null) { return false; }
    checkValid();
    that.checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public final boolean isValid() {
    return state.isValid();
  }

  @Override
  public final ByteOrder getResourceOrder() {
    assertValid();
    return state.getResourceOrder();
  }

  @Override
  public final boolean isSwapBytes() {
    return state.isSwapBytes();
  }

  @Override
  public final String toHexString(final String header, final long offsetBytes,
      final int lengthBytes) {
    checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state, localReadOnly);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public final void putBoolean(final boolean value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
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
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public final void putByte(final byte value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
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
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  //PRIMITIVE putXXX() Native Endian (used by both endians) XXX
  final void putNativeOrderedChar(final char value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + pos, value);
  }

  final void putNativeOrderedChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedInt(final int value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + pos, value);
  }

  final void putNativeOrderedInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedLong(final long value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + pos, value);
  }

  final void putNativeOrderedLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedShort(final short value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + pos, value);
  }

  final void putNativeOrderedShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  //OTHER XXX
  @Override
  public final Object getArray() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public final ByteBuffer getByteBuffer() {
    assertValid();
    return state.getByteBuffer();
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
      unsafe.setMemory(unsafeObj, cumBaseOffset + pos, chunk, value);
      pos += chunk;
      len -= chunk;
    }
  }

  @Override
  final ResourceState getResourceState() {
    assertValid();
    return state;
  }

  @Override
  final void assertValid() {
    assert state.isValid() : "Buffer not valid.";
  }

  @Override
  final void checkValid() {
    state.checkValid();
  }

  final void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
  }

  final void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
    assert !localReadOnly : "Buffer is read-only.";
  }
}
