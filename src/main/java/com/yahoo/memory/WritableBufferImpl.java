/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.BOOLEAN_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

/**
 * @author Lee Rhodes
 */
class WritableBufferImpl extends WritableBuffer implements Positional {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long capacity;
  final long cumBaseOffset; //Holds the cum offset to the start of data.
  long relOffset;
  long upperBound;
  long tag;

  WritableBufferImpl(final ResourceState state) {
    this.state = state;
    this.unsafeObj = state.getUnsafeObject();
    this.unsafeObjHeader = state.getUnsafeObjectHeader();
    this.capacity = state.getCapacity();
    this.cumBaseOffset = state.getCumBaseOffset();
    this.relOffset = 0L;
    this.upperBound = this.capacity;
    this.tag = -1L;
  }

  //REGIONS XXX
  @Override
  public Buffer region() {
    checkValid();
    return writableRegion(getRelOffset(), getUpperBound() - getRelOffset());
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

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean() {
    checkValid();
    assertBounds(relOffset, ARRAY_BOOLEAN_INDEX_SCALE, this.capacity);
    relOffset++;
    return unsafe.getBoolean(this.unsafeObj, this.cumBaseOffset + relOffset);
  }

  @Override
  public void getBooleanArray(final boolean[] dstArray, final int dstOffset, final int length) {
    checkValid();
    final long copyBytes = length << BOOLEAN_SHIFT;
    assertBounds(relOffset, copyBytes, this.capacity);
    assertBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
      this.unsafeObj,
      this.cumBaseOffset + relOffset,
      dstArray,
      ARRAY_BOOLEAN_BASE_OFFSET + (dstOffset << BOOLEAN_SHIFT),
      copyBytes);
    relOffset += copyBytes;
  }

  @Override
  public byte getByte() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getByteArray(final byte[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public char getChar() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public double getDouble() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public float getFloat() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public int getInt() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getLong() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public short getShort() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffset, final int length) {
    // TODO Auto-generated method stub

  }

  //OTHER PRIMITIVE READ METHODS: copy, final isYYYY(), final areYYYY() XXX
  @Override
  public int compareTo(final long thisLengthBytes, final Buffer that, final long thatLengthBytes) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public void copyTo(final WritableBuffer destination, final long lengthBytes) {
    // TODO Auto-generated method stub

  }

  @Override
  public boolean isAllBitsClear(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAllBitsSet(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsClear(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAnyBitsSet(final byte bitMask) {
    // TODO Auto-generated method stub
    return false;
  }

  //OTHER READ METHODS XXX

  @Override
  public long getCapacity() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public long getCumulativeOffset() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean hasArray() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean hasByteBuffer() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isDirect() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isResourceReadOnly() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isValid() {
    // TODO Auto-generated method stub
    return false;
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
    sb.append("Call Params         : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, this.state);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putBoolean(final boolean value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putBooleanArray(final boolean[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByte(final byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putByteArray(final byte[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putChar(final char value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDouble(final double value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloat(final float value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putInt(final int value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLong(final long value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShort(final short value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffset, final int length) {
    // TODO Auto-generated method stub

  }

  @Override
  public long getAndAddLong(final long delta) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean compareAndSwapLong(final long expect, final long update) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public long getAndSetLong(final long newValue) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Object getArray() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void clear() {
    // TODO Auto-generated method stub

  }

  @Override
  public void clearBits(final byte bitMask) {
    // TODO Auto-generated method stub

  }

  @Override
  public void fill(final byte value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void setBits(final byte bitMask) {
    // TODO Auto-generated method stub

  }

  @Override
  public MemoryRequest getMemoryRequest() {
    // TODO Auto-generated method stub
    return null;
  }

  //POSITIONAL

  @Override
  public long getRelOffset() { //get position
    return this.relOffset;
  }

  @Override
  public WritableBufferImpl setRelOffset(final long relOff) { //set position
    assertBounds(0, relOff, this.upperBound);
    this.relOffset = relOff;
    if (this.tag > this.relOffset) { tag = -1; }
    return this;
  }

  @Override
  public long getUpperBound() { //get limit
    return this.upperBound;
  }

  @Override
  public WritableBufferImpl setUpperBound(final long ub) { //set limit
    assertBounds(0, ub, this.capacity);
    this.upperBound = ub;
    if (relOffset > upperBound) { relOffset = upperBound; }
    if (tag > upperBound) { tag = -1; }
    return this;
  }

  @Override
  public WritableBufferImpl setTagToRelOffset() { //set mark
    this.tag = this.relOffset;
    return this;
  }

  @Override
  public WritableBufferImpl setRelOffsetToTag() { //reset
    assertBounds(0, tag, this.capacity);
    this.relOffset = this.tag;
    return this;
  }

  @Override
  public WritableBufferImpl reset() { //clear
    this.relOffset = 0;
    this.upperBound = this.getCapacity();
    this.tag = -1;
    return this;
  }

  @Override
  public WritableBufferImpl exchange() { //flip
    this.upperBound = this.relOffset;
    this.relOffset = 0;
    this.tag = -1;
    return this;
  }

  @Override
  public WritableBufferImpl setRelOffsetToZero() {
    this.relOffset = 0;
    this.tag = -1;
    return this;
  }

  @Override
  public long getRemaining() {
    return this.upperBound - this.relOffset;
  }

  @Override
  public boolean hasRemaining() {
    return (this.upperBound - this.relOffset) > 0;
  }

  //RESTRICTED READ AND WRITE
  private final void checkValid() { //applies to both readable and writable
    assert this.state.isValid() : "Memory not valid.";
  }
}
