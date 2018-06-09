/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_DOUBLE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_FLOAT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.CHAR_SHIFT;
import static com.yahoo.memory.UnsafeUtil.DOUBLE_SHIFT;
import static com.yahoo.memory.UnsafeUtil.FLOAT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.INT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.LONG_SHIFT;
import static com.yahoo.memory.UnsafeUtil.SHORT_SHIFT;
import static com.yahoo.memory.UnsafeUtil.unsafe;
import static com.yahoo.memory.Util.nonNativeOrder;

import java.nio.ByteBuffer;

/*
 * Developer notes: The heavier methods, such as put/get arrays, duplicate, region, clear, fill,
 * compareTo, etc., use hard checks (check*() and incrementAndCheck*() methods), which execute at
 * runtime and throw exceptions if violated. The cost of the runtime checks are minor compared to
 * the rest of the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assert*() and
 * incrementAndAssert*() methods), which only execute when asserts are enabled and JIT will remove
 * them entirely from production runtime code. The offset versions of the light weight methods will
 * simplify to a single unsafe call, which is further simplified by JIT to an intrinsic that is
 * often a single CPU instruction.
 */

/**
 * Implementation of {@link WritableBuffer} for non-native endian byte order. Native variant is
 * {@link WritableBufferImpl}.
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class NonNativeWritableBufferImpl extends BaseWritableBufferImpl {

  //ctor for all parameters
  NonNativeWritableBufferImpl(
      final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes, final boolean readOnly, final ByteBuffer byteBuf,
      final StepBoolean valid, final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly, nonNativeOrder,
        byteBuf, valid, originMemory);
  }

  //DUPLICATES XXX
  @Override
  WritableBuffer writableDuplicateImpl(final boolean localReadOnly) {
    checkValid();
    if (getCapacity() == 0) {
      throw new AssertionError("Don't expect zero capacity");
    }
    final NonNativeWritableBufferImpl wBufImpl =
        new NonNativeWritableBufferImpl(getUnsafeObject(),
            getNativeBaseOffset(), getRegionOffset(), getCapacity(), isReadOnly(), getByteBuffer(),
            getValid(), originMemory);
    wBufImpl.setStartPositionEnd(getStart(), getPosition(), getEnd());
    return wBufImpl;
  }

  //REGIONS XXX
  @Override
  WritableBuffer writableRegionImpl(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly) {
    checkValidAndBounds(offsetBytes, capacityBytes);
    return new NonNativeWritableBufferImpl(getUnsafeObject(), getNativeBaseOffset(),
        getRegionOffset() + offsetBytes, capacityBytes, isReadOnly() || localReadOnly,
        getByteBuffer(), getValid(), originMemory);
  }

  //PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public char getChar() {
    return Character.reverseBytes(getNativeOrderedChar());
  }

  @Override
  public char getChar(final long offsetBytes) {
    return Character.reverseBytes(getNativeOrderedChar(offsetBytes));
  }

  @Override
  public void getCharArray(final char[] dstArray, final int dstOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeChars(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetChars, lengthChars);
  }

  @Override
  public double getDouble() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_DOUBLE_INDEX_SCALE);
    return Double.longBitsToDouble(
        Long.reverseBytes(unsafe.getLong(getUnsafeObject(), getCumulativeOffset() + pos)));
  }

  @Override
  public double getDouble(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    return Double.longBitsToDouble(
        Long.reverseBytes(unsafe.getLong(getUnsafeObject(), getCumulativeOffset() + offsetBytes)));
  }

  @Override
  public void getDoubleArray(final double[] dstArray, final int dstOffsetDoubles,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeDoubles(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetDoubles, lengthDoubles);
  }

  @Override
  public float getFloat() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_FLOAT_INDEX_SCALE);
    return Float.intBitsToFloat(
        Integer.reverseBytes(unsafe.getInt(getUnsafeObject(), getCumulativeOffset() + pos)));
  }

  @Override
  public float getFloat(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    return Float.intBitsToFloat(
        Integer.reverseBytes(unsafe.getInt(getUnsafeObject(), getCumulativeOffset() + offsetBytes)));
  }

  @Override
  public void getFloatArray(final float[] dstArray, final int dstOffsetFloats,
      final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeFloats(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetFloats, lengthFloats);
  }

  @Override
  public int getInt() {
    return Integer.reverseBytes(getNativeOrderedInt());
  }

  @Override
  public int getInt(final long offsetBytes) {
    return Integer.reverseBytes(getNativeOrderedInt(offsetBytes));
  }

  @Override
  public void getIntArray(final int[] dstArray, final int dstOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeInts(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetInts, lengthInts);
  }

  @Override
  public long getLong() {
    return Long.reverseBytes(getNativeOrderedLong());
  }

  @Override
  public long getLong(final long offsetBytes) {
    return Long.reverseBytes(getNativeOrderedLong(offsetBytes));
  }

  @Override
  public void getLongArray(final long[] dstArray, final int dstOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeLongs(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetLongs, lengthLongs);
  }

  @Override
  public short getShort() {
    return Short.reverseBytes(getNativeOrderedShort());
  }

  @Override
  public short getShort(final long offsetBytes) {
    return Short.reverseBytes(getNativeOrderedShort(offsetBytes));
  }

  @Override
  public void getShortArray(final short[] dstArray, final int dstOffsetShorts,
      final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForRead(pos, copyBytes);
    CompareAndCopy.getNonNativeShorts(getUnsafeObject(), getCumulativeOffset(), pos, copyBytes,
        dstArray, dstOffsetShorts, lengthShorts);
  }

  //PRIMITIVE putXXX() and putXXXArray() XXX
  @Override
  public void putChar(final char value) {
    putNativeOrderedChar(Character.reverseBytes(value));
  }

  @Override
  public void putChar(final long offsetBytes, final char value) {
    putNativeOrderedChar(offsetBytes, Character.reverseBytes(value));
  }

  @Override
  public void putCharArray(final char[] srcArray, final int srcOffsetChars, final int lengthChars) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthChars) << CHAR_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeChars(srcArray, srcOffsetChars, lengthChars, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

  @Override
  public void putDouble(final double value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset() + pos,
        Long.reverseBytes(Double.doubleToRawLongBits(value)));
  }

  @Override
  public void putDouble(final long offsetBytes, final double value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_DOUBLE_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset() + offsetBytes,
        Long.reverseBytes(Double.doubleToRawLongBits(value)));
  }

  @Override
  public void putDoubleArray(final double[] srcArray, final int srcOffsetDoubles,
      final int lengthDoubles) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthDoubles) << DOUBLE_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeDoubles(srcArray, srcOffsetDoubles, lengthDoubles, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

  @Override
  public void putFloat(final float value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset() + pos,
        Integer.reverseBytes(Float.floatToRawIntBits(value)));
  }

  @Override
  public void putFloat(final long offsetBytes, final float value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_FLOAT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset() + offsetBytes,
        Integer.reverseBytes(Float.floatToRawIntBits(value)));
  }

  @Override
  public void putFloatArray(final float[] srcArray, final int srcOffsetFloats,
      final int lengthFloats) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthFloats) << FLOAT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeFloats(srcArray, srcOffsetFloats, lengthFloats, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

  @Override
  public void putInt(final int value) {
    putNativeOrderedInt(Integer.reverseBytes(value));
  }

  @Override
  public void putInt(final long offsetBytes, final int value) {
    putNativeOrderedInt(offsetBytes, Integer.reverseBytes(value));
  }

  @Override
  public void putIntArray(final int[] srcArray, final int srcOffsetInts, final int lengthInts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthInts) << INT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeInts(srcArray, srcOffsetInts, lengthInts, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

  @Override
  public void putLong(final long value) {
    putNativeOrderedLong(Long.reverseBytes(value));
  }

  @Override
  public void putLong(final long offsetBytes, final long value) {
    putNativeOrderedLong(offsetBytes, Long.reverseBytes(value));
  }

  @Override
  public void putLongArray(final long[] srcArray, final int srcOffsetLongs, final int lengthLongs) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthLongs) << LONG_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeLongs(srcArray, srcOffsetLongs, lengthLongs, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

  @Override
  public void putShort(final short value) {
    putNativeOrderedShort(Short.reverseBytes(value));
  }

  @Override
  public void putShort(final long offsetBytes, final short value) {
    putNativeOrderedShort(offsetBytes, Short.reverseBytes(value));
  }

  @Override
  public void putShortArray(final short[] srcArray, final int srcOffsetShorts,
      final int lengthShorts) {
    final long pos = getPosition();
    final long copyBytes = ((long) lengthShorts) << SHORT_SHIFT;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    CompareAndCopy.putNonNativeShorts(srcArray, srcOffsetShorts, lengthShorts, copyBytes,
        getUnsafeObject(), getCumulativeOffset(), pos);
  }

}
