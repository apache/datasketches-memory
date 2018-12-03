/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.LS;
import static com.yahoo.memory.UnsafeUtil.assertBounds;
import static com.yahoo.memory.UnsafeUtil.checkBounds;
import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps key configuration state for Memory and Buffer plus some common static variables
 * and check methods.
 *
 * @author Lee Rhodes
 */
abstract class BaseState {

  //Monitoring
  static final AtomicLong currentDirectMemoryAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryAllocated_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocated_ = new AtomicLong();

  static final MemoryRequestServer defaultMemReqSvr = new DefaultMemoryRequestServer();

  //class type IDs. Do not change the bit orders
  // 0000 0XXX
  static final int READONLY = 1;
  static final int REGION = 2;
  static final int DUPLICATE = 4;

  // 000X X000
  static final int HEAP = 0;
  static final int DIRECT = 1 << 3;
  static final int MAP = 2 << 3;
  static final int BYTEBUF = 3 << 3;

  // 00X0 0000
  static final int NATIVE = 0;
  static final int NONNATIVE = 1 << 5;

  // 0X00 0000
  static final int MEMORY = 0;
  static final int BUFFER = 1 << 6;

  private final long capacityBytes_;

  /**
   * This becomes the base offset used by all Unsafe calls. It is cumulative in that in includes
   * all offsets from regions, user-defined offsets when creating Memory, and the array object
   * header offset when creating Memory from primitive arrays.
   */
  private final long cumBaseOffset_;

  /**
   *
   * @param unsafeObj The primitive backing array. It may be null. Used by Unsafe calls.
   * @param nativeBaseOffset The off-heap memory address including DirectByteBuffer split offsets.
   * @param regionOffset This offset defines address zero of this object (usually a region)
   * relative to address zero of the backing resource. It is used to compute cumBaseOffset.
   * This will be loaded from heap ByteBuffers, which have a similar field used for slices.
   * It is used by region() and writableRegion().
   * This offset does not include the size of an object array header, if there is one.
   * @param capacityBytes the capacity of this object. Used by all methods when checking bounds.
   */
  BaseState(final Object unsafeObj, final long nativeBaseOffset, final long regionOffset,
      final long capacityBytes) {
    capacityBytes_ = capacityBytes;
    cumBaseOffset_ = regionOffset + ((unsafeObj == null)
        ? nativeBaseOffset
        : unsafe.arrayBaseOffset(unsafeObj.getClass()));
  }

  /**
   * Returns true if the given object is an instance of this class and has equal data contents.
   * @param that the given object
   * @return true if the given Object is an instance of this class and has equal data contents.
   */
  @Override
  public final boolean equals(final Object that) {
    if (this == that) { return true; }
    return (that instanceof BaseState)
      ? CompareAndCopy.equals(this, ((BaseState) that))
      : false;
  }

  /**
   * Returns true if the given object is an instance of this class and has equal contents to
   * this object in the given range of bytes. This will also check two distinct ranges within the
   * same object for eauals.
   * @param thisOffsetBytes the starting offset in bytes for this object.
   * @param that the given object
   * @param thatOffsetBytes the starting offset in bytes for the given object
   * @param lengthBytes the size of the range in bytes
   * @return true if the given object has equal contents to this object in the given range of
   * bytes.
   */
  public final boolean equalTo(final long thisOffsetBytes, final Object that,
      final long thatOffsetBytes, final long lengthBytes) {
    return (that instanceof BaseState)
      ? CompareAndCopy.equals(this, thisOffsetBytes, (BaseState) that, thatOffsetBytes, lengthBytes)
      : false;
  }

  /**
   * Gets the backing ByteBuffer if it exists, otherwise returns null.
   * @return the backing ByteBuffer if it exists, otherwise returns null.
   */
  //Overridden by ByteBuffer Leafs
  public ByteBuffer getByteBuffer() {
    return null;
  }

  /**
   * Gets the current ByteOrder.
   * This may be different from the ByteOrder of the backing resource.
   * @return the current ByteOrder.
   */
  public final ByteOrder getByteOrder() {
    return isNonNativeType() ? Util.nonNativeOrder : Util.nativeOrder;
  }

