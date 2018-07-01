/*
 * Copyright 2018, Yahoo! Inc. Licensed under the terms of the
 * Apache License 2.0. See LICENSE file at the project root for terms.
 */

package com.yahoo.memory;

import java.nio.ByteOrder;

/**
 * Implementation of {@link WritableBuffer} for map memory, native byte order.
 *
 * @author Roman Leventov
 * @author Lee Rhodes
 */
final class MapWritableBufferImpl extends WritableBufferImpl {
  private static final int id = BUFFER | NATIVE | MAP;
  private final long nativeBaseOffset; //used to compute cumBaseOffset
  private final StepBoolean valid; //a reference only
  private final byte typeId;

  MapWritableBufferImpl(
      final long nativeBaseOffset,
      final long regionOffset,
      final long capacityBytes,
      final int typeId,
      final StepBoolean valid,
      final BaseWritableMemoryImpl originMemory) {
    super(null, nativeBaseOffset, regionOffset, capacityBytes, originMemory);
    this.nativeBaseOffset = nativeBaseOffset;
    this.valid = valid;
    this.typeId = (byte) (id | (typeId & 0x7));
  }

  @Override
  BaseWritableBufferImpl toWritableRegion(final long offsetBytes, final long capacityBytes,
      final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | REGION | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, originMemory)
        : new MapNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(offsetBytes), capacityBytes,
            type, valid, originMemory);
  }

  @Override
  BaseWritableBufferImpl toDuplicate(final boolean readOnly, final ByteOrder byteOrder) {
    final int type = typeId | DUPLICATE | (readOnly ? READONLY : 0);
    return Util.isNativeOrder(byteOrder)
        ? new MapWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, originMemory)
        : new MapNonNativeWritableBufferImpl(
            nativeBaseOffset, getRegionOffset(), getCapacity(),
            type, valid, originMemory);
  }

  @Override
  long getNativeBaseOffset() {
    return nativeBaseOffset;
  }

  @Override
  int getTypeId() {
    return typeId & 0xff;
  }

  @Override
  public boolean isValid() {
    return valid.get();
  }

}
