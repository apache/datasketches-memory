/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.BaseWritableMemoryImpl.copyMemoryCheckingDifferentObject;
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
 * compareTo, etc., use hard checks (checkValid() and checkBounds()), which execute at runtime
 * and throw exceptions if violated. The cost of the runtime checks are minor compared to
 * the rest of the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assertValid() and
 * assertBounds()), which only execute when asserts are enabled and JIT will remove them
 * entirely from production runtime code. The offset versions of the light weight methods
 * will simplify to a single unsafe call, which is further simplified by JIT to an intrinsic
 * that is often a single CPU instruction.
 */

/**
 * Common base of native-ordered and non-native-ordered {@link WritableBuffer} implementations.
 * Contains methods which are agnostic to the byte order.
 */
abstract class BaseWritableBufferImpl extends WritableBuffer {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.

  BaseWritableBufferImpl(final ResourceState state) {
    super(state);
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public boolean getBoolean(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset,
      final int lengthBooleans) {
    state.checkValid();
    checkBounds(dstOffset, lengthBooleans, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPosition(pos, copyBytes);
    copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + dstOffset,
            copyBytes);
  }

  @Override
  public byte getByte() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int lengthBytes) {
    state.checkValid();
    checkBounds(dstOffset, lengthBytes, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPosition(pos, copyBytes);
    copyMemoryCheckingDifferentObject(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffset,
            copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Buffer thatBuf,
          final long thatOffsetBytes, final long thatLengthBytes) {
    state.checkValid();
    checkBounds(thisOffsetBytes, thisLengthBytes, capacity);
    final BaseWritableBufferImpl that = (BaseWritableBufferImpl) thatBuf;
    that.state.checkValid();
    checkBounds(thatOffsetBytes, thatLengthBytes, that.capacity);
    final long thisAdd = getCumulativeOffset() + thisOffsetBytes;
    final long thatAdd = that.getCumulativeOffset() + thatOffsetBytes;
    final Object thisObj = this.unsafeObj;
    final Object thatObj = that.unsafeObj;
    final long lenBytes = Math.min(thisLengthBytes, thatLengthBytes);
    for (long i = 0; i < lenBytes; i++) {
      final int thisByte = unsafe.getByte(thisObj, thisAdd + i);
      final int thatByte = unsafe.getByte(thatObj, thatAdd + i);
      if (thisByte < thatByte) { return -1; }
      if (thisByte > thatByte) { return  1; }
    }
    return Long.compare(thisLengthBytes, thatLengthBytes);
  }

  /*
   * Develper notes: There is no copyTo for Buffers because of the ambiguity of what to do with
   * the positional values. Switch to Memory view to do copyTo.
   */

  //OTHER READ METHODS XXX
  @Override
  public void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    state.checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public long getCapacity() {
    state.assertValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset() {
    state.assertValid();
    return cumBaseOffset;
  }

  @Override
  public long getRegionOffset() {
    state.assertValid();
    return state.getRegionOffset();
  }

  @Override
  public boolean hasArray() {
    state.assertValid();
    return (unsafeObj != null);
  }

  @Override
  public boolean hasByteBuffer() {
    state.assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    state.assertValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    state.assertValid();
    return state.isResourceReadOnly();
  }

  @Override
  public boolean isSameResource(final Buffer that) {
    if (that == null) { return false; }
    state.assertValid();
    that.getResourceState().checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public ByteOrder getResourceOrder() {
    state.assertValid();
    return state.order();
  }

  @Override
  public boolean swapBytes() {
    return state.isSwapBytes();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
    state.checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putBoolean(final boolean value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset,
      final int lengthBooleans) {
    state.checkValid();
    checkBounds(srcOffset, lengthBooleans, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPosition(pos, copyBytes);
    copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BOOLEAN_BASE_OFFSET + srcOffset,
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putByte(final byte value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int lengthBytes) {
    state.checkValid();
    checkBounds(srcOffset, lengthBytes, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPosition(pos, copyBytes);
    copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + srcOffset,
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  //OTHER XXX
  @Override
  public Object getArray() {
    state.assertValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    state.assertValid();
    return state.getByteBuffer();
  }

  @Override
  public void clear() {
    fill((byte)0);
  }

  @Override
  public void fill(final byte value) {
    state.checkValid();
    final long pos = getPosition();
    final long len = getEnd() - pos;
    checkInvariants(getStart(), pos + len, getEnd(), getCapacity());
    unsafe.setMemory(unsafeObj, cumBaseOffset + pos, len, value);
  }

  @Override
  final ResourceState getResourceState() {
    state.assertValid();
    return state;
  }
}
