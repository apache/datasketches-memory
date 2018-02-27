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
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.io.IOException;

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
class WritableMemoryImpl extends BaseWritableMemoryImpl {

  //Static variable for cases where byteBuf/array/direct sizes are zero
  final static WritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    ZERO_SIZE_MEMORY = new WritableMemoryImpl(new ResourceState(new byte[0], Prim.BYTE, 0));
  }

  WritableMemoryImpl(final ResourceState state) {
    super(state);
  }

  //REGIONS XXX
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
    CompareAndCopy.copyMemoryCheckingDifferentObject(
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
    CompareAndCopy.copyMemoryCheckingDifferentObject(
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
    CompareAndCopy.copyMemoryCheckingDifferentObject(
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
    CompareAndCopy.copyMemoryCheckingDifferentObject(
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
}
