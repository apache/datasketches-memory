/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
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
  static AtomicLong currentDirectMemoryAllocations_ = new AtomicLong();
  static AtomicLong currentDirectMemoryAllocated_ = new AtomicLong();
  static AtomicLong currentDirectMemoryMapAllocations_ = new AtomicLong();
  static AtomicLong currentDirectMemoryMapAllocated_ = new AtomicLong();

  //FOUNDATION PARAMETERS
  /**
   * Only used to compute cumBaseOffset for off-heap resources.
   * If this changes, cumBaseOffset is recomputed.
   * A slice() of a Direct ByteBuffer includes the array_offset here.  It is originally computed
   * either from the Unsafe.allocateMemory() call or from the mapping class.
   */
  private long nativeBaseOffset_ = 0L;

  /**
   * The object used in most Unsafe calls. This is effectively the array object if on-heap and
   * null for direct memory and determines how cumBaseOffset is computed.
   * This is effectively supplied by the user.
   */
  private Object unsafeObj_ = null;

  /**
   * If unsafeObj_ is non-null, this is the object header space for the specific array type,
   * typically either 16 or 24 bytes.  However, a slice of a Heap ByteBuffer adds the array-offset
   * to this value. This is computed based on the type of unsafeObj_ or extracted from a sliced
   * heap ByteBuffer. This is used to compute cumBaseOffset for heap resources.
   * If this changes, cumBaseOffset is recomputed.
   */
  private long unsafeObjHeader_ = 0L;

  /**
   * The size of the backing resource in bytes. Used by all methods when checking bounds.
   */
  private long capacity_ = 0L;

  /**
   * This becomes the base offset used by all Unsafe calls.
   */
  private long cumBaseOffset_ = 0L; //Holds the cumulative offset to the start of data.

  /**
   * Only relevant when user allocated direct memory is the backing resource. It is a callback
   * mechanism for the client of a resource to request more memory from the owner of the resource.
   */
  private MemoryRequestServer memReqSvr_ = DefaultMemoryManager.getInstance();

  /**
   * Only relevant when user allocated direct memory is the backing resource.
   */
  private WritableDirectHandle handle_;

  //FLAGS
  /**
   * Only set true if the backing resource has an independent read-only state and is, in fact,
   * read-only. This can only be changed from false (writable) to true (read-only) once. The
   * initial state is false (writable).
   */
  private StepBoolean resourceIsReadOnly_ = new StepBoolean(false); //initial state is writable

  /**
   * Only the backing resources that use AutoCloseable can set this to false.  It can only be
   * changed from true to false once. The initial state is valid.
   */
  private StepBoolean valid_ = new StepBoolean(true);

  //REGIONS
  /**
   * This is the offset that defines the start of a sub-region of the backing resource. It is
   * used to compute cumBaseOffset. If this changes, cumBaseOffset is recomputed.
   */
  private long regionOffset_ = 0L;

  //BYTE BUFFER RESOURCE
  /**
   * This holds a reference to a ByteBuffer until we are done with it.
   * This is also a user supplied parameter passed to AccessByteBuffer.
   */
  private ByteBuffer byteBuf_ = null;

  //MEMORY MAPPED FILE RESOURCES
  /**
   * This is user supplied parameter that is passed to the mapping class..
   */
  private File file_ = null;

  /**
   * The position or offset of a file that defines the starting region for the memory map. This is
   * a user supplied parameter that is passed to the mapping class.
   */
  private long fileOffset_;

  /**
   * This is used by the mapping class as a passing parameter.
   */
  private RandomAccessFile raf_ = null;

  /**
   * This is used by the mapping class as a passing parameter.
   */
  private MappedByteBuffer mbb_ = null;

  //POSITIONAL
  /**
   * BaseBuffer.
   */
  private BaseBuffer baseBuf_ = null;

  //ENDIANNESS PROPERTIES
  private ByteOrder resourceOrder_ = nativeOrder_;

  private boolean swapBytes_ = false; //true if resourceOrder != nativeOrder_

  ResourceState() {}

  //Constructor for heap primitive arrays
  ResourceState(final Object obj, final Prim prim, final long arrLen) {
    unsafeObj_ = obj;
    unsafeObjHeader_ = prim.off();
    if (arrLen < 0) {
      throw new IllegalArgumentException("Array length cannot be < 0");
    }
    capacity_ = arrLen << prim.shift();
    compute();
  }

  ResourceState copy() { //shallow copy
    final ResourceState out = new ResourceState();
    //FOUNDATION PARAMETERS
    out.nativeBaseOffset_ = nativeBaseOffset_;
    out.unsafeObj_ = unsafeObj_;
    out.unsafeObjHeader_ = unsafeObjHeader_;
    out.capacity_ = capacity_;
    //cumBaseOffset is computed
    out.memReqSvr_ = memReqSvr_; //retains memReqSvr reference

    //FLAGS
    out.resourceIsReadOnly_ = resourceIsReadOnly_;
    out.valid_ = valid_; //retains valid reference

    //REGIONS
    out.regionOffset_ = regionOffset_;

    //BYTE BUFFER
    out.byteBuf_ = byteBuf_; //retains ByteBuffer reference

    //MEMORY MAPPED FILES
    out.file_ = file_; //retains file reference
    out.fileOffset_ = fileOffset_;
    out.raf_ = raf_;
    out.mbb_ = mbb_;

    //ENDIANNESS
    out.resourceOrder_ = resourceOrder_; //retains resourseOrder
    out.swapBytes_ = swapBytes_;
    out.compute();

    //POSITIONAL
    out.baseBuf_ = new BaseBuffer(out);
    return out;
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
    nativeBaseOffset_ = nativeBaseOffset;
    compute();
  }

  Object getUnsafeObject() {
    return unsafeObj_;
  }

  void putUnsafeObject(final Object unsafeObj) {
    if (unsafeObj == null) {
      throw new IllegalArgumentException("Object may not be assigned null");
    }
    unsafeObj_ = unsafeObj;
    compute();
  }

  long getUnsafeObjectHeader() {
    return unsafeObjHeader_;
  }

  void putUnsafeObjectHeader(final long unsafeObjHeader) {
    if (unsafeObjHeader < 0) {
      throw new IllegalArgumentException("Object Header may not be negative.");
    }
    unsafeObjHeader_ = unsafeObjHeader;
    compute();
  }

  long getCapacity() {
    return capacity_;
  }

  void putCapacity(final long capacity) {
    if (capacity < 0) {
      throw new IllegalArgumentException("Capacity may not be negative.");
    }
    capacity_ = capacity;
  }

  long getCumBaseOffset() {
    return cumBaseOffset_;
  }

  MemoryRequestServer getMemoryRequestServer() {
    return memReqSvr_;
  }

  void setMemoryRequestServer(final MemoryRequestServer memReqSvr) {
    memReqSvr_ = memReqSvr;
  }

  WritableDirectHandle getHandle() {
    return handle_;
  }

  void setHandle(final WritableDirectHandle handler) {
    handle_ = handler;
  }

  //FLAGS
  boolean isResourceReadOnly() {
    return resourceIsReadOnly_.get();
  }

  void setResourceReadOnly() {
    resourceIsReadOnly_.change();
  }

  boolean isValid() {
    return valid_.get();
  }

  void setInvalid() {
    valid_.change();
  }

  boolean isDirect() {
    return nativeBaseOffset_ > 0L;
  }

  boolean isSameResource(final ResourceState that) {
    if (this == that) { return true; }

    return (getCumBaseOffset() == that.getCumBaseOffset())
            && (getCapacity() == that.getCapacity())
            && (getUnsafeObject() == that.getUnsafeObject())
            && (getByteBuffer() == that.getByteBuffer());
  }

  //REGIONS
  long getRegionOffset() {
    return regionOffset_;
  }

  void putRegionOffset(final long regionOffset) {
    if (regionOffset < 0) {
      throw new IllegalArgumentException("Region Offset may not be negative.");
    }
    regionOffset_ = regionOffset;
    compute();
  }

  //BYTE BUFFER
  ByteBuffer getByteBuffer() {
    return byteBuf_;
  }

  void putByteBuffer(final ByteBuffer byteBuf) {
    if (byteBuf == null) {
      throw new IllegalArgumentException("ByteBuffer may not be assigned null");
    }
    byteBuf_ = byteBuf;
    resourceOrder_ = byteBuf_.order();
    swapBytes_ = (resourceOrder_ != nativeOrder_);
  }

  //MEMORY MAPPED FILES
  File getFile() {
    return file_;
  }

  void putFile(final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File may not be assigned null");
    }
    file_ = file;
  }

  long getFileOffset() {
    return fileOffset_;
  }

  void putFileOffset(final long fileOffset) {
    if (fileOffset < 0) {
      throw new IllegalArgumentException("File Offset may not be negative.");
    }
    fileOffset_ = fileOffset;
  }

  RandomAccessFile getRandomAccessFile() {
    return raf_;
  }

  void putRandomAccessFile(final RandomAccessFile raf) {
    if (raf == null) {
      throw new IllegalArgumentException("RandomAccessFile may not be assigned null");
    }
    raf_ = raf;
  }

  MappedByteBuffer getMappedByteBuffer() {
    return mbb_;
  }

  void putMappedByteBuffer(final MappedByteBuffer mbb) {
    if (mbb == null) {
      throw new IllegalArgumentException("MappedByteBuffer may not be assigned null");
    }
    mbb_ = mbb;
  }

  //POSITIONAL BASE BUFFER
  BaseBuffer getBaseBuffer() {
    return baseBuf_;
  }

  void putBaseBuffer(final BaseBuffer baseBuf) {
    baseBuf_ = baseBuf;
  }

  //ENDIANNESS
  ByteOrder order() {
    return resourceOrder_;
  }

  void order(final ByteOrder resourceOrder) {
    resourceOrder_ = resourceOrder;
    swapBytes_ = (resourceOrder_ != nativeOrder_);
  }

  boolean isSwapBytes() {
    return swapBytes_;
  }

}
