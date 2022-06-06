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

import static java.nio.channels.FileChannel.MapMode.READ_ONLY;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.Objects;

import org.apache.datasketches.memory.Buffer;
import org.apache.datasketches.memory.Memory;
import org.apache.datasketches.memory.MemoryRequestServer;
import org.apache.datasketches.memory.ReadOnlyException;
import org.apache.datasketches.memory.WritableBuffer;
import org.apache.datasketches.memory.WritableMemory;

import jdk.incubator.foreign.MemoryAccess;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.ResourceScope;

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
 * Common base of native-ordered and non-native-ordered {@link WritableMemory} implementations.
 * Contains methods which are agnostic to the byte order.
 */
public abstract class BaseWritableMemoryImpl extends BaseStateImpl implements WritableMemory {

  //Pass-through constructor
  BaseWritableMemoryImpl(
      final MemorySegment seg,
      final int typeId) {
    super(seg, typeId);
  }

  //WRAP HEAP ARRAY RESOURCE

  public static WritableMemory wrapSegmentAsArray(
      final MemorySegment seg,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    int type = MEMORY
        | (seg.isReadOnly() ? READONLY : 0);
    if (byteOrder == ByteOrder.nativeOrder()) {
      type |= NATIVE;
      return new HeapWritableMemoryImpl(seg, type, memReqSvr);
    }
    type |= NONNATIVE;
    return new HeapNonNativeWritableMemoryImpl(seg, type, memReqSvr);
  }

  //BYTE BUFFER RESOURCE

  public static WritableMemory wrapByteBuffer(
      final ByteBuffer byteBuffer,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    final ByteBuffer byteBuf = localReadOnly ? byteBuffer.asReadOnlyBuffer() : byteBuffer.duplicate();
    byteBuf.clear(); //resets position to zero and limit to capacity. Does not clear data.
    final MemorySegment seg = MemorySegment.ofByteBuffer(byteBuf); //from 0 to capacity
    int type = MEMORY | BYTEBUF
        | (localReadOnly ? READONLY : 0)
        | (seg.isNative() ? DIRECT : 0)
        | (seg.isMapped() ? MAP : 0);
    if (byteOrder == ByteOrder.nativeOrder()) {
      type |= NATIVE;
      return new BBWritableMemoryImpl(seg, type);
    }
    type |= NONNATIVE;
    return new BBNonNativeWritableMemoryImpl(seg, type);
  }

  //MAP RESOURCE

  /**
   * Maps the specified portion of the given file into Memory for write operations.
   * This chooses the correct Map leaf node based on the byte order.
   * @param file the file being wrapped. It must be non-null with length &gt; 0.
   * @param fileOffsetBytes the position in the given file in bytes. It must not be negative.
   * @param capacityBytes the size of the mapped Memory. It must be &ge; 0.
   * @param scope the given ResourceScope. It must be non-null.
   * @param localReadOnly the requested read-only state
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @return mapped WritableMemory.
   * @throws Exception for various IO exceptions
   */
  @SuppressWarnings("resource")
  public static WritableMemory wrapMap(
      final File file,
      final long fileOffsetBytes,
      final long capacityBytes,
      final ResourceScope scope,
      final boolean localReadOnly,
      final ByteOrder byteOrder) throws Exception {
    final FileChannel.MapMode mapMode = (localReadOnly) ? READ_ONLY : READ_WRITE;
    final MemorySegment seg;
    try {
      seg = MemorySegment.mapFile(
          file.toPath(),
          fileOffsetBytes,
          capacityBytes,
          mapMode,
          scope); }
    catch (final IllegalArgumentException | IllegalStateException | UnsupportedOperationException
        | IOException | SecurityException e) { throw e; }
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final int type = MEMORY | MAP | DIRECT
        | (localReadOnly ? READONLY : 0)
        | (nativeBOType ? NATIVE : NONNATIVE);
    return nativeBOType
        ? new MapWritableMemoryImpl(seg, type)
        : new MapNonNativeWritableMemoryImpl(seg, type);
  }

  //DIRECT RESOURCE

  /**
   * The static constructor that chooses the correct Direct leaf node based on the byte order.
   * @param capacityBytes the requested capacity for the Direct (off-heap) memory.  It must be &ge; 0.
   * @param alignmentBytes requested segment alignment. Typically 1, 2, 4 or 8.
   * @param scope ResourceScope for the backing MemorySegment.
   * Typically <i>ResourceScope.newConfinedScope()</i>.
   * @param byteOrder the byte order to be used.  It must be non-null.
   * @param memReqSvr A user-specified MemoryRequestServer, which may be null.
   * This is a callback mechanism for a user client of direct memory to request more memory.
   * @return WritableMemory
   */
  @SuppressWarnings("resource")
  public static WritableMemory wrapDirect(
      final long capacityBytes,
      final long alignmentBytes,
      final ResourceScope scope,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    final MemorySegment seg = MemorySegment.allocateNative(
        capacityBytes,
        alignmentBytes,
        scope);
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final int type = MEMORY | DIRECT
        | (nativeBOType ? NATIVE : NONNATIVE);
    return nativeBOType
        ? new DirectWritableMemoryImpl(seg, type, memReqSvr)
        : new DirectNonNativeWritableMemoryImpl(seg, type, memReqSvr);
  }

