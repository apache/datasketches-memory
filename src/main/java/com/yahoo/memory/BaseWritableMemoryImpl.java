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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/*
 * Developer notes: The heavier methods, such as put/get arrays, duplicate, region, clear, fill,
 * compareTo, etc., use hard checks (checkValid() and checkBounds()), which execute at runtime
 * and throw exceptions if violated. The cost of the runtime checks are minor compared to
 * the rest of the work these methods are doing.
 *
 * <p>The light weight methods, such as put/get primitives, use asserts (assertValid() and
 * assertBounds()), which only execute when asserts are enabled and JIT will remove them
 * entirely from production runtime code. The light weight methods
 * will simplify to a single unsafe call, which is further simplified by JIT to an intrinsic
 * that is often a single CPU instruction.
 */


/**
 * Common base of native-ordered and non-native-ordered {@link WritableMemory} implementations.
 * Contains methods which are agnostic to the byte order.
 */
abstract class BaseWritableMemoryImpl extends WritableMemory {

  /**
   * Don't use {@link sun.misc.Unsafe#copyMemory} to copy blocks of memory larger than this
   * threshold, because internally it doesn't have safepoint polls, that may cause long
   * "Time To Safe Point" pauses in the application. This has been fixed in JDK 9 (see
   * https://bugs.openjdk.java.net/browse/JDK-8149596 and
   * https://bugs.openjdk.java.net/browse/JDK-8141491), but not in JDK 8, so the Memory library
   * should keep having this boilerplate as long as it supports Java 8.
   */
  static final long UNSAFE_COPY_MEMORY_THRESHOLD = 1024 * 1024;

  final ResourceState state;
  final Object unsafeObj; //Array objects are held here.
  final long capacity;
  final long cumBaseOffset; //Holds the cumulative offset to the start of data.

  BaseWritableMemoryImpl(final ResourceState state) {
    unsafeObj = state.getUnsafeObject();
    this.state = state;
    capacity = state.getCapacity();
    cumBaseOffset = state.getCumBaseOffset();
  }

