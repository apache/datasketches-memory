/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
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
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
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
 * Implementation of WritableBuffer
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableBufferImpl extends WritableBuffer {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  BaseWritableMemoryImpl originMemory = null; //If I came from here ...

  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableBufferImpl ZERO_SIZE_BUFFER;

  static {
    ZERO_SIZE_BUFFER = new WritableBufferImpl(new ResourceState(new byte[0], Prim.BYTE, 0));
  }

  WritableBufferImpl(final ResourceState state) {
    super(state);
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    unsafeObjHeader = state.getUnsafeObjectHeader();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //DUPLICATES & REGIONS XXX
  @Override
  public Buffer duplicate() {
    return writableDuplicate();
  }

  @Override
  public WritableBuffer writableDuplicate() {
    state.checkValid();
    checkBounds(0, capacity, capacity);
    final WritableBufferImpl wBufImpl = new WritableBufferImpl(state);
    wBufImpl.setStartPositionEnd(getStart(), getPosition(), getEnd());
    return wBufImpl;
  }

  @Override
  public Buffer region() {
    return writableRegion(getPosition(), getEnd() - getPosition());
  }

  @Override
  public WritableBuffer writableRegion() {
    return writableRegion(getPosition(), getEnd() - getPosition());
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    state.checkValid();
    checkBounds(offsetBytes, capacityBytes, capacity);
    if (capacityBytes == 0) { return ZERO_SIZE_BUFFER; }
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    final WritableBufferImpl wBufImpl = new WritableBufferImpl(newState);
    wBufImpl.setStartPositionEnd(0L, 0L, capacityBytes);
    return wBufImpl;
  }

  //MEMORY XXX
  @Override
  public Memory asMemory() {
    return asWritableMemory();
  }

  @Override
  public WritableMemory asWritableMemory() {
    state.checkValid();
    if (originMemory != null) { return originMemory; }
    originMemory = new WritableMemoryImpl(state);
    return originMemory;
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
    unsafe.copyMemory(
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
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffset,
            copyBytes);
  }

  @Override
  public char getChar() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public char getChar(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int lengthChars) {
    state.checkValid();
    checkBounds(dstOffset, lengthChars, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) dstOffset) << CHAR_SHIFT),
            copyBytes);
  }

  @Override
  public double getDouble() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset,
      final int lengthDoubles) {
    state.checkValid();
    checkBounds(dstOffset, lengthDoubles, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffset) << DOUBLE_SHIFT),
            copyBytes);
  }

  @Override
  public float getFloat() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int lengthFloats) {
    state.checkValid();
    checkBounds(dstOffset, lengthFloats, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffset) << FLOAT_SHIFT),
            copyBytes);
  }

  @Override
  public int getInt() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public int getInt(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int lengthInts) {
    state.checkValid();
    checkBounds(dstOffset, lengthInts, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_INT_BASE_OFFSET + (((long) dstOffset) << INT_SHIFT),
            copyBytes);
  }

  @Override
  public long getLong() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public long getLong(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int lengthLongs) {
    state.checkValid();
    checkBounds(dstOffset, lengthLongs, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_LONG_BASE_OFFSET + (((long) dstOffset) << LONG_SHIFT),
            copyBytes);
  }

  @Override
  public short getShort() {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + pos);
  }

  @Override
  public short getShort(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int lengthShorts) {
    state.checkValid();
    checkBounds(dstOffset, lengthShorts, dstArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) dstOffset) << SHORT_SHIFT),
            copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Buffer that,
          final long thatOffsetBytes, final long thatLengthBytes) {
    state.checkValid();
    that.getResourceState().checkValid();
    checkBounds(thisOffsetBytes, thisLengthBytes, capacity);
    checkBounds(thatOffsetBytes, thatLengthBytes, that.capacity);
    final long thisAdd = getCumulativeOffset() + thisOffsetBytes;
    final long thatAdd = that.getCumulativeOffset() + thatOffsetBytes;
    final Object thisObj = (isDirect()) ? null : unsafeObj;
    final Object thatObj = (that.isDirect()) ? null : ((WritableBuffer)that).getArray();
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
  public ByteOrder getResourceOrder() {
    state.assertValid();
    return state.order();
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
    unsafe.copyMemory(
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
    unsafe.copyMemory(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + srcOffset,
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putChar(final char value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int lengthChars) {
    state.checkValid();
    checkBounds(srcOffset, lengthChars, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_CHAR_BASE_OFFSET + (((long) srcOffset) << CHAR_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset,
      final int lengthDoubles) {
    state.checkValid();
    checkBounds(srcOffset, lengthDoubles, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffset) << DOUBLE_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int lengthFloats) {
    state.checkValid();
    checkBounds(srcOffset, lengthFloats, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffset) << FLOAT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putInt(final int value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int lengthInts) {
    state.checkValid();
    checkBounds(srcOffset, lengthInts, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_INT_BASE_OFFSET + (((long) srcOffset) << INT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putLong(final long value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int lengthLongs) {
    state.checkValid();
    checkBounds(srcOffset, lengthLongs, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_LONG_BASE_OFFSET + (((long) srcOffset) << LONG_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes);
  }

  @Override
  public void putShort(final short value) {
    state.assertValid();
    final long pos = getPosition();
    incrementAndAssertPosition(pos, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + pos, value);
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int lengthShorts) {
    state.checkValid();
    checkBounds(srcOffset, lengthShorts, srcArray.length);
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPosition(pos, copyBytes);
    unsafe.copyMemory(
            srcArray,
            ARRAY_SHORT_BASE_OFFSET + (((long) srcOffset) << SHORT_SHIFT),
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
