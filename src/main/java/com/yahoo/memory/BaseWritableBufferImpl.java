/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
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

  BaseWritableBufferImpl(final ResourceState state, final boolean localReadOnly) {
    super(state.getCapacity());
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    cumBaseOffset = state.getCumBaseOffset();
    this.localReadOnly = localReadOnly;
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + dstOffset,
            copyBytes);
  }

  @Override
  public byte getByte() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffset, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffset,
            copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
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
  public void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public long getCapacity() {
    assertValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset() {
    assertValid();
    return cumBaseOffset;
  }

  @Override
  public long getRegionOffset() {
    assertValid();
    return state.getRegionOffset();
  }

  @Override
  public boolean hasArray() {
    assertValid();
    return unsafeObj != null;
  }

  @Override
  public boolean hasByteBuffer() {
    assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    assertValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    assertValid();
    return state.isResourceReadOnly();
  }

  @Override
  boolean isLocalReadOnly() {
    assertValid();
    return localReadOnly;
  }

  @Override
  public boolean isSameResource(final Buffer that) {
    if (that == null) { return false; }
    checkValid();
    that.checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public ByteOrder getResourceOrder() {
    assertValid();
    return state.getResourceOrder();
  }

  @Override
  public boolean isSwapBytes() {
    return state.isSwapBytes();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
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
  public void putBoolean(final boolean value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthBooleans, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BOOLEAN_BASE_OFFSET + srcOffset,
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putByte(final byte value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffset, lengthBytes, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + srcOffset,
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  //OTHER XXX
  @Override
  public Object getArray() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return state.getByteBuffer();
  }

  @Override
  public void clear() {
    fill((byte)0);
  }

  @Override
  public void fill(final byte value) {
    checkValidForWrite();
    long pos = getPosition();
    long len = getEnd() - pos;
    checkInvariants(getStart(), pos + len, getEnd(), getCapacity());
    while (len > 0) {
      final long chunk = Math.min(len, CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD);
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
  void assertValid() {
    assert state.isValid() : "Buffer not valid.";
  }

  @Override
  void checkValid() {
    state.checkValid();
  }

  void checkValidForWrite() {
    checkValid();
    if (isResourceReadOnly()) {
      throw new ReadOnlyException("Buffer is read-only.");
    }
  }

  void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
  }

  void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
    assert !localReadOnly : "Buffer is read-only.";
  }
}
