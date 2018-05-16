/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_CHAR_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_INT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_LONG_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_SHORT_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

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
abstract class BaseWritableMemoryImpl extends WritableMemory {
  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  final boolean localReadOnly;

  //Static variable for cases where byteBuf/array/direct sizes are zero
  final static WritableMemoryImpl ZERO_SIZE_MEMORY;

  static {
    final ResourceState state = new ResourceState(new byte[0], Prim.BYTE, 0);
    ZERO_SIZE_MEMORY = new WritableMemoryImpl(state, true);
  }

  BaseWritableMemoryImpl(final ResourceState state, final boolean localReadOnly) {
    unsafeObj = state.getUnsafeObject();
    this.state = state;
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
    this.localReadOnly = localReadOnly;
  }

  static BaseWritableMemoryImpl newInstance(final ResourceState state,
      final boolean localReadOnly) {
    if (state.getCapacity() == 0) { return BaseWritableMemoryImpl.ZERO_SIZE_MEMORY; }
    if (state.getResourceOrder() == ByteOrder.nativeOrder()) {
      return new WritableMemoryImpl(state, localReadOnly);
    } else {
      return new NonNativeWritableMemoryImpl(state, localReadOnly);
    }
  }

  //REGIONS XXX
  @Override
  public Memory region(final long offsetBytes, final long capacityBytes) {
    return writableRegionImpl(offsetBytes, capacityBytes, true);
  }

  @Override
  public WritableMemory writableRegion(final long offsetBytes, final long capacityBytes) {
    if (localReadOnly) {
      throw new ReadOnlyException("Writable region of a read-only Memory is not allowed.");
    }
    return writableRegionImpl(offsetBytes, capacityBytes, false);
  }

  abstract WritableMemory writableRegionImpl(long offsetBytes, long capacity,
      boolean localReadOnly);

  @Override
  public ByteBuffer unsafeByteBufferView(final long offsetBytes, final int capacityBytes) {
    checkValidAndBounds(offsetBytes, capacityBytes);
    long cumOffset = getCumulativeOffset(offsetBytes);
    Object unsafeObj = this.unsafeObj;
    ByteBuffer result;
    if (unsafeObj == null) {
      result = AccessByteBuffer.getDummyReadOnlyDirectByteBuffer(cumOffset, capacityBytes);
    } else if (unsafeObj instanceof byte[]) {
      int arrayOffset = (int) (cumOffset - ARRAY_BYTE_BASE_OFFSET);
      result = ByteBuffer.wrap((byte[]) unsafeObj, arrayOffset, capacityBytes)
          .slice().asReadOnlyBuffer();
    } else {
      throw new UnsupportedOperationException(
          "This Memory object is the result of wrapping a " +
              unsafeObj.getClass().getSimpleName() +
              " array, it could not be viewed as a ByteBuffer.");
    }
    result.order(getResourceOrder());
    return result;
  }

  //BUFFER XXX
  @Override
  public Buffer asBuffer() {
    return asWritableBufferImpl(true);
  }

  @Override
  public WritableBuffer asWritableBuffer() {
    if (localReadOnly) {
      throw new ReadOnlyException("Wrapping a read-only Memory as a writable Buffer is not allowed.");
    }
    return asWritableBufferImpl(false);
  }

  abstract WritableBuffer asWritableBufferImpl(boolean localReadOnly);

