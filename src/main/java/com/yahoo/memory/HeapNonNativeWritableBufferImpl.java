/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for heap-based, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
class HeapNonNativeWritableBufferImpl extends NonNativeWritableBufferImpl {
  private final Object unsafeObj;

  HeapNonNativeWritableBufferImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, 0, regionOffset, capacityBytes, readOnly, null, null, originMemory);
    this.unsafeObj = unsafeObj;
  }

  @Override
  public ByteBuffer getByteBuffer() {
    return null;
  }

  @Override
  public ByteOrder getByteOrder() {
    return Util.nonNativeOrder;
  }

  @Override //TODO remove from baseWBufImpl NOTE WRITABLE ONLY
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