  /**
   * Gets the capacity of this object in bytes
   * @return the capacity of this object in bytes
   */
  public final long getCapacity() {
    assertValid();
    return capacityBytes_;
  }

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource
   * including the Java object header, if any.
   *
   * @return the cumulative offset in bytes of this object
   */
  public final long getCumulativeOffset() {
    assertValid();
    return cumBaseOffset_;
  }

  /**
   * Gets the cumulative offset in bytes of this object from the backing resource
   * including the Java object header, if any.
   *
   * @param offsetBytes offset to be added to the base cumulative offset.
   * @return the cumulative offset in bytes of this object
   */
  public final long getCumulativeOffset(final long offsetBytes) {
    assertValid();
    return cumBaseOffset_ + offsetBytes;
  }

  //made public in WritableMemory and WritableBuffer, only implemented in Direct Leafs
  abstract MemoryRequestServer getMemoryRequestServer();

  //Overridden by ByteBuffer, Direct and Map leafs
  long getNativeBaseOffset() {
    return 0;
  }

  /**
   * Returns the offset of address zero of this object relative to the address zero of the
   * backing resource but not including the size of any Java object header.
   * @return the offset of address zero of this object relative to the address zero of the
   * backing resource but not including the size of any Java object header.
   */
  public final long getRegionOffset() {
    final Object unsafeObj = getUnsafeObject();
    return (unsafeObj == null)
        ? cumBaseOffset_ - getNativeBaseOffset()
        : cumBaseOffset_ - unsafe.arrayBaseOffset(unsafeObj.getClass());
  }

  /**
   * Returns the offset of address zero of this object relative to the address zero of the
   * backing resource plus the given offsetBytes but not including the size of any Java object
   * header.
   * @param offsetBytes the given offsetBytes
   * @return the offset of address zero of this object relative to the address zero of the
   * backing resource plus the given offsetBytes but not including the size of any Java object
   * header.
   */
  public final long getRegionOffset(final long offsetBytes) {
    return getRegionOffset() + offsetBytes;
  }

  //Overridden by all leafs
  abstract int getTypeId();

  //Overridden by Heap and ByteBuffer Leafs. Made public as getArray() in WritableMemory and
  // WritableBuffer
  Object getUnsafeObject() {
    return null;
  }

  /**
   * Returns true if this object is backed by an on-heap primitive array
   * @return true if this object is backed by an on-heap primitive array
   */
  public final boolean hasArray() {
    assertValid();
    return getUnsafeObject() != null;
  }

  /**
   * Returns the hash code of this object depends upon all of its contents. Because of this, it is
   * inadvisable to use these objects as keys in hash maps or similar data structures unless it is
   * known that their contents will not change.</p>
   *
   * <p>If it is desirable to use these objects in a hash map depending only on object identity,
   * than the {@link java.util.IdentityHashMap} can be used.</p>
   *
   * <p>Use {@link #longHashCode(long, long) longHashCode(0, getCapacity())} if a 64-bit hash code
   * is required.</p>
   *
   * @return the hashCode of this object.
   */
  @Override
  public final int hashCode() {
    return CompareAndCopy.hashCode(this);
  }

  /**
   * Returns the 64-bit hash of the sequence of bytes in this object specified by 
   * <i>offsetBytes</i> and <i>lengthBytes</i>.
   *
   * @param offsetBytes the given offset in bytes to the first byte of the byte sequence.
   * @param lengthBytes the given length in bytes of the byte sequence.
   * @return the 64-bit hash of the sequence of bytes in this object specified by 
   * <i>offsetBytes</i> and <i>lengthBytes</i>.
   */
  public final long longHashCode(final long offsetBytes, final long lengthBytes) {
    return CompareAndCopy.longHashCode(this, offsetBytes, lengthBytes);
  }

  /**
   * Returns true if this Memory is backed by a ByteBuffer.
   * @return true if this Memory is backed by a ByteBuffer.
   */
  public final boolean hasByteBuffer() {
    assertValid();
    return getByteBuffer() != null;
  }

