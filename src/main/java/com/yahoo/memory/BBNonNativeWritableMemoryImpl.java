/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableMemory} for ByteBuffer, non-native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class BBNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private final Object unsafeObj;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final ByteBuffer byteBuf; //holds a reference to a ByteBuffer until we are done with it.

  BBNonNativeWritableMemoryImpl(
      final Object unsafeObj,
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final boolean readOnly,
      final ByteBuffer byteBuf) {
    super(unsafeObj, nativeBaseOffset, regionOffset, capacityBytes, readOnly);
    this.unsafeObj = unsafeObj;
    this.nativeBaseOffset = nativeBaseOffset;
    this.byteBuf = byteBuf;
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            localReadOnly, getByteBuffer())
        : new BBNonNativeWritableMemoryImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            localReadOnly, getByteBuffer());
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean localReadOnly, final ByteOrder byteOrder) {
    return Util.isNativeOrder(byteOrder)
        ? new BBWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly,
            byteBuf, this)
        : new BBNonNativeWritableBufferImpl(
            unsafeObj, nativeBaseOffset, getRegionOffset(), getCapacity(), localReadOnly,
            byteBuf, this);
  }

  @Override
  public ByteBuffer getByteBuffer() {
    assertValid();
    return byteBuf;
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
  Object getUnsafeObject() {
    assertValid();
    return unsafeObj;
  }

  @Override
  public boolean isDirect() {
    return unsafeObj == null;
  }

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  void setMemoryRequestServer(final MemoryRequestServer svr) {
    //do nothing
  }
}