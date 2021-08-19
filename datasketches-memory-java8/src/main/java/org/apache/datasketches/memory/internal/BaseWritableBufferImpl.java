/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.datasketches.memory.internal;

import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static org.apache.datasketches.memory.internal.UnsafeUtil.checkBounds;
import static org.apache.datasketches.memory.internal.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.ReadOnlyException;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;

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
 * Common base of native-ordered and non-native-ordered {@link WritableBuffer} implementations.
 * Contains methods which are agnostic to the byte order.
 */
@SuppressWarnings("restriction")
public abstract class BaseWritableBufferImpl extends BaseBufferImpl implements WritableBuffer {

  //Pass-through ctor
  BaseWritableBufferImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  /**
   * The static constructor that chooses the correct ByteBuffer leaf node based on the byte order.
   * @param byteBuf the ByteBuffer being wrapped
   * @param localReadOnly the requested read-only state
   * @param byteOrder the requested byteOrder
   * @param memReqSvr the requested MemoryRequestServer, which may be null.
   * @return this class constructed via the leaf node.
   */
  public static BaseWritableBufferImpl wrapByteBuffer(
      final ByteBuffer byteBuf, final boolean localReadOnly, final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    final AccessByteBuffer abb = new AccessByteBuffer(byteBuf);
    final int typeId = (abb.resourceReadOnly || localReadOnly) ? READONLY : 0;
    final BaseWritableBufferImpl bwbi = Util.isNativeByteOrder(byteOrder)
        ? new BBWritableBufferImpl(abb.unsafeObj, abb.nativeBaseOffset,
            abb.regionOffset, abb.capacityBytes, typeId, byteBuf, memReqSvr)
        : new BBNonNativeWritableBufferImpl(abb.unsafeObj, abb.nativeBaseOffset,
            abb.regionOffset, abb.capacityBytes,  typeId, byteBuf, memReqSvr);
    bwbi.setStartPositionEnd(0, byteBuf.position(), byteBuf.limit());
    return bwbi;
  }