  /**
   * Returns true if the backing resource is direct (off-heap) memory.
   * This is the case for allocated direct memory, memory mapped files,
   * @return true if the backing resource is direct (off-heap) memory.
   */
  public final boolean isDirect() {
    return getUnsafeObject() == null;
  }

  /**
   * Returns true if the current byte order is native order.
   * @return true if the current byte order is native order.
   */
  public final boolean isNativeOrder() {
    assertValid();
    return Util.isNativeOrder(getByteOrder());
  }

  /**
   * Returns true if this object or the backing resource is read-only.
   * @return true if this object or the backing resource is read-only.
   */
  public final boolean isReadOnly() {
    assertValid();
    return isReadOnlyType();
  }

  /**
   * Returns true if the backing resource of <i>this</i> is identical with the backing resource
   * of <i>that</i>. The capacities must be the same.  If <i>this</i> is a region,
   * the region offset must also be the same.
   * @param that A different non-null object
   * @return true if the backing resource of <i>this</i> is the same as the backing resource
   * of <i>that</i>.
   */
  public final boolean isSameResource(final Object that) {
    checkValid();
    if (that == null) { return false; }
    final BaseState that1 = (BaseState) that;
    that1.checkValid();
    if (this == that1) { return true; }

    return (getCumulativeOffset() == that1.getCumulativeOffset())
            && (getCapacity() == that1.getCapacity())
            && (getUnsafeObject() == that1.getUnsafeObject())
            && (getByteBuffer() == that1.getByteBuffer());
  }

  /**
   * Returns true if this object is valid and has not been closed.
   * This is relevant only for direct (off-heap) memory and Mapped Files.
   * @return true if this object is valid and has not been closed.
   */
  //Overridden by Direct and Map leafs
  public boolean isValid() {
    return true;
  }

  //ASSERTS AND CHECKS
  final void assertValid() {
    assert isValid() : "Memory not valid.";
  }

  final void checkValid() {
    if (!isValid()) {
      throw new IllegalStateException("Memory not valid.");
    }
  }

