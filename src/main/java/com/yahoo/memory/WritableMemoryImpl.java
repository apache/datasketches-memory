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
import static com.yahoo.memory.UnsafeUtil.UNSAFE_COPY_THRESHOLD;
import static com.yahoo.memory.UnsafeUtil.checkOverlap;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of WritableMemory
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableMemoryImpl extends WritableMemory {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long unsafeObjHeader; //Heap ByteBuffer includes the slice() offset here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  //Static variable for cases where byteBuf/array sizes are zero
  final static WritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    ZERO_SIZE_MEMORY = new WritableMemoryImpl(
        new ResourceState(new byte[0], Prim.BYTE, 0)
    );
  }

  WritableMemoryImpl(final ResourceState state) {
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    unsafeObjHeader = state.getUnsafeObjectHeader();
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //REGIONS/DUPLICATES XXX
  @Override
  public WritableMemory writableDuplicate() {
    return writableRegion(0, capacity);
  }

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    assertValid();
    return writableRegion(offsetBytes, capacityBytes);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    checkBounds(offsetBytes, capacityBytes);
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    return new WritableMemoryImpl(newState);
  }

  //BUFFER XXX
  @Override
  public Buffer asBuffer() {
    return asWritableBuffer();
  }

  @Override
  public WritableBuffer asWritableBuffer() {
    final WritableBufferImpl impl = new WritableBufferImpl(state);
    impl.setStartPositionEnd(0, 0, state.getCapacity());
    return impl;
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final long offsetBytes, final boolean[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << BOOLEAN_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + (dstOffset << BOOLEAN_SHIFT),
        copyBytes);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final long offsetBytes, final byte[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << BYTE_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + (dstOffset << BYTE_SHIFT),
        copyBytes);
  }

  @Override
  public char getChar(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << CHAR_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (dstOffset << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public void getCharsFromUtf8(final long offsetBytes, final int utf8Length, final Appendable dst)
      throws IOException, Utf8CodingException {
    Utf8.getCharsFromUtf8(offsetBytes, utf8Length, dst, state);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << DOUBLE_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (dstOffset << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << FLOAT_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (dstOffset << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << INT_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_INT_BASE_OFFSET + (dstOffset << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << LONG_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (dstOffset << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffset,
      final int length) {
    final long copyBytes = length << SHORT_SHIFT;
    checkBounds(offsetBytes, copyBytes);
    UnsafeUtil.checkBounds(dstOffset, length, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (dstOffset << SHORT_SHIFT),
        copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Memory that,
      final long thatOffsetBytes, final long thatLengthBytes) {
    ((WritableMemoryImpl)that).assertValid();
    checkBounds(thisOffsetBytes, thisLengthBytes);
    that.checkBounds(thatOffsetBytes, thatLengthBytes);
    final long thisAdd = getCumulativeOffset(thisOffsetBytes);
    final long thatAdd = that.getCumulativeOffset(thatOffsetBytes);
    final Object thisObj = (isDirect()) ? null : unsafeObj;
    final Object thatObj = (that.isDirect()) ? null : ((WritableMemory)that).getArray();
    final long lenBytes = Math.min(thisLengthBytes, thatLengthBytes);
    for (long i = 0; i < lenBytes; i++) {
      final int thisByte = unsafe.getByte(thisObj, thisAdd + i);
      final int thatByte = unsafe.getByte(thatObj, thatAdd + i);
      if (thisByte < thatByte) { return -1; }
      if (thisByte > thatByte) { return  1; }
    }
    return Long.compare(thisLengthBytes, thatLengthBytes);
  }

  @Override
  public void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    checkBounds(srcOffsetBytes, lengthBytes);
    destination.checkBounds(dstOffsetBytes, lengthBytes);
    assert ((this == destination)
        ? checkOverlap(srcOffsetBytes, dstOffsetBytes, lengthBytes)
        : true) : "Region Overlap" ;

    long srcAdd = getCumulativeOffset(srcOffsetBytes);
    long dstAdd = destination.getCumulativeOffset(dstOffsetBytes);
    final Object srcParent = (isDirect()) ? null : unsafeObj;
    final Object dstParent = (destination.isDirect()) ? null : destination.getArray();
    long lenBytes = lengthBytes;

    while (lenBytes > 0) {
      final long chunkBytes = (lenBytes > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : lenBytes;
      unsafe.copyMemory(srcParent, srcAdd, dstParent, dstAdd, lenBytes);
      lenBytes -= chunkBytes;
      srcAdd += chunkBytes;
      dstAdd += chunkBytes;
    }
  }

  //OTHER READ METHODS XXX
  @Override
  public long getCapacity() {
    assertValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset(final long offsetBytes) {
    assertValid();
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public void checkBounds(final long offsetBytes, final long length) {
    assertValid();
    UnsafeUtil.checkBounds(offsetBytes, length, capacity);
  }

  private void assertBounds(final long offsetBytes, final long length) {
    assertValid();
    UnsafeUtil.assertBounds(offsetBytes, length, capacity);
  }

  @Override
  public long getRegionOffset(final long offsetBytes) {
    assertValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public ByteOrder getResourceOrder() {
    assertValid();
    return state.order();
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
  public boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
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
    assertValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Params         : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final long offsetBytes, final boolean[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << BOOLEAN_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + (srcOffset << BOOLEAN_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final long offsetBytes, final byte[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << BYTE_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BYTE_BASE_OFFSET + (srcOffset << BYTE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << CHAR_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (srcOffset << CHAR_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public long putCharsToUtf8(final long offsetBytes, final CharSequence src) {
    return Utf8.putCharsToUtf8(offsetBytes, src, state);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << DOUBLE_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (srcOffset << DOUBLE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << FLOAT_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (srcOffset << FLOAT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << INT_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (srcOffset << INT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << LONG_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (srcOffset << LONG_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffset,
      final int length) {
    final long copyBytes = length << SHORT_SHIFT;
    UnsafeUtil.checkBounds(srcOffset, length, srcArray.length);
    checkBounds(offsetBytes, copyBytes);
    unsafe.copyMemory(
        srcArray,
        ARRAY_SHORT_BASE_OFFSET + (srcOffset << SHORT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  //Atomic Write Methods XXX
  @Override
  public long getAndAddLong(final long offsetBytes, final long delta) { //JDK 8+
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndAddLong(unsafeObj, add, delta) + delta;
  }

  @Override
  public long getAndSetLong(final long offsetBytes, final long newValue) { //JDK 8+
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndSetLong(unsafeObj, add, newValue);
  }

  @Override
  public boolean compareAndSwapLong(final long offsetBytes, final long expect, final long update) {
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.compareAndSwapLong(unsafeObj, cumBaseOffset + offsetBytes, expect, update);
  }

  //OTHER WRITE METHODS XXX
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
    fill(0, capacity, (byte) 0);
  }

  @Override
  public void clear(final long offsetBytes, final long lengthBytes) {
    fill(offsetBytes, lengthBytes, (byte) 0);
  }

  @Override
  public void clearBits(final long offsetBytes, final byte bitMask) {
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long cumBaseOff = cumBaseOffset + offsetBytes;
    int value = unsafe.getByte(unsafeObj, cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(unsafeObj, cumBaseOff, (byte)value);
  }

  @Override
  public void fill(final byte value) {
    fill(0, capacity, value);
  }

  @Override
  public void fill(final long offsetBytes, final long lengthBytes, final byte value) {
    checkBounds(offsetBytes, lengthBytes);
    unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, lengthBytes, value);
  }

  @Override
  public void setBits(final long offsetBytes, final byte bitMask) {
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
    assertValid();
    return state.getMemoryRequestServer();
  }

  @Override
  public void setMemoryRequest(final MemoryRequestServer memReqSvr) {
    state.setMemoryRequestServer(memReqSvr);
  }

  @Override
  public WritableDirectHandle getHandle() {
    return state.getHandle();
  }

  @Override
  public void setHandle(final WritableDirectHandle handle) {
    state.setHandle(handle);
  }

  //RESTRICTED READ AND WRITE XXX
  private final void assertValid() { //applies to both readable and writable
    assert state.isValid() : "Memory not valid.";
  }

  @Override
  ResourceState getResourceState() {
    return state;
  }

}
