/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.UnsafeUtil.unsafe;
import static com.yahoo.memory.Util.negativeCheck;
import static com.yahoo.memory.Util.nullCheck;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps the configuration state for Resources.
 *
 * @author Lee Rhodes
 */
class ResourceState {

  /**
   * Native Endianness
   */
  private static final ByteOrder nativeOrder_ = ByteOrder.nativeOrder();

  //Monitoring
  static final AtomicLong currentDirectMemoryAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryAllocated_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocations_ = new AtomicLong();
  static final AtomicLong currentDirectMemoryMapAllocated_ = new AtomicLong();

  //FOUNDATION PARAMETERS
  /**
   * Only used to compute cumBaseOffset for off-heap resources.
   * If this changes, cumBaseOffset is recomputed.
   * A slice() of a Direct ByteBuffer includes the array_offset here.  It is originally computed
   * either from the Unsafe.allocateMemory() call or from the mapping class.
   */
  private long nativeBaseOffset_; //cannot be final; set to 0 when valid -> false

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
  private final long capacity_;

  /**
   * Primarily used for when the user allocated direct memory is the backing resource.
   * It is a callback mechanism for the client of a resource to request more memory from the
   * owner of the memory resource.
   */
  private MemoryRequestServer memReqSvr_ = new DefaultMemoryRequestServer();

  //FLAGS
  /**
   * Only set true if the backing resource has an independent read-only state and is, in fact,
   * read-only.
   */
  private final boolean resourceIsReadOnly_;

  /**
   * Can be set true anytime. If true, it cannot be set false. If resourceIsReadOnly is true,
   * this is automatically true.
   */
  private boolean localReadOnly_;

  /**
   * Only the backing resources that uses AutoCloseable can set this to false.  It can only be
   * changed from true to false once. The initial state is valid (true).
   */
  private final StepBoolean valid_ = new StepBoolean(true);

  //BYTE BUFFER RESOURCE
  /**
   * This holds a reference to a ByteBuffer until we are done with it.
   * This is also a user supplied parameter passed to AccessByteBuffer.
   */
  private final ByteBuffer byteBuf_;

  //RESOURCE ENDIANNESS PROPERTIES
  private final ByteOrder resourceOrder_;

  //****CONSTRUCTORS****
  //Raw constructor


  //Constructor for heap primitive arrays
  ResourceState(
      final Object unsafeObj,
      final Prim prim,
      final long arrLen,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    nullCheck(unsafeObj, "Array Object");
    negativeCheck(arrLen, "Capacity");
    nativeBaseOffset_ = 0;
    unsafeObj_ = unsafeObj;
    regionOffset_ = 0;
    capacity_ = arrLen << prim.shift();
    //memReqSvr ignore
    resourceIsReadOnly_ = false;
    localReadOnly_ = localReadOnly;
    //valid ignore
    byteBuf_ = null;
    resourceOrder_ = byteOrder;
    cumBaseOffset_ = compute();
  }

  //copy & region construtor
  ResourceState(
      final ResourceState src,
      final long offsetBytes,
      final long capacityBytes,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    negativeCheck(offsetBytes, "Offset");
    nativeBaseOffset_ = src.nativeBaseOffset_;
    unsafeObj_ = src.unsafeObj_;
    regionOffset_ = src.regionOffset_ + offsetBytes;
    capacity_ = capacityBytes;
    memReqSvr_ = src.memReqSvr_;
    resourceIsReadOnly_ = src.resourceIsReadOnly_;
    localReadOnly_ = localReadOnly || resourceIsReadOnly_;
    byteBuf_ = src.byteBuf_;
    resourceOrder_ = byteOrder;
    cumBaseOffset_ = compute();
  }