  ///PRIMITIVE getXXX() and getXXXArray() XXX
  @Override
  public boolean getBoolean(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    return unsafe.getBoolean(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getBooleanArray(final long offsetBytes, final boolean[] dstArray,
      final int dstOffset, final int lengthBooleans) {
    state.checkValid();
    final long copyBytes = lengthBooleans;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthBooleans, dstArray.length);
    copyMemoryCheckingNonOverlapping(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BOOLEAN_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  @Override
  public byte getByte(final long offsetBytes) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    return unsafe.getByte(unsafeObj, cumBaseOffset + offsetBytes);
  }

  @Override
  public void getByteArray(final long offsetBytes, final byte[] dstArray, final int dstOffset,
      final int lengthBytes) {
    state.checkValid();
    final long copyBytes = lengthBytes;
    checkBounds(offsetBytes, copyBytes, capacity);
    checkBounds(dstOffset, lengthBytes, dstArray.length);
    copyMemoryCheckingNonOverlapping(
        unsafeObj,
        cumBaseOffset + offsetBytes,
        dstArray,
        ARRAY_BYTE_BASE_OFFSET + dstOffset,
        copyBytes);
  }

  //OTHER PRIMITIVE READ METHODS: compareTo, copyTo XXX
  @Override
  public int compareTo(final long thisOffsetBytes, final long thisLengthBytes, final Memory thatMem,
      final long thatOffsetBytes, final long thatLengthBytes) {
    state.checkValid();
    checkBounds(thisOffsetBytes, thisLengthBytes, capacity);
    final BaseWritableMemoryImpl that = (BaseWritableMemoryImpl) thatMem;
    that.state.checkValid();
    checkBounds(thatOffsetBytes, thatLengthBytes, that.capacity);
    final long thisAdd = getCumulativeOffset(thisOffsetBytes);
    final long thatAdd = that.getCumulativeOffset(thatOffsetBytes);
    final Object thisObj = this.unsafeObj;
    final Object thatObj = that.unsafeObj;
    if (thisObj != thatObj || thisAdd != thatAdd) {
      final long lenBytes = Math.min(thisLengthBytes, thatLengthBytes);
      for (long i = 0; i < lenBytes; i++) {
        final int thisByte = unsafe.getByte(thisObj, thisAdd + i);
        final int thatByte = unsafe.getByte(thatObj, thatAdd + i);
        if (thisByte < thatByte) {
          return -1;
        }
        if (thisByte > thatByte) {
          return 1;
        }
      }
    }
    return Long.compare(thisLengthBytes, thatLengthBytes);
  }

  @Override
  public void copyTo(final long srcOffsetBytes, final WritableMemory destination,
      final long dstOffsetBytes, final long lengthBytes) {
    final BaseWritableMemoryImpl dst = (BaseWritableMemoryImpl) destination;
    state.checkValid();
    checkBounds(srcOffsetBytes, lengthBytes, capacity);
    dst.state.checkValid();
    checkBounds(dstOffsetBytes, lengthBytes, dst.capacity);
    final long srcAdd = getCumulativeOffset(srcOffsetBytes);
    final long dstAdd = destination.getCumulativeOffset(dstOffsetBytes);
    copyMemory(unsafeObj, srcAdd, dst.unsafeObj, dstAdd, lengthBytes);
  }

  static void copyMemory(final Object srcUnsafeObj, final long srcAdd,
      final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if (srcUnsafeObj != dstUnsafeObj) {
      copyNonOverlappingMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
    } else {
      copyMemorySlowPath(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
    }
  }

  private static void copyMemorySlowPath(final Object srcUnsafeObj, final long srcAdd,
      final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if (((srcAdd + lengthBytes) <= dstAdd) || ((dstAdd + lengthBytes) <= srcAdd)) {
      copyNonOverlappingMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
      return;
    }
    if (srcAdd == dstAdd) {
      throw new IllegalArgumentException(
          "Attempt to copy a block of memory exactly in-place, should be a bug");
    }
    // If regions do overlap, fall back to unsafe.copyMemory, tolerating potentially long
    // Time to Safe Point pauses.
    unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
  }

  static void copyMemoryCheckingNonOverlapping(final Object srcUnsafeObj, final long srcAdd,
      final Object dstUnsafeObj, final long dstAdd, final long lengthBytes) {
    if ((srcUnsafeObj != dstUnsafeObj) || ((srcAdd + lengthBytes) <= dstAdd)
        || ((dstAdd + lengthBytes) <= srcAdd)) {
      copyNonOverlappingMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, lengthBytes);
    } else {
      throw new IllegalArgumentException("Not expecting memory blocks to overlap: obj="
          + srcUnsafeObj + ", srcAddress=" + srcAdd + ", dstAddress=" + dstAdd
          + ", lengthBytes=" + lengthBytes);
    }
  }

  /* @see #UNSAFE_COPY_MEMORY_THRESHOLD */
  private static void copyNonOverlappingMemory(final Object srcUnsafeObj, long srcAdd,
      final Object dstUnsafeObj, long dstAdd, long lengthBytes) {
    while (lengthBytes > 0) {
      final long copy = Math.min(lengthBytes, UNSAFE_COPY_MEMORY_THRESHOLD);
      unsafe.copyMemory(srcUnsafeObj, srcAdd, dstUnsafeObj, dstAdd, copy);
      lengthBytes -= copy;
      srcAdd += copy;
      dstAdd += copy;
    }
  }

  //OTHER READ METHODS XXX
  @Override
  public void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    state.checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
  }

  @Override
  public long getCapacity() {
    state.assertValid();
    return capacity;
  }

  @Override
  public long getCumulativeOffset(final long offsetBytes) {
    state.assertValid();
    return cumBaseOffset + offsetBytes;
  }

  @Override
  public long getRegionOffset(final long offsetBytes) {
    state.assertValid();
    return state.getRegionOffset() + offsetBytes;
  }

  @Override
  public boolean hasArray() {
    state.assertValid();
    return unsafeObj != null;
  }

  @Override
  public boolean hasByteBuffer() {
    state.assertValid();
    return state.getByteBuffer() != null;
  }

  @Override
  public boolean isDirect() {
    state.assertValid();
    return state.isDirect();
  }

  @Override
  public boolean isResourceReadOnly() {
    state.assertValid();
    return state.isResourceReadOnly();
  }

  @Override
  public boolean isSameResource(final Memory that) {
    if (that == null) { return false; }
    state.checkValid();
    that.getResourceState().checkValid();
    return state.isSameResource(that.getResourceState());
  }

  @Override
  public boolean isValid() {
    return state.isValid();
  }

  @Override
  public ByteOrder getResourceOrder() {
    state.assertValid();
    return state.order();
  }

  @Override
  public boolean swapBytes() {
    state.assertValid();
    return state.isSwapBytes();
  }

  @Override
  public String toHexString(final String header, final long offsetBytes, final int lengthBytes) {
    state.checkValid();
    final String klass = this.getClass().getSimpleName();
    final String s1 = String.format("(..., %d, %d)", offsetBytes, lengthBytes);
    final long hcode = hashCode() & 0XFFFFFFFFL;
    final String call = ".toHexString" + s1 + ", hashCode: " + hcode;
    final StringBuilder sb = new StringBuilder();
    sb.append("### ").append(klass).append(" SUMMARY ###").append(LS);
    sb.append("Header Comment      : ").append(header).append(LS);
    sb.append("Call Parameters     : ").append(call);
    return Memory.toHex(sb.toString(), offsetBytes, lengthBytes, state);
  }

  //PRIMITIVE putXXX() and putXXXArray() implementations XXX
  @Override
  public void putBoolean(final long offsetBytes, final boolean value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BOOLEAN_INDEX_SCALE, capacity);
    unsafe.putBoolean(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putBooleanArray(final long offsetBytes, final boolean[] srcArray, final int srcOffset,
      final int lengthBooleans) {
    state.checkValid();
    final long copyBytes = lengthBooleans;
    checkBounds(srcOffset, lengthBooleans, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    copyMemoryCheckingNonOverlapping(
        srcArray,
        ARRAY_BOOLEAN_BASE_OFFSET + srcOffset,
        unsafeObj,
        cumBaseOffset + offsetBytes,
        copyBytes
    );
  }

  @Override
  public void putByte(final long offsetBytes, final byte value) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    unsafe.putByte(unsafeObj, cumBaseOffset + offsetBytes, value);
  }

  @Override
  public void putByteArray(final long offsetBytes, final byte[] srcArray, final int srcOffset,
      final int lengthBytes) {
    state.checkValid();
    final long copyBytes = lengthBytes;
    checkBounds(srcOffset, lengthBytes, srcArray.length);
    checkBounds(offsetBytes, copyBytes, capacity);
    copyMemoryCheckingNonOverlapping(
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
    state.assertValid();
    return unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    state.assertValid();
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
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
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
  public void fill(final long offsetBytes, final long lengthBytes, final byte value) {
    state.checkValid();
    checkBounds(offsetBytes, lengthBytes, capacity);
    unsafe.setMemory(unsafeObj, cumBaseOffset + offsetBytes, lengthBytes, value);
  }

  @Override
  public void setBits(final long offsetBytes, final byte bitMask) {
    state.assertValid();
    assertBounds(offsetBytes, ARRAY_BYTE_INDEX_SCALE, capacity);
    final long myOffset = cumBaseOffset + offsetBytes;
    final byte value = unsafe.getByte(unsafeObj, myOffset);
    unsafe.putByte(unsafeObj, myOffset, (byte)(value | bitMask));
  }

  //OTHER XXX
  @Override
  public MemoryRequestServer getMemoryRequestServer() { //only applicable to writable
    state.assertValid();
    return state.getMemoryRequestServer();
  }

  @Override
  public void setMemoryRequest(final MemoryRequestServer memReqSvr) {
    state.assertValid();
    state.setMemoryRequestServer(memReqSvr);
  }

  @Override
  public WritableDirectHandle getHandle() {
    state.assertValid();
    return state.getHandle();
  }

  @Override
  public void setHandle(final WritableDirectHandle handle) {
    state.assertValid();
    state.setHandle(handle);
  }

  //RESTRICTED XXX
  @Override
  ResourceState getResourceState() {
    return state;
  }
}
