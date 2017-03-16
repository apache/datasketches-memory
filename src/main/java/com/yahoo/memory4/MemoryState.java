/*
 * Copyright 2017, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory4;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @author Lee Rhodes
 */
final class MemoryState {
  private static final ByteOrder nativeOrder_ = ByteOrder.nativeOrder();
  private ByteOrder myOrder_ = nativeOrder_;
  private boolean swapBytes_ = false;
  private long nativeBaseOffset_ = 0L; //Direct ByteBuffer includes the slice() offset here.
  private Object unsafeObj_ = null; //##Array objects are held here.
  private long unsafeObjHeader_ = 0L; //##Heap ByteBuffer includes the slice() offset here.
  private ByteBuffer byteBuf_ = null; //Holding this until we are done with it.
  private File file_ = null; //Holding this until we are done with it.
  private long fileOffset_;
  private long regionOffset_ = 0L;
  private long capacity_ = 0L;//##
  private long cumBaseOffset_ = 0L; //##Holds the cum offset to the start of data.
  private MemoryRequest memReq_ = null; //##
  private boolean positional_ = false;

  private StepBoolean resourceIsReadOnly_ = new StepBoolean(false); //initial state is writable
  private StepBoolean valid_ = new StepBoolean(true); //## initial state is valid

  MemoryState() {}

  MemoryState copy() {
    final MemoryState out = new MemoryState();
    out.myOrder_ = myOrder_;
    out.swapBytes_ = swapBytes_;
    out.nativeBaseOffset_ = nativeBaseOffset_;
    out.unsafeObj_ = unsafeObj_;
    out.unsafeObjHeader_ = unsafeObjHeader_;
    out.byteBuf_ = byteBuf_;
    out.file_ = file_;
    out.fileOffset_ = fileOffset_;
    out.regionOffset_ = regionOffset_;
    out.capacity_ = capacity_;
    out.memReq_ = memReq_;
    out.positional_ = positional_;
    out.resourceIsReadOnly_ = resourceIsReadOnly_;
    out.valid_ = valid_;
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
