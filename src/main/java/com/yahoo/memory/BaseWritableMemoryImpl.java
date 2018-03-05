/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BOOLEAN_INDEX_SCALE;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_BASE_OFFSET;
import static com.yahoo.memory.UnsafeUtil.ARRAY_BYTE_INDEX_SCALE;
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

  private static final ByteBuffer ZERO_DIRECT_BUFFER = ByteBuffer.allocateDirect(0);
  private static final long NIO_BUFFER_ADDRESS_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "address");
  private static final long NIO_BUFFER_CAPACITY_FIELD_OFFSET =
      UnsafeUtil.getFieldOffset(java.nio.Buffer.class, "capacity");

  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.
  final boolean localReadOnly;

  BaseWritableMemoryImpl(final ResourceState state, final boolean localReadOnly) {
    unsafeObj = state.getUnsafeObject();
    this.state = state;
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
    this.localReadOnly = localReadOnly;
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final long offsetBytes, final boolean[] dstArray,
      final int dstOffset, final int lengthBooleans) {
    final long copyBytes = lengthBooleans;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthBooleans, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    assertValidAndBoundsForRead(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final long offsetBytes, final byte[] dstArray, final int dstOffset,
      final int lengthBytes) {
    final long copyBytes = lengthBytes;
    checkValidAndBounds(offsetBytes, copyBytes);
    checkBounds(dstOffset, lengthBytes, dstArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo, equals XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes,
      final Memory thatMem, final long thatOffsetBytes, final long thatLengthBytes) {
    return CompareAndCopy.compare(state, thisOffsetBytes, thisLengthBytes,
        thatMem.getResourceState(), thatOffsetBytes, thatLengthBytes);
  }

  @Override
  public void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    CompareAndCopy.copy(state, srcOffsetBytes, destination.getResourceState(),
        dstOffsetBytes, lengthBytes);
  }

  @Override
  public void writeTo(final long offsetBytes, final long lengthBytes,
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
  public boolean equals(final Object that) {
    if (this == that) { return true; }
    return (that instanceof Memory)
        ? CompareAndCopy.equals(state, ((Memory)that).getResourceState()) : false;
  }

  @Override
  public boolean equalTo(final long thisOffsetBytes, final Memory that,
      final long thatOffsetBytes, final long lengthBytes) {
    return CompareAndCopy.equals(state, thisOffsetBytes, that.getResourceState(),
        thatOffsetBytes, lengthBytes);
  }

  @Override
  public int hashCode() {
    return CompareAndCopy.hashCode(state);
  }

  //OTHER READ METHODS XXX
  @Override
  public void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public long getCapacity() {
    assertValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset(final long offsetBytes) {
    assertValid();
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public long getRegionOffset(final long offsetBytes) {
    assertValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public ByteOrder getResourceOrder() {
    assertValid();
    return state.order();
  }

  @Override
  public boolean hasArray() {
    assertValid();
    return unsafeObj != null;
  }

  @Override
  public boolean hasByteBuffer() {
    assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    assertValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    assertValid();
    return state.isResourceReadOnly();
  }

  @Override
  public boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
    checkValid();
    ((BaseWritableMemoryImpl) that).checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public boolean swapBytes() {
    assertValid();
    return state.isSwapBytes();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
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
  public void putBoolean(final long offsetBytes, final boolean value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final long offsetBytes, final boolean[] srcArray, final int srcOffset,
      final int lengthBooleans) {
    final long copyBytes = lengthBooleans;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthBooleans, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + srcOffset,
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final long offsetBytes, final byte[] srcArray, final int srcOffset,
      final int lengthBytes) {
    final long copyBytes = lengthBytes;
    checkValidAndBoundsForWrite(offsetBytes, copyBytes);
    checkBounds(srcOffset, lengthBytes, srcArray.length);
    CompareAndCopy.copyMemoryCheckingDifferentObject(
        srcArray,
        ARRAY_BYTE_BASE_OFFSET + srcOffset,
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  //OTHER WRITE METHODS XXX
  @Override
  public Object getArray() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return state.getByteBuffer();
  }

  @Override
  public void clear() {
    fill(0, capacity, (byte) 0);
  }

  @Override
  public void clear(final long offsetBytes, final long lengthBytes) {
    fill(offsetBytes, lengthBytes, (byte) 0);
  }

  @Override
  public void clearBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long cumBaseOff = cumBaseOffset + offsetBytes;
    int value = unsafe.getByte(unsafeObj, cumBaseOff) & 0XFF;
    value &= ~bitMask;
    unsafe.putByte(unsafeObj, cumBaseOff, (byte)value);
  }

  @Override
  public void fill(final byte value) {
    fill(0, capacity, value);
  }

  @Override
  public void fill(long offsetBytes, long lengthBytes, final byte value) {
    checkValidAndBoundsForWrite(offsetBytes, lengthBytes);
    while (lengthBytes > 0) {
      final long chunk = Math.min(lengthBytes, CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD);
      unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, chunk, value);
      offsetBytes += chunk;
      lengthBytes -= chunk;
    }
  }

  @Override
  public void setBits(final long offsetBytes, final byte bitMask) {
    assertValidAndBoundsForWrite(offsetBytes, ARRAY_BYTE_INDEX_SCALE);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
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
      final int chunk = (int) Math.min(CompareAndCopy.UNSAFE_COPY_MEMORY_THRESHOLD, lengthBytes);
      final ByteBuffer bufToWrite = wrap(addr, chunk);
      writeFully(bufToWrite, out);
      addr += chunk;
      lengthBytes -= chunk;
    }
  }

  /**
   * This method is copied from https://github.com/odnoklassniki/one-nio/blob/
   * 27c768cbd28ece949c299f2d437c9a0ebd874500/src/one/nio/mem/DirectMemory.java#L95
   */
  private static ByteBuffer wrap(final long address, final int capacity) {
    final ByteBuffer buf = ZERO_DIRECT_BUFFER.duplicate();
    unsafe.putLong(buf, NIO_BUFFER_ADDRESS_FIELD_OFFSET, address);
    unsafe.putInt(buf, NIO_BUFFER_CAPACITY_FIELD_OFFSET, capacity);
    buf.limit(capacity);
    return buf;
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
  ResourceState getResourceState() {
    return state;
  }

  void assertValid() {
    assert state.isValid() : "Memory not valid.";
  }

  void checkValid() {
    state.checkValid();
  }

  void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
  }

  void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, capacity);
    assert !localReadOnly : "Memory is read-only.";
  }

  void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
    if (localReadOnly) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }
}
