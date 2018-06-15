/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for heap-based, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class HeapWritableMemoryImpl extends WritableMemoryImpl {
  private final Object unsafeObj;

  HeapWritableMemoryImpl(
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
    return Util.nativeOrder;
  }

  @Override //TODO remove from baseWMemImpl NOTE WRITABLE ONLY
  public MemoryRequestServer getMemoryRequestServer() {
    return null;
  }

  @Override
  long getNativeBaseOffset() {
    assertValid();
    return 0;
  }

  @Override
  Object getUnsafeObject() {
    assertValid();
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
