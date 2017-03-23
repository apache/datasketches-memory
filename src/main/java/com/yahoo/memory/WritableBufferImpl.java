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

/**
 * @author Lee Rhodes
 */
class WritableBufferImpl extends WritableBuffer {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long capacity;
  final long cumBaseOffset; //Holds the cum offset to the start of data.

  WritableBufferImpl(final ResourceState state) {
    super(state);
    this.state = state;
    this.unsafeObj = state.getUnsafeObject();
    this.unsafeObjHeader = state.getUnsafeObjectHeader();
    this.capacity = state.getCapacity();
    this.cumBaseOffset = state.getCumBaseOffset();
  }

  //REGIONS/DUPLICATES XXX
  @Override
  public Buffer duplicate() {
    return writableRegion(0, this.capacity);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    return writableRegion(0, this.capacity);
  }

  @Override
  public Buffer region() {
    return writableRegion(getPos(), getHigh() - getPos());
  }

  @Override
  public WritableBuffer writableRegion() {
    return writableRegion(getPos(), getHigh() - getPos());
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes) {
    checkValid();
    assert offsetBytes + capacityBytes <= this.capacity
        : "newOff + newCap: " + (offsetBytes + capacityBytes) + ", origCap: " + this.capacity;
    final ResourceState newState = this.state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    return new WritableBufferImpl(newState);
  }

  //MEMORY XXX
  @Override
  public Memory asMemory() {
    checkValid();
    return new WritableMemoryImpl(this.state.copy());
  }

