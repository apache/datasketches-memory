/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for heap-based, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class HeapWritableBufferImpl extends WritableBufferImpl {
  private static final int id = BUFFER | NATIVE | HEAP;
  private final Object unsafeObj;
  private final byte typeId;

  HeapWritableBufferImpl(
      final Object unsafeObj,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final BaseWritableMemoryImpl originMemory) {
    super(unsafeObj, 0L, regionOffset, capacityBytes, originMemory);
    this.unsafeObj = unsafeObj;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableBufferImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type, originMemory)
        : new HeapNonNativeWritableBufferImpl(
            unsafeObj, getRegionOffset(offsetBytes), capacityBytes,
            type, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | DUPLICATE | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new HeapWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, originMemory)
        : new HeapNonNativeWritableBufferImpl(
            unsafeObj, getRegionOffset(), getCapacity(),
            type, originMemory);
  }

  @Override
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  Object getUnsafeObject() {
    return unsafeObj;
  }

}
