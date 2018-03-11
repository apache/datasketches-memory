/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import static com.yahoo.memory.Util.negativeCheck;
import static com.yahoo.memory.Util.nullCheck;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Keeps the configuration state primarily for Resources.
 *
 * @author Lee Rhodes
 */
final class ResourceState {

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
  private long nativeBaseOffset_;

  /**
   * The object used in most Unsafe calls. This is effectively the array object if on-heap and
   * null for direct memory and determines how cumBaseOffset is computed.
   */
  private Object unsafeObj_;

  /**
   * If unsafeObj_ is non-null, this is the object header space for the specific array type,
   * typically either 16 or 24 bytes. This is used to compute cumBaseOffset.
   * If this changes, cumBaseOffset is recomputed.
   */
  private long unsafeObjHeader_;

  /**
   * This is the offset that defines the start of a sub-region of the backing resource. It is
   * used to compute cumBaseOffset. If this changes, cumBaseOffset is recomputed.
   * This will be loaded from heap ByteBuffers as they have a similar field used for slices.
   * It is used by region() and writableRegion().
   */
  private long regionOffset_;

  /**
   * This becomes the base offset used by all Unsafe calls. It is cumulative in that in includes
   * all offsets from regions, user-defined offsets when creating Memory, and the array object
   * header offset when creating Memory from primitive arrays.
   */
  private long cumBaseOffset_; //= f(regionOffset, unsafeObj, nativeBaseOffset, unsafeObjHeader)

  /**
   * The size of the backing resource in bytes. Used by all methods when checking bounds.
   */
  private long capacity_;

  /**
   * Only relevant when user allocated direct memory is the backing resource. It is a callback
   * mechanism for the client of a resource to request more memory from the owner of the resource.
   */
  private MemoryRequestServer memReqSvr_;

  //FLAGS
  /**
   * Only set true if the backing resource has an independent read-only state and is, in fact,
   * read-only.
   */
  private final boolean resourceIsReadOnly_;

  /**
   * Only the backing resources that uses AutoCloseable can set this to false.  It can only be
   * changed from true to false once. The initial state is valid (true).
   */
  private StepBoolean valid_;

  //BYTE BUFFER RESOURCE
  /**
   * This holds a reference to a ByteBuffer until we are done with it.
   * This is also a user supplied parameter passed to AccessByteBuffer.
   */
  private ByteBuffer byteBuf_;

  //RESOURCE ENDIANNESS PROPERTIES
  private ByteOrder resourceOrder_ = nativeOrder_; //default

  //****CONSTRUCTORS****
  ResourceState(final boolean resourceReadOnly) {
    resourceIsReadOnly_ = resourceReadOnly;
    valid_ = new StepBoolean(true);
  }

  //Constructor for heap primitive arrays
  ResourceState(final Object obj, final Prim prim, final long arrLen) {
    this(false); //set resourceIsReadOnly=false, valid
    nullCheck(obj, "Array Object");
    Util.negativeCheck(arrLen, "Capacity");
    unsafeObj_ = obj;
    unsafeObjHeader_ = prim.off();
    capacity_ = arrLen << prim.shift();
    compute();
  }

  //copy construtor
  private ResourceState(final ResourceState src) {
    //FOUNDATION PARAMETERS
    nativeBaseOffset_ = src.nativeBaseOffset_;
    unsafeObj_ = src.unsafeObj_;
    unsafeObjHeader_ = src.unsafeObjHeader_;
    capacity_ = src.capacity_;

    memReqSvr_ = src.memReqSvr_; //retains memReqSvr reference

    //FLAGS
    resourceIsReadOnly_ = src.resourceIsReadOnly_;
    valid_ = src.valid_; //retains valid reference

    //REGIONS
    regionOffset_ = src.regionOffset_;

    //BYTE BUFFER
    byteBuf_ = src.byteBuf_; //retains ByteBuffer reference

    //ENDIANNESS
    resourceOrder_ = src.resourceOrder_; //retains resourseOrder
    compute(); //for sanity
  }
  //****END CONSTRUCTORS****

  ResourceState copy() {
    return new ResourceState(this);
  }

  private void compute() {
    cumBaseOffset_ = regionOffset_
        + ((unsafeObj_ == null) ? nativeBaseOffset_ : unsafeObjHeader_);
  }

  //FOUNDATION PARAMETERS
  long getNativeBaseOffset() {
    return nativeBaseOffset_;
  }

  void putNativeBaseOffset(final long nativeBaseOffset) {
    negativeCheck(nativeBaseOffset, "nativeBaseOffset");
    nativeBaseOffset_ = nativeBaseOffset;
    compute();
  }

  Object getUnsafeObject() {
    return unsafeObj_;
  }

  //starts null, then only changed once to non-null array object
  void putUnsafeObject(final Object unsafeObj) {
    nullCheck(unsafeObj, "Array Object");
    unsafeObj_ = unsafeObj;
    compute();
  }

  long getUnsafeObjectHeader() {
    return unsafeObjHeader_;
  }

  void putUnsafeObjectHeader(final long unsafeObjHeader) {
    negativeCheck(unsafeObjHeader, "Object Header");
    unsafeObjHeader_ = unsafeObjHeader;
    compute();
  }

  long getCapacity() {
    return capacity_;
  }

  void putCapacity(final long capacity) {
    Util.negativeCheck(capacity, "Capacity");
    capacity_ = capacity;
  }

  long getCumBaseOffset() {
    return cumBaseOffset_;
  }

  MemoryRequestServer getMemoryRequestServer() {
    return memReqSvr_;
  }

  void putMemoryRequestServer(final MemoryRequestServer memReqSvr) {
    memReqSvr_ = memReqSvr; //may be null
  }

  //FLAGS
  boolean isResourceReadOnly() {
    return resourceIsReadOnly_;
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

  void setInvalid() {
    valid_.change();
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

  void putRegionOffset(final long regionOffset) {
    negativeCheck(regionOffset, "Region Offset");
    regionOffset_ = regionOffset;
    compute();
  }

  //BYTE BUFFER
  ByteBuffer getByteBuffer() {
    return byteBuf_;
  }

  void putByteBuffer(final ByteBuffer byteBuf) {
    nullCheck(byteBuf, "ByteBuffer");
    byteBuf_ = byteBuf;
    resourceOrder_ = byteBuf_.order();
  }

  //ENDIANNESS
  ByteOrder getResourceOrder() {
    return resourceOrder_;
  }

  void putResourceOrder(final ByteOrder resourceOrder) {
    nullCheck(resourceOrder, "ByteOrder");
    resourceOrder_ = resourceOrder;
  }

  boolean isSwapBytes() {
    return (resourceOrder_ != nativeOrder_);
  }

}
