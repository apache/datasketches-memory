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

package org.apache.datasketches.memory;

import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static org.apache.datasketches.memory.UnsafeUtil.checkBounds;
import static org.apache.datasketches.memory.UnsafeUtil.unsafe;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.WritableByteChannel;

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
@SuppressWarnings({"restriction"})
abstract class BaseWritableMemoryImpl extends WritableMemory {

  //1KB of empty bytes for speedy clear()
  private final static byte[] EMPTY_BYTES;

  //Static variable for cases where byteBuf/array/direct sizes are zero
  final static BaseWritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    EMPTY_BYTES = new byte[1024];
    ZERO_SIZE_MEMORY = new HeapWritableMemoryImpl(new byte[0], 0L, 0L, READONLY);
  }

  //Pass-through ctor
  BaseWritableMemoryImpl(final Object unsafeObj, final long nativeBaseOffset,
      final long regionOffset, final long capacityBytes) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes);
  }

  static BaseWritableMemoryImpl wrapHeapArray(final Object arr, final long offsetBytes,
      final long lengthBytes, final boolean localReadOnly, final ByteOrder byteOrder) {
    if (lengthBytes == 0) { return BaseWritableMemoryImpl.ZERO_SIZE_MEMORY; }
    final int typeId = localReadOnly ? READONLY : 0;
    return isNativeByteOrder(byteOrder)
        ? new HeapWritableMemoryImpl(arr, offsetBytes, lengthBytes, typeId)
        : new HeapNonNativeWritableMemoryImpl(arr, offsetBytes, lengthBytes, typeId);
  }

  static BaseWritableMemoryImpl wrapByteBuffer(
      final ByteBuffer byteBuf, final boolean localReadOnly, final ByteOrder byteOrder) {
    final AccessByteBuffer abb = new AccessByteBuffer(byteBuf);
    if (abb.resourceReadOnly && !localReadOnly) {
      throw new ReadOnlyException("ByteBuffer is Read Only");
    }
    final int typeId = (abb.resourceReadOnly || localReadOnly) ? READONLY : 0;
    return isNativeByteOrder(byteOrder)
        ? new BBWritableMemoryImpl(abb.unsafeObj, abb.nativeBaseOffset,
            abb.regionOffset, abb.capacityBytes, typeId, byteBuf)
        : new BBNonNativeWritableMemoryImpl(abb.unsafeObj, abb.nativeBaseOffset,
            abb.regionOffset, abb.capacityBytes,  typeId, byteBuf);
  }

  @SuppressWarnings("resource")
  static WritableMapHandle wrapMap(final File file, final long fileOffsetBytes,
      final long capacityBytes, final boolean localReadOnly, final ByteOrder byteOrder) {

    final AllocateDirectWritableMap dirWMap =
        new AllocateDirectWritableMap(file, fileOffsetBytes, capacityBytes, localReadOnly);
    if (dirWMap.resourceReadOnly && !localReadOnly) {
      dirWMap.close();
      throw new ReadOnlyException("File is Read Only");
    }
    final int typeId = (dirWMap.resourceReadOnly || localReadOnly) ? READONLY : 0;
    final BaseWritableMemoryImpl wmem = isNativeByteOrder(byteOrder)
        ? new MapWritableMemoryImpl(dirWMap.nativeBaseOffset, 0L, capacityBytes,
            typeId, dirWMap.getValid())
        : new MapNonNativeWritableMemoryImpl(dirWMap.nativeBaseOffset, 0L, capacityBytes,
            typeId, dirWMap.getValid());
    return new WritableMapHandle(dirWMap, wmem);
  }

  @SuppressWarnings("resource")
  static WritableDirectHandle wrapDirect(final long capacityBytes,
      final ByteOrder byteOrder, final MemoryRequestServer memReqSvr) {
    if (capacityBytes <= 0) {
      throw new IllegalArgumentException(
          "Capacity bytes should be positive, " + capacityBytes + " given");
    }
    final AllocateDirect direct = new AllocateDirect(capacityBytes);
    final int typeId = 0; //direct is never read-only on construction
    final BaseWritableMemoryImpl wmem = isNativeByteOrder(byteOrder)
        ? new DirectWritableMemoryImpl(direct.getNativeBaseOffset(), 0L, capacityBytes,
            typeId, direct.getValid(), memReqSvr)
        : new DirectNonNativeWritableMemoryImpl(direct.getNativeBaseOffset(), 0L, capacityBytes,
            typeId, direct.getValid(), memReqSvr);

    final WritableDirectHandle handle = new WritableDirectHandle(direct, wmem);
    return handle;
  }

  //UNSAFE BYTE BUFFER VIEW FOR DIRECT MEMORY ONLY
  @Override
  public ByteBuffer unsafeByteBufferView(final long offsetBytes, final int capacityBytes) {
    checkValidAndBounds(offsetBytes, capacityBytes);
    final long cumOffset = getCumulativeOffset(offsetBytes);
    final Object unsafeObj = getUnsafeObject();
    final ByteBuffer result;
    if (unsafeObj == null) {
      result = AccessByteBuffer.getDummyReadOnlyDirectByteBuffer(cumOffset, capacityBytes);
    } else if (unsafeObj instanceof byte[]) {
      final int arrayOffset = (int) (cumOffset - ARRAY_BYTE_BASE_OFFSET);
      result = ByteBuffer.wrap((byte[]) unsafeObj, arrayOffset, capacityBytes)
          .slice().asReadOnlyBuffer();
    } else {
      throw new UnsupportedOperationException(
          "This Memory object is the result of wrapping a "
              + unsafeObj.getClass().getSimpleName()
              + " array, it could not be viewed as a ByteBuffer.");
    }
    result.order(getTypeByteOrder());
    return result;
  }

  //REGIONS
  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, true, getTypeByteOrder());
  }

  @Override
  public Memory region(final long offsetBytes, final long capacityBytes, final ByteOrder byteOrder) {
    return writableRegionImpl(offsetBytes, capacityBytes, true, byteOrder);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, false, getTypeByteOrder());
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes,
      final ByteOrder byteOrder) {
    return writableRegionImpl(offsetBytes, capacityBytes, false, byteOrder);
  }

  WritableMemory writableRegionImpl(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    if (capacityBytes == 0) { return ZERO_SIZE_MEMORY; }
    if (isReadOnly() && !localReadOnly) {
      throw new ReadOnlyException("Writable region of a read-only Memory is not allowed.");
    }
    final boolean readOnly = isReadOnly() || localReadOnly;
    checkValidAndBounds(offsetBytes, capacityBytes);
    return toWritableRegion(offsetBytes, capacityBytes, readOnly, byteOrder);
  }

  abstract BaseWritableMemoryImpl toWritableRegion(
      long offsetBytes, long capcityBytes, boolean readOnly, ByteOrder byteOrder);

  //AS BUFFER
  @Override
  public Buffer asBuffer() {
    return asWritableBufferImpl(true, getTypeByteOrder());
  }

  @Override
  public Buffer asBuffer(final ByteOrder byteOrder) {
    return asWritableBufferImpl(true, byteOrder);
  }

  @Override
  public WritableBuffer asWritableBuffer() {
    return asWritableBufferImpl(false, getTypeByteOrder());
  }

  @Override
  public WritableBuffer asWritableBuffer(final ByteOrder byteOrder) {
    return asWritableBufferImpl(false, byteOrder);
  }

  WritableBuffer asWritableBufferImpl(final boolean localReadOnly, final ByteOrder byteOrder) {
    if (isReadOnly() && !localReadOnly) {
      throw new ReadOnlyException(
          "Converting a read-only Memory to a writable Buffer is not allowed.");
    }
    final boolean readOnly = isReadOnly() || localReadOnly;
    final WritableBuffer wbuf = toWritableBuffer(readOnly, byteOrder);
    wbuf.setStartPositionEnd(0, 0, getCapacity());
    return wbuf;
  }

  abstract BaseWritableBufferImpl toWritableBuffer(boolean readOnly, ByteOrder byteOrder);

  //PRIMITIVE getX() and getXArray() ENDIAN INDEPENDENT
  @Override
  public final boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public final void getBooleanArray(final long offsetBytes, final boolean[] dstArray,
      final int dstOffsetBooleans, final int lengthBooleans) {
    final long copyBytes = lengthBooleans;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetBooleans, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + dstOffsetBooleans,
        copyBytes);
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  @Override
  public final void getByteArray(final long offsetBytes, final byte[] dstArray,
      final int dstOffsetBytes, final int lengthBytes) {
    final long copyBytes = lengthBytes;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + dstOffsetBytes,
        copyBytes);
  }

  @Override
  public final int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final Appendable dst) throws IOException, Utf8CodingException {
    checkValidAndBounds(offsetBytes, utf8LengthBytes);
    return Utf8.getCharsFromUtf8(offsetBytes, utf8LengthBytes, dst, getCumulativeOffset(),
        getUnsafeObject());
  }

  @Override
  public final int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final StringBuilder dst) throws Utf8CodingException {
    try {
      // Ensure that we do at most one resize of internal StringBuilder's char array
      dst.ensureCapacity(dst.length() + utf8LengthBytes);
      return getCharsFromUtf8(offsetBytes, utf8LengthBytes, (Appendable) dst);
    } catch (final IOException e) {
      throw new RuntimeException("Should not happen", e);
    }
  }

  //PRIMITIVE getX() Native Endian (used by both endians)
  final char getNativeOrderedChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final int getNativeOrderedInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final long getNativeOrderedLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  final short getNativeOrderedShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(getUnsafeObject(), getCumulativeOffset(offsetBytes));
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo, equals
  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Memory thatMem, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(this, thisOffsetBytes, thisLengthBytes,
        thatMem, thatOffsetBytes, thatLengthBytes);
  }

  @Override
  public final void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    CompareAndCopy.copy(this, srcOffsetBytes, destination,
        dstOffsetBytes, lengthBytes);
  }

  @Override
  public final void writeTo(final long offsetBytes, final long lengthBytes,
      final WritableByteChannel out) throws IOException {
    checkValidAndBounds(offsetBytes, lengthBytes);
    if (getUnsafeObject() instanceof byte[]) {
      writeByteArrayTo((byte[]) getUnsafeObject(), offsetBytes, lengthBytes, out);
    } else if (getUnsafeObject() == null) {
      writeDirectMemoryTo(offsetBytes, lengthBytes, out);
    } else {
      // Memory is backed by some array that is not byte[], for example int[], long[], etc.
      // We don't have other choice as to do extra intermediate copy.
      writeToWithExtraCopy(offsetBytes, lengthBytes, out);
    }
  }

  //PRIMITIVE putX() and putXArray() implementations
  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public final void putBooleanArray(final long offsetBytes, final boolean[] srcArray,
      final int srcOffsetBooleans, final int lengthBooleans) {
    final long copyBytes = lengthBooleans;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetBooleans, lengthBooleans, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + srcOffsetBooleans,
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  @Override
  public final void putByteArray(final long offsetBytes, final byte[] srcArray,
      final int srcOffsetBytes, final int lengthBytes) {
    final long copyBytes = lengthBytes;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffsetBytes, lengthBytes, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_BYTE_BASE_OFFSET + srcOffsetBytes,
        getUnsafeObject(),
        getCumulativeOffset(offsetBytes),
        copyBytes
    );
  }

  @Override
  public final long putCharsToUtf8(final long offsetBytes, final CharSequence src) {
    checkValid();
    return Utf8.putCharsToUtf8(offsetBytes, src, getCapacity(), getCumulativeOffset(),
        getUnsafeObject());
  }

  //PRIMITIVE putX() Native Endian (used by both endians)
  final void putNativeOrderedChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  final void putNativeOrderedShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(getUnsafeObject(), getCumulativeOffset(offsetBytes), value);
  }

  //OTHER WRITE METHODS
  @Override
  public final Object getArray() {
    assertValid();
    return getUnsafeObject();
  }

  @Override
  public final void clear() {
    clear(0, getCapacity());
  }

  @Override
  public final void clear(final long offsetBytes, final long lengthBytes)
  {
    //No need to check bounds, since putByteArray calls checkValidAndBoundsForWrite

    final long endBytes = offsetBytes + lengthBytes;
    for (long i = offsetBytes; i < endBytes; i += EMPTY_BYTES.length) {
      putByteArray(i, EMPTY_BYTES, 0, (int) Math.min(EMPTY_BYTES.length, endBytes - i));
    }
  }

  @Override
  public final void clearBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long cumBaseOff = getCumulativeOffset(offsetBytes);
    int value = unsafe.getByte(getUnsafeObject(), cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(getUnsafeObject(), cumBaseOff, (byte)value);
  }

  @Override
  public final void fill(final byte value) {
    fill(0, getCapacity(), value);
  }

  @Override
  public final void fill(long offsetBytes, long lengthBytes, final byte value) {
    checkValidAndBoundsForWrite(offsetBytes, lengthBytes);
    while (lengthBytes > 0) {
      final long chunk = Math.min(lengthBytes, CompareAndCopy.UNSAFE_COPY_THRESHOLD_BYTES);
      unsafe.setMemory(getUnsafeObject(), getCumulativeOffset(offsetBytes), chunk, value);
      offsetBytes += chunk;
      lengthBytes -= chunk;
    }
  }

  @Override
  public final void setBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long myOffset = getCumulativeOffset(offsetBytes);
    final byte value = unsafe.getByte(getUnsafeObject(), myOffset);
    unsafe.putByte(getUnsafeObject(), myOffset, (byte)(value | bitMask));
  }

  //RESTRICTED
  private void writeByteArrayTo(final byte[] unsafeObj, final long offsetBytes,
      final long lengthBytes, final WritableByteChannel out) throws IOException {
    final int off =
        Ints.checkedCast((getCumulativeOffset(offsetBytes)) - UnsafeUtil.ARRAY_BYTE_BASE_OFFSET);
    final int len = Ints.checkedCast(lengthBytes);
    final ByteBuffer bufToWrite = ByteBuffer.wrap(unsafeObj, off, len);
    writeFully(bufToWrite, out);
  }

  private void writeDirectMemoryTo(final long offsetBytes, long lengthBytes,
      final WritableByteChannel out) throws IOException {
    long addr = getCumulativeOffset(offsetBytes);
    // Do chunking, because it's likely that WritableByteChannel.write(ByteBuffer) in some network-
    // or file-backed WritableByteChannel implementations with direct ByteBuffer argument could
    // be subject of the same safepoint problems as in Unsafe.copyMemory and Unsafe.setMemory.
    while (lengthBytes > 0) {
      final int chunk = (int) Math.min(CompareAndCopy.UNSAFE_COPY_THRESHOLD_BYTES, lengthBytes);
      final ByteBuffer bufToWrite = AccessByteBuffer.getDummyReadOnlyDirectByteBuffer(addr, chunk);
      writeFully(bufToWrite, out);
      addr += chunk;
      lengthBytes -= chunk;
    }
  }

  private void writeToWithExtraCopy(long offsetBytes, long lengthBytes,
      final WritableByteChannel out) throws IOException {
    // Keep the bufLen a multiple of 8, to maybe allow getByteArray() to go a faster path.
    final int bufLen = Ints.checkedCast(Math.max(8, Math.min((getCapacity() / 1024) & ~7L, 4096)));
    final byte[] buf = new byte[bufLen];
    final ByteBuffer bufToWrite = ByteBuffer.wrap(buf);
    while (lengthBytes > 0) {
      final int chunk = (int) Math.min(buf.length, lengthBytes);
      getByteArray(offsetBytes, buf, 0, chunk);
      bufToWrite.clear().limit(chunk);
      writeFully(bufToWrite, out);
      offsetBytes += chunk;
      lengthBytes -= chunk;
    }
  }

  private static void writeFully(final ByteBuffer bufToWrite, final WritableByteChannel out)
      throws IOException {
    while (bufToWrite.remaining() > 0) {
      out.write(bufToWrite);
    }
  }

}
