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
final class HeapNonNativeWritableMemoryImpl extends NonNativeWritableMemoryImpl {
  private static final int id = MEMORY | NONNATIVE | HEAP;
  private final Object unsafeObj;
  private final byte typeId;

  HeapNonNativeWritableMemoryImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final int typeId) {
    super(unsafeObj, 0L, regionOffset, capacityBytes);
    this.unsafeObj = unsafeObj;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableMemoryImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type)
        : new HeapNonNativeWritableMemoryImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type);
  }

  @Override
  BaseWritableBufferImpl toWritableBuffer(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = readOnly ? READONLY : 0;
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, this)
        : new HeapNonNativeWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, this);
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
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

  @Override
  public boolean isDirect() {
    return false;
  }

  @Override
  public boolean isValid() {
    return true;
  }

}
