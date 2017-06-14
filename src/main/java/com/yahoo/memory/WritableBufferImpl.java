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
import static com.yahoo.memory.UnsafeUtil.BOOLEAN_SHIFT;
import static com.yahoo.memory.UnsafeUtil.BYTE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of WritableBuffer
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableBufferImpl extends WritableBuffer {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  //Static variable for cases where bytebuf/array sizes are zero
  final static WritableBufferImpl MEMORY_ZERO_SIZE;

  static {
    MEMORY_ZERO_SIZE = new WritableBufferImpl(
        new ResourceState(new byte[0], Prim.BYTE, 0)
    );
  }

  WritableBufferImpl(final ResourceState state) {
    super(state);
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    unsafeObjHeader = state.getUnsafeObjectHeader();
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //REGIONS/DUPLICATES XXX
  @Override
  public Buffer duplicate() {
    return regOrDup(0, capacity, true);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    return regOrDup(0, capacity, true);
  }

  @Override
  public Buffer region() {
    return regOrDup(getPosition(), getEnd() - getPosition(), false);
  }

  @Override
  public WritableBuffer writableRegion() {
    return regOrDup(getPosition(), getEnd() - getPosition(), false);
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    return regOrDup(offsetBytes, capacityBytes, false);
  }

  private WritableBuffer regOrDup(final long offsetBytes, final long capacityBytes,
          final boolean dup) {
    checkValid();
    assert (offsetBytes + capacityBytes) <= capacity
            : "newOff + newCap: " + (offsetBytes + capacityBytes) + ", origCap: " + capacity;
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    if (!dup) { newState.putBaseBuffer(null); }
    return new WritableBufferImpl(newState);
  }

  //MEMORY XXX
  @Override
  public Memory asMemory() {
    checkValid();
    return new WritableMemoryImpl(state.copy());
  }

  @Override
  public WritableMemory asWritableMemory() {
    checkValid();
    return new WritableMemoryImpl(state.copy());
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    final boolean ret = unsafe.getBoolean(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_BOOLEAN_INDEX_SCALE);
    return ret;
  }

  @Override
  public boolean getBoolean(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + (dstOffset << BOOLEAN_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public byte getByte() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final byte ret = unsafe.getByte(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_BYTE_INDEX_SCALE);
    return ret;
  }

  @Override
  public byte getByte(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + (dstOffset << BYTE_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public char getChar() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_CHAR_INDEX_SCALE, capacity);
    final char ret = unsafe.getChar(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_CHAR_INDEX_SCALE);
    return ret;
  }

  @Override
  public char getChar(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_CHAR_BASE_OFFSET + (dstOffset << CHAR_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public double getDouble() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    final double ret = unsafe.getDouble(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_DOUBLE_INDEX_SCALE);
    return ret;
  }

  @Override
  public double getDouble(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_DOUBLE_BASE_OFFSET + (dstOffset << DOUBLE_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public float getFloat() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_FLOAT_INDEX_SCALE, capacity);
    final float ret = unsafe.getFloat(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_FLOAT_INDEX_SCALE);
    return ret;
  }

  @Override
  public float getFloat(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_FLOAT_BASE_OFFSET + (dstOffset << FLOAT_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public int getInt() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_INT_INDEX_SCALE, capacity);
    final int ret = unsafe.getInt(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_INT_INDEX_SCALE);
    return ret;
  }

  @Override
  public int getInt(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_INT_BASE_OFFSET + (dstOffset << INT_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public long getLong() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, capacity);
    final long ret = unsafe.getLong(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_LONG_INDEX_SCALE);
    return ret;
  }

  @Override
  public long getLong(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_LONG_BASE_OFFSET + (dstOffset << LONG_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  @Override
  public short getShort() {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_SHORT_INDEX_SCALE, capacity);
    final short ret = unsafe.getShort(unsafeObj, cumBaseOffset + pos);
    incrementPosition(ARRAY_SHORT_INDEX_SCALE);
    return ret;
  }

  @Override
  public short getShort(final long offsetBytes) {
    checkValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(pos, copyBytes, capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
            unsafeObj,
            cumBaseOffset + pos,
            dstArray,
            ARRAY_SHORT_BASE_OFFSET + (dstOffset << SHORT_SHIFT),
            copyBytes);
    incrementPosition(copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Buffer that,
          final long thatOffsetBytes, final long thatLengthBytes) {
    checkValid();
    ((WritableBufferImpl)that).checkValid();
    assertBounds(thisOffsetBytes, thisLengthBytes, capacity);
    assertBounds(thatOffsetBytes, thatLengthBytes, that.getCapacity());
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
    if (thisLengthBytes < thatLengthBytes) { return -1; }
    if (thisLengthBytes > thatLengthBytes) { return  1; }
    return 0;
  }

  //OTHER READ METHODS XXX

  @Override
  public long getCapacity() {
    checkValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset() {
    checkValid();
    return cumBaseOffset;
  }

  @Override
  public long getRegionOffset() {
    checkValid();
    return state.getRegionOffset();
  }

  @Override
  public ByteOrder getResourceOrder() {
    checkValid();
    return state.order();
  }

  @Override
  public boolean hasArray() {
    checkValid();
    return (unsafeObj != null);
  }

  @Override
  public boolean hasByteBuffer() {
    checkValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    checkValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    checkValid();
    return state.isResourceReadOnly();
  }

  @Override
  public boolean isSameResource(final Buffer that) {
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
    checkValid();
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
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_BOOLEAN_INDEX_SCALE);
  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_BOOLEAN_BASE_OFFSET + (srcOffset << BOOLEAN_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putByte(final byte value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    unsafe.putByte(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_BYTE_INDEX_SCALE);
  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + (srcOffset << BYTE_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putChar(final char value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_CHAR_INDEX_SCALE, capacity);
    unsafe.putChar(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_CHAR_INDEX_SCALE);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_CHAR_BASE_OFFSET + (srcOffset << CHAR_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    unsafe.putDouble(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_DOUBLE_INDEX_SCALE);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_DOUBLE_BASE_OFFSET + (srcOffset << DOUBLE_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_FLOAT_INDEX_SCALE, capacity);
    unsafe.putFloat(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_FLOAT_INDEX_SCALE);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_FLOAT_BASE_OFFSET + (srcOffset << FLOAT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putInt(final int value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_INT_INDEX_SCALE, capacity);
    unsafe.putInt(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_INT_INDEX_SCALE);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_INT_BASE_OFFSET + (srcOffset << INT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putLong(final long value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, capacity);
    unsafe.putLong(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_LONG_INDEX_SCALE);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_LONG_BASE_OFFSET + (srcOffset << LONG_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  @Override
  public void putShort(final short value) {
    checkValid();
    final long pos = getPosition();
    assertBounds(pos, ARRAY_SHORT_INDEX_SCALE, capacity);
    unsafe.putShort(unsafeObj, cumBaseOffset + pos, value);
    incrementPosition(ARRAY_SHORT_INDEX_SCALE);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPosition();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, capacity);
    unsafe.copyMemory(
            srcArray,
            ARRAY_SHORT_BASE_OFFSET + (srcOffset << SHORT_SHIFT),
            unsafeObj,
            cumBaseOffset + pos,
            copyBytes
            );
    incrementPosition(copyBytes);
  }

  //Atomic Write Methods XXX
  //Use WritableMemory for atomic methods

  //OTHER WRITE METHODS XXX
  @Override
  public Object getArray() {
    checkValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    checkValid();
    return state.getByteBuffer();
  }

  @Override
  public void clear() {
    fill((byte)0);
  }

  @Override
  public void fill(final byte value) {
    checkValid();
    final long pos = getPosition();
    final long len = getEnd() - pos;
    assertBounds(pos, len, capacity);
    unsafe.setMemory(unsafeObj, cumBaseOffset + pos, len, value);
  }

  //RESTRICTED READ AND WRITE XXX
  private final void checkValid() { //applies to both readable and writable
    assert state.isValid() : "Memory not valid.";
  }

  @Override
  ResourceState getResourceState() {
    return state;
  }
}
