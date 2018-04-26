/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteOrder;

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
 * Implementation of {@link WritableMemory} for native endian byte order. Non-native variant is
 * {@link NonNativeWritableMemoryImpl}.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class WritableMemoryImpl extends BaseWritableMemoryImpl {

  WritableMemoryImpl(final ResourceState state, final boolean localReadOnly) {
    super(state, localReadOnly);
    if (state.getResourceOrder() != ByteOrder.nativeOrder()) {
      throw new IllegalStateException(
          "Expected native ordered state. This may be a bug in the Memory library.");
    }
  }

  //REGIONS XXX
  @Override
  WritableMemory writableRegionImpl(final long offsetBytes, final long capacityBytes,
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
  WritableBuffer asWritableBufferImpl(final boolean localReadOnly) {
    checkValid();
    final BaseWritableBufferImpl wbuf;
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
    return getNativeOrderedChar(offsetBytes);
  }

  @Override
  public void getCharArray(final long offsetBytes, final char[] dstArray, final int dstOffsetChars,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetChars, lengthChars, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) dstOffsetChars) << CHAR_SHIFT),
        copyBytes);
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return unsafe.getDouble(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getDoubleArray(final long offsetBytes, final double[] dstArray, final int dstOffsetDoubles,
      final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetDoubles, lengthDoubles, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) dstOffsetDoubles) << DOUBLE_SHIFT),
        copyBytes);
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return unsafe.getFloat(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getFloatArray(final long offsetBytes, final float[] dstArray, final int dstOffsetFloats,
      final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetFloats, lengthFloats, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) dstOffsetFloats) << FLOAT_SHIFT),
        copyBytes);
  }

  @Override
  public int getInt(final long offsetBytes) {
    return getNativeOrderedInt(offsetBytes);
  }

  @Override
  public void getIntArray(final long offsetBytes, final int[] dstArray, final int dstOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetInts, lengthInts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_INT_BASE_OFFSET + (((long) dstOffsetInts) << INT_SHIFT),
        copyBytes);
  }

  @Override
  public long getLong(final long offsetBytes) {
    return getNativeOrderedLong(offsetBytes);
  }

  @Override
  public void getLongArray(final long offsetBytes, final long[] dstArray, final int dstOffsetLongs,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetLongs, lengthLongs, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_LONG_BASE_OFFSET + (((long) dstOffsetLongs) << LONG_SHIFT),
        copyBytes);
  }

  @Override
  public short getShort(final long offsetBytes) {
    return getNativeOrderedShort(offsetBytes);
  }

  @Override
  public void getShortArray(final long offsetBytes, final short[] dstArray, final int dstOffsetShorts,
      final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetShorts, lengthShorts, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) dstOffsetShorts) << SHORT_SHIFT),
        copyBytes);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putChar(final long offsetBytes, final char value) {
    putNativeOrderedChar(offsetBytes, value);
  }

  @Override
  public void putCharArray(final long offsetBytes, final char[] srcArray, final int srcOffsetChars,
      final int lengthChars) {
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetChars, lengthChars, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_CHAR_BASE_OFFSET + (((long) srcOffsetChars) << CHAR_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putDouble(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putDoubleArray(final long offsetBytes, final double[] srcArray, final int srcOffsetDoubles,
      final int lengthDoubles) {
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetDoubles, lengthDoubles, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_DOUBLE_BASE_OFFSET + (((long) srcOffsetDoubles) << DOUBLE_SHIFT),
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
  public void putFloatArray(final long offsetBytes, final float[] srcArray, final int srcOffsetFloats,
      final int lengthFloats) {
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetFloats, lengthFloats, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_FLOAT_BASE_OFFSET + (((long) srcOffsetFloats) << FLOAT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    putNativeOrderedInt(offsetBytes, value);
  }

  @Override
  public void putIntArray(final long offsetBytes, final int[] srcArray, final int srcOffsetInts,
      final int lengthInts) {
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetInts, lengthInts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_INT_BASE_OFFSET + (((long) srcOffsetInts) << INT_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    putNativeOrderedLong(offsetBytes, value);
  }

  @Override
  public void putLongArray(final long offsetBytes, final long[] srcArray, final int srcOffsetLongs,
      final int lengthLongs) {
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetLongs, lengthLongs, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_LONG_BASE_OFFSET + (((long) srcOffsetLongs) << LONG_SHIFT),
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    putNativeOrderedShort(offsetBytes, value);
  }

  @Override
  public void putShortArray(final long offsetBytes, final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts) {
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetShorts, lengthShorts, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_SHORT_BASE_OFFSET + (((long) srcOffsetShorts) << SHORT_SHIFT),
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
