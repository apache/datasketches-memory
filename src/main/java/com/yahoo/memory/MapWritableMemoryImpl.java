/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for map memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapWritableMemoryImpl extends WritableMemoryImpl {
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only

  MapWritableMemoryImpl(
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
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid)
        : new MapNonNativeWritableMemoryImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid);
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly, valid, this)
        : new MapNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly, valid, this);
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

  @Override //TODO remove from baseWMemImpl NOTE WRITABLE ONLY
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
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

  @Override //TODO make abstract in BaseWritable?
  void setMemoryRequestServer(final MemoryRequestServer svr) {
    //do nothing
  }

}
