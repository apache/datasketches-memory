/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps the configuration state for Resources.
 *
 * @author Lee Rhodes
 */
class BaseState {

  //Monitoring
  static final AtomicLong currentDirectMemoryAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryAllocated_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocated_ = new AtomicLong();

  /**
   * The object used in most Unsafe calls. This is effectively the array object if on-heap and
   * null for direct memory and determines how cumBaseOffset is computed.
   */
  private final Object unsafeObj_;

  /**
   * This is the offset that defines the start of a sub-region of the backing resource. It is
   * used to compute cumBaseOffset. If this changes, cumBaseOffset is recomputed.
   * This will be loaded from heap ByteBuffers as they have a similar field used for slices.
   * It is used by region() and writableRegion().
   */
  private final long regionOffset_;

  /**
   * This becomes the base offset used by all Unsafe calls. It is cumulative in that in includes
   * all offsets from regions, user-defined offsets when creating Memory, and the array object
   * header offset when creating Memory from primitive arrays.
   */
  private final long cumBaseOffset_; //= f(regionOffset, unsafeObj, nativeBaseOffset, unsafeObjHeader)

  /**
   * The size of the backing resource in bytes. Used by all methods when checking bounds.
   */
  final long capacityBytes_;

  //FLAGS
  /**
   * Only the backing resources that uses AutoCloseable can set this to false.  It can only be
   * changed from true to false once. The initial state is valid (true).
   *
   * <p>There is one and only one valid object and it only resides here. Everywhere else it is
   * used is just a reference to this object.
   */
  private StepBoolean valid_ = null;

  /**
   * This holds a reference to a ByteBuffer until we are done with it.
   * This is also a user supplied parameter passed to AccessByteBuffer.
   */
  private final ByteBuffer byteBuf_;

  private final ByteOrder dataByteOrder_;

  /**
   * Only used to compute cumBaseOffset for off-heap resources.
   * If this changes, cumBaseOffset is recomputed.
   * A slice() of a Direct ByteBuffer includes the array_offset here.  It is originally computed
   * either from the Unsafe.allocateMemory() call or from the mapping class.
   */
  private long nativeBaseOffset_; //cannot be final; set to 0 when valid -> false

  /**
   * Only used when user allocated direct memory is the backing resource.
   * It is a callback mechanism for the client of a resource to request more memory from the
   * owner of the memory resource.
   */
  private MemoryRequestServer memReqSvr_ = null;

  /**
   * Can be set true anytime. If true, it cannot be set false. If resourceIsReadOnly is true,
   * this is automatically true.
   */
  private boolean readOnly_;

  //****CONSTRUCTORS****

  //All fields constructor except for memReqSvr and cumBaseOffset
  BaseState(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final ByteOrder dataByteOrder,
      final ByteBuffer byteBuf,
      final StepBoolean valid)
  {
    unsafeObj_ = unsafeObj;
    nativeBaseOffset_ = nativeBaseOffset;
    regionOffset_ = regionOffset;
    capacityBytes_ = capacityBytes;
    readOnly_ = readOnly;
    dataByteOrder_ = dataByteOrder;
    byteBuf_ = byteBuf;
    cumBaseOffset_ = compute();
    valid_ = (valid == null) ? new StepBoolean(true) : valid;
    //not memReqSvr
  }

  //****END CONSTRUCTORS****

  private final long compute() {
    return regionOffset_ + ((unsafeObj_ == null)
        ? nativeBaseOffset_
        : unsafe.arrayBaseOffset(unsafeObj_.getClass()));
  }

  boolean equalTo(final Object that) {
    if (this == that) { return true; }
    return (that instanceof BaseState)
        ? CompareAndCopy.equals(this, ((BaseState)that)) : false;
  }

  boolean equalTo(final long thisOffsetBytes, final BaseState that,
      final long thatOffsetBytes, final long lengthBytes) {
    return CompareAndCopy.equals(this, thisOffsetBytes, that, thatOffsetBytes, lengthBytes);
  }

  ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf_;
  }

  long getCapacity() {
    assertValid();
    return capacityBytes_;
  }

  long getCumulativeOffset() {
    return cumBaseOffset_;
  }

  ByteOrder getDataByteOrder() {
    assertValid();
    return dataByteOrder_;
  }

  MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    return memReqSvr_;
  }

  long getNativeBaseOffset() {
    return nativeBaseOffset_;
  }

  long getRegionOffset() {
    assertValid();
    return regionOffset_;
  }

  Object getUnsafeObject() {
    return unsafeObj_;
  }

  StepBoolean getValid() {
    return valid_;
  }

  boolean hasArray() {
    assertValid();
    return unsafeObj_ != null;
  }

  int theHashCode() {
    return CompareAndCopy.hashCode(this);
  }

  boolean hasByteBuffer() {
    assertValid();
    return byteBuf_ != null;
  }

  boolean isDirect() {
    assertValid();
    return nativeBaseOffset_ > 0L;
  }

  boolean isNativeOrder() {
    return Util.isNativeOrder(dataByteOrder_);
  }

  boolean isReadOnly() {
    assertValid();
    return readOnly_;
  }

  //Must already have checked that for null.
  boolean isSameResource(final BaseState that) {
    checkValid();
    if (that == null) { return false; }
    that.checkValid();
    if (this == that) { return true; }

    return (getCumulativeOffset() == that.getCumulativeOffset())
            && (getCapacity() == that.getCapacity())
            && (getUnsafeObject() == that.getUnsafeObject())
            && (getByteBuffer() == that.getByteBuffer());
  }

  boolean isValid() {
    return valid_.get();
  }

  void setInvalid() { //only used in test
    valid_.change();
  }

  void setMemoryRequestServer(final MemoryRequestServer svr) {
    memReqSvr_ = svr;
  }

  void zeroNativeBaseOffset() {
    nativeBaseOffset_ = 0L;
  }

  //Asserts and checks
  final void assertValid() {
    assert valid_.get() : "Memory not valid.";
  }

  final void checkValid() {
    if (!valid_.get()) {
      throw new IllegalStateException("Memory not valid.");
    }
  }

  //MONITORING
  /**
   * Gets the current number of active direct memory allocations.
   * @return the current number of active direct memory allocations.
   */
  public static long getCurrentDirectMemoryAllocations() {
    return BaseState.currentDirectMemoryAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory allocated.
   * @return the current size of active direct memory allocated.
   */
  public static long getCurrentDirectMemoryAllocated() {
    return BaseState.currentDirectMemoryAllocated_.get();
  }

  /**
   * Gets the current number of active direct memory map allocations.
   * @return the current number of active direct memory map allocations.
   */
  public static long getCurrentDirectMemoryMapAllocations() {
    return BaseState.currentDirectMemoryMapAllocations_.get();
  }

  /**
   * Gets the current size of active direct memory map allocated.
   * @return the current size of active direct memory map allocated.
   */
  public static long getCurrentDirectMemoryMapAllocated() {
    return BaseState.currentDirectMemoryMapAllocated_.get();
  }

}
