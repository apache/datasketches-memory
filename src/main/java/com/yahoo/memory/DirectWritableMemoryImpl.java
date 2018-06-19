/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for direct memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class DirectWritableMemoryImpl extends WritableMemoryImpl {
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private MemoryRequestServer memReqSvr = null; //cannot be final;

  DirectWritableMemoryImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final StepBoolean valid) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes, readOnly);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    final BaseWritableMemoryImpl wmem = Util.isNativeOrder(byteOrder)
        ? new DirectWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly, valid)
        : new DirectNonNativeWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly, valid);
    wmem.setMemoryRequestServer(memReqSvr);
    return wmem;
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean localReadOnly, final ByteOrder byteOrder) {
    final BaseWritableBufferImpl wmem = Util.isNativeOrder(byteOrder)
        ? new DirectWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly, valid, this)
        : new DirectNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly, valid, this);
    wmem.setMemoryRequestServer(memReqSvr);
    return wmem;
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
  public MemoryRequestServer getMemoryRequestServer() {
    assertValid();
    return memReqSvr; //cannot be null
  }

  @Override
  long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  @Override
  Object getUnsafeObject() {
    return null;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

  @Override
  void setMemoryRequestServer(final MemoryRequestServer svr) {
    memReqSvr = svr;
  }

}
