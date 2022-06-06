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
import org.apache.datasketches.memory.ReadOnlyException;
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
      final int typeId) {
    super(seg, typeId);
  }

  //NO WRAP HEAP ARRAY RESOURCE

  //BYTE BUFFER RESOURCE

  public static WritableBuffer wrapByteBuffer(
      final ByteBuffer byteBuffer,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    final ByteBuffer byteBuf = localReadOnly ? byteBuffer.asReadOnlyBuffer() : byteBuffer.duplicate();
    byteBuf.clear(); //resets position to zero and limit to capacity. Does not clear data.
    final MemorySegment seg = MemorySegment.ofByteBuffer(byteBuf); //from 0 to capacity
    int type = BUFFER | BYTEBUF
        | (localReadOnly ? READONLY : 0)
        | (seg.isNative() ? DIRECT : 0)
        | (seg.isMapped() ? MAP : 0);
    if (byteOrder == ByteOrder.nativeOrder()) {
      type |= NATIVE;
      final WritableBuffer wbuf = new BBWritableBufferImpl(seg, type);
      wbuf.setStartPositionEnd(0, byteBuffer.position(), byteBuffer.limit());
      return wbuf;
    }
    type |= NONNATIVE;
    final WritableBuffer wbuf = new BBNonNativeWritableBufferImpl(seg, type);
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
      throw new ReadOnlyException("Cannot create a writable region from a read-only Memory.");
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
    final boolean duplicateType = isDuplicateType();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = isByteBufferType();
    final int type = BUFFER | REGION
        | (readOnly ? READONLY : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);
    if (byteBufferType) {
      if (nativeBOType) { return new BBWritableBufferImpl(slice, type); }
      return new BBNonNativeWritableBufferImpl(slice, type);
    }
    if (mapType) {
      if (nativeBOType) { return new MapWritableBufferImpl(slice, type); }
      return new MapNonNativeWritableBufferImpl(slice, type);
    }
    if (directType) {
      if (nativeBOType) { return new DirectWritableBufferImpl(slice, type, memReqSvr); }
      return new DirectNonNativeWritableBufferImpl(slice, type, memReqSvr);
    }
    //else heap type
    if (nativeBOType) { return new HeapWritableBufferImpl(slice, type, memReqSvr); }
    return new HeapNonNativeWritableBufferImpl(slice, type, memReqSvr);
  }

  //DUPLICATES
  @Override
  public Buffer duplicate() {
    return toWritableDuplicateImpl(true, getTypeByteOrder());
  }

  @Override
  public Buffer duplicate(final ByteOrder byteOrder) {
    return toWritableDuplicateImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer writableDuplicate() {
    if (isReadOnly()) {
      throw new ReadOnlyException("Cannot create a writable duplicate from a read-only Buffer.");
    }
    return toWritableDuplicateImpl(false, getTypeByteOrder());
  }

  @Override
  public WritableBuffer writableDuplicate(final ByteOrder byteOrder) {
    if (isReadOnly()) {
      throw new ReadOnlyException("Cannot create a writable duplicate from a read-only Buffer.");
    }
    return toWritableDuplicateImpl(false, byteOrder);
  }

  private WritableBuffer toWritableDuplicateImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Memory is not alive."); }
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegionType();
    final boolean duplicateType = true;
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = isByteBufferType();
    final int type = BUFFER
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    WritableBuffer wbuf;
    if (byteBufferType) {
      if (nativeBOType) { wbuf = new BBWritableBufferImpl(seg2, type); }
      else { wbuf = new BBNonNativeWritableBufferImpl(seg2, type); }
    }
    if (mapType) {
      if (nativeBOType) { wbuf = new MapWritableBufferImpl(seg2, type); }
      else { wbuf = new MapNonNativeWritableBufferImpl(seg2, type); }
    }
    if (directType) {
      if (nativeBOType) { wbuf = new DirectWritableBufferImpl(seg2, type, memReqSvr); }
      else { wbuf = new DirectNonNativeWritableBufferImpl(seg2, type, memReqSvr); }
    }
    //else heap type
    if (nativeBOType) { wbuf = new HeapWritableBufferImpl(seg2, type, memReqSvr); }
    else { wbuf = new HeapNonNativeWritableBufferImpl(seg2, type, memReqSvr); }
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
      throw new ReadOnlyException(
          "Cannot create a writable Memory from a read-only Buffer.");
    }
    return asWritableMemoryImpl(false, byteOrder);
  }

  private WritableMemory asWritableMemoryImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Buffer is not alive."); }
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegionType();
    final boolean duplicateType = isDuplicateType();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = isByteBufferType();
    final int type = MEMORY
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);
    WritableMemory wmem;
    if (byteBufferType) {
      if (nativeBOType) { wmem = new BBWritableMemoryImpl(seg2, type); }
      else { wmem = new BBNonNativeWritableMemoryImpl(seg2, type); }
    }
    if (mapType) {
      if (nativeBOType) { wmem = new MapWritableMemoryImpl(seg2, type); }
      else { wmem = new MapNonNativeWritableMemoryImpl(seg2, type); }
    }
    if (directType) {
      if (nativeBOType) { wmem = new DirectWritableMemoryImpl(seg2, type, memReqSvr); }
      else { wmem = new DirectNonNativeWritableMemoryImpl(seg2, type, memReqSvr); }
    }
    //else heap type
    if (nativeBOType) { wmem = new HeapWritableMemoryImpl(seg2, type, memReqSvr); }
    else { wmem = new HeapNonNativeWritableMemoryImpl(seg2, type, memReqSvr); }
    return wmem;
  }

  //PRIMITIVE getX() and getXArray()

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
