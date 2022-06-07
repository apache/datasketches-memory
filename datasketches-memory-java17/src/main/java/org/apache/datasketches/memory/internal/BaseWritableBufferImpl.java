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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;

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

  //Pass-through constructor
  BaseWritableBufferImpl(
      final MemorySegment seg,
      final int typeId,
      final MemoryRequestServer memReqSvr) {
    super(seg, typeId, memReqSvr);
  }

  //NO WRAP HEAP ARRAY RESOURCE

  //BYTE BUFFER RESOURCE

  public static WritableBuffer wrapByteBuffer(
      final ByteBuffer byteBuffer,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    Objects.requireNonNull(byteBuffer, "ByteBuffer must not be null");
    Objects.requireNonNull(byteOrder, "ByteOrder must not be null");
    final ByteBuffer byteBuf;
    if (localReadOnly) {
      if (byteBuffer.isReadOnly()) {
        byteBuf = byteBuffer.duplicate();
      } else { //bb writable
        byteBuf = byteBuffer.asReadOnlyBuffer();
      }
    } else { //want writable
      if (byteBuffer.isReadOnly()) {
        throw new IllegalArgumentException("ByteBuffer must be writable.");
      }
      byteBuf = byteBuffer.duplicate();
    }
    byteBuf.clear(); //resets position to zero and limit to capacity. Does not clear data.
    final MemorySegment seg = MemorySegment.ofByteBuffer(byteBuf); //from 0 to capacity
    int type = BUFFER | BYTEBUF
        | (localReadOnly ? READONLY : 0)
        | (seg.isNative() ? DIRECT : 0)
        | (seg.isMapped() ? MAP : 0);
    final WritableBuffer wbuf;
    if (byteOrder == NON_NATIVE_BYTE_ORDER) {
      type |= NONNATIVE;
      wbuf = new NonNativeWritableBufferImpl(seg, type, null);
    } else {
      wbuf = new NativeWritableBufferImpl(seg, type, null);
    }
    wbuf.setStartPositionEnd(0, byteBuffer.position(), byteBuffer.limit());
    return wbuf;
  }

  //NO MAP RESOURCE
  //NO DIRECT RESOURCE

  //REGIONS DERIVED

  @Override
  public Buffer region(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    return regionImpl(offsetBytes, capacityBytes, true, byteOrder);
  }

  @Override
  public WritableBuffer writableRegion(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    if (this.isReadOnly()) {
      throw new IllegalArgumentException("Cannot create a writable region from a read-only Memory.");
    }
    return regionImpl(offsetBytes, capacityBytes, false, byteOrder);
  }

  private WritableBuffer regionImpl(
      final long offsetBytes,
      final long capacityBytes,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Buffer is not alive."); }
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null.");
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment slice = (readOnly && !seg.isReadOnly())
        ? seg.asSlice(offsetBytes, capacityBytes).asReadOnly()
        : seg.asSlice(offsetBytes, capacityBytes);
    final boolean duplicateType = isDuplicate();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = hasByteBuffer();
    final int type = BUFFER | REGION
        | (readOnly ? READONLY : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    final WritableBuffer wbuf = selectBuffer(slice, type, memReqSvr, byteBufferType, mapType, nativeBOType);
    wbuf.setStartPositionEnd(0, 0, wbuf.getCapacity());
    return wbuf;
  }

  //DUPLICATES, DERIVED
  @Override
  public Buffer duplicate() {
    return duplicateImpl(true, getByteOrder());
  }

  @Override
  public Buffer duplicate(final ByteOrder byteOrder) {
    return duplicateImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    if (isReadOnly()) {
      throw new IllegalArgumentException("Cannot create a writable duplicate from a read-only Buffer.");
    }
    return duplicateImpl(false, getByteOrder());
  }

  @Override
  public WritableBuffer writableDuplicate(final ByteOrder byteOrder) {
    if (isReadOnly()) {
      throw new IllegalArgumentException("Cannot create a writable duplicate from a read-only Buffer.");
    }
    return duplicateImpl(false, byteOrder);
  }

  private WritableBuffer duplicateImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Memory is not alive."); }
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegion();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = hasByteBuffer();
    final int type = BUFFER | DUPLICATE
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    final WritableBuffer wbuf = selectBuffer(seg2, type, memReqSvr, byteBufferType, mapType, nativeBOType);
    wbuf.setStartPositionEnd(getStart(), getPosition(), getEnd());
    return wbuf;
  }

  //AS MEMORY DERIVED

  @Override
  public Memory asMemory(final ByteOrder byteOrder) {
    return asWritableMemoryImpl(true, byteOrder);
  }

  @Override
  public WritableMemory asWritableMemory(final ByteOrder byteOrder) {
    if (isReadOnly()) {
      throw new IllegalArgumentException(
          "Cannot create a writable Memory from a read-only Buffer.");
    }
    return asWritableMemoryImpl(false, byteOrder);
  }

  private WritableMemory asWritableMemoryImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Buffer is not alive."); }
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegion();
    final boolean duplicateType = isDuplicate();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = hasByteBuffer();
    final int type = MEMORY
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    final WritableMemory wmem = selectMemory(seg2, type, memReqSvr, byteBufferType, mapType, nativeBOType);
    return wmem;
  }

  //PRIMITIVE getX() and getXArray()

  @Override
  public final boolean getBoolean() {
    return getByte() != 0;
  }

  @Override
  public final boolean getBoolean(final long offsetBytes) {
    return getByte(offsetBytes) != 0;
  }

  @Override
  public final byte getByte() {
    final long pos = getPosition();
    final byte aByte = MemoryAccess.getByteAtOffset(seg, pos);
    setPosition(pos + Byte.BYTES);
    return aByte;
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    return MemoryAccess.getByteAtOffset(seg, offsetBytes);
  }

  @Override
  public final void getByteArray(final byte[] dstArray, final int dstOffsetBytes,
      final int lengthBytes) {
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetBytes, lengthBytes);
    final long pos = getPosition();
    final MemorySegment srcSlice = seg.asSlice(pos, lengthBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + lengthBytes);
  }

  //OTHER PRIMITIVE READ METHODS: e.g., copyTo, compareTo. No writeTo

  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Buffer that, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(seg, thisOffsetBytes, thisLengthBytes,
        ((BaseStateImpl)that).seg, thatOffsetBytes, thatLengthBytes);
  }

  /*
   * Developer notes: There is no copyTo for Buffers because of the ambiguity of what to do with
   * the positional values. Switch to asMemory view to do copyTo.
   */

  //PRIMITIVE putX() and putXArray() implementations

  @Override
  public final void putBoolean(final boolean value) {
    putByte(value ? (byte)1 : 0);
  }

  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    putByte(offsetBytes, value ? (byte)1 : 0);
  }

  @Override
  public final void putByte(final byte value) {
    final long pos = getPosition();
    MemoryAccess.setByteAtOffset(seg, pos, value);
    setPosition(pos + Byte.BYTES);
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    MemoryAccess.setByteAtOffset(seg, offsetBytes, value);
  }

  @Override
  public final void putByteArray(final byte[] srcArray, final int srcOffsetBytes,
      final int lengthBytes) {
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetBytes, lengthBytes);
    final long pos = getPosition();
    final MemorySegment dstSlice = seg.asSlice(pos, lengthBytes);
    dstSlice.copyFrom(srcSlice);
    setPosition(pos + lengthBytes);
  }

  //OTHER

  @Override
  public final void clear() {
    seg.fill((byte)0);
  }

  @Override
  public final void fill(final byte value) {
    seg.fill(value);
  }
}