  @Override
  public WritableMemory asWritableMemory() {
    checkValid();
    return new WritableMemoryImpl(this.state.copy());
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BOOLEAN_INDEX_SCALE, this.capacity);
    final boolean ret = unsafe.getBoolean(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_BOOLEAN_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_BOOLEAN_BASE_OFFSET + (dstOffset << BOOLEAN_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public byte getByte() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, this.capacity);
    final byte ret = unsafe.getByte(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_BYTE_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_BYTE_BASE_OFFSET + (dstOffset << BYTE_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public char getChar() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_CHAR_INDEX_SCALE, this.capacity);
    final char ret = unsafe.getChar(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_CHAR_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_CHAR_BASE_OFFSET + (dstOffset << CHAR_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public double getDouble() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_DOUBLE_INDEX_SCALE, this.capacity);
    final double ret = unsafe.getDouble(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_DOUBLE_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_DOUBLE_BASE_OFFSET + (dstOffset << DOUBLE_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public float getFloat() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_FLOAT_INDEX_SCALE, this.capacity);
    final float ret = unsafe.getFloat(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_FLOAT_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_FLOAT_BASE_OFFSET + (dstOffset << FLOAT_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public int getInt() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_INT_INDEX_SCALE, this.capacity);
    final int ret = unsafe.getInt(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_INT_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_INT_BASE_OFFSET + (dstOffset << INT_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public long getLong() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, this.capacity);
    final long ret = unsafe.getLong(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_LONG_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_LONG_BASE_OFFSET + (dstOffset << LONG_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  @Override
  public short getShort() {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_SHORT_INDEX_SCALE, this.capacity);
    final short ret = unsafe.getShort(this.unsafeObj, this.cumBaseOffset + pos);
    incPos(ARRAY_SHORT_INDEX_SCALE);
    return ret;
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(pos, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + pos,
      dstArray,
      ARRAY_SHORT_BASE_OFFSET + (dstOffset << SHORT_SHIFT),
      copyBytes);
    incPos(copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copy, final isYYYY(), final areYYYY() XXX

  @Override
  public int compareTo(final Buffer that) {
    return 0;  //TODO convert the Memory based compareTo to static so it can be leveraged here.
  }

  @Override
  public boolean isAllBitsClear(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final int value = ~unsafe.getByte(unsafeObj, cumBaseOffset + pos) & bitMask & 0XFF;
    incPos(ARRAY_BYTE_INDEX_SCALE);
    return value == bitMask;
  }

  @Override
  public boolean isAllBitsSet(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final int value = unsafe.getByte(unsafeObj, cumBaseOffset + pos) & bitMask & 0XFF;
    incPos(ARRAY_BYTE_INDEX_SCALE);
    return value == bitMask;
  }

  @Override
  public boolean isAnyBitsClear(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final int value = ~unsafe.getByte(unsafeObj, cumBaseOffset + pos) & bitMask & 0XFF;
    incPos(ARRAY_BYTE_INDEX_SCALE);
    return value != 0;
  }

  @Override
  public boolean isAnyBitsSet(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final int value = unsafe.getByte(unsafeObj, cumBaseOffset + pos) & bitMask & 0XFF;
    return value != 0;
  }

  //OTHER READ METHODS XXX

  @Override
  public long getCapacity() {
    checkValid();
    return this.capacity;
  }

  @Override
  public long getCumulativeOffset() {
    checkValid();
    return this.cumBaseOffset;
  }

  @Override
  public boolean hasArray() {
    checkValid();
    return (this.unsafeObj != null);
  }

  @Override
  public boolean hasByteBuffer() {
    checkValid();
    return this.state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    checkValid();
    return this.state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    checkValid();
    return this.state.isResourceReadOnly();
  }

  @Override
  public boolean isValid() {
    return this.state.isValid();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
    checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = this.hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, this.state);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putBoolean(final boolean value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BOOLEAN_INDEX_SCALE, this.capacity);
    unsafe.putBoolean(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_BOOLEAN_INDEX_SCALE);
  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_BOOLEAN_BASE_OFFSET + (srcOffset << BOOLEAN_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putByte(final byte value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, this.capacity);
    unsafe.putByte(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_BYTE_INDEX_SCALE);
  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << BYTE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_BYTE_BASE_OFFSET + (srcOffset << BYTE_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putChar(final char value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_CHAR_INDEX_SCALE, this.capacity);
    unsafe.putChar(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_CHAR_INDEX_SCALE);
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << CHAR_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_CHAR_BASE_OFFSET + (srcOffset << CHAR_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putDouble(final double value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_DOUBLE_INDEX_SCALE, this.capacity);
    unsafe.putDouble(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_DOUBLE_INDEX_SCALE);
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << DOUBLE_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_DOUBLE_BASE_OFFSET + (srcOffset << DOUBLE_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putFloat(final float value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_FLOAT_INDEX_SCALE, this.capacity);
    unsafe.putFloat(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_FLOAT_INDEX_SCALE);
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << FLOAT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_FLOAT_BASE_OFFSET + (srcOffset << FLOAT_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putInt(final int value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_INT_INDEX_SCALE, this.capacity);
    unsafe.putInt(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_INT_INDEX_SCALE);
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << INT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_INT_BASE_OFFSET + (srcOffset << INT_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putLong(final long value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, this.capacity);
    unsafe.putLong(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_LONG_INDEX_SCALE);
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << LONG_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_LONG_BASE_OFFSET + (srcOffset << LONG_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  @Override
  public void putShort(final short value) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_SHORT_INDEX_SCALE, this.capacity);
    unsafe.putShort(this.unsafeObj, this.cumBaseOffset + pos, value);
    incPos(ARRAY_SHORT_INDEX_SCALE);
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int length) {
    checkValid();
    final long pos = getPos();
    final long copyBytes = length << SHORT_SHIFT;
    assertBounds(srcOffset, length, srcArray.length);
    assertBounds(pos, copyBytes, this.capacity);
    unsafe.copyMemory(
      srcArray,
      ARRAY_SHORT_BASE_OFFSET + (srcOffset << SHORT_SHIFT),
      this.unsafeObj,
      this.cumBaseOffset + pos,
      copyBytes
      );
    incPos(copyBytes);
  }

  //Atomic Write Methods XXX
  @Override
  public long getAndAddLong(final long delta) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + pos;
    incPos(ARRAY_LONG_INDEX_SCALE);
    return UnsafeUtil.compatibilityMethods.getAndAddLong(unsafeObj, add, delta) + delta;
  }

  @Override
  public long getAndSetLong(final long newValue) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + pos;
    incPos(ARRAY_LONG_INDEX_SCALE);
    return UnsafeUtil.compatibilityMethods.getAndSetLong(unsafeObj, add, newValue);
  }

  @Override
  public boolean compareAndSwapLong(final long expect, final long update) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_INT_INDEX_SCALE, capacity);
    incPos(ARRAY_LONG_INDEX_SCALE);
    return unsafe.compareAndSwapLong(unsafeObj, cumBaseOffset + pos, expect, update);
  }

  //OTHER WRITE METHODS XXX
  @Override
  public Object getArray() {
    checkValid();
    return unsafeObj;
  }

  @Override
  public void clear() {
    fill((byte)0);
  }

  @Override
  public void clearBits(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, capacity);
    final long cumBaseOff = this.cumBaseOffset + pos;
    int value = unsafe.getByte(this.unsafeObj, cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(this.unsafeObj, cumBaseOff, (byte)value);
    incPos(ARRAY_BYTE_INDEX_SCALE);
  }

  @Override
  public void fill(final byte value) {
    checkValid();
    final long pos = getPos();
    final long len = getHigh() - pos;
    assertBounds(pos, len, this.capacity);
    unsafe.setMemory(this.unsafeObj, this.cumBaseOffset + pos, len, value);
  }

  @Override
  public void setBits(final byte bitMask) {
    checkValid();
    final long pos = getPos();
    assertBounds(pos, ARRAY_BYTE_INDEX_SCALE, this.capacity);
    final long myOffset = this.cumBaseOffset + pos;
    final byte value = unsafe.getByte(this.unsafeObj, myOffset);
    unsafe.putByte(this.unsafeObj, myOffset, (byte)(value | bitMask));
    incPos(ARRAY_BYTE_INDEX_SCALE);
  }

  //OTHER XXX
  @Override
  public MemoryRequest getMemoryRequest() {
    checkValid();
    return this.state.getMemoryRequest();
  }

  //RESTRICTED READ AND WRITE XXX
  private final void checkValid() { //applies to both readable and writable
    assert this.state.isValid() : "Memory not valid.";
  }
}