  //Direct Memory constructor
  ResourceState(
      final long nativeBaseOffset,
      final long capacityBytes,
      final ByteOrder byteOrder,
      final MemoryRequestServer memReqSvr) {
    negativeCheck(capacityBytes, "Capacity");
    negativeCheck(nativeBaseOffset, "NativeBaseOffset");
    nativeBaseOffset_ = nativeBaseOffset;
    unsafeObj_ = null;
    regionOffset_ = 0;
    capacity_ = capacityBytes;
    memReqSvr_ = memReqSvr;
    resourceIsReadOnly_ = false;
    localReadOnly_ = false;
    byteBuf_ = null;
    resourceOrder_ = byteOrder;
    cumBaseOffset_ = compute();
  }

  //Memory Mapped File constructor
  ResourceState(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean resourceReadOnly,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    negativeCheck(nativeBaseOffset, "NativeBaseOffset");
    negativeCheck(regionOffset, "RegionOffset");
    negativeCheck(capacityBytes, "Capacity");
    nativeBaseOffset_ = nativeBaseOffset;
    unsafeObj_ = null;
    regionOffset_ = regionOffset;
    capacity_ = capacityBytes;
    resourceIsReadOnly_ = resourceReadOnly;
    localReadOnly_ = localReadOnly || resourceReadOnly;
    byteBuf_ = null;
    resourceOrder_ = byteOrder;
    cumBaseOffset_ = compute();
  }

  //ByteBuffer constructor
  ResourceState(
      final ByteBuffer byteBuf,
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean resourceReadOnly,
      final boolean localReadOnly,
      final ByteOrder byteOrder) {
    negativeCheck(nativeBaseOffset, "NativeBaseOffset");
    negativeCheck(regionOffset, "RegionOffset");
    negativeCheck(capacityBytes, "Capacity");
    nativeBaseOffset_ = nativeBaseOffset;
    unsafeObj_ = unsafeObj;
    regionOffset_ = regionOffset;
    capacity_ = capacityBytes;
    resourceIsReadOnly_ = resourceReadOnly;
    localReadOnly_ = localReadOnly || resourceReadOnly;
    byteBuf_ = byteBuf;
    resourceOrder_ = byteOrder;
    cumBaseOffset_ = compute();
  }

  //****END CONSTRUCTORS****

  private final long compute() {
    return regionOffset_
        + ((unsafeObj_ == null) ? nativeBaseOffset_
            : unsafe.arrayBaseOffset(unsafeObj_.getClass()));
  }

  //FOUNDATION PARAMETERS
  long getNativeBaseOffset() {
    return nativeBaseOffset_;
  }

  Object getUnsafeObject() {
    return unsafeObj_;
  }

  long getCapacity() {
    return capacity_;
  }

  long getCumBaseOffset() {
    return cumBaseOffset_;
  }

  MemoryRequestServer getMemoryRequestServer() {
    return memReqSvr_;
  }

  //FLAGS
  boolean isReadOnly() {
    return localReadOnly_ || resourceIsReadOnly_;
  }

  boolean isDirect() {
    return nativeBaseOffset_ > 0L;
  }

  //Must already have checked that for null.
  boolean isSameResource(final ResourceState that) {
    if (this == that) { return true; }

    return (getCumBaseOffset() == that.getCumBaseOffset())
            && (getCapacity() == that.getCapacity())
            && (getUnsafeObject() == that.getUnsafeObject())
            && (getByteBuffer() == that.getByteBuffer());
  }

  boolean isValid() {
    return valid_.get();
  }

  StepBoolean getValid() {
    return valid_;
  }

  void setInvalid() {
    valid_.change();
  }

  final void assertValid() {
    assert valid_.get() : "Memory not valid.";
  }

  final void checkValid() {
    if (!valid_.get()) {
      throw new IllegalStateException("Memory not valid.");
    }
  }

  //REGIONS
  long getRegionOffset() {
    return regionOffset_;
  }

  //BYTE BUFFER
  ByteBuffer getByteBuffer() {
    return byteBuf_;
  }

  //ENDIANNESS
  ByteOrder getResourceOrder() {
    return resourceOrder_;
  }

  boolean isSwapBytes() {
    return (resourceOrder_ != nativeOrder_);
  }

}