  final void assertValidAndBoundsForRead(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, getCapacity());
  }

  final void assertValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    assertValid();
    assertBounds(offsetBytes, lengthBytes, getCapacity());
    assert !isReadOnly() : "Memory is read-only.";
  }

  /**
   * Checks that the specified range of bytes is within bounds of this object, throws
   * {@link IllegalArgumentException} if it's not: i. e. if offsetBytes &lt; 0, or length &lt; 0,
   * or offsetBytes + length &gt; {@link #getCapacity()}.
   * @param offsetBytes the given offset in bytes of this object
   * @param lengthBytes the given length in bytes of this object
   */
  public final void checkValidAndBounds(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, getCapacity());
  }

  final void checkValidAndBoundsForWrite(final long offsetBytes, final long lengthBytes) {
    checkValid();
    checkBounds(offsetBytes, lengthBytes, getCapacity());
    if (isReadOnly()) {
      throw new ReadOnlyException("Memory is read-only.");
    }
  }

  //TYPE ID CHECKS
  final boolean isReadOnlyType() {
    return (getTypeId() & READONLY) > 0;
  }

  final boolean isBufferType() {
    return (getTypeId() & BUFFER) > 0;
  }

  final boolean isDuplicateType() {
    return (getTypeId() & DUPLICATE) > 0;
  }

  final boolean isRegionType() {
    return (getTypeId() & REGION) > 0;
  }

  final boolean isNonNativeType() {
    return (getTypeId() & NONNATIVE) > 0;
  }

  final boolean isHeapType() {
    return ((getTypeId() >>> 3) & 3) == 0;
  }

  final boolean isDirectType() {
    return ((getTypeId() >>> 3) & 3) == 1;
  }

  final boolean isMapType() {
    return ((getTypeId() >>> 3) & 3) == 2;
  }

  final boolean isBBType() {
    return ((getTypeId() >>> 3) & 3) == 3;
  }

  //MONITORING
  /**
   * Gets the current number of active direct memory allocations.
   * @return the current number of active direct memory allocations.
   */
  public static final long getCurrentDirectMemoryAllocations() {
    return BaseState.currentDirectMemoryAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory allocated.
   * @return the current size of active direct memory allocated.
   */
  public static final long getCurrentDirectMemoryAllocated() {
    return BaseState.currentDirectMemoryAllocated_.get();
  }

  /**
   * Gets the current number of active direct memory map allocations.
   * @return the current number of active direct memory map allocations.
   */
  public static final long getCurrentDirectMemoryMapAllocations() {
    return BaseState.currentDirectMemoryMapAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory map allocated.
   * @return the current size of active direct memory map allocated.
   */
  public static final long getCurrentDirectMemoryMapAllocated() {
    return BaseState.currentDirectMemoryMapAllocated_.get();
  }

  //TO STRING
  /**
   * Returns a formatted hex string of a range of this object.
   * Used primarily for testing.
   * @param header a descriptive header
   * @param offsetBytes offset bytes relative to this object start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
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
    return toHex(this, sb.toString(), offsetBytes, lengthBytes);
  }

  /**
   * Returns a formatted hex string of an area of this object.
   * Used primarily for testing.
   * @param state the BaseState
   * @param preamble a descriptive header
   * @param offsetBytes offset bytes relative to the Memory start
   * @param lengthBytes number of bytes to convert to a hex string
   * @return a formatted hex string in a human readable array
   */
  static final String toHex(final BaseState state, final String preamble, final long offsetBytes,
      final int lengthBytes) {
    final long capacity = state.getCapacity();
    UnsafeUtil.checkBounds(offsetBytes, lengthBytes, capacity);
    final StringBuilder sb = new StringBuilder();
    final Object uObj = state.getUnsafeObject();
    final String uObjStr;
    final long uObjHeader;
    if (uObj == null) {
      uObjStr = "null";
      uObjHeader = 0;
    } else {
      uObjStr =  uObj.getClass().getSimpleName() + ", " + (uObj.hashCode() & 0XFFFFFFFFL);
      uObjHeader = unsafe.arrayBaseOffset(uObj.getClass());
    }
    final ByteBuffer bb = state.getByteBuffer();
    final String bbStr = (bb == null) ? "null"
            : bb.getClass().getSimpleName() + ", " + (bb.hashCode() & 0XFFFFFFFFL);
    final MemoryRequestServer memReqSvr = state.getMemoryRequestServer();
    final String memReqStr = (memReqSvr != null)
        ? memReqSvr.getClass().getSimpleName() + ", " + (memReqSvr.hashCode() & 0XFFFFFFFFL)
        : "null";
    final long cumBaseOffset = state.getCumulativeOffset();
    sb.append(preamble).append(LS);
    sb.append("UnsafeObj, hashCode : ").append(uObjStr).append(LS);
    sb.append("UnsafeObjHeader     : ").append(uObjHeader).append(LS);
    sb.append("ByteBuf, hashCode   : ").append(bbStr).append(LS);
    sb.append("RegionOffset        : ").append(state.getRegionOffset()).append(LS);
    sb.append("Capacity            : ").append(capacity).append(LS);
    sb.append("CumBaseOffset       : ").append(cumBaseOffset).append(LS);
    sb.append("MemReq, hashCode    : ").append(memReqStr).append(LS);
    sb.append("Valid               : ").append(state.isValid()).append(LS);
    sb.append("Read Only           : ").append(state.isReadOnly()).append(LS);
    sb.append("Byte Order          : ").append(state.getByteOrder().toString()).append(LS);
    sb.append("JDK Major Version   : ").append(UnsafeUtil.JDK).append(LS);
    //Data detail
    sb.append("Data, littleEndian  :  0  1  2  3  4  5  6  7");

    for (long i = 0; i < lengthBytes; i++) {
      final int b = unsafe.getByte(uObj, cumBaseOffset + offsetBytes + i) & 0XFF;
      if ((i % 8) == 0) { //row header
        sb.append(String.format("%n%20s: ", offsetBytes + i));
      }
      sb.append(String.format("%02x ", b));
    }
    sb.append(LS);

    return sb.toString();
  }

}
