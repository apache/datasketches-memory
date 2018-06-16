/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for heap-based, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class HeapNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private final Object unsafeObj;

  HeapNonNativeWritableMemoryImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly) {
    super(unsafeObj, 0L, regionOffset, capacityBytes, readOnly);
    this.unsafeObj = unsafeObj;
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes, localReadOnly)
        : new HeapNonNativeWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes, localReadOnly);
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(), localReadOnly, this)
        : new HeapNonNativeWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(), localReadOnly, this);
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

  @Override
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }

  @Override
  long getNativeBaseOffset() {
    return 0;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

  @Override
  StepBoolean getValid() {
    return null;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public void setMemoryRequestServer(final MemoryRequestServer svr) {

  }
}
