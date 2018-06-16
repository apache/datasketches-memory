/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for ByteBuffer, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBWritableBufferImpl extends WritableBufferImpl {
  private final Object unsafeObj;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.

  BBWritableBufferImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final ByteBuffer byteBuf,
      final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly, originMemory);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.byteBuf = byteBuf;
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            localReadOnly, byteBuf, originMemory)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            localReadOnly, byteBuf, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            localReadOnly, byteBuf, originMemory)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(),
            localReadOnly, byteBuf, originMemory);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
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
    return nativeBaseOffset;
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
