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

/**
 * @author Lee Rhodes
 */
final class MemoryState {

  /** Place holder for some future endian extension. */
  private static final ByteOrder nativeOrder_ = ByteOrder.nativeOrder();

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
  private long capacity_ = 0L;//##

  /**
   * This becomes the base offset used by all Unsafe calls.
   */
  private long cumBaseOffset_ = 0L; //##Holds the cum offset to the start of data.

  /**
   * Only relevant when user allocated direct memory is the backing resource. It is a callback
   * mechanism for the client of a resource to request more memory from the owner of the resource.
   */
  private MemoryRequest memReq_ = null; //##

  //FLAGS
  /**
   * Only set true if the backing resource has an independen read-only state and is, in fact,
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

  //BYTE BUFFER
  /**
   * This holds a reference to a ByteBuffer until we are done with it.
   * This is also a user supplied parameter passed to AccessByteBuffer.
   */
  private ByteBuffer byteBuf_ = null;

  //MAPPED FILES
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
   * Place holder for future positional memory extension.
   */
  private boolean positional_ = false;

  //ENDIANNESS PLACE HOLDERS

  /** Place holder for some future endian extension. */
  private ByteOrder myOrder_ = nativeOrder_;

  /** Place holder for some future endian extension. */
  private boolean swapBytes_ = false; //true if myOrder != nativeOrder_

  MemoryState() {}

  MemoryState copy() {
    final MemoryState out = new MemoryState();
    //FOUNDATION PARAMETERS
    out.nativeBaseOffset_ = nativeBaseOffset_;
    out.unsafeObj_ = unsafeObj_;
    out.unsafeObjHeader_ = unsafeObjHeader_;
    out.capacity_ = capacity_;
    //cumBaseOffset to compute
    out.memReq_ = memReq_;

    //Flags
    out.resourceIsReadOnly_ = resourceIsReadOnly_;
    out.valid_ = valid_;

    //REGIONS
    out.regionOffset_ = regionOffset_;

    //Byte Buffer
    out.byteBuf_ = byteBuf_;

    //Mapped files
    out.file_ = file_;
    out.fileOffset_ = fileOffset_;
    out.raf_ = raf_;
    out.mbb_ = mbb_;

    //POSITIONAL
    out.positional_ = positional_;

    //ENDIANNESS PLACE HOLDERS
    out.myOrder_ = myOrder_;
    out.swapBytes_ = swapBytes_;
    out.compute();
    return out;
  }

  private void compute() {
    this.cumBaseOffset_ = regionOffset_
        + ((unsafeObj_ == null) ? nativeBaseOffset_ : unsafeObjHeader_);
  }

  long getNativeBaseOffset() {
    return nativeBaseOffset_;
  }

  Object getUnsafeObject() {
    return unsafeObj_;
  }

  long getUnsafeObjectHeader() {
    return unsafeObjHeader_;
  }

  ByteBuffer getByteBuffer() {
    return byteBuf_;
  }

  File getFile() {
    return file_;
  }

  RandomAccessFile getRandomAccessFile() {
    return raf_;
  }

  MappedByteBuffer getMappedByteBuffer() {
    return mbb_;
  }

  long getFileOffset() {
    return fileOffset_;
  }

  long getRegionOffset() {
    return regionOffset_;
  }

  long getCapacity() {
    return capacity_;
  }

  long getCumBaseOffset() {
    return cumBaseOffset_;
  }

  MemoryRequest getMemoryRequest() {
    return memReq_;
  }

  boolean isResourceReadOnly() {
    return resourceIsReadOnly_.get();
  }

  boolean isValid() {
    return valid_.get();
  }

  boolean isDirect() {
    return nativeBaseOffset_ > 0L;
  }

  boolean isPositional() {
    return positional_;
  }

  void putNativeBaseOffset(final long nativeBaseOffset) {
    this.nativeBaseOffset_ = nativeBaseOffset;
    compute();
  }

  void putUnsafeObject(final Object unsafeObj) {
    if (unsafeObj == null) {
      throw new IllegalArgumentException("Object may not be assigned null");
    }
    this.unsafeObj_ = unsafeObj;
    compute();
  }

  void putUnsafeObjectHeader(final long unsafeObjHeader) {
    if (unsafeObjHeader < 0) {
      throw new IllegalArgumentException("Object Header may not be negative.");
    }
    this.unsafeObjHeader_ = unsafeObjHeader;
    compute();
  }

  void putByteBuffer(final ByteBuffer byteBuf) {
    if (byteBuf == null) {
      throw new IllegalArgumentException("ByteBuffer may not be assigned null");
    }
    this.byteBuf_ = byteBuf;
  }

  void putFile(final File file) {
    if (file == null) {
      throw new IllegalArgumentException("File may not be assigned null");
    }
    this.file_ = file;
  }

  void putRandomAccessFile(final RandomAccessFile raf) {
    if (raf == null) {
      throw new IllegalArgumentException("RandomAccessFile may not be assigned null");
    }
    this.raf_ = raf;
  }

  void putMappedByteBuffer(final MappedByteBuffer mbb) {
    if (mbb == null) {
      throw new IllegalArgumentException("MappedByteBuffer may not be assigned null");
    }
    this.mbb_ = mbb;
  }

  void putFileOffset(final long fileOffset) {
    if (fileOffset < 0) {
      throw new IllegalArgumentException("File Offset may not be negative.");
    }
    this.fileOffset_ = fileOffset;
  }

  void putRegionOffset(final long regionOffset) {
    if (regionOffset < 0) {
      throw new IllegalArgumentException("Region Offset may not be negative.");
    }
    this.regionOffset_ = regionOffset;
    compute();
  }

  void putCapacity(final long capacity) {
    if (capacity <= 0) {
      throw new IllegalArgumentException("Capacity may not be negative or zero.");
    }
    this.capacity_ = capacity;
  }

  void putMemoryRequest(final MemoryRequest memReq) {
    this.memReq_ = memReq;
  }

  void setResourceReadOnly() {
    this.resourceIsReadOnly_.change();
  }

  void setInvalid() {
    this.valid_.change();
  }

  void setPositional(final boolean positional) {
    this.positional_ = positional;
  }

  ByteOrder order() {
    return myOrder_;
  }

  boolean swapBytes() {
    return swapBytes_;
  }

  void order(final ByteOrder order) {
    this.myOrder_ = order;
    this.swapBytes_ = (myOrder_ != nativeOrder_);
  }
}