  //REGIONS
  @Override
  public Buffer region() {
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), true, getTypeByteOrder());
  }

  @Override
  public Buffer region(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    final WritableBuffer buf = writableRegionImpl(offsetBytes, capacityBytes, true, byteOrder);
    buf.setAndCheckStartPositionEnd(0, 0, capacityBytes);
    return buf;
  }

  @Override
  public WritableBuffer writableRegion() {
    return writableRegionImpl(getPosition(), getEnd() - getPosition(), false, getTypeByteOrder());
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    final WritableBuffer wbuf = writableRegionImpl(offsetBytes, capacityBytes, false, byteOrder);
    wbuf.setAndCheckStartPositionEnd(0, 0, capacityBytes);
    return wbuf;
  }

  WritableBuffer writableRegionImpl(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    if (isReadOnly() && !localReadOnly) {
      throw new ReadOnlyException("Writable region of a read-only Buffer is not allowed.");
    }
    checkValidAndBounds(offsetBytes, capacityBytes);
    final boolean readOnly = isReadOnly() || localReadOnly;
    final WritableBuffer wbuf = toWritableRegion(offsetBytes, capacityBytes, readOnly, byteOrder);
    wbuf.setStartPositionEnd(0, 0, capacityBytes);
    return wbuf;
  }

  abstract BaseWritableBufferImpl toWritableRegion(
      long offsetBytes, long capcityBytes, boolean readOnly, ByteOrder byteOrder);

  //DUPLICATES
  @Override
  public Buffer duplicate() {
    return writableDuplicateImpl(true, getTypeByteOrder());
  }

  @Override
  public Buffer duplicate(final ByteOrder byteOrder) {
    return writableDuplicateImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    return writableDuplicateImpl(false, getTypeByteOrder());
  }

  @Override
  public WritableBuffer writableDuplicate(final ByteOrder byteOrder) {
    return writableDuplicateImpl(false, byteOrder);
  }

  WritableBuffer writableDuplicateImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (isReadOnly() && !localReadOnly) {
      throw new ReadOnlyException("Writable duplicate of a read-only Buffer is not allowed.");
    }
    final boolean readOnly = isReadOnly() || localReadOnly;
    final WritableBuffer wbuf = toDuplicate(readOnly, byteOrder);
    wbuf.setStartPositionEnd(getStart(), getPosition(), getEnd());
    return wbuf;
  }

  abstract BaseWritableBufferImpl toDuplicate(boolean readOnly, ByteOrder byteOrder);

  //AS MEMORY
  @Override
  public Memory asMemory(final ByteOrder byteOrder) {
    return asWritableMemory(true, byteOrder);
  }

  @Override
  public WritableMemory asWritableMemory(final ByteOrder byteOrder) {
    return asWritableMemory(false, byteOrder);
  }

  WritableMemory asWritableMemory(final boolean localReadOnly, final ByteOrder byteOrder) {
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    if (isReadOnly() && !localReadOnly) {
      throw new ReadOnlyException(
          "Converting a read-only Buffer to a writable Memory is not allowed.");
    }
    final boolean readOnly = isReadOnly() || localReadOnly;
    final WritableMemory wmem = toWritableMemory(readOnly, byteOrder);
    return wmem;
  }

  abstract BaseWritableMemoryImpl toWritableMemory(boolean readOnly, ByteOrder byteOrder);

  //PRIMITIVE getX() and getXArray()
  @Override
  public final boolean getBoolean() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(getUnsafeObject(), getCumulativeOffset(pos));
  }

  @Override
  public final boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public final void getBooleanArray(final boolean[] dstArray, final int dstOffsetBooleans,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBooleans, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_BOOLEAN_BASE_OFFSET + dstOffsetBooleans,
            copyBytes);
  }

  @Override
  public final byte getByte() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(getUnsafeObject(), getCumulativeOffset(pos));
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public final void getByteArray(final byte[] dstArray, final int dstOffsetBytes,
      final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForRead(pos, copyBytes);
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            getUnsafeObject(),
            getCumulativeOffset(pos),
            dstArray,
            ARRAY_BYTE_BASE_OFFSET + dstOffsetBytes,
            copyBytes);
  }

  //PRIMITIVE getX() Native Endian (used by both endians)
  final char getNativeOrderedChar() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(getUnsafeObject(), getCumulativeOffset(pos));
  }

  final char getNativeOrderedChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final int getNativeOrderedInt() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(getUnsafeObject(), getCumulativeOffset(pos));
  }

  final int getNativeOrderedInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final long getNativeOrderedLong() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(getUnsafeObject(), getCumulativeOffset(pos));
  }

  final long getNativeOrderedLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final short getNativeOrderedShort() {
    final long pos = getPosition();
    incrementAndAssertPositionForRead(pos, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(getUnsafeObject(), getCumulativeOffset(pos));
  }

  final short getNativeOrderedShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  //OTHER PRIMITIVE READ METHODS: copyTo, compareTo
  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Buffer thatBuf, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare((BaseStateImpl)this, thisOffsetBytes, thisLengthBytes,
        (BaseStateImpl)thatBuf, thatOffsetBytes, thatLengthBytes);
  }

  /*
   * Develper notes: There is no copyTo for Buffers because of the ambiguity of what to do with
   * the positional values. Switch to MemoryImpl view to do copyTo.
   */

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public final void putBoolean(final boolean value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public final void putBooleanArray(final boolean[] srcArray, final int srcOffsetBooleans,
      final int lengthBooleans) {
    final long pos = getPosition();
    final long copyBytes = lengthBooleans;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetBooleans, lengthBooleans, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BOOLEAN_BASE_OFFSET + srcOffsetBooleans,
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  @Override
  public final void putByte(final byte value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public final void putByteArray(final byte[] srcArray, final int srcOffsetBytes,
      final int lengthBytes) {
    final long pos = getPosition();
    final long copyBytes = lengthBytes;
    incrementAndCheckPositionForWrite(pos, copyBytes);
    checkBounds(srcOffsetBytes, lengthBytes, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
            srcArray,
            ARRAY_BYTE_BASE_OFFSET + srcOffsetBytes,
            getUnsafeObject(),
            getCumulativeOffset(pos),
            copyBytes);
  }

  //PRIMITIVE putX() Native Endian (used by both endians)
  final void putNativeOrderedChar(final char value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  final void putNativeOrderedChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedInt(final int value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  final void putNativeOrderedInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedLong(final long value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  final void putNativeOrderedLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedShort(final short value) {
    final long pos = getPosition();
    incrementAndAssertPositionForWrite(pos, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(getUnsafeObject(), getCumulativeOffset(pos), value);
  }

  final void putNativeOrderedShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  //OTHER
  @Override
  public final Object getArray() {
    assertValid();
    return getUnsafeObject();
  }

  @Override
  public final void clear() {
    fill((byte)0);
  }

  @Override
  public final void fill(final byte value) {
    checkValidForWrite();
    long pos = getPosition();
    long len = getEnd() - pos;
    checkInvariants(getStart(), pos + len, getEnd(), getCapacity());
    while (len > 0) {
      final long chunk = Math.min(len, Util.UNSAFE_COPY_THRESHOLD_BYTES);
      unsafe.setMemory(getUnsafeObject(), getCumulativeOffset(pos), chunk, value);
      pos += chunk;
      len -= chunk;
    }
  }
}
