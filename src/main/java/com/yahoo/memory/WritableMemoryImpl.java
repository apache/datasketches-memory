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

import java.io.IOException;
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
 * entirely from production runtime code. The light weight methods
 * will simplify to a single unsafe call, which is further simplified by JIT to an intrinsic
 * that is often a single CPU instruction.
 */

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

  //Static variable for cases where byteBuf/array/direct sizes are zero
  final static WritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    ZERO_SIZE_MEMORY = new WritableMemoryImpl(new ResourceState(new byte[0], Prim.BYTE, 0));
  }

  WritableMemoryImpl(final ResourceState state) {
    this.state = state;
    unsafeObj = state.getUnsafeObject();
    unsafeObjHeader = state.getUnsafeObjectHeader();
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
  }

  //DUPLICATES & REGIONS XXX
  @Override
  public WritableMemory writableDuplicate() {
    state.checkValid();
    final WritableMemoryImpl wMemImpl = new WritableMemoryImpl(state);
    return wMemImpl;
  }

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    return writableRegion(offsetBytes, capacityBytes);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    state.checkValid();
    checkBounds(offsetBytes, capacityBytes, capacity);
    if (capacityBytes == 0) { return ZERO_SIZE_MEMORY; }
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
    state.checkValid();
    final WritableBufferImpl wbuf;
    if (capacity == 0) {
      wbuf = WritableBufferImpl.ZERO_SIZE_BUFFER;
      wbuf.originMemory = ZERO_SIZE_MEMORY;
    } else {
      wbuf = new WritableBufferImpl(state);
      wbuf.setAndCheckStartPositionEnd(0, 0, capacity);
      wbuf.originMemory = this;
    }
    return wbuf;
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final long offsetBytes, final boolean[] dstArray,
      final int dstOffset, final int lengthBooleans) {
    state.checkValid();
    final long copyBytes = lengthBooleans;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthBooleans, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final long offsetBytes, final byte[] dstArray, final int dstOffset,
      final int lengthBytes) {
    state.checkValid();
    final long copyBytes = lengthBytes;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthBytes, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  @Override
  public char getChar(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffset,
      final int lengthChars) {
    state.checkValid();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthChars, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) dstOffset) << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final Appendable dst) throws IOException, Utf8CodingException {
    state.checkValid();
    checkBounds(offsetBytes, utf8LengthBytes, state.getCapacity());
    return Utf8.getCharsFromUtf8(offsetBytes, utf8LengthBytes, dst, state);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffset,
      final int lengthDoubles) {
    state.checkValid();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthDoubles, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffset) << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffset,
      final int lengthFloats) {
    state.checkValid();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthFloats, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffset) << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffset,
      final int lengthInts) {
    state.checkValid();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthInts, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_INT_BASE_OFFSET + (((long) dstOffset) << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffset,
      final int lengthLongs) {
    state.checkValid();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthLongs, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (((long) dstOffset) << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffset,
      final int lengthShorts) {
    state.checkValid();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthShorts, dstArray.length);
    unsafe.copyMemory(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) dstOffset) << SHORT_SHIFT),
        copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Memory that,
      final long thatOffsetBytes, final long thatLengthBytes) {
    state.checkValid();
    checkBounds(thisOffsetBytes, thisLengthBytes, capacity);
    checkBounds(thatOffsetBytes, thatLengthBytes, that.getCapacity());
    if (isSameResource(that)) {
      if (thisOffsetBytes == thatOffsetBytes) {
        return 0;
      }
    } else {
      that.getResourceState().checkValid();
    }
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
    state.checkValid();
    checkBounds(srcOffsetBytes, lengthBytes, capacity);
    checkBounds(dstOffsetBytes, lengthBytes, destination.getCapacity());
    if (isSameResource(destination)) {
      if (srcOffsetBytes == dstOffsetBytes) {
        return;
      }
    } else {
      destination.getResourceState().checkValid();
    }
    final long srcAdd = getCumulativeOffset(srcOffsetBytes);
    final long dstAdd = destination.getCumulativeOffset(dstOffsetBytes);
    final Object srcParent = (isDirect()) ? null : unsafeObj;
    final Object dstParent = (destination.isDirect()) ? null : destination.getArray();
    final long lenBytes = lengthBytes;
    unsafe.copyMemory(srcParent, srcAdd, dstParent, dstAdd, lenBytes);
  }

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
  public long getCumulativeOffset(final long offsetBytes) {
    state.assertValid();
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public long getRegionOffset(final long offsetBytes) {
    state.assertValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public ByteOrder getResourceOrder() {
    state.assertValid();
    return state.order();
  }

  @Override
  public boolean hasArray() {
    state.assertValid();
    return unsafeObj != null;
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
  public boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
    state.checkValid();
    that.getResourceState().checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public boolean swapBytes() {
    state.assertValid();
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

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final long offsetBytes, final boolean[] srcArray, final int srcOffset,
      final int lengthBooleans) {
    state.checkValid();
    final long copyBytes = lengthBooleans;
    checkBounds(srcOffset, lengthBooleans, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + srcOffset,
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final long offsetBytes, final byte[] srcArray, final int srcOffset,
      final int lengthBytes) {
    state.checkValid();
    final long copyBytes = lengthBytes;
    checkBounds(srcOffset, lengthBytes, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_BYTE_BASE_OFFSET + srcOffset,
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_CHAR_INDEX_SCALE, capacity);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffset,
      final int lengthChars) {
    state.checkValid();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkBounds(srcOffset, lengthChars, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) srcOffset) << CHAR_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public long putCharsToUtf8(final long offsetBytes, final CharSequence src) {
    state.checkValid();
    return Utf8.putCharsToUtf8(offsetBytes, src, state);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE, capacity);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffset,
      final int lengthDoubles) {
    state.checkValid();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkBounds(srcOffset, lengthDoubles, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffset) << DOUBLE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_FLOAT_INDEX_SCALE, capacity);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffset,
      final int lengthFloats) {
    state.checkValid();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkBounds(srcOffset, lengthFloats, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffset) << FLOAT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_INT_INDEX_SCALE, capacity);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffset,
      final int lengthInts) {
    state.checkValid();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkBounds(srcOffset, lengthInts, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (((long) srcOffset) << INT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffset,
      final int lengthLongs) {
    state.checkValid();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkBounds(srcOffset, lengthLongs, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (((long) srcOffset) << LONG_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_SHORT_INDEX_SCALE, capacity);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffset,
      final int lengthShorts) {
    state.checkValid();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkBounds(srcOffset, lengthShorts, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    unsafe.copyMemory(
        srcArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) srcOffset) << SHORT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  //Atomic Write Methods XXX
  @Override
  public long getAndAddLong(final long offsetBytes, final long delta) { //JDK 8+
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndAddLong(unsafeObj, add, delta) + delta;
  }

  @Override
  public long getAndSetLong(final long offsetBytes, final long newValue) { //JDK 8+
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    final long add = cumBaseOffset + offsetBytes;
    return UnsafeUtil.compatibilityMethods.getAndSetLong(unsafeObj, add, newValue);
  }

  @Override
  public boolean compareAndSwapLong(final long offsetBytes, final long expect, final long update) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_LONG_INDEX_SCALE, capacity);
    return unsafe.compareAndSwapLong(unsafeObj, cumBaseOffset + offsetBytes, expect, update);
  }

  //OTHER WRITE METHODS XXX
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
    fill(0, capacity, (byte) 0);
  }

  @Override
  public void clear(final long offsetBytes, final long lengthBytes) {
    fill(offsetBytes, lengthBytes, (byte) 0);
  }

  @Override
  public void clearBits(final long offsetBytes, final byte bitMask) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
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
    state.checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
    unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, lengthBytes, value);
  }

  @Override
  public void setBits(final long offsetBytes, final byte bitMask) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
    state.assertValid();
    return state.getMemoryRequestServer();
  }

  @Override
  public void setMemoryRequest(final MemoryRequestServer memReqSvr) {
    state.assertValid();
    state.setMemoryRequestServer(memReqSvr);
  }

  @Override
  public WritableDirectHandle getHandle() {
    state.assertValid();
    return state.getHandle();
  }

  @Override
  public void setHandle(final WritableDirectHandle handle) {
    state.assertValid();
    state.setHandle(handle);
  }

  //RESTRICTED XXX

  @Override
  ResourceState getResourceState() {
    return state;
  }

}