  //PRIMITIVE getXXX() and getXXXArray() ENDIAN INDEPENDENT XXX
  @Override
  public final boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public final void getBooleanArray(final long offsetBytes, final boolean[] dstArray,
      final int dstOffsetBooleans, final int lengthBooleans) {
    final long copyBytes = lengthBooleans;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetBooleans, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + dstOffsetBooleans,
        copyBytes);
  }

  @Override
  public final byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public final void getByteArray(final long offsetBytes, final byte[] dstArray,
      final int dstOffsetBytes, final int lengthBytes) {
    final long copyBytes = lengthBytes;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffsetBytes, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + dstOffsetBytes,
        copyBytes);
  }

  @Override
  public final int getCharsFromUtf8(final long offsetBytes, final int utf8LengthBytes,
      final Appendable dst) throws IOException, Utf8CodingException {
    checkValidAndBounds(offsetBytes, utf8LengthBytes);
    return Utf8.getCharsFromUtf8(offsetBytes, utf8LengthBytes, dst, state);
  }

  //PRIMITIVE getXXX() Native Endian (used by both endians) XXX
  final char getNativeOrderedChar(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    return unsafe.getChar(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final int getNativeOrderedInt(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_INT_INDEX_SCALE);
    return unsafe.getInt(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final long getNativeOrderedLong(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    return unsafe.getLong(unsafeObj, cumBaseOffset + offsetBytes);
  }

  final short getNativeOrderedShort(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    return unsafe.getShort(unsafeObj, cumBaseOffset + offsetBytes);
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo, equals XXX
  @Override
  public final int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Memory thatMem, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(state, thisOffsetBytes, thisLengthBytes,
        thatMem.getResourceState(), thatOffsetBytes, thatLengthBytes);
  }

  @Override
  public final void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    CompareAndCopy.copy(state, srcOffsetBytes, destination.getResourceState(),
        dstOffsetBytes, lengthBytes);
  }

  @Override
  public final void writeTo(final long offsetBytes, final long lengthBytes,
      final WritableByteChannel out) throws IOException {
    checkValidAndBounds(offsetBytes, lengthBytes);
    if (unsafeObj instanceof byte[]) {
      writeByteArrayTo((byte[]) unsafeObj, offsetBytes, lengthBytes, out);
    } else if (unsafeObj == null) {
      writeDirectMemoryTo(offsetBytes, lengthBytes, out);
    } else {
      // Memory is backed by some array that is not byte[], for example int[], long[], etc.
      // We don't have other choice as to do extra intermediate copy.
      writeToWithExtraCopy(offsetBytes, lengthBytes, out);
    }
  }

  @Override
  public final boolean equals(final Object that) {
    if (this == that) { return true; }
    return (that instanceof Memory)
        ? CompareAndCopy.equals(state, ((Memory)that).getResourceState()) : false;
  }

  @Override
  public final boolean equalTo(final long thisOffsetBytes, final Memory that,
      final long thatOffsetBytes, final long lengthBytes) {
    return CompareAndCopy.equals(state, thisOffsetBytes, that.getResourceState(),
        thatOffsetBytes, lengthBytes);
  }

  @Override
  public final int hashCode() {
    return CompareAndCopy.hashCode(state);
  }

  //OTHER READ METHODS XXX
  @Override
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public final long getCapacity() {
    assertValid();
    return capacity;
  }

  @Override
  public final long getCumulativeOffset(final long offsetBytes) {
    assertValid();
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public final long getRegionOffset(final long offsetBytes) {
    assertValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public final ByteOrder getResourceOrder() {
    assertValid();
    return state.getResourceOrder();
  }

  @Override
  public final boolean hasArray() {
    assertValid();
    return unsafeObj != null;
  }

  @Override
  public final boolean hasByteBuffer() {
    assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public final boolean isDirect() {
    assertValid();
    return state.isDirect();
  }

  @Override
  public final boolean isReadOnly() {
    assertValid();
    return state.isResourceReadOnly() || localReadOnly;
  }

  @Override
  public final boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
    checkValid();
    ((BaseWritableMemoryImpl) that).checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public final boolean isValid() {
    return state.isValid();
  }

  @Override
  public final boolean isSwapBytes() {
    assertValid();
    return state.isSwapBytes();
  }

  @Override
  public final String toHexString(final String header, final long offsetBytes,
      final int lengthBytes) {
    checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state, localReadOnly);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public final void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
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
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public final void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
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
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public final long putCharsToUtf8(final long offsetBytes, final CharSequence src) {
    checkValid();
    return Utf8.putCharsToUtf8(offsetBytes, src, state);
  }

  //PRIMITIVE putXXX() Native Endian (used by both endians) XXX
  final void putNativeOrderedChar(final long offsetBytes, final char value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_CHAR_INDEX_SCALE);
    unsafe.putChar(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedInt(final long offsetBytes, final int value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_INT_INDEX_SCALE);
    unsafe.putInt(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedLong(final long offsetBytes, final long value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_LONG_INDEX_SCALE);
    unsafe.putLong(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  final void putNativeOrderedShort(final long offsetBytes, final short value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_SHORT_INDEX_SCALE);
    unsafe.putShort(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  //OTHER WRITE METHODS XXX
  @Override
  public final Object getArray() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public final ByteBuffer getByteBuffer() {
    assertValid();
    return state.getByteBuffer();
  }

  @Override
  public final void clear() {
    fill(0, capacity, (byte) 0);
  }

  @Override
  public final void clear(final long offsetBytes, final long lengthBytes) {
    fill(offsetBytes, lengthBytes, (byte) 0);
  }

  @Override
  public final void clearBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long cumBaseOff = cumBaseOffset + offsetBytes;
    int value = unsafe.getByte(unsafeObj, cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(unsafeObj, cumBaseOff, (byte)value);
  }

  @Override
  public final void fill(final byte value) {
    fill(0, capacity, value);
  }

  @Override
  public final void fill(long offsetBytes, long lengthBytes, final byte value) {
    checkValidAndBoundsForWrite(offsetBytes, lengthBytes);
    while (lengthBytes > 0) {
      final long chunk = Math.min(lengthBytes, CompareAndCopy.UNSAFE_COPY_THRESHOLD_BYTES);
      unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, chunk, value);
      offsetBytes += chunk;
      lengthBytes -= chunk;
    }
  }

  @Override
  public final void setBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public final MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
    assertValid();
    return state.getMemoryRequestServer();
  }

  //RESTRICTED XXX
  private void writeByteArrayTo(final byte[] unsafeObj, final long offsetBytes,
      final long lengthBytes, final WritableByteChannel out) throws IOException {
    final int off =
        Ints.checkedCast((cumBaseOffset + offsetBytes) - UnsafeUtil.ARRAY_BYTE_BASE_OFFSET);
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
    final int bufLen = Ints.checkedCast(Math.max(8, Math.min((capacity / 1024) & ~7L, 4096)));
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

  @Override
  final ResourceState getResourceState() {
    return state;
  }

  final void assertValid() {
    assert state.isValid() : "Memory not valid.";
  }

  final void checkValid() {
    state.checkValid();
  }

  final void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
  }

  final void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
    assert !localReadOnly : "Memory is read-only.";
  }

  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
    if (localReadOnly) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }
}
