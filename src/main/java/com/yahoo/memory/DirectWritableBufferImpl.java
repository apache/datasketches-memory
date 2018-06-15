/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for direct memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class DirectWritableBufferImpl extends WritableBufferImpl {
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private MemoryRequestServer memReqSvr = null; //cannot be final;

  DirectWritableBufferImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final StepBoolean valid,
      final BaseWritableMemoryImpl originMemory) {
    super(regionOffset, capacityBytes, readOnly, originMemory);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
    if (valid == null) {
      throw new IllegalArgumentException("Valid cannot be null.");
    }
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid, originMemory)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly,
            valid, originMemory)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly,
            valid, originMemory);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public ByteOrder getByteOrder() {
    assertValid();
    return Util.nativeOrder;
  }

  @Override
  int getClassID() {
    return BUF | NAT | DIR;
  }

  @Override //TODO remove from baseWMemImpl NOTE WRITABLE ONLY
  public MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    if (memReqSvr == null) {
      memReqSvr = new DefaultMemoryRequestServer();
    }
    return memReqSvr;
  }

  @Override
  long getNativeBaseOffset() {
    assertValid();
    return nativeBaseOffset;
  }

  @Override
  Object getUnsafeObject() {
    return null;
  }

  @Override
  StepBoolean getValid() {
    return valid;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

  @Override
  public void setMemoryRequestServer(final MemoryRequestServer svr) {
    memReqSvr = svr;
  }

}
