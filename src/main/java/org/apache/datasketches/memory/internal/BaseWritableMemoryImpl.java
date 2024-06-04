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
      final int typeId,
      final MemoryRequestServer memReqSvr) {
    super(seg, typeId, memReqSvr);
  }

  //WRAP HEAP ARRAY RESOURCE

  /**
   * Wrap a <i>MemorySegment</i> as an array
   * @param seg the given <i>MemorySegment</i>.
   * @param byteOrder the given <i>ByteOrder</i>.
   * @param memReqSvr the given <i>MemoryRequestServer</i>.
   * @return a <i>WritableMemory</i>.
   */
  public static WritableMemory wrapSegmentAsArray(
      final MemorySegment seg,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    int type = MEMORY
        | (seg.isReadOnly() ? READONLY : 0);
    if (byteOrder == NON_NATIVE_BYTE_ORDER) {
      type |= NONNATIVE;
      return new NonNativeWritableMemoryImpl(seg, type, memReqSvr);
    }
    return new NativeWritableMemoryImpl(seg, type, memReqSvr);
  }

  //BYTE BUFFER RESOURCE

  /**
   * The implementation of <i>wrapByteBuffer</i> for WritableMemory.
   * @param byteBuffer the given <i>ByteBuffer</i>
   * @param localReadOnly true if read-only is being imposed locally, independent of the read-only state of the given ByteBuffer.
   * @param byteOrder the given <i>ByteOrder</i>.
   * @param memReqSvr the given <i>MemoryRequestServer</i>.
   * @return a <i>WritableMemory</i>.
   */
  public static WritableMemory wrapByteBuffer(
      final ByteBuffer byteBuffer,
      final boolean localReadOnly,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
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
    int type = MEMORY | BYTEBUF
        | (localReadOnly ? READONLY : 0)
        | (seg.isNative() ? DIRECT : 0)
        | (seg.isMapped() ? MAP : 0);
    final WritableMemory wmem;
    if (byteOrder == NON_NATIVE_BYTE_ORDER) {
      type |= NONNATIVE;
      wmem = new NonNativeWritableMemoryImpl(seg, type, memReqSvr);
    } else {
      wmem = new NativeWritableMemoryImpl(seg, type, memReqSvr);
    }
    return wmem;
  }

  //MAP FILE RESOURCE

  /**
   * The implementation of <i>wrapMap</i> for <i>WritableMemory</i>.
   * @param file the given file to map.
   * @param fileOffsetBytes the file starting offset in bytes.
   * @param capacityBytes the capacity of the mapped memory.
   * @param scope the given scope
   * @param localReadOnly true if read-only is being imposed locally, independent of the read-only state of the given ByteBuffer.
   * @param byteOrder the given <i>ByteOrder</i>
   * @return a <i>WritableMemory</i>
   * @throws IllegalArgumentException if file is not readable.
   * @throws IOException if mapping is not successful.
   */
  public static WritableMemory wrapMap(
      final File file,
      final long fileOffsetBytes,
      final long capacityBytes,
      final ResourceScope scope,
      final boolean localReadOnly,
      final ByteOrder byteOrder)
          throws IllegalArgumentException, IOException {
    Objects.requireNonNull(file, "File must be non-null.");
    Objects.requireNonNull(byteOrder, "ByteOrder must be non-null.");
    Objects.requireNonNull(scope, "ResourceScope must be non-null.");
    if (!file.canRead()) { throw new IllegalArgumentException("File must be readable."); }
    if (!localReadOnly && !file.canWrite()) { throw new IllegalArgumentException("File must be writable."); }
    final FileChannel.MapMode mapMode = (localReadOnly) ? READ_ONLY : READ_WRITE;

    final MemorySegment seg = MemorySegment.mapFile(file.toPath(), fileOffsetBytes, capacityBytes, mapMode, scope);
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final int type = MEMORY | MAP | DIRECT
        | (localReadOnly ? READONLY : 0)
        | (nativeBOType ? NATIVE : NONNATIVE);
    return nativeBOType
        ? new NativeWritableMemoryImpl(seg, type, null)
        : new NonNativeWritableMemoryImpl(seg, type, null);
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
  //@SuppressWarnings("resource")
  public static WritableMemory wrapDirect(
      final long capacityBytes,
      final long alignmentBytes,
      final ResourceScope scope,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    Objects.requireNonNull(scope, "ResourceScope must be non-null");
    Objects.requireNonNull(byteOrder, "ByteOrder must be non-null");
    final MemorySegment seg = MemorySegment.allocateNative(
        capacityBytes,
        alignmentBytes,
        scope);
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final int type = MEMORY | DIRECT
        | (nativeBOType ? NATIVE : NONNATIVE);
    return nativeBOType
        ? new NativeWritableMemoryImpl(seg, type, memReqSvr)
        : new NonNativeWritableMemoryImpl(seg, type, memReqSvr);
  }

  //REGION DERIVED

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    return regionImpl(offsetBytes, capacityBytes, true, byteOrder);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    if (this.isReadOnly()) {
      throw new IllegalArgumentException("Cannot create a writable region from a read-only Memory.");
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
    final boolean duplicateType = isDuplicate();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = hasByteBuffer();
    final int type = MEMORY | REGION
        | (readOnly ? READONLY : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (mapType ? MAP : 0)
        | (directType ? DIRECT : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    final WritableMemory wmem = selectMemory(slice, type, memReqSvr, byteBufferType, mapType, nativeBOType);
    return wmem;
  }

  //AS BUFFER DERIVED

  @Override
  public Buffer asBuffer(final ByteOrder byteOrder) {
    return asWritableBufferImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer asWritableBuffer(final ByteOrder byteOrder) {
    if (isReadOnly()) {
      throw new IllegalArgumentException(
          "Cannot create a writable buffer from a read-only Memory.");
    }
    return asWritableBufferImpl(false, byteOrder);
  }

  private WritableBuffer asWritableBufferImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (!this.isAlive()) { throw new IllegalStateException("This Memory is not alive."); }
    Objects.requireNonNull(byteOrder, "byteOrder must be non-null");
    final boolean readOnly = isReadOnly() || localReadOnly;
    final MemorySegment seg2 = (readOnly && !seg.isReadOnly()) ? seg.asReadOnly() : seg;
    final boolean regionType = isRegion();
    final boolean duplicateType = isDuplicate();
    final boolean mapType = seg.isMapped();
    final boolean directType = seg.isNative();
    final boolean nativeBOType = byteOrder == ByteOrder.nativeOrder();
    final boolean byteBufferType = hasByteBuffer();
    final int type = BUFFER
        | (readOnly ? READONLY : 0)
        | (regionType ? REGION : 0)
        | (duplicateType ? DUPLICATE : 0)
        | (directType ? DIRECT : 0)
        | (mapType ? MAP : 0)
        | (nativeBOType ? NATIVE : NONNATIVE)
        | (byteBufferType ? BYTEBUF : 0);

    final WritableBuffer wbuf = selectBuffer(seg2, type, memReqSvr, byteBufferType, mapType, nativeBOType);
    wbuf.setStartPositionEnd(0, 0, wbuf.getCapacity());
    return wbuf;
  }

  //PRIMITIVE getX() and getXArray()

  @Override
  public final boolean getBoolean(final long offsetBytes) {
    return getByte(offsetBytes) != 0;
  }

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
  public final void putBoolean(final long offsetBytes, final boolean value) {
    putByte(offsetBytes, value ? (byte)1 : 0);
  }

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
  public final byte[] getArray() {
    return seg.toByteArray();
  }
  
  @Override
  public final void setBits(final long offsetBytes, final byte bitMask) {
    final byte b = MemoryAccess.getByteAtOffset(seg, offsetBytes);
    MemoryAccess.setByteAtOffset(seg, offsetBytes, (byte)(b | bitMask));
  }

}
