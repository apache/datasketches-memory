/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

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
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.IOException;

/*
 * Developer notes: The heavier methods, such as put/get arrays, duplicate, region, clear, fill,
 * compareTo, etc., use hard checks (checkValid*() and checkBounds()), which execute at runtime and
 * throw exceptions if violated. The cost of the runtime checks are minor compared to the rest of
 * the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assertValid*()), which only
 * execute when asserts are enabled and JIT will remove them entirely from production runtime code.
 * The light weight methods will simplify to a single unsafe call, which is further simplified by
 * JIT to an intrinsic that is often a single CPU instruction.
 */

/**
 * Implementation of WritableMemory for Native Endian ByteOrder.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class WritableMemoryImpl extends BaseWritableMemoryImpl {

  WritableMemoryImpl(final ResourceState state, final boolean localReadOnly) {
    super(state, localReadOnly);
  }

  static WritableMemoryImpl newInstance(final ResourceState state, final boolean localReadOnly) {
    if (state.getCapacity() == 0) { return ZERO_SIZE_MEMORY; }
    return new WritableMemoryImpl(state, localReadOnly);
  }

  //REGIONS XXX
  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, true);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, localReadOnly);
  }

  private WritableMemory writableRegionImpl(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly) {
    checkValidAndBounds(offsetBytes, capacityBytes);
    if (capacityBytes == 0) { return ZERO_SIZE_MEMORY; }
    final ResourceState newState = state.copy();
    newState.putRegionOffset(newState.getRegionOffset() + offsetBytes);
    newState.putCapacity(capacityBytes);
    return new WritableMemoryImpl(newState, localReadOnly);
  }

  //BUFFER XXX
  @Override
  public Buffer asBuffer() {
    return asWritableBufferImpl(true);
  }

  @Override
  public WritableBuffer asWritableBuffer() {
    return asWritableBufferImpl(localReadOnly);
  }

  private WritableBuffer asWritableBufferImpl(final boolean localReadOnly) {
    checkValid();
    final WritableBufferImpl wbuf;
    if (capacity == 0) {
      wbuf = BaseWritableBufferImpl.ZERO_SIZE_BUFFER;
    } else {
      wbuf = new WritableBufferImpl(state, localReadOnly, this);
      wbuf.setAndCheckStartPositionEnd(0, 0, capacity);
    }
    return wbuf;
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public char getChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffset,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthChars, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) dstOffset) << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final Appendable dst) throws IOException, Utf8CodingException {
    checkValidAndBounds(offsetBytes, utf8LengthBytes);
    return Utf8.getCharsFromUtf8(offsetBytes, utf8LengthBytes, dst, state);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffset,
      final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthDoubles, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffset) << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffset,
      final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthFloats, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffset) << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffset,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthInts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_INT_BASE_OFFSET + (((long) dstOffset) << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffset,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthLongs, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (((long) dstOffset) << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffset,
      final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthShorts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) dstOffset) << SHORT_SHIFT),
        copyBytes);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffset,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthChars, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) srcOffset) << CHAR_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public long putCharsToUtf8(final long offsetBytes, final CharSequence src) {
    checkValid();
    return Utf8.putCharsToUtf8(offsetBytes, src, state);
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffset,
      final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthDoubles, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffset) << DOUBLE_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putFloat(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffset,
      final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthFloats, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffset) << FLOAT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffset,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthInts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (((long) srcOffset) << INT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffset,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthLongs, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (((long) srcOffset) << LONG_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffset,
      final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthShorts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
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
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long addr = cumBaseOffset + offsetBytes;
    if (UnsafeUtil.JDK8_OR_ABOVE) {
      return unsafe.getAndAddLong(unsafeObj, addr, delta);
    } else {
      return JDK7Compatible.getAndAddLong(unsafeObj, addr, delta);
    }
  }

  @Override
  public long getAndSetLong(final long offsetBytes, final long newValue) { //JDK 8+
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    final long addr = cumBaseOffset + offsetBytes;
    if (UnsafeUtil.JDK8_OR_ABOVE) {
      return unsafe.getAndSetLong(unsafeObj, addr, newValue);
    } else {
      return JDK7Compatible.getAndSetLong(unsafeObj, addr, newValue);
    }
  }

  @Override
  public boolean compareAndSwapLong(final long offsetBytes, final long expect, final long update) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.compareAndSwapLong(unsafeObj, cumBaseOffset + offsetBytes, expect, update);
  }
}
