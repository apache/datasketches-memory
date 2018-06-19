/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for map memory, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapNonNativeWritableBufferImpl extends NonNativeWritableBufferImpl {
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only

  MapNonNativeWritableBufferImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final StepBoolean valid,
      final BaseWritableMemoryImpl originMemory) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes, readOnly, originMemory);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid, originMemory)
        : new MapNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes, localReadOnly,
            valid, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly,
            valid, originMemory)
        : new MapNonNativeWritableBufferImpl(
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
    return Util.nonNativeOrder;
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