  //REGION DERIVED

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    return regionImpl(offsetBytes, capacityBytes, true, byteOrder);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    if (this.isReadOnly()) {
      throw new ReadOnlyException("Cannot create a writable region from a read-only Memory.");
    }
    return regionImpl(offsetBytes, capacityBytes, false, byteOrder);
  }

  private WritableMemory regionImpl(
      final long offsetBytes,
      final long capacityBytes,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Memory is not alive."); }
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
    final int type = MEMORY | REGION
        | (readOnly ? READONLY : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);
    if (byteBufferType) {
      if (nativeBOType) { return new BBWritableMemoryImpl(slice, type); }
      return new BBNonNativeWritableMemoryImpl(slice, type);
    }
    if (mapType) {
      if (nativeBOType) { return new MapWritableMemoryImpl(slice, type); }
      return new MapNonNativeWritableMemoryImpl(slice, type);
    }
    if (directType) {
      if (nativeBOType) { return new DirectWritableMemoryImpl(slice, type, memReqSvr); }
      return new DirectNonNativeWritableMemoryImpl(slice, type, memReqSvr);
    }
    //else heap type
    if (nativeBOType) { return new HeapWritableMemoryImpl(slice, type, memReqSvr); }
    return new HeapNonNativeWritableMemoryImpl(slice, type, memReqSvr);
  }

  //AS BUFFER DERIVED

  @Override
  public Buffer asBuffer(final ByteOrder byteOrder) {
    return asWritableBufferImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer asWritableBuffer(final ByteOrder byteOrder) {
    if (isReadOnly()) {
      throw new ReadOnlyException(
          "Cannot create a writable buffer from a read-only Memory.");
    }
    return asWritableBufferImpl(false, byteOrder);
  }

  private WritableBuffer asWritableBufferImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Memory is not alive."); }
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegionType();
    final boolean duplicateType = isDuplicateType();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = isByteBufferType();
    final int type = BUFFER
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (directType ? DIRECT : 0)
        | (mapType ? MAP : 0)
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
    wbuf.setStartPositionEnd(0, 0, getCapacity());
    return wbuf;
  }

  //PRIMITIVE getX() and getXArray()

  @Override
  public final byte getByte(final long offsetBytes) {
    return MemoryAccess.getByteAtOffset(seg, offsetBytes);
  }

  @Override
  public final void getByteArray(final long offsetBytes, final byte[] dstArray,
      final int dstOffsetBytes, final int lengthBytes) {
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    final MemorySegment srcSlice = seg.asSlice(offsetBytes, lengthBytes);
    final MemorySegment dstSlice = MemorySegment.ofArray(dstArray).asSlice(dstOffsetBytes, lengthBytes);
    dstSlice.copyFrom(srcSlice);
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo, writeTo

  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Memory that, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(seg, thisOffsetBytes, thisLengthBytes,
        ((BaseStateImpl)that).seg, thatOffsetBytes, thatLengthBytes);
  }

  @Override
  public final void copyTo(final long srcOffsetBytes,
      final WritableMemory destination, final long dstOffsetBytes, final long lengthBytes) {
    CompareAndCopy.copy(seg, srcOffsetBytes,
        ((BaseStateImpl)destination).seg, dstOffsetBytes, lengthBytes);
  }

  @Override
  public final void writeToByteStream(final long offsetBytes, final int lengthBytes,
      final ByteArrayOutputStream out) throws IOException {
    checkBounds(offsetBytes, lengthBytes, seg.byteSize());
    final byte[] bArr = new byte[lengthBytes];
    getByteArray(offsetBytes,bArr, 0, lengthBytes); //fundamental limitation of MemorySegment
    out.writeBytes(bArr);
  }

  //  //PRIMITIVE putX() and putXArray() implementations

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    MemoryAccess.setByteAtOffset(seg, offsetBytes, value);
  }

  @Override
  public final void putByteArray(final long offsetBytes, final byte[] srcArray,
      final int srcOffsetBytes, final int lengthBytes) {
    final MemorySegment srcSlice = MemorySegment.ofArray(srcArray).asSlice(srcOffsetBytes, lengthBytes);
    final MemorySegment dstSlice = seg.asSlice(offsetBytes, lengthBytes);
    dstSlice.copyFrom(srcSlice);
  }

  // OTHER WRITE METHODS

  @Override
  public final void clear() {
    seg.fill((byte)0);
  }

  @Override
  public final void clear(final long offsetBytes, final long lengthBytes) {
    final MemorySegment slice = seg.asSlice(offsetBytes, lengthBytes);
    slice.fill((byte)0);
  }

  @Override
  public final void clearBits(final long offsetBytes, final byte bitMask) {
    final byte b = MemoryAccess.getByteAtOffset(seg, offsetBytes);
    MemoryAccess.setByteAtOffset(seg, offsetBytes, (byte)(b & ~bitMask));
  }

  @Override
  public final void fill(final byte value) {
    seg.fill(value);
  }

  @Override
  public final void fill(final long offsetBytes, final long lengthBytes, final byte value) {
    final MemorySegment slice = seg.asSlice(offsetBytes, lengthBytes);
    slice.fill(value);
  }

  @Override
  public final void setBits(final long offsetBytes, final byte bitMask) {
    final byte b = MemoryAccess.getByteAtOffset(seg, offsetBytes);
    MemoryAccess.setByteAtOffset(seg, offsetBytes, (byte)(b | bitMask));
  }

}
